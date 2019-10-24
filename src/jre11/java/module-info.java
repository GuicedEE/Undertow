import com.guicedee.guicedinjection.interfaces.IGuiceScanJarExclusions;
import com.guicedee.guicedinjection.interfaces.IGuiceScanModuleExclusions;
import com.guicedee.guicedservlets.undertow.GuicedUndertowWebSocketConfiguration;
import com.guicedee.guicedservlets.undertow.UndertowGuicedHandlerExtension;
import com.guicedee.guicedservlets.undertow.UndertowWebSocketSessionProvider;
import com.guicedee.guicedservlets.undertow.implementations.UndertowModuleExclusions;
import com.guicedee.guicedservlets.undertow.services.UndertowDeploymentConfigurator;
import com.guicedee.guicedservlets.websockets.services.IWebSocketPreConfiguration;
import com.guicedee.guicedservlets.websockets.services.IWebSocketSessionProvider;

module com.guicedee.guicedservlets.undertow
{
	exports com.guicedee.guicedservlets.undertow;
	exports com.guicedee.guicedservlets.undertow.services;

	requires com.google.guice.extensions.servlet;

	requires undertow.core;
	requires undertow.servlet;
	requires javax.servlet.api;
	requires java.logging;
	requires xnio.api;
	requires undertow.websockets.jsr;
	requires com.google.common;
	requires com.google.guice;
	requires javax.inject;

	requires transitive com.guicedee.logmaster;
	requires transitive com.guicedee.guicedservlets.websockets;

	requires transitive com.guicedee.guicedinjection;
	//requires transitive com.guicedee.core;
	requires transitive com.guicedee.guicedservlets;
	requires java.validation;

	requires transitive jdk.unsupported;
	requires io.github.classgraph;

	provides io.undertow.servlet.ServletExtension with UndertowGuicedHandlerExtension;
	provides IWebSocketPreConfiguration with GuicedUndertowWebSocketConfiguration;
	provides IWebSocketSessionProvider with UndertowWebSocketSessionProvider;

	provides IGuiceScanJarExclusions with UndertowModuleExclusions;
	provides IGuiceScanModuleExclusions with UndertowModuleExclusions;

	opens com.guicedee.guicedservlets.undertow to com.google.guice;
	opens com.guicedee.guicedservlets.undertow.services to com.google.guice;

	uses UndertowDeploymentConfigurator;
}
