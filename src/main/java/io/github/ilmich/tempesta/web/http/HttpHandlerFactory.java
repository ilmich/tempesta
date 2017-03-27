package io.github.ilmich.tempesta.web.http;

import java.util.regex.Pattern;


import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import io.github.ilmich.tempesta.configuration.AnnotationsScanner;
import io.github.ilmich.tempesta.configuration.Configuration;
import io.github.ilmich.tempesta.util.HttpUtil;
import io.github.ilmich.tempesta.web.handler.BadRequestRequestHandler;
import io.github.ilmich.tempesta.web.handler.HandlerFactory;
import io.github.ilmich.tempesta.web.handler.HttpContinueRequestHandler;
import io.github.ilmich.tempesta.web.handler.NotFoundRequestHandler;
import io.github.ilmich.tempesta.web.handler.RequestHandler;
import io.github.ilmich.tempesta.web.handler.RequestHandlerFactory;
import io.github.ilmich.tempesta.web.handler.StaticContentHandler;
import io.github.ilmich.tempesta.web.handler.UnAuthorizedBasicRequestHandler;
import io.github.ilmich.tempesta.web.http.auth.AuthHandler;

public class HttpHandlerFactory implements HandlerFactory {

    /**
     * "Normal/Absolute" (non group capturing) RequestHandlers e.g. "/",
     * "/persons"
     */
    private ImmutableMap<String, HttpRequestHandler> absoluteHandlers;

    /**
     * Group capturing RequestHandlers e.g. "/persons/([0-9]+)",
     * "/persons/(\\d{1,3})"
     */
    private ImmutableMap<String, HttpRequestHandler> capturingHandlers;

    /**
     * A mapping between group capturing RequestHandlers and their corresponding
     * pattern ( e.g. "([0-9]+)" )
     */
    private ImmutableMap<HttpRequestHandler, Pattern> patterns;

    /**
     * The directory where static content (files) will be served from.
     */
    private String staticContentDir;

    /**
     * A copy of the <code>Configuration</code> used to create this type.
     */
    //private Configuration configuration;

    private AuthHandler authHandler;

    public HttpHandlerFactory() {
	super();
    }
    
    public HttpHandlerFactory(Configuration conf) {
	ImmutableMap.Builder<String, HttpRequestHandler> builder = new ImmutableMap.Builder<String, HttpRequestHandler>();
	ImmutableMap.Builder<String, HttpRequestHandler> capturingBuilder = new ImmutableMap.Builder<String, HttpRequestHandler>();
	ImmutableMap.Builder<HttpRequestHandler, Pattern> patternsBuilder = new ImmutableMap.Builder<HttpRequestHandler, Pattern>();

	if (!Strings.isNullOrEmpty(conf.getHandlerPackage()))
	    conf.addHandlers(new AnnotationsScanner().findHandlers(conf.getHandlerPackage()));

	for (String path : conf.getHandlerMap().keySet()) {
	    int index = path.lastIndexOf("/");
	    String group = path.substring(index + 1, path.length());
	    if (containsCapturingGroup(group)) {
		// path ends with capturing group, e.g path ==
		// "/person/([0-9]+)"
		capturingBuilder.put(path.substring(0, index + 1), conf.getHandlerMap().get(path));
		patternsBuilder.put(conf.getHandlerMap().get(path), Pattern.compile(group));
	    } else {
		// "normal" path, e.g. path == "/"
		builder.put(path, conf.getHandlerMap().get(path));
	    }
	}
	absoluteHandlers = builder.build();
	capturingHandlers = capturingBuilder.build();
	patterns = patternsBuilder.build();
	staticContentDir = conf.getStaticDirectory();
    }

    /**
     * 
     * @param path
     *            Requested path
     * @return Returns the {@link RequestHandler} associated with the given
     *         path. If no mapping exists a {@link NotFoundRequestHandler} is
     *         returned.
     */
    private HttpRequestHandler getHandler(String path) {

	HttpRequestHandler rh = absoluteHandlers.get(path);
	if (rh == null) {
	    rh = getCapturingHandler(path);
	    if (rh == null) {
		rh = getStaticContentHandler(path);
		if (rh != null) {
		    return rh;
		}
	    } else {
		return (rh.isSingleton()) ? rh : RequestHandlerFactory.cloneHandler(rh);
	    }
	} else {
	    return (rh.isSingleton()) ? rh : RequestHandlerFactory.cloneHandler(rh);
	}

	return NotFoundRequestHandler.getInstance();
    }

    public RequestHandler getHandler(Request request) {

	if (!HttpUtil.verifyRequest(request)) {
	    return BadRequestRequestHandler.getInstance();
	}
	// if @Authenticated annotation is present, make sure that the
	// request/user is authenticated
	// (i.e RequestHandler.getCurrentUser() != null).
	HttpRequestHandler rh = getHandler(request.getRequestedPath());	
	rh.setAuthHandler(getAuthHandler());	
	if (rh.isMethodAuthenticated(request.getMethod())
		&& (getAuthHandler() == null || !getAuthHandler().isAuthorizedRequest(request))) {	    
	    return getAuthHandler() == null ? new UnAuthorizedBasicRequestHandler("Unknown") : 
					      getAuthHandler().getUnAuthorizedRequestHandler(request);
	}

	if (request.expectContinue()) {
	    return HttpContinueRequestHandler.getInstance();
	}

	return rh;
    }

    private static boolean containsCapturingGroup(String group) {
	boolean containsGroup = group.matches("^\\(.*\\)$");
	Pattern.compile(group); // throws PatternSyntaxException if group is
				// malformed regular expression
	return containsGroup;
    }

    private HttpRequestHandler getCapturingHandler(String path) {
	int index = path.lastIndexOf("/");
	if (index != -1) {
	    String init = path.substring(0, index + 1); // path without its last
							// segment
	    String group = path.substring(index + 1, path.length());
	    HttpRequestHandler handler = capturingHandlers.get(init);
	    if (handler != null) {
		Pattern regex = patterns.get(handler);
		if (regex.matcher(group).matches()) {
		    return handler;
		}
	    }
	}
	return null;
    }

    protected HttpRequestHandler getStaticContentHandler(String path) {
	if (staticContentDir == null || path.length() <= staticContentDir.length()) {
	    return null; // quick reject (no static dir or simple contradiction)
	}

	if (path.substring(1).startsWith(staticContentDir)) {
	    return StaticContentHandler.getInstance();
	} else {
	    return null;
	}
    }

    void setStaticContentDir(String scd) {
	staticContentDir = scd;
    }

    /**
     * Set the <code>Configuration</code> for use with this type.
     * 
     * @param configuration
     *            the <code>Configuration</code> to apply.
     */
    /*public void setConfiguration(Configuration configuration) {
	this.configuration = configuration;
    }*/

    /**
     * Retrieve the <code>Configuration</code> used by this type.
     * 
     * @return the current <code>Configuration</code>.
     */
    /*public Configuration getConfiguration() {
	return configuration;
    }*/
    
    public AuthHandler getAuthHandler() {
	return authHandler;
    }

    public HttpHandlerFactory setAuthHandler(AuthHandler authHandler) {
	this.authHandler = authHandler;
	return this;
    }

}
