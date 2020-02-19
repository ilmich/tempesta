package io.github.ilmich.tempesta.web.http.auth;

import io.github.ilmich.tempesta.web.handler.RequestHandler;
import io.github.ilmich.tempesta.web.http.Request;
import io.github.ilmich.tempesta.web.http.Response;

public interface AuthHandler {

	public boolean isAuthorizedRequest(Request request);

	public void authorize(Request request, Response response);

	public void deAuthorize(Request request, Response response);

	public RequestHandler getUnAuthorizedRequestHandler(Request request);

}
