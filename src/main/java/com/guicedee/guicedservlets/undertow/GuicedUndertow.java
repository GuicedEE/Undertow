package com.guicedee.guicedservlets.undertow;

import com.google.common.base.*;
import com.guicedee.client.*;
import com.guicedee.guicedinjection.interfaces.IDefaultService;
import com.guicedee.guicedinjection.interfaces.IGuicePreDestroy;
import com.guicedee.guicedservlets.undertow.services.*;
//import com.guicedee.guicedservlets.websockets.implementations.GuicedUndertowWebSocketConfiguration;
import io.undertow.*;
import io.undertow.attribute.*;
import io.undertow.server.*;
import io.undertow.server.handlers.*;
import io.undertow.server.handlers.encoding.*;
import io.undertow.server.session.*;
import io.undertow.servlet.*;
import io.undertow.servlet.api.*;
import import com.google.inject.Singleton;;
import lombok.extern.java.Log;
import org.xnio.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.*;
import java.util.*;
import java.util.logging.*;


import static io.undertow.Handlers.*;
import static io.undertow.servlet.Servlets.*;

@SuppressWarnings({"rawtypes", "unused"})
@Log
public class GuicedUndertow implements IGuicePreDestroy
{
	private String serverKeystore;
	private char[] storePassword;
	
	private Class sslStoreReferenceClass;
	
	private boolean http2 = true;
	
	private String host;
	private int port;
	private boolean ssl;
	
	private String sslKeyLocation;
	private String serverTruststoreLocation;
	private String sslKeyName;
	
	private KeyStore sslKeystore;
	private KeyStore trustKeystore;
	
	private Undertow.Builder server = Undertow.builder();
	
	private Undertow undertow;
	
	public GuicedUndertow()
	{
		//no config
	}
	
	public static Undertow boot(String host, int port, boolean ssl, KeyStore serverKeystore, KeyStore serverTruststore, String sslKey, char[] sslPassword, Class referenceClass,
	                            boolean http2) throws Exception
	{
		GuicedUndertow guicedUndertow = new GuicedUndertow();
		guicedUndertow.host = host;
		guicedUndertow.port = port;
		guicedUndertow.ssl = ssl;
		guicedUndertow.sslKeyLocation = sslKey;
		guicedUndertow.storePassword = sslPassword;
		guicedUndertow.sslStoreReferenceClass = referenceClass;
		guicedUndertow.http2 = http2;
		guicedUndertow.sslKeystore = serverKeystore;
		guicedUndertow.trustKeystore = serverTruststore;
		return guicedUndertow.bootMe();
	}
	
	@SuppressWarnings("UnusedReturnValue")
	public static Undertow boot(String host, int port) throws Exception
	{
		GuicedUndertow undertow = new GuicedUndertow();
		undertow.host = host;
		undertow.port = port;
		return undertow.bootMe();
	}
	
	public static Undertow boot(String host, int port, boolean ssl, String serverKeystoreSystemPropertyName, String serverTruststoreSystemPropertyName, String sslKeyLocation,
	                            char[] sslPassword,
	                            Class referenceClass,
	                            boolean http2) throws Exception
	{
		return boot(host, port, ssl, serverKeystoreSystemPropertyName, serverTruststoreSystemPropertyName, sslKeyLocation, null, sslPassword, referenceClass, http2);
	}
	
	public static Undertow boot(String host, int port, boolean ssl, String serverKeystoreSystemPropertyName, String serverTruststoreSystemPropertyName, String sslKeyLocation, String sslAliasName,
	                            char[] sslPassword,
	                            Class referenceClass,
	                            boolean http2) throws Exception
	{
		GuicedUndertow undertow = new GuicedUndertow();
		undertow.host = host;
		undertow.port = port;
		undertow.ssl = ssl;
		undertow.sslKeyLocation = sslKeyLocation;
		undertow.storePassword = sslPassword;
		undertow.sslStoreReferenceClass = referenceClass;
		undertow.http2 = http2;
		undertow.serverKeystore = serverKeystoreSystemPropertyName;
		undertow.serverTruststoreLocation = serverTruststoreSystemPropertyName;
		undertow.sslKeyName = sslAliasName;
		return undertow.bootMe();
	}
	
