package com.guicedee.guicedservlets.undertow;

import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedinjection.json.StaticStrings;
import com.guicedee.guicedservlets.undertow.services.UndertowDeploymentConfigurator;
import com.guicedee.logger.LogFactory;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.attribute.ExchangeAttributes;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.LearningPushHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.xnio.Xnio;

import javax.net.ssl.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import static com.guicedee.guicedinjection.json.StaticStrings.*;
import static io.undertow.Handlers.*;
import static io.undertow.servlet.Servlets.*;

public class GuicedUndertow
{
	private static final Logger log = LogFactory.getLog("JWebMP Undertow");

	private String serverKeystore;
	private char[] storePassword;
	private Class referenceClass;
	private boolean http2 = true;
	private String host;
	private int port;
	private boolean ssl;
	private String sslKeyLocation;
	private String serverTruststoreLocation;
	private KeyStore sslKeystore;
	private KeyStore trustKeystore;

	public static Undertow boot(String host, int port, boolean ssl, String serverKeystore, String serverTruststore, String sslKey, char[] sslPassword, Class referenceClass, boolean http2) throws Exception
	{
		GuicedUndertow undertow = new GuicedUndertow();
		undertow.host = host;
		undertow.port = port;
		undertow.ssl = ssl;
		undertow.sslKeyLocation = sslKey;
		undertow.storePassword = sslPassword;
		undertow.referenceClass = referenceClass;
		undertow.http2 = http2;
		undertow.serverKeystore = serverKeystore;
		undertow.serverTruststoreLocation = serverTruststore;

		return undertow.bootMe();
	}

	public static Undertow boot(String host, int port, boolean ssl, KeyStore serverKeystore, KeyStore serverTruststore, String sslKey, char[] sslPassword, Class referenceClass, boolean http2) throws Exception
	{
		GuicedUndertow undertow = new GuicedUndertow();
		undertow.host = host;
		undertow.port = port;
		undertow.ssl = ssl;
		undertow.sslKeyLocation = sslKey;
		undertow.storePassword = sslPassword;
		undertow.referenceClass = referenceClass;
		undertow.http2 = http2;
		undertow.sslKeystore = serverKeystore;
		undertow.trustKeystore = serverTruststore;

		return undertow.bootMe();
	}

	private Undertow bootMe() throws Exception
	{
		SSLContext sslContext = null;
		if (ssl)
		{
			if (sslKeystore == null)
			{
				sslContext = GuicedUndertow.createSSLContext(GuicedUndertow.loadKeyStore(referenceClass, serverKeystore, storePassword),
				                                             GuicedUndertow.loadKeyStore(referenceClass, serverTruststoreLocation, storePassword),
				                                             storePassword);
			}
			else
			{
				sslContext = GuicedUndertow.createSSLContext(sslKeystore,
				                                             trustKeystore,
				                                             storePassword);
			}

		}


		log.fine("Setting XNIO Provider : " + Xnio.getInstance()
		                                            .getName());
		Undertow.Builder server = Undertow.builder();
		//server.setServerOption(UndertowOptions.MAX_COOKIES, 0);
		if (http2)
		{
			server.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
			server.setServerOption(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, true);
		}
		if (ssl)
		{
			server.addHttpsListener(port, host, sslContext);
		}
		else
		{
			server.addHttpListener(port, host);
		}

		DeploymentInfo deploymentInfo = deployment()
				                                .setClassLoader(GuicedUndertow.class.getClassLoader())
				                                .setContextPath(STRING_FORWARD_SLASH)
				                                .setDeploymentName(host + "-" + port + ".war");

		ServiceLoader.load(UndertowDeploymentConfigurator.class);
		for (UndertowDeploymentConfigurator config : ServiceLoader.load(UndertowDeploymentConfigurator.class))
		{
			deploymentInfo = config.configure(deploymentInfo);
		}

		DeploymentManager manager = Servlets.defaultContainer()
		                                    .addDeployment(deploymentInfo);

		GuiceContext.inject();
		manager.deploy();

		HttpHandler jwebSwingHandler = manager.start();
		HttpHandler encodingHandler = new EncodingHandler.Builder().build(null)
		                                                           .wrap(jwebSwingHandler);

		PathHandler ph;
		if (GuicedUndertowWebSocketConfiguration.getWebSocketHandler() != null)
		{
			ph = path().addPrefixPath("/wssocket", GuicedUndertowWebSocketConfiguration.getWebSocketHandler())
			           .addPrefixPath(STRING_FORWARD_SLASH, encodingHandler);
		}
		else
		{
			ph = path().addPrefixPath(STRING_FORWARD_SLASH, encodingHandler);
		}
		server.setHandler(new SessionAttachmentHandler(
				new LearningPushHandler(100, -1,
				                        Handlers.header(ph,
				                                        "x-undertow-transport", ExchangeAttributes.transportProtocol())),
				new InMemorySessionManager("sessionManager"), new SessionCookieConfig().setSecure(true)
				                                                                       .setHttpOnly(true)
		));

		Undertow u = server.build();
		u.start();
		return u;
	}

	private static SSLContext createSSLContext(KeyStore keyStore, KeyStore trustStore, char[] password) throws Exception
	{
		KeyManager[] keyManagers;
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, password);
		keyManagers = keyManagerFactory.getKeyManagers();

		TrustManager[] trustManagers;
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		trustManagers = trustManagerFactory.getTrustManagers();

		SSLContext sslContext;
		sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagers, trustManagers, null);

		return sslContext;
	}

	public static KeyStore loadKeyStore(Class referencePath, String name, char[] password) throws Exception
	{
		String storeLoc = System.getProperty(name);
		InputStream stream;
		if (storeLoc == null)
		{
			stream = referencePath.getResourceAsStream(name);
		}
		else
		{
			stream = Files.newInputStream(Paths.get(storeLoc));
		}

		if (stream == null)
		{
			throw new RuntimeException("Could not load keystore");
		}
		try (InputStream is = stream)
		{
			KeyStore loadedKeystore = KeyStore.getInstance("JKS");
			loadedKeystore.load(is, password);
			return loadedKeystore;
		}
	}

	public static Undertow boot(String host, int port) throws Exception
	{
		GuicedUndertow undertow = new GuicedUndertow();
		undertow.host = host;
		undertow.port = port;
		return undertow.bootMe();
	}

}
