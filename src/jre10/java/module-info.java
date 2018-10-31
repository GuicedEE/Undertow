import com.jwebmp.undertow.JWebMPUndertowWebSocketConfiguration;
import com.jwebmp.undertow.UndertowJWebMPHandlerExtension;
import com.jwebmp.undertow.UndertowWebSocketSessionProvider;
import com.jwebmp.websockets.services.IWebSocketPreConfiguration;

module com.jwebmp.undertow
{
	exports com.jwebmp.undertow;

	requires transitive undertow.core;
	requires transitive undertow.servlet;
	requires transitive xnio.api;
	requires transitive undertow.websockets.jsr;
	requires transitive com.jwebmp.websockets;

	requires transitive com.jwebmp.core;

	provides io.undertow.servlet.ServletExtension with UndertowJWebMPHandlerExtension;
	provides IWebSocketPreConfiguration with JWebMPUndertowWebSocketConfiguration;
	provides com.jwebmp.websockets.services.IWebSocketSessionProvider with UndertowWebSocketSessionProvider;

	opens com.jwebmp.undertow;
}
