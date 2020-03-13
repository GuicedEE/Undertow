module com.guicedee.guicedservlets.undertow
{
	exports com.guicedee.guicedservlets.undertow;
	exports com.guicedee.guicedservlets.undertow.services;

	requires com.google.guice.extensions.servlet;

	requires transitive undertow.core;
	requires transitive undertow.servlet;
	requires transitive javax.servlet.api;
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
	requires transitive java.validation;

	requires transitive jdk.unsupported;
	requires io.github.classgraph;

	provides io.undertow.servlet.ServletExtension with com.guicedee.guicedservlets.undertow.UndertowGuicedHandlerExtension;
	provides com.guicedee.guicedservlets.websockets.services.IWebSocketPreConfiguration with com.guicedee.guicedservlets.undertow.GuicedUndertowWebSocketConfiguration;
	provides com.guicedee.guicedservlets.websockets.services.IWebSocketSessionProvider with com.guicedee.guicedservlets.undertow.UndertowWebSocketSessionProvider;

	uses com.guicedee.guicedservlets.undertow.services.UndertowDeploymentConfigurator;
}
