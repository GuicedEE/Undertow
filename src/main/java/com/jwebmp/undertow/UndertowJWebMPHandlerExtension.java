/*
 * Copyright (C) 2017 GedMarc
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

import com.jwebmp.guicedinjection.GuiceContext;
import com.jwebmp.guicedservlets.GuicedFilter;
import com.jwebmp.guicedservlets.GuicedServletContextListener;
import com.jwebmp.guicedservlets.GuicedServletSessionManager;
import com.jwebmp.logger.LogFactory;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;

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
		//No config required
	}

	@Override
	public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext)
	{
		if (servletContext.getAttribute(ATTRIBUTE_NAME) == null)
		{
			UndertowJWebMPHandlerExtension.log.fine("Registering Guice Filter in Undertow");
			InstanceFactory guicedContextInstanceFactory = new ImmediateInstanceFactory<>(GuiceContext.get(GuicedServletContextListener.class));
			InstanceFactory guiceInstanceFactory = new ImmediateInstanceFactory<>(GuiceContext.get(GuicedServletSessionManager.class));
			InstanceFactory guiceFilterFactory = new ImmediateInstanceFactory<>(GuiceContext.get(GuicedFilter.class));
			deploymentInfo.addFilter(new FilterInfo("GuiceUndertowFilter", GuicedFilter.class, guiceFilterFactory).setAsyncSupported(true));
			deploymentInfo.addFilterUrlMapping("GuiceUndertowFilter", "/*", DispatcherType.REQUEST);
			deploymentInfo.addListener(new ListenerInfo(GuicedServletContextListener.class, guicedContextInstanceFactory));
			deploymentInfo.addListener(new ListenerInfo(GuicedServletSessionManager.class, guiceInstanceFactory));
		}
		else
		{
			UndertowJWebMPHandlerExtension.log.fine("Requested to configure guice for web sockets - skipped. - " + deploymentInfo.getDeploymentName());
		}
		UndertowJWebMPHandlerExtension.log.config("Configuring Resources to be found in META-INF/resources");
		deploymentInfo.setResourceManager(new ClassPathResourceManager(deploymentInfo.getClassLoader(), "META-INF/resources"));
		UndertowJWebMPHandlerExtension.log.fine("Undertow Configured");
	}
}
