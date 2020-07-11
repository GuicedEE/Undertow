package com.guicedee.guicedservlets.undertow;

import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.logger.LogFactory;
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
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.guicedee.guicedinjection.json.StaticStrings.*;

public class GuicedUndertowResourceManager
		extends ClassPathResourceManager
{

	private static final Set<String> blacklistCriteria = new HashSet<>();

	private static final ClassPathResourceManager cpr = new ClassPathResourceManager(ClassLoader.getSystemClassLoader());

	private static String[] resourceLocations = {"", "META-INF/resources/"};

	static
	{
		blacklistCriteria.add(".class");
	}

	private ScanResult getScanResult()
	{
		return GuiceContext.instance()
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
		if("/RES_NOT_FOUND".equals(path))
		{
			System.out.println("Resource not found!");
		}
		String pathOriginal = path;
		String pathExt = null;

		if (path.indexOf(CHAR_DOT) >= 0)
		{
			pathExt = path.substring(path.lastIndexOf(CHAR_DOT));
		}
		else
		{
			return super.getResource(path);
		}
		if (blacklistCriteria.contains(pathExt.toLowerCase()))
		{
			throw new IOException("Blacklisted Fetch : " + path);
		}

		try
		{
			for (String resourceLocation : resourceLocations)
			{
				java.util.Collection<io.github.classgraph.Resource> resources =getScanResult().getResourcesWithPath(resourceLocation + path);
				if(resources != null)
				for (io.github.classgraph.Resource resource : resources)
				{
					URL url = resource.getURL();
					if(url == null)
					{
						LogFactory.getLog(getClass()).log(Level.SEVERE,"Cannot find through scan result -" + pathOriginal);
						continue;
					}
					if (url.toString()
					       .contains("jrt:/"))
					{
						URI uri = URI.create(url.toString());
						return new URLResource(uri.toURL(), pathOriginal);
					}
					return new URLResource(resource.getURL(), pathOriginal);
				}
			}
		}
		catch (Exception e)
		{
			LogFactory.getLog(getClass()).log(Level.FINE,"No scan result -" + pathOriginal,e);
		}
		Resource r = super.getResource(pathOriginal);
		if (r == null)
		{
			r = new ClassPathResourceManager(classLoader, "META-INF/resources/").getResource(pathOriginal);
		}
		if (r == null)
		{
			if(pathOriginal.startsWith("/resources/"))
			{
				String newPathOriginal = pathOriginal.replaceFirst("/resources/", "");
				r = getResource(newPathOriginal);
				if(r == null)
				{
					System.out.println("Not found resource : " + pathOriginal);
				}
				return r;
			}
		}
		return r;
	}
}
