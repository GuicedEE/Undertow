import com.jwebmp.undertow.UndertowJWebMPHandlerExtension;

module com.jwebmp.undertow
{
	exports com.jwebmp.undertow;

	requires com.google.guice.extensions.servlet;
	requires com.jwebmp.logmaster;
	requires undertow.core;
	requires undertow.servlet;
	requires javax.servlet.api;
	requires java.logging;

	provides io.undertow.servlet.ServletExtension with UndertowJWebMPHandlerExtension;
	opens com.jwebmp.undertow;
}
