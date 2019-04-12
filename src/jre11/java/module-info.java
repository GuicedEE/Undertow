module com.jwebmp.undertow
{
	exports com.jwebmp.undertow;

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

	requires transitive com.jwebmp.logmaster;
	requires transitive com.jwebmp.websockets;

	requires transitive com.jwebmp.guicedinjection;
	//requires transitive com.jwebmp.core;
	requires transitive com.jwebmp.guicedservlets;
	requires java.validation;

	requires transitive jdk.unsupported;

	provides io.undertow.servlet.ServletExtension with com.jwebmp.undertow.UndertowJWebMPHandlerExtension;
	provides com.jwebmp.websockets.services.IWebSocketPreConfiguration with com.jwebmp.undertow.JWebMPUndertowWebSocketConfiguration;
	provides com.jwebmp.websockets.services.IWebSocketSessionProvider with com.jwebmp.undertow.UndertowWebSocketSessionProvider;

	provides com.jwebmp.guicedinjection.interfaces.IGuiceScanJarExclusions with com.jwebmp.undertow.implementations.UndertowModuleExclusions;
	provides com.jwebmp.guicedinjection.interfaces.IGuiceScanModuleExclusions with com.jwebmp.undertow.implementations.UndertowModuleExclusions;

	opens com.jwebmp.undertow;
}
