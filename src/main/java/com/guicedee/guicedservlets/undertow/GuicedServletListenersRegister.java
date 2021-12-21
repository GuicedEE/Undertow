package com.guicedee.guicedservlets.undertow;

import com.guicedee.guicedservlets.*;
import com.guicedee.guicedservlets.undertow.services.*;
import io.undertow.servlet.api.*;

public class GuicedServletListenersRegister implements UndertowDeploymentConfigurator
{
	@Override
	public DeploymentInfo configure(DeploymentInfo deploymentInfo)
	{
		return deploymentInfo.addListener(new ListenerInfo(GuicedServletSessionManager.class));
	}
}
