package za.co.mmagon.plugins.atmosphere.undertow;

import com.google.inject.servlet.GuiceFilter;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import org.atmosphere.cpr.AtmosphereServlet;
import za.co.mmagon.logger.LogFactory;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class UndertowAtmosphereHandlerExtension implements ServletExtension
{
	private static final Logger log = LogFactory.getLog("JwebSwingUndertow");

	@Override
	public void handleDeployment(DeploymentInfo servletBuilder, ServletContext servletContext)
	{
		log.config("Registering Atmosphere Push in Undertow");

		servletBuilder.addFilter(new FilterInfo("AtmospherePushFilter", GuiceFilter.class));
		servletBuilder.addFilterUrlMapping("AtmospherePushFilter", "/*", DispatcherType.REQUEST);

		servletBuilder.addServlet(Servlets.servlet("AtmosphereServlet", AtmosphereServlet.class)
				                          .addInitParam("org.atmosphere.cpr.packages", "za.co.mmagon.plugins.atmosphere")
				                          .addMapping("/jwatmospush")
				                          .setAsyncSupported(true));

		log.config("Completed Registering Atmosphere Push in Undertow");
	}
}
