package com.guicedee.guicedservlets.undertow;

import com.guicedee.guicedservlets.GuicedServletSessionManager;
import com.guicedee.guicedservlets.websockets.services.IWebSocketSessionProvider;

import jakarta.servlet.http.HttpSession;

public class UndertowWebSocketSessionProvider implements IWebSocketSessionProvider
{
	@Override
	public HttpSession getSession(String sessionID)
	{
		return GuicedServletSessionManager.getSessionMap().get(sessionID);
	}
}
