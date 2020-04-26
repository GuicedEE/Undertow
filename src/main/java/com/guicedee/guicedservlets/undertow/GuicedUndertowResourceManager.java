package com.guicedee.guicedservlets.undertow;

import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.Resource;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GuicedUndertowResourceManager
		extends ClassPathResourceManager
{
	private static final Set<String> whitelistCriteria = new HashSet<>();

	private static String resourceLocation = "META-INF/resources/";

	static
	{
		whitelistCriteria.add(".css");
		whitelistCriteria.add(".js");
		whitelistCriteria.add(".jpg");
		whitelistCriteria.add(".gif");
		whitelistCriteria.add(".jpeg");
		whitelistCriteria.add(".json");
		whitelistCriteria.add(".woff");
		whitelistCriteria.add(".woff2");
		whitelistCriteria.add(".svg");
		whitelistCriteria.add(".ttf");
		whitelistCriteria.add(".eot");
		whitelistCriteria.add(".png");
		whitelistCriteria.add(".html");
		whitelistCriteria.add(".htm");
		whitelistCriteria.add(".xhtml");
	}

	private static final ClassPathResourceManager cpr = new ClassPathResourceManager(ClassLoader.getSystemClassLoader());

	public GuicedUndertowResourceManager(ClassLoader loader, Package p)
	{
		super(loader, p);
	}

	public GuicedUndertowResourceManager(ClassLoader classLoader, String prefix)
	{
		super(classLoader, prefix);
	}

	public GuicedUndertowResourceManager(ClassLoader classLoader)
	{
		super(classLoader, "META-INF/resources/");
	}

	@Override
	public Resource getResource(String path) throws IOException
	{
		String pathExt = null;
		if (path.indexOf('.') >= 0)
		{
			pathExt = path.substring(path.lastIndexOf('.'));
		}
		else
		{
			pathExt = path;
		}

		if (whitelistCriteria.contains(pathExt.toLowerCase()))
		{
			return super.getResource(path);
		}
		throw new IOException("Not able to read resource : " + path);
	}

}
