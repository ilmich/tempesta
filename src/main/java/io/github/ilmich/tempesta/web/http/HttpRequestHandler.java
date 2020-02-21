package io.github.ilmich.tempesta.web.http;

import io.github.ilmich.tempesta.web.handler.RequestHandler;
import io.github.ilmich.tempesta.web.http.protocol.HttpStatus;

public abstract class HttpRequestHandler extends RequestHandler {

	public HttpRequestHandler() {
		
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

	public void patch(HttpRequest request, HttpResponse response) {
		response.setStatus(HttpStatus.SERVER_ERROR_NOT_IMPLEMENTED);
		response.write(" ");
	}
	
}
