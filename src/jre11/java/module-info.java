module com.jwebmp.undertow
{
	exports com.jwebmp.undertow;
	exports com.jwebmp.undertow.services;

	requires com.google.guice.extensions.servlet;

	requires transitive undertow.core;
	requires transitive undertow.servlet;
	requires transitive javax.servlet.api;
	requires java.logging;
	requires transitive xnio.api;
	requires transitive xnio;

	requires undertow.websockets.jsr;
	requires com.google.common;
	requires transitive com.google.guice;
	requires transitive javax.inject;

	requires transitive com.jwebmp.logmaster;
	requires transitive com.jwebmp.websockets;

	requires transitive com.jwebmp.guicedinjection;
	//requires transitive com.jwebmp.core;
	requires transitive com.jwebmp.guicedservlets;
	requires java.validation;

	requires jdk.unsupported;

	provides io.undertow.servlet.ServletExtension with com.jwebmp.undertow.UndertowJWebMPHandlerExtension;
	provides com.jwebmp.websockets.services.IWebSocketPreConfiguration with com.jwebmp.undertow.GuicedUndertowWebSocketConfiguration;
	provides com.jwebmp.websockets.services.IWebSocketSessionProvider with com.jwebmp.undertow.UndertowWebSocketSessionProvider;

	provides com.jwebmp.guicedinjection.interfaces.IGuiceScanJarExclusions with com.jwebmp.undertow.implementations.UndertowModuleExclusions;
	provides com.jwebmp.guicedinjection.interfaces.IGuiceScanModuleExclusions with com.jwebmp.undertow.implementations.UndertowModuleExclusions;

	opens com.jwebmp.undertow to com.google.guice;

	uses com.jwebmp.undertow.services.UndertowDeploymentConfigurator;
}
