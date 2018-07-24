package com.jwebmp.undertow;

import com.jwebmp.logger.LogFactory;
import com.jwebmp.websockets.JWebMPSocketEndpoint;
import com.jwebmp.websockets.services.IJWebMPWebSocketPreConfiguration;
import io.undertow.Undertow;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JWebMPUndertowWebSocketConfiguration
		implements IJWebMPWebSocketPreConfiguration
{
	private static final Logger log = LogFactory.getLog("UndertowWebSockets");
	private static WebSocketDeploymentInfo webSocketDeploymentInfo;

	/**
	 * Returns the WebSocketDeploymentInfo for use in the Servlet Extension
	 *
	 * @return The Web Socket Deployment Info
	 */
	public static WebSocketDeploymentInfo getWebSocketDeploymentInfo()
	{
		return webSocketDeploymentInfo;
	}

	@Override
	public void configure()
	{
		log.config("Setting up XNIO for Websockets at /jwebmpwssocket");
		final Xnio xnio = Xnio.getInstance("nio", Undertow.class.getClassLoader());
		final XnioWorker xnioWorker;
		try
		{
			xnioWorker = xnio.createWorker(OptionMap.builder()
			                                        .getMap());
			JWebMPUndertowWebSocketConfiguration.webSocketDeploymentInfo = new WebSocketDeploymentInfo()
					                                                               .addEndpoint(JWebMPSocketEndpoint.class)
					                                                               .setWorker(xnioWorker);
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Unable to configure XNIO with WebSocket Handler", e);
		}
	}
}
