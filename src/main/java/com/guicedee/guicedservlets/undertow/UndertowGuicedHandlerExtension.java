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

package com.guicedee.guicedservlets.undertow;

import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.guicedservlets.GuicedFilter;
import com.guicedee.guicedservlets.GuicedServletContextListener;
import com.guicedee.guicedservlets.GuicedServletSessionManager;
import com.guicedee.logger.LogFactory;
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
public class UndertowGuicedHandlerExtension
		implements ServletExtension
{
	private static final Logger log = LogFactory.getLog("GuicedUndertow");

	public UndertowGuicedHandlerExtension()
	{
		//No config required
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext)
	{
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();

		if (servletContext.getAttribute(ATTRIBUTE_NAME) == null)
		{
			UndertowGuicedHandlerExtension.log.fine("Registering Guice Filter in Undertow");

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
			UndertowGuicedHandlerExtension.log.fine("Requested to configure guice for web sockets - skipped. - " + deploymentInfo.getDeploymentName());
		}

		UndertowGuicedHandlerExtension.log.config("Configuring Resources to be found by GuicedUndertowResourceManager");
		deploymentInfo.setResourceManager(new GuicedUndertowResourceManager(classLoader));
		UndertowGuicedHandlerExtension.log.fine("Undertow Configured");
	}
}
