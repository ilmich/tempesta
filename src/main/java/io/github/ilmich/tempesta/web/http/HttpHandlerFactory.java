package io.github.ilmich.tempesta.web.http;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.github.ilmich.tempesta.configuration.Configuration;
import io.github.ilmich.tempesta.util.HttpUtil;
import io.github.ilmich.tempesta.web.handler.BadRequestRequestHandler;
import io.github.ilmich.tempesta.web.handler.HandlerFactory;
import io.github.ilmich.tempesta.web.handler.HttpContinueRequestHandler;
import io.github.ilmich.tempesta.web.handler.NotFoundRequestHandler;
import io.github.ilmich.tempesta.web.handler.RequestHandler;
import io.github.ilmich.tempesta.web.handler.StaticContentHandler;

public class HttpHandlerFactory implements HandlerFactory {

	/**
	 * "Normal/Absolute" (non group capturing) RequestHandlers e.g. "/", "/persons"
	 */
	private Map<String, HttpRequestHandler> absoluteHandlers = new HashMap<String, HttpRequestHandler>();

	/**
	 * Group capturing RequestHandlers e.g. "/persons/([0-9]+)",
	 * "/persons/(\\d{1,3})"
	 */
	private Map<String, HttpRequestHandler> capturingHandlers = new HashMap<String, HttpRequestHandler>();

	/**
	 * A mapping between group capturing RequestHandlers and their corresponding
	 * pattern ( e.g. "([0-9]+)" )
	 */
	private Map<HttpRequestHandler, Pattern> patterns = new HashMap<HttpRequestHandler, Pattern>();

	/**
	 * The directory where static content (files) will be served from.
	 */
	private String staticContentDir;

	/**
	 * A copy of the <code>Configuration</code> used to create this type.
	 */
	// private Configuration configuration;

	public HttpHandlerFactory() {
		super();
	}

	public HttpHandlerFactory(Configuration conf) {

		/*if (!Strings.isNullOrEmpty(conf.getHandlerPackage()))
			conf.addHandlers(new AnnotationsScanner().findHandlers(conf.getHandlerPackage()));*/

		for (String path : conf.getHandlerMap().keySet()) {
			addRoute(path, conf.getHandlerMap().get(path));
		}

		staticContentDir = conf.getStaticDirectory();
	}

	public HttpHandlerFactory addRoute(String path, HttpRequestHandler handler) {
		int index = path.lastIndexOf("/");
		String group = path.substring(index + 1, path.length());
		if (containsCapturingGroup(group)) {
			// path ends with capturing group, e.g path ==
			// "/person/([0-9]+)"
			capturingHandlers.put(path.substring(0, index + 1), handler);
			patterns.put(handler, Pattern.compile(group));
		} else {
			// "normal" path, e.g. path == "/"
			absoluteHandlers.put(path, handler);
		}
		return this;
	}

	/**
	 * 
	 * @param path Requested path
	 * @return Returns the {@link RequestHandler} associated with the given path. If
	 *         no mapping exists a {@link NotFoundRequestHandler} is returned.
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
				return rh;
				//return (rh.isSingleton()) ? rh : RequestHandlerFactory.cloneHandler(rh);
			}
		} else {
			return rh;
			//return (rh.isSingleton()) ? rh : RequestHandlerFactory.cloneHandler(rh);
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
		if (rh == null) {
			return NotFoundRequestHandler.getInstance();
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
	 * @param configuration the <code>Configuration</code> to apply.
	 */
	/*
	 * public void setConfiguration(Configuration configuration) {
	 * this.configuration = configuration; }
	 */

	/**
	 * Retrieve the <code>Configuration</code> used by this type.
	 * 
	 * @return the current <code>Configuration</code>.
	 */
	/*
	 * public Configuration getConfiguration() { return configuration; }
	 */


}
