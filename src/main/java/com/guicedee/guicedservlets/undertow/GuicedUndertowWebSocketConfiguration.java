package com.guicedee.guicedservlets.undertow;

import com.guicedee.guicedservlets.websockets.GuicedWebSocket;
import com.guicedee.guicedservlets.websockets.services.IWebSocketPreConfiguration;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import lombok.extern.java.Log;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.util.logging.Level;

import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.websockets.jsr.WebSocketDeploymentInfo.ATTRIBUTE_NAME;

@Log
public class GuicedUndertowWebSocketConfiguration
		implements IWebSocketPreConfiguration<GuicedUndertowWebSocketConfiguration>
{

	private static WebSocketDeploymentInfo webSocketDeploymentInfo;
	private static HttpHandler webSocketHandler;


	/**
	 * Returns the WebSocketDeploymentInfo for use in the Servlet Extension
	 *
	 * @return The Web Socket Deployment Info
	 */
	public static WebSocketDeploymentInfo getWebSocketDeploymentInfo()
	{
		return GuicedUndertowWebSocketConfiguration.webSocketDeploymentInfo;
	}

	public static HttpHandler getWebSocketHandler()
	{
		return GuicedUndertowWebSocketConfiguration.webSocketHandler;
	}

	@Override
	public void configure()
	{
		GuicedUndertowWebSocketConfiguration.log.config("Setting up XNIO for Websockets at /wssocket");
		Xnio xnio = Xnio.getInstance("nio");
		XnioWorker xnioWorker;
		try
		{
			xnioWorker = xnio.createWorker(OptionMap.builder()
			                                        .getMap());
			GuicedUndertowWebSocketConfiguration.webSocketDeploymentInfo = new WebSocketDeploymentInfo()
					                                                               .addEndpoint(GuicedWebSocket.class)
					                                                               .setWorker(xnioWorker);

			DeploymentInfo websocketDeployment = deployment()
					                                     .setContextPath("/wssocket")
					                                     .addServletContextAttribute(ATTRIBUTE_NAME, GuicedUndertowWebSocketConfiguration.webSocketDeploymentInfo)
					                                     .setDeploymentName("websocket-deployment")
					                                     .setClassLoader(Thread.currentThread()
					                                                           .getContextClassLoader());

			DeploymentManager manager = Servlets.defaultContainer()
			                                    .addDeployment(websocketDeployment);

			manager.deploy();
			GuicedUndertowWebSocketConfiguration.log.fine("Registering WebSockets in Undertow - [/wssocket]");
			GuicedUndertowWebSocketConfiguration.webSocketHandler = manager.start();
			GuicedUndertowWebSocketConfiguration.log.fine("Completed WebSocket [/wssocket]");
		}
		catch (Exception e)
		{
			GuicedUndertowWebSocketConfiguration.log.log(Level.SEVERE, "Unable to configure XNIO with WebSocket Handler", e);
		}
	}

	@Override
	public boolean enabled()
	{
		return true;
	}
}
