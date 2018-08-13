/*
 * Copyright (C) 2017 Marc Magon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jwebmp.undertow;

import com.google.inject.servlet.GuiceFilter;
import com.jwebmp.guicedservlets.GuicedServletSessionManager;
import com.jwebmp.logger.LogFactory;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.ListenerInfo;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import java.util.logging.Logger;

import static io.undertow.websockets.jsr.WebSocketDeploymentInfo.*;

@SuppressWarnings("unused")
public class UndertowJWebMPHandlerExtension
		implements ServletExtension
{
	private static final Logger log = LogFactory.getLog("JWebMPUndertow");

	public UndertowJWebMPHandlerExtension()
	{
	}

	@Override
	public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext)
	{
		if (servletContext.getAttribute(ATTRIBUTE_NAME) == null)
		{
			UndertowJWebMPHandlerExtension.log.fine("Registering Guice Filter in Undertow");
			deploymentInfo.addFilter(new FilterInfo("GuiceFilter", GuiceFilter.class).setAsyncSupported(true));
			deploymentInfo.addFilterUrlMapping("GuiceFilter", "/*", DispatcherType.REQUEST);
			deploymentInfo.addListener(new ListenerInfo(GuicedServletSessionManager.class));
			deploymentInfo.addListener(Servlets.listener(GuicedServletSessionManager.class));
		}
		else
		{
			UndertowJWebMPHandlerExtension.log.fine("Skipping Guice Filter Deployment Info for Web Sockets " + deploymentInfo.getDeploymentName());
		}
		UndertowJWebMPHandlerExtension.log.config("Configuring Resources to be found in META-INF/resources");
		deploymentInfo.setResourceManager(new ClassPathResourceManager(deploymentInfo.getClassLoader(), "META-INF/resources"));
		UndertowJWebMPHandlerExtension.log.fine("Undertow Ready");
	}
}
