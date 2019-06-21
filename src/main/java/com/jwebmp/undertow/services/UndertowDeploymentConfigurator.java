package com.jwebmp.undertow.services;

import com.jwebmp.guicedinjection.interfaces.IDefaultService;
import io.undertow.servlet.api.DeploymentInfo;

@FunctionalInterface
public interface UndertowDeploymentConfigurator extends IDefaultService<UndertowDeploymentConfigurator>
{
	DeploymentInfo configure(DeploymentInfo deploymentInfo);
}
