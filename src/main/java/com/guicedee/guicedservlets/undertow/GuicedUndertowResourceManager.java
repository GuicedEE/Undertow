package com.guicedee.guicedservlets.undertow;

import com.guicedee.guicedinjection.GuiceContext;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.URLResource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class GuicedUndertowResourceManager
		extends ClassPathResourceManager
{

	private static final Set<String> blacklistCriteria = new HashSet<>();

	private static final ClassPathResourceManager cpr = new ClassPathResourceManager(ClassLoader.getSystemClassLoader());

	private static String[] resourceLocations = {"", "META-INF/resources/"};
	private static ScanResult sr;

	static
	{
		blacklistCriteria.add(".class");
	}

	static
	{
		sr = GuiceContext.instance()
		                 .getScanResult();
	}

	private ClassLoader classLoader;

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
		super(classLoader, "/");
		this.classLoader = classLoader;
	}

	@Override
	public Resource getResource(String path) throws IOException
	{
		String pathOriginal = path;
		String pathExt = null;

		if (path.indexOf('.') >= 0)
		{
			pathExt = path.substring(path.lastIndexOf('.'));
		}
		else
		{
			pathExt = path;
		}
		if (blacklistCriteria.contains(pathExt.toLowerCase()))
		{
			throw new IOException("Blacklisted Fetch : " + path);
		}

		try
		{
			for (String resourceLocation : resourceLocations)
			{
				for (io.github.classgraph.Resource resource : sr.getResourcesWithPath(resourceLocation + path))
				{
					return new URLResource(resource.getURL(), pathOriginal);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Resource r = super.getResource(pathOriginal);
		if (r == null)
		{
			r = new ClassPathResourceManager(classLoader, "META-INF/resources/").getResource(pathOriginal);
		}
		if (r == null)
		{
			//System.out.println("really not found : " + pathOriginal);
		}
		return r;
	}
}
