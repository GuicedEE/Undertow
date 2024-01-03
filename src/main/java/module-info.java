import com.guicedee.guicedinjection.interfaces.IGuicePreDestroy;
import com.guicedee.guicedservlets.undertow.*;
import com.guicedee.guicedservlets.undertow.services.*;

module com.guicedee.guicedservlets.undertow {
	exports com.guicedee.guicedservlets.undertow;
	
	requires transitive com.guicedee.client;
	requires transitive com.guicedee.guicedservlets;
	
	
	requires transitive undertow.core;
	requires transitive undertow.servlet;
	//requires undertow.websockets.jsr;
	
	requires static lombok;
	

	
	requires transitive jakarta.validation;
	requires static jakarta.servlet;
	//requires static jakarta.persistence;
	requires static jakarta.xml.bind;
	requires com.guicedee.guicedinjection;
	
	//	requires jdk.unsupported;
	
	provides io.undertow.servlet.ServletExtension with com.guicedee.guicedservlets.undertow.UndertowGuicedHandlerExtension;
	
	provides IGuicePreDestroy with GuicedUndertow;
	
	provides com.guicedee.guicedservlets.undertow.services.UndertowDeploymentConfigurator with GuicedServletListenersRegister;
	
	opens com.guicedee.guicedservlets.undertow to com.google.guice;
	
	
	uses com.guicedee.guicedservlets.undertow.services.UndertowDeploymentConfigurator;
	uses UndertowPathHandler;
}
