package com.guicedee.guicedservlets.undertow;

import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.Resource;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GuicedUndertowResourceManager extends ClassPathResourceManager {

	private static final Set<String> deniedSearchCriteria = new HashSet<>();

	private static final ClassPathResourceManager cpr = new ClassPathResourceManager(ClassLoader.getSystemClassLoader());

	public GuicedUndertowResourceManager(ClassLoader loader, Package p) {
		super(loader, p);
	}

	public GuicedUndertowResourceManager(ClassLoader classLoader, String prefix) {
		super(classLoader, prefix);
	}

	public GuicedUndertowResourceManager(ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	public Resource getResource(String path) throws IOException {
		for (String deniedSearchCriterion : deniedSearchCriteria) {
			if(path.contains(deniedSearchCriterion))
				return null;
		}
		Resource r = super.getResource(path);
		return r;
	}
}