	public Undertow bootMe() throws Exception
	{
		SSLContext sslContext = null;
		if (ssl)
		{
			if (sslKeystore == null)
			{
				sslKeystore = GuicedUndertow.loadKeyStore(sslStoreReferenceClass, serverKeystore, storePassword);
				trustKeystore = GuicedUndertow.loadKeyStore(sslStoreReferenceClass, serverTruststoreLocation, storePassword);
			}
		}
		
		sslContext = createSSLContext(sslKeystore, trustKeystore, storePassword);
		
		log.fine("Setting XNIO Provider : " + Xnio.getInstance()
						.getName());
		if (http2)
		{
			server.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
			server.setServerOption(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, true);
		}
		
		if (ssl)
		{
			server.addHttpsListener(port, host, sslContext);
		} else
		{
			server.addHttpListener(port, host);
		}
		
		DeploymentInfo deploymentInfo = deployment().setClassLoader(GuicedUndertow.class.getClassLoader())
						.setContextPath("/")
						.setDeploymentName(host + "-" + port + ".war");
		
		java.util.Set<UndertowDeploymentConfigurator> confs = IGuiceContext.loaderToSetNoInjection(ServiceLoader.load(UndertowDeploymentConfigurator.class));
		for (UndertowDeploymentConfigurator config : confs)
		{
			deploymentInfo = config.configure(deploymentInfo);
		}
		
		DeploymentManager manager = Servlets.defaultContainer()
						.addDeployment(deploymentInfo);
		try
		{
			IGuiceContext.getContext().inject();
		} catch (Throwable T)
		{
			log.log(Level.SEVERE, "Unable to start injections", T);
		}
		manager.deploy();
		
		HttpHandler guicedHandler = manager.start();
		
		Map<String, HttpHandler> handlers = new LinkedHashMap<>();
		
		HttpHandler encodingHandler = new EncodingHandler.Builder().build(null)
						.wrap(guicedHandler);
		
		final PathHandler ph = path();
		
		Set<UndertowPathHandler> pathHandlers = IGuiceContext.loaderToSetNoInjection(ServiceLoader.load(UndertowPathHandler.class));
		for (UndertowPathHandler pathHandler : pathHandlers)
		{
			handlers.putAll(pathHandler.registerPathHandler());
		}
		handlers.put("/", encodingHandler);
		handlers.forEach((path,handler)->{
			ph.addPrefixPath(path,handler);
		});
		
		server.setHandler(new SessionAttachmentHandler(new LearningPushHandler(100, -1,
										Handlers.header(ph, "x-undertow-transport", ExchangeAttributes.transportProtocol())),
										new InMemorySessionManager("sessionManager"),
										new SessionCookieConfig().setSecure(true)
														.setHttpOnly(true)
														.setDiscard(true)
														.setMaxAge(1)
						)
		);
		
		undertow = server.build();
		undertow.start();
		return undertow;
	}
	
	
	@Override
	public void onDestroy()
	{
		if (undertow != null)
		{
			undertow.stop();
		}
	}
	
	
	private SSLContext createSSLContext(KeyStore keyStore, KeyStore trustStore, char[] password) throws Exception
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
		
		
		if (!Strings.isNullOrEmpty(sslKeyName))
		{
			Enumeration<String> aliases = trustKeystore.aliases();
			X509Certificate xCert = null;
			while (aliases.hasMoreElements())
			{
				String alias = aliases.nextElement();
				if (!alias.equalsIgnoreCase(sslKeyName))
				{
					continue;
				}
				Key key = trustKeystore.getKey(alias, storePassword);
				final Certificate cert = trustKeystore.getCertificate(alias);
				if (!(cert instanceof X509Certificate))
				{
					continue;
				}
				xCert = (X509Certificate) cert;
			}
			if (xCert == null)
			{
				throw new RuntimeException("Cannot load that given alias as a private key cert");
			}
			KeyManager[] keys = new KeyManager[]{new FilteredKeyManager((X509KeyManager) keyManagerFactory.getKeyManagers()[0], xCert, sslKeyName)};
			sslContext.init(keys, trustManagerFactory.getTrustManagers(), new SecureRandom());
		} else
		{
			sslContext.init(keyManagers, trustManagers, null);
		}
		
