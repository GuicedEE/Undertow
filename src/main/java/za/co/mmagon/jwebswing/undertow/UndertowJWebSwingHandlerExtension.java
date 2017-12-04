package za.co.mmagon.jwebswing.undertow;

import com.armineasy.injection.GuiceContext;
import com.google.inject.servlet.GuiceFilter;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

@SuppressWarnings("unused")
public class UndertowJWebSwingHandlerExtension implements ServletExtension
{
	@Override
	public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext)
	{
		System.out.println("Registering JWebSwing in undertow");
		deploymentInfo.addFilter(new FilterInfo("GuiceFilter",GuiceFilter.class));
		deploymentInfo.addFilterUrlMapping("GuiceFilter", "/*", DispatcherType.REQUEST);
		deploymentInfo.setResourceManager(new ClassPathResourceManager(deploymentInfo.getClassLoader(),"META-INF/resources"));
		GuiceContext.inject();
	}
}
