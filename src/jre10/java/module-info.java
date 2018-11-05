import com.jwebmp.guicedinjection.interfaces.IGuiceScanJarExclusions;
import com.jwebmp.guicedinjection.interfaces.IGuiceScanModuleExclusions;
import com.jwebmp.undertow.JWebMPUndertowWebSocketConfiguration;
import com.jwebmp.undertow.UndertowJWebMPHandlerExtension;
import com.jwebmp.undertow.UndertowWebSocketSessionProvider;
import com.jwebmp.undertow.implementations.UndertowModuleExclusions;
import com.jwebmp.websockets.services.IWebSocketPreConfiguration;

module com.jwebmp.undertow
{
	exports com.jwebmp.undertow;

	requires com.google.guice.extensions.servlet;
	requires com.jwebmp.logmaster;
	requires undertow.core;
	requires undertow.servlet;
	requires javax.servlet.api;
	requires java.logging;
	requires xnio.api;
	requires undertow.websockets.jsr;
	requires com.jwebmp.websockets;
	requires com.google.common;
	requires com.google.guice;
	requires javax.inject;
	requires com.jwebmp.guicedinjection;
	requires com.jwebmp.core;
	requires com.jwebmp.guicedservlets;
	requires java.validation;

	provides io.undertow.servlet.ServletExtension with UndertowJWebMPHandlerExtension;
	provides IWebSocketPreConfiguration with JWebMPUndertowWebSocketConfiguration;
	provides com.jwebmp.websockets.services.IWebSocketSessionProvider with UndertowWebSocketSessionProvider;

	provides IGuiceScanJarExclusions with UndertowModuleExclusions;
	provides IGuiceScanModuleExclusions with UndertowModuleExclusions;

	opens com.jwebmp.undertow;
}
