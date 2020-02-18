package io.github.ilmich.tempesta.web.http.auth.handlers;


import io.github.ilmich.tempesta.util.Base64;
import io.github.ilmich.tempesta.util.Strings;
import io.github.ilmich.tempesta.web.handler.RequestHandler;
import io.github.ilmich.tempesta.web.handler.UnAuthorizedBasicRequestHandler;
import io.github.ilmich.tempesta.web.http.Request;
import io.github.ilmich.tempesta.web.http.Response;
import io.github.ilmich.tempesta.web.http.auth.AuthDb;
import io.github.ilmich.tempesta.web.http.auth.AuthHandler;

public class HttpBasicAuthHandler implements AuthHandler {

    private AuthDb authDb;
    private UnAuthorizedBasicRequestHandler handler;

    public HttpBasicAuthHandler(String realm) {
	super();
	this.handler = new UnAuthorizedBasicRequestHandler(realm);
    }

    public HttpBasicAuthHandler(AuthDb authDb, UnAuthorizedBasicRequestHandler handler) {
	super();
	this.authDb = authDb;
	this.handler = handler;
    }

    public boolean isAuthorizedRequest(Request request) {
	String token = request.getHeader("Authorization"); // get header
	if (Strings.isNullOrEmpty(token))
	    return false;

	String[] parts = token.split(" "); // sanity check
	if (parts.length != 2 || !parts[0].equalsIgnoreCase("Basic"))
	    return false;

	// split and decode username and password
	parts = new String(Base64.decode(parts[1])).split(":");
	if (parts.length != 2 || !authDb.userExists(parts[0]))
	    return false;

	if (!authDb.getUser(parts[0]).getPassword().equals(parts[1])) // check username and password
	    return false;

	return true;
    }

    public RequestHandler getUnAuthorizedRequestHandler(Request request) {
	return this.handler;
    }

    public HttpBasicAuthHandler setAuthDb(AuthDb authDb) {
	this.authDb = authDb;
	return this;
    }

    public AuthDb getAuthDb() {
	return this.authDb;
    }

    @Override
    public void authorize(Request request, Response response) { }

    @Override
    public void deAuthorize(Request request, Response response) {
	
    }
}
