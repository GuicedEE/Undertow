import com.guicedee.guicedservlets.undertow.*;
import com.guicedee.guicedservlets.undertow.services.*;

module com.guicedee.guicedservlets.undertow {
	exports com.guicedee.guicedservlets.undertow;
	exports com.guicedee.guicedservlets.undertow.services;
	
	requires transitive com.guicedee.guicedservlets.websockets;
	
	requires transitive undertow.core;
	requires transitive undertow.servlet;
	
	requires undertow.websockets.jsr;
	
	requires transitive jakarta.validation;
	requires static jakarta.servlet;
	//requires static jakarta.persistence;
	requires static jakarta.xml.bind;
	
	//	requires jdk.unsupported;
	
	provides io.undertow.servlet.ServletExtension with com.guicedee.guicedservlets.undertow.UndertowGuicedHandlerExtension;
	provides com.guicedee.guicedservlets.websockets.services.IWebSocketPreConfiguration with com.guicedee.guicedservlets.undertow.GuicedUndertowWebSocketConfiguration;
	provides com.guicedee.guicedservlets.websockets.services.IWebSocketSessionProvider with com.guicedee.guicedservlets.undertow.UndertowWebSocketSessionProvider;
	
	provides com.guicedee.guicedservlets.undertow.services.UndertowDeploymentConfigurator with GuicedServletListenersRegister;
	
	opens com.guicedee.guicedservlets.undertow to com.google.guice;
	opens com.guicedee.guicedservlets.undertow.services to com.google.guice;
	
	uses com.guicedee.guicedservlets.undertow.services.UndertowDeploymentConfigurator;
	uses UndertowPathHandler;
}
