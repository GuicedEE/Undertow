package com.jwebmp.undertow;

import com.jwebmp.logger.LogFactory;
import com.jwebmp.websockets.GuicedWebSocket;
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
		implements IWebSocketPreConfiguration<JWebMPUndertowWebSocketConfiguration>
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
		return JWebMPUndertowWebSocketConfiguration.webSocketDeploymentInfo;
	}

	public static HttpHandler getWebSocketHandler()
	{
		return JWebMPUndertowWebSocketConfiguration.webSocketHandler;
	}

	@Override
	public void configure()
	{
		JWebMPUndertowWebSocketConfiguration.log.config("Setting up XNIO for Websockets at /jwebmpwssocket");
		Xnio xnio = Xnio.getInstance("nio");
		XnioWorker xnioWorker;
		try
		{
			xnioWorker = xnio.createWorker(OptionMap.builder()
			                                        .getMap());
			JWebMPUndertowWebSocketConfiguration.webSocketDeploymentInfo = new WebSocketDeploymentInfo()
					                                                               .addEndpoint(GuicedWebSocket.class)
					                                                               .setWorker(xnioWorker);
			DeploymentInfo websocketDeployment = deployment()
					                                     .setContextPath("/jwebmpwssocket")
					                                     .addServletContextAttribute(ATTRIBUTE_NAME, JWebMPUndertowWebSocketConfiguration.webSocketDeploymentInfo)
					                                     .setDeploymentName("websocket-deployment")
					                                     .setClassLoader(Thread.currentThread()
					                                                           .getContextClassLoader());

			DeploymentManager manager = Servlets.defaultContainer()
			                                    .addDeployment(websocketDeployment);

			manager.deploy();
			JWebMPUndertowWebSocketConfiguration.log.fine("Registering WebSockets in Undertow - [/jwebmpwssocket]");
			JWebMPUndertowWebSocketConfiguration.webSocketHandler = manager.start();
			JWebMPUndertowWebSocketConfiguration.log.fine("Completed WebSocket [/jwebmpwssocket]");
		}
		catch (Exception e)
		{
			JWebMPUndertowWebSocketConfiguration.log.log(Level.SEVERE, "Unable to configure XNIO with WebSocket Handler", e);
		}
	}

	@Override
	public boolean enabled()
	{
		return true;
	}
}
