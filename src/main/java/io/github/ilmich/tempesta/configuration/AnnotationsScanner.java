/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package io.github.ilmich.tempesta.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


import com.google.common.base.Strings;

import io.github.ilmich.tempesta.annotation.Path;
import io.github.ilmich.tempesta.util.ReflectionTools;
import io.github.ilmich.tempesta.web.http.HttpRequestHandler;

/**
 * Provides functionality to retrieve known <code>Annotation</code> types and
 * associated values.
 */
public class AnnotationsScanner {

    private final static Logger logger = Logger.getLogger(AnnotationsScanner.class.getName());

    /**
     * A <code>Map</code> of <code>RequestHandler</code>s associated with
     * {@link Path}s.
     */
    private Map<String, HttpRequestHandler> pathHandlers = new HashMap<String, HttpRequestHandler>();

    /**
     * Recursively iterate the given package, and attempt to resolve all
     * annotated references for <code>RequestHandler</code> implementations.
     * 
     * @param handlerPackage the base package to scan, for example
     *            "org.apache.awf".
     * @return a <code>Map&lt;String, RequestHandler&gt;</code> of handlers,
     *         which may be empty but not <code>null</code>.
     */
    public Map<String, HttpRequestHandler> findHandlers(String handlerPackage) {

        if (Strings.isNullOrEmpty(handlerPackage)) {
            logger.warning("No RequestHandler package defined");
            return pathHandlers;
        }

        List<Class<?>> classes = findClasses(handlerPackage);
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Path.class)) {

        	HttpRequestHandler handler = (HttpRequestHandler) ReflectionTools.createInstance(clazz.getCanonicalName());
                Path path = clazz.getAnnotation(Path.class);
                pathHandlers.put(path.value(), handler);

                logger.info("Added RequestHandler [" + clazz.getCanonicalName() + "] for Path [" + path.value() + "]");
            }
        }

        return pathHandlers;
    }

    /**
     * Recursively finds all classes available to the context
     * <code>ClassLoader</code> from the given package.
     * 
     * @param packageName the package from which to commence the scan.
     * @return A <code>List</code> of <code>Class</code> references.
     */
    private List<Class<?>> findClasses(String packageName) {

        List<Class<?>> classes = new ArrayList<Class<?>>();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            logger.severe("Context ClassLoader was not available");
            return classes;
        }

        String path = packageName.replace('.', '/');
        try {
            List<File> directories = new ArrayList<File>();

            Enumeration<URL> resources = loader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                directories.add(new File(resource.getFile()));
            }

            for (File directory : directories) {
                classes.addAll(findClasses(directory, packageName));
            }
        } catch (IOException e) {
            logger.severe("Exception accessing resources for [" + path + "]: " + e.getMessage());
        }

        return classes;
    }

    /**
     * Recursively finds all class files available for the given package from
     * the passed directory.
     * 
     * @param packageName the package from which to commence the scan.
     * @return A <code>List</code> of <code>Class</code> references.
     */
    private List<Class<?>> findClasses(File directory, String packageName) {

        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (directory == null || !directory.exists()) {
            logger.severe("Directory is null value or non-existent, [" + directory + "]");
            return classes;
        }

        for (File file : directory.listFiles()) {
            try {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.'
                            + file.getName().substring(0, file.getName().length() - 6)));
                }
            } catch (ClassNotFoundException e) {
                logger.severe("ClassNotFoundException: " + e.getMessage());
            }
        }

        return classes;
    }
}
