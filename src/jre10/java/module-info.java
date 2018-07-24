import com.jwebmp.undertow.JWebMPUndertowWebSocketConfiguration;
import com.jwebmp.undertow.UndertowJWebMPHandlerExtension;
import com.jwebmp.websockets.services.IJWebMPWebSocketPreConfiguration;

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

	provides io.undertow.servlet.ServletExtension with UndertowJWebMPHandlerExtension;
	provides IJWebMPWebSocketPreConfiguration with JWebMPUndertowWebSocketConfiguration;

	opens com.jwebmp.undertow;
}
