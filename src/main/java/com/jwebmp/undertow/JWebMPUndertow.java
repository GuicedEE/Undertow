package com.jwebmp.undertow;

import com.jwebmp.guicedinjection.GuiceContext;
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

import javax.net.ssl.*;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

import static io.undertow.Handlers.*;
import static io.undertow.servlet.Servlets.*;

public class JWebMPUndertow
{
	private String serverKeystore;
	private char[] storePassword;
	private Class referenceClass;
	private boolean http2 = true;
	private String host;
	private int port;
	private boolean ssl;
	private String sslKey;
	private String serverTruststore;

	public static Undertow boot(String host, int port, boolean ssl, String serverKeystore, String serverTruststore, String sslKey, char[] sslPassword, Class referenceClass, boolean http2) throws Exception
	{
		JWebMPUndertow undertow = new JWebMPUndertow();
		undertow.host = host;
		undertow.port = port;
		undertow.ssl = ssl;
		undertow.sslKey = sslKey;
		undertow.storePassword = sslPassword;
		undertow.referenceClass = referenceClass;
		undertow.http2 = http2;
		undertow.serverKeystore = serverKeystore;
		undertow.serverTruststore = serverTruststore;

		return undertow.bootMe();
	}

	private Undertow bootMe() throws Exception
	{
		SSLContext sslContext = null;
		if (ssl)
		{
			sslContext = JWebMPUndertow.createSSLContext(JWebMPUndertow.loadKeyStore(referenceClass, serverKeystore, storePassword),
			                                             JWebMPUndertow.loadKeyStore(referenceClass, serverTruststore, storePassword),
			                                             storePassword);
		}
		Undertow.Builder server = Undertow.builder();
		if (http2)
		{
			server.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
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
				                                .setClassLoader(JWebMPUndertow.class.getClassLoader())
				                                .setContextPath("/")
				                                .setDeploymentName(host + "-" + port + ".war");

		DeploymentManager manager = Servlets.defaultContainer()
		                                    .addDeployment(deploymentInfo);

		GuiceContext.inject();
		manager.deploy();

		HttpHandler jwebSwingHandler = manager.start();
		HttpHandler encodingHandler = new EncodingHandler.Builder().build(null)
		                                                           .wrap(jwebSwingHandler);

		PathHandler ph;
		if (JWebMPUndertowWebSocketConfiguration.getWebSocketHandler() != null)
		{
			ph = path().addPrefixPath("/jwebmpwssocket", JWebMPUndertowWebSocketConfiguration.getWebSocketHandler())
			           .addPrefixPath("/", encodingHandler);
		}
		else
		{
			ph = path().addPrefixPath("/", encodingHandler);
		}

		server.setHandler(new SessionAttachmentHandler(
				new LearningPushHandler(100, -1,
				                        Handlers.header(ph,
				                                        "x-undertow-transport", ExchangeAttributes.transportProtocol())),
				new InMemorySessionManager("sessionManager"), new SessionCookieConfig().setSecure(true)
				                                                                       .setHttpOnly(true)));

		Undertow u = server.build();
		u.start();
		return u;
	}

	private static SSLContext createSSLContext(final KeyStore keyStore, final KeyStore trustStore, char[] password) throws Exception
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

	private static KeyStore loadKeyStore(Class referencePath, String name, char[] password) throws Exception
	{
		String storeLoc = System.getProperty(name);
		final InputStream stream;
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
		JWebMPUndertow undertow = new JWebMPUndertow();
		undertow.host = host;
		undertow.port = port;
		return undertow.bootMe();
	}

}
