package com.jwebmp.undertow;

import com.jwebmp.guicedservlets.GuicedServletSessionManager;
import com.jwebmp.websockets.services.IWebSocketSessionProvider;

import javax.servlet.http.HttpSession;

public class UndertowWebSocketSessionProvider implements IWebSocketSessionProvider
{
	@Override
	public HttpSession getSession(String sessionID)
	{
		return GuicedServletSessionManager.getSessionMap().get(sessionID);
	}
}
