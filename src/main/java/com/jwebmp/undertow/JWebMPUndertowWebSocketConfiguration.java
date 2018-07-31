package com.jwebmp.undertow;

import com.jwebmp.logger.LogFactory;
import com.jwebmp.websockets.JWebMPSocket;
import com.jwebmp.websockets.services.IWebSocketPreConfiguration;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.undertow.servlet.Servlets.*;
import static io.undertow.websockets.jsr.WebSocketDeploymentInfo.*;

public class JWebMPUndertowWebSocketConfiguration
		implements IWebSocketPreConfiguration
{
	private static final Logger log = LogFactory.getLog("UndertowWebSockets");
	private static WebSocketDeploymentInfo webSocketDeploymentInfo;
	private static HttpHandler webSocketHandler;

	/**
	 * Returns the WebSocketDeploymentInfo for use in the Servlet Extension
	 *
	 * @return The Web Socket Deployment Info
	 */
	public static WebSocketDeploymentInfo getWebSocketDeploymentInfo()
	{
		return webSocketDeploymentInfo;
	}

	public static HttpHandler getWebSocketHandler()
	{
		return webSocketHandler;
	}

	@Override
	public void configure()
	{
		log.config("Setting up XNIO for Websockets at /jwebmpwssocket");
		final Xnio xnio = Xnio.getInstance("nio");
		final XnioWorker xnioWorker;
		try
		{
			xnioWorker = xnio.createWorker(OptionMap.builder()
			                                        .getMap());
			JWebMPUndertowWebSocketConfiguration.webSocketDeploymentInfo = new WebSocketDeploymentInfo()
					                                                               .addEndpoint(JWebMPSocket.class)
					                                                               .setWorker(xnioWorker);
			DeploymentInfo websocketDeployment = deployment()
					                                     .setContextPath("/jwebmpwssocket")
					                                     .addServletContextAttribute(ATTRIBUTE_NAME, webSocketDeploymentInfo)
					                                     .setDeploymentName("websocket-deployment")
					                                     .setClassLoader(Thread.currentThread()
					                                                           .getContextClassLoader());

			DeploymentManager manager = Servlets.defaultContainer()
			                                    .addDeployment(websocketDeployment);

			manager.deploy();
			log.fine("Registering WebSockets in Undertow");
			webSocketHandler = manager.start();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Unable to configure XNIO with WebSocket Handler", e);
		}
	}
}
