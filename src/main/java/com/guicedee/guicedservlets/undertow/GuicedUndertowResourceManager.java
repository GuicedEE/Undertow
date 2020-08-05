package com.guicedee.guicedservlets.undertow;

import com.guicedee.guicedinjection.GuiceContext;
import com.guicedee.logger.LogFactory;
import io.github.classgraph.ScanResult;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.URLResource;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static com.guicedee.guicedinjection.json.StaticStrings.*;

@SuppressWarnings("unused")
public class GuicedUndertowResourceManager
        extends ClassPathResourceManager {

    private static final Set<String> rejectListCriteria = new HashSet<>();
    private static final Map<String,Resource> resourceCache = new ConcurrentHashMap<>();

    static {
        rejectListCriteria.add(".class");
        rejectListCriteria.add(".map");
    }

    private ScanResult getScanResult() {
        return GuiceContext.instance()
                .getScanResult();
    }

    public GuicedUndertowResourceManager(ClassLoader loader, Package p) {
        super(loader, p);
    }

    public GuicedUndertowResourceManager(ClassLoader classLoader, String prefix) {
        super(classLoader, prefix);
    }

    public GuicedUndertowResourceManager(ClassLoader classLoader) {
        super(classLoader, STRING_FORWARD_SLASH);
    }

    @Override
    public Resource getResource(String path) throws IOException {
        if ("/RES_NOT_FOUND".equals(path)) {
            return null;
        }
        String pathOriginal = path.startsWith(STRING_FORWARD_SLASH) ? path.substring(1) : path;
        if(resourceCache.containsKey(pathOriginal))
            return resourceCache.get(pathOriginal);

        StringBuilder pathDir = new StringBuilder(pathOriginal.indexOf(CHAR_SLASH) < 0 ? STRING_EMPTY : pathOriginal.substring(0, pathOriginal.lastIndexOf(CHAR_SLASH)));
        String pathName = pathOriginal.indexOf(CHAR_SLASH) < 0 ? pathOriginal : pathOriginal.substring(pathOriginal.lastIndexOf(CHAR_SLASH) + 1);
        String pathExt;
        if (path.indexOf(CHAR_DOT) >= 0) {
            pathExt = pathName.substring(pathName.lastIndexOf(CHAR_DOT));
        } else {
            Resource r =super.getResource(path);
            if(r!= null)
            {
                resourceCache.put(pathOriginal, r);
                return r;
            }
            return null;
        }
        if (rejectListCriteria.contains(pathExt.toLowerCase())) {
            throw new IOException("Rejected Fetch : " + path);
        }
        try {
            if (pathName.startsWith("/javax.faces.resource/")) {
                pathName = pathName.substring(22);
            } else if (pathName.startsWith("javax.faces.resource/")) {
                pathName = pathName.substring(21);
            }
            String newPattern;
            if (pathDir.length() > 0) {
                pathDir.append(STRING_FORWARD_SLASH);
                newPattern = ".*(" + pathDir + pathName + ")";
            } else {
                newPattern = "(" + pathDir + pathName + ")";
            }
            Pattern pattern = Pattern.compile(newPattern);
            java.util.Collection<io.github.classgraph.Resource> resources = getScanResult().getResourcesMatchingPattern(pattern);
            if (resources != null)
                for (io.github.classgraph.Resource resource : resources) {
                    URL url = resource.getURL();
                    if (url == null) {
                        LogFactory.getLog(getClass()).log(Level.SEVERE, "Cannot find through scan result -" + pathOriginal);
                        continue;
                    }
                    resourceCache.put(pathOriginal, new URLResource(resource.getURL(), pathOriginal));
                    return resourceCache.get(pathOriginal);
                }
        } catch (Exception e) {
            LogFactory.getLog(getClass()).log(Level.FINE, "No scan result -" + pathOriginal, e);
        }
        Resource r = super.getResource(path);
        if(r != null) {
            resourceCache.put(pathOriginal, super.getResource(path));
            return resourceCache.get(pathOriginal);
        }else {
            LogFactory.getLog(getClass()).log(Level.WARNING, "Resource not found -" + pathOriginal);
            return null;
        }
    }

    /**
     * Set or update a reject list criteria for file extensions
     *
     * @return A set of list of extensions excluded
     */
    public static Set<String> getRejectListCriteria() {
        return rejectListCriteria;
    }
}
