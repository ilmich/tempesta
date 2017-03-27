package io.github.ilmich.tempesta.web.http;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import io.github.ilmich.tempesta.annotation.Asynchronous;
import io.github.ilmich.tempesta.annotation.Authenticated;
import io.github.ilmich.tempesta.annotation.SingletonHandler;
import io.github.ilmich.tempesta.web.handler.RequestHandler;
import io.github.ilmich.tempesta.web.http.auth.AuthHandler;
import io.github.ilmich.tempesta.web.http.protocol.HttpStatus;
import io.github.ilmich.tempesta.web.http.protocol.HttpVerb;

public abstract class HttpRequestHandler extends RequestHandler {
    
    private final ImmutableMap<HttpVerb, Boolean> asynchVerbs;
    private final ImmutableMap<HttpVerb, Boolean> authVerbs;
    private final boolean singleton;
    private AuthHandler authHandler;
    
    public HttpRequestHandler() {

        Map<HttpVerb, Boolean> asyncV = Maps.newHashMap();
        Map<HttpVerb, Boolean> authV = Maps.newHashMap();
        for (HttpVerb verb : HttpVerb.values()) {
            asyncV.put(verb, isMethodAnnotated(verb, Asynchronous.class));
            authV.put(verb, isMethodAnnotated(verb, Authenticated.class));
        }

        asynchVerbs = ImmutableMap.copyOf(asyncV);
        authVerbs = ImmutableMap.copyOf(authV);
        singleton = getClass().isAnnotationPresent(SingletonHandler.class);        
    }
    
    private boolean isMethodAnnotated(HttpVerb verb, Class<? extends Annotation> annotation) {
        try {
            Class<?>[] parameterTypes = { HttpRequest.class, HttpResponse.class };
            return getClass().getMethod(verb.toString().toLowerCase(), parameterTypes).getAnnotation(annotation) != null;
        } catch (NoSuchMethodException nsme) {
            return false;
        }
    }

    public boolean isMethodAsynchronous(HttpVerb verb) {
        return asynchVerbs.get(verb);
    }
    
    public boolean isSingleton() {
    	return singleton;
    }
        
    public boolean isMethodAuthenticated(HttpVerb verb) {
        return authVerbs.get(verb);
    }    

    public String getCurrentUser(Request request) {
        return null;
    }        
    
    // Default implementation of HttpMethods return a 501 page
    public void get(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.SERVER_ERROR_NOT_IMPLEMENTED);
        response.write(" ");
    }

    public void post(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.SERVER_ERROR_NOT_IMPLEMENTED);
        response.write(" ");
    }

    public void put(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.SERVER_ERROR_NOT_IMPLEMENTED);
        response.write(" ");
    }

    public void delete(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.SERVER_ERROR_NOT_IMPLEMENTED);
        response.write(" ");
    }

    public void head(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.SERVER_ERROR_NOT_IMPLEMENTED);
        response.write(" ");
    }
    
    public void option(HttpRequest request, HttpResponse response) {
        response.setStatus(HttpStatus.SERVER_ERROR_NOT_IMPLEMENTED);
        response.write(" ");
    }

    /**
     * @return the authHandler
     */
    public AuthHandler getAuthHandler() {
        return authHandler;
    }

    /**
     * @param authHandler the authHandler to set
     */
    public void setAuthHandler(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }
}
