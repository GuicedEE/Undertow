package com.guicedee.guicedservlets.undertow;

import com.guicedee.client.*;
import io.github.classgraph.*;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.*;
import lombok.extern.java.Log;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.regex.*;

@SuppressWarnings("unused")
@Log
public class GuicedUndertowResourceManager extends ClassPathResourceManager
{
	
	private static final Set<String> rejectListCriteria = new HashSet<>();
	private static final Set<String> pathRemoveEntrySet = new HashSet<>();
	private static final Map<String, Resource> resourceCache = new ConcurrentHashMap<>();
	
	private ClassLoader loader;
	private static ResourceManager resourceManager;
	private static ResourceManager pathManager;
	
	static
	{
		rejectListCriteria.add(".class");
		
		pathRemoveEntrySet.add("jakarta.faces.resource");
		pathRemoveEntrySet.add("javax.faces.resource");
	}
	
	private ScanResult getScanResult()
	{
		return IGuiceContext
				       .getContext()
				       .getScanResult();
	}
	
	public GuicedUndertowResourceManager(ClassLoader loader, Package p)
	{
		super(loader, p);
		this.loader = loader;
		resourceManager = new ClassPathResourceManager(loader, "META-INF/resources/");
		
	}
	
	public GuicedUndertowResourceManager(ClassLoader classLoader, String prefix)
	{
		super(classLoader, prefix);
		this.loader = classLoader;
		resourceManager = new ClassPathResourceManager(loader, "META-INF/resources/");
		
	}
	
	public GuicedUndertowResourceManager(ClassLoader classLoader)
	{
		super(classLoader, "/");
		this.loader = classLoader;
		resourceManager = new ClassPathResourceManager(loader, "META-INF/resources/");
	}
	
	public static void setPathManager(ResourceManager pathManager)
	{
		GuicedUndertowResourceManager.pathManager = pathManager;
	}
	
	@Override
	public Resource getResource(String path) throws IOException
	{
		if ("/RES_NOT_FOUND".equals(path))
		{
			return null;
		}
		String pathOriginal = path.startsWith("/") ? path.substring(1) : path;
		for (String s : pathRemoveEntrySet)
		{
			String searchable = s;
			if (pathOriginal.startsWith(searchable))
			{
				pathOriginal = pathOriginal.substring(searchable.length());
			}
			searchable = "/" + s + "/";
			if (pathOriginal.startsWith(searchable))
			{
				pathOriginal = pathOriginal.substring(searchable.length());
			}
		}
		
		if (resourceCache.containsKey(pathOriginal))
		{
			return resourceCache.get(pathOriginal);
		}
		
		StringBuilder pathDir = new StringBuilder(pathOriginal.indexOf('/') < 0 ? "" : pathOriginal.substring(0, pathOriginal.lastIndexOf('/')));
		String pathName = pathOriginal.indexOf('/') < 0 ? pathOriginal : pathOriginal.substring(pathOriginal.lastIndexOf('/') + 1);
		String pathExt;
		if (path.indexOf('.') >= 0)
		{
			pathExt = pathName.substring(pathName.lastIndexOf('.'));
		}
		else
		{
			Resource r = super.getResource(path);
			if (r != null)
			{
				resourceCache.put(pathOriginal, r);
				return r;
			}
			return null;
		}
		if (rejectListCriteria.contains(pathExt.toLowerCase()))
		{
			log.log(Level.FINE, "Rejected request - banned criteria - " + pathOriginal);
			return null;
		}
		if (pathManager != null)
		{
			//fixed path fetching
			Resource r = pathManager.getResource(path);
			if (r != null)
			{
				resourceCache.put(pathOriginal, r);
				return r;
			}
		}
		try
		{
			String newPattern;
			if (pathDir.length() > 0)
			{
				pathDir.append("/");
				newPattern = ".*(" + pathDir + pathName + ")";
			}
			else
			{
				newPattern = "(" + pathDir + pathName + ")";
			}
			Pattern pattern = Pattern.compile(newPattern);
			java.util.Collection<io.github.classgraph.Resource> resources = getResourcesMatchingPattern(pattern);
			if (resources != null)
			{
				for (io.github.classgraph.Resource resource : resources)
				{
					URL url = resource.getURL();
					if (url == null)
					{
						log.log(Level.FINE, "Cannot find through scan result -" + pathOriginal);
						continue;
					}
					resourceCache.put(pathOriginal, new URLResource(resource.getURL(), pathOriginal));
					return resourceCache.get(pathOriginal);
				}
			}
		}
		catch (Exception e)
		{
			log.log(Level.FINER, "No scan result -" + pathOriginal);
		}
		Resource r = super.getResource(path);
		if (r == null)
		{
			r = resourceManager.getResource(pathDir + pathName);
		}
		
		if (r != null)
		{
			resourceCache.put(pathOriginal, r);
			return resourceCache.get(pathOriginal);
		}
		else
		{
			Pattern pattern = Pattern.compile(pathName + "$");
			java.util.Collection<io.github.classgraph.Resource> resources = getResourcesMatchingPattern(pattern);
			if (resources != null)
			{
				for (io.github.classgraph.Resource resource : resources)
				{
					URL url = resource.getURL();
					if (url == null)
					{
						log.log(Level.FINE, "Cannot find through scan result -" + pathOriginal);
						continue;
					}
					resourceCache.put(pathOriginal, new URLResource(resource.getURL(), pathOriginal));
					return resourceCache.get(pathOriginal);
				}
			}
			
			/*if (pathManager != null)
			{
				//fixed path fetching
				Resource indexHtmlResource = pathManager.getResource("index.html");
				if (indexHtmlResource != null)
				{
					resourceCache.put(pathOriginal, indexHtmlResource);
					return indexHtmlResource;
				}
			}*/
			
			log.log(Level.FINER, "Resource not found -" + pathOriginal);
			return null;
		}
	}
	
	private Map<Pattern, ResourceList> resourceListMap = new HashMap<>();
	
	private ResourceList getResourcesMatchingPattern(Pattern pattern)
	{
		if (resourceListMap.containsKey(pattern))
		{
			return resourceListMap.get(pattern);
		}
		ResourceList rl = getScanResult().getResourcesMatchingPattern(pattern);
		resourceListMap.put(pattern, rl);
		return rl;
	}
	
	/**
	 * Set or update a reject list criteria for file extensions
	 *
	 * @return A set of list of extensions excluded
	 */
	public static Set<String> getRejectListCriteria()
	{
		return rejectListCriteria;
	}
	
	/**
	 * A list of entries to remove from URLS (that start with)
	 *
	 * @return
	 */
	public static Set<String> getPathRemoveEntrySet()
	{
		return pathRemoveEntrySet;
	}
}
