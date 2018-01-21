package za.co.mmagon.jwebswing.undertow;

import com.google.inject.servlet.GuiceFilter;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import za.co.mmagon.logger.LogFactory;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class UndertowJWebSwingHandlerExtension implements ServletExtension
{
	private static final Logger log = LogFactory.getLog("JwebSwingUndertow");

	@Override
	public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext)
	{
		log.config("Registering JWebSwing in undertow");

		deploymentInfo.addFilter(new FilterInfo("GuiceFilter", GuiceFilter.class));
		deploymentInfo.addFilterUrlMapping("GuiceFilter", "/*", DispatcherType.REQUEST);
		deploymentInfo.setResourceManager(new ClassPathResourceManager(deploymentInfo.getClassLoader(), "META-INF/resources"));

		log.config("Completed Registering JWebSwing in undertow");
	}
}
