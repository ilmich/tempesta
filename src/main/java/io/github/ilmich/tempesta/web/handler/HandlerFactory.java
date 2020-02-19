package io.github.ilmich.tempesta.web.handler;

import io.github.ilmich.tempesta.web.handler.RequestHandler;
import io.github.ilmich.tempesta.web.http.HttpHandlerFactory;
import io.github.ilmich.tempesta.web.http.HttpRequestHandler;
import io.github.ilmich.tempesta.web.http.Request;

public interface HandlerFactory {

	public RequestHandler getHandler(Request request);

	public HttpHandlerFactory addRoute(String path, HttpRequestHandler handler);

}