		return sslContext;
	}
	
	public static KeyStore loadKeyStore(Class referencePath, String name, char[] password) throws Exception
	{
		String storeLoc = System.getProperty(name);
		InputStream stream;
		if (storeLoc == null)
		{
			stream = referencePath.getResourceAsStream(name);
		} else
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
	
	
	public String getServerKeystore()
	{
		return serverKeystore;
	}
	
	public GuicedUndertow setServerKeystore(String serverKeystore)
	{
		this.serverKeystore = serverKeystore;
		return this;
	}
	
	public char[] getStorePassword()
	{
		return storePassword;
	}
	
	public GuicedUndertow setStorePassword(char[] storePassword)
	{
		this.storePassword = storePassword;
		return this;
	}
	
	public Class getSslStoreReferenceClass()
	{
		return sslStoreReferenceClass;
	}
	
	public GuicedUndertow setSSLStoresReferenceClass(Class referenceClass)
	{
		sslStoreReferenceClass = referenceClass;
		return this;
	}
	
	public boolean isHttp2()
	{
		return http2;
	}
	
	public GuicedUndertow setHttp2(boolean http2)
	{
		this.http2 = http2;
		return this;
	}
	
	public String getHost()
	{
		return host;
	}
	
	public GuicedUndertow setHost(String host)
	{
		this.host = host;
		return this;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public GuicedUndertow setPort(int port)
	{
		this.port = port;
		return this;
	}
	
	public boolean isSsl()
	{
		return ssl;
	}
	
	public GuicedUndertow setSsl(boolean ssl)
	{
		this.ssl = ssl;
		return this;
	}
	
	public String getSslKeyLocation()
	{
		return sslKeyLocation;
	}
	
	public GuicedUndertow setSslKeyLocation(String sslKeyLocation)
	{
		this.sslKeyLocation = sslKeyLocation;
		return this;
	}
	
	public String getServerTruststoreLocation()
	{
		return serverTruststoreLocation;
	}
	
	public GuicedUndertow setServerTruststoreLocation(String serverTruststoreLocation)
	{
		this.serverTruststoreLocation = serverTruststoreLocation;
		return this;
	}
	
	public KeyStore getSslKeystore()
	{
		return sslKeystore;
	}
	
	public GuicedUndertow setSslKeystore(KeyStore sslKeystore)
	{
		this.sslKeystore = sslKeystore;
		return this;
	}
	
	public KeyStore getTrustKeystore()
	{
		return trustKeystore;
	}
	
	public GuicedUndertow setTrustKeystore(KeyStore trustKeystore)
	{
		this.trustKeystore = trustKeystore;
		return this;
	}
	
	public Undertow.Builder getServer()
	{
		return server;
	}
	
	public void setServer(Undertow.Builder server)
	{
		this.server = server;
	}
	
	public String getSslKeyName()
	{
		return sslKeyName;
	}
	
	public GuicedUndertow setSslKeyName(String sslKeyName)
	{
		this.sslKeyName = sslKeyName;
		return this;
	}

	/**
	 * filters the SSLCertificate we want to use for SSL <code>
	 * KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
	 * kmf.init(keyStore, null);
	 * String SSLCertificateKeyStoreAlias = keyStore.getCertificateAlias(sslCertificate);
	 * KeyManager[] keyManagers = new KeyManager[] { new FilteredKeyManager((X509KeyManager)kmf.getKeyManagers()[0], sslCertificate, SSLCertificateKeyStoreAlias) };
	 * </code>
	 */
	static class FilteredKeyManager implements X509KeyManager
	{
		
		private final X509KeyManager originatingKeyManager;
		private final X509Certificate sslCertificate;
		private final String SSLCertificateKeyStoreAlias;
		
		/**
		 * @param originatingKeyManager,       original X509KeyManager
		 * @param sslCertificate,              X509Certificate to use
		 * @param SSLCertificateKeyStoreAlias, Alias of the certificate in the provided keystore
		 */
		public FilteredKeyManager(X509KeyManager originatingKeyManager, X509Certificate sslCertificate, String SSLCertificateKeyStoreAlias)
		{
			this.originatingKeyManager = originatingKeyManager;
			this.sslCertificate = sslCertificate;
			this.SSLCertificateKeyStoreAlias = SSLCertificateKeyStoreAlias;
		}
		
		@Override
		public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket)
		{
			return SSLCertificateKeyStoreAlias;
		}
		
		@Override
		public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket)
		{
			return originatingKeyManager.chooseServerAlias(keyType, issuers, socket);
		}
		
		@Override
		public X509Certificate[] getCertificateChain(String alias)
		{
			return new X509Certificate[]{sslCertificate};
		}
		
		@Override
		public String[] getClientAliases(String keyType, Principal[] issuers)
		{
			return originatingKeyManager.getClientAliases(keyType, issuers);
		}
		
		@Override
		public String[] getServerAliases(String keyType, Principal[] issuers)
		{
			return originatingKeyManager.getServerAliases(keyType, issuers);
		}
		
		@Override
		public PrivateKey getPrivateKey(String alias)
		{
			return originatingKeyManager.getPrivateKey(alias);
		}
	}
	
	@Override
	public int compareTo(Object o)
	{
		return 0;
	}
	
	@Override
	public int compare(Object o1, Object o2)
	{
		return 0;
	}
}
