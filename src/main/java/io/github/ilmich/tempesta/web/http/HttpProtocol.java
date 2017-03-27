package io.github.ilmich.tempesta.web.http;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.logging.Logger;


import com.google.common.collect.Maps;

import io.github.ilmich.tempesta.io.Protocol;
import io.github.ilmich.tempesta.web.handler.HandlerFactory;

public class HttpProtocol extends Protocol {
    
    /**
     * a queue of half-baked (pending/unfinished) HTTP post request
     */
    private final Map<SelectableChannel, HttpRequest> partials = Maps.newHashMap();
    
    /**
     * Logger
     */
    private final Logger logger = Logger.getLogger(HttpProtocol.class.getName());
    
    /**
     * Http request parser
     */
    private HttpRequestParser parser = new HttpRequestParser();
    
    private HandlerFactory factory = null;    
    
    public HttpProtocol() {
	super();	
    }

    public HttpProtocol(HandlerFactory factory) {
	super();
	this.factory = factory;
    }

    public Request onRead(final ByteBuffer buffer, SocketChannel client) {
	HttpRequest request = parser.parseRequestBuffer(buffer, partials.get(client));
	if (!request.isFinished()) {
	    partials.put(client, request);
	} else {
	    partials.remove(client);	    
	}
	if (request.expectContinue() || request.isFinished()) {
	    return request;
	}
	return null;
    }
    
    public Response processRequest(final Request request) {
	logger.fine(request.toString());
	HttpResponse response = new HttpResponse(request.isKeepAlive());
	// TODO: add pre http pipelina handlers
	HttpRequestHandler rh = (HttpRequestHandler) factory.getHandler(request);	
	HttpRequestDispatcher.dispatch(rh, (HttpRequest)request, response);
	// TODO: add post http pipelina handlers
	response.setHeader("Server", "Tempesta/0.5.0");
	response.prepare();
	return response;
    }

    public HandlerFactory getFactory() {
        return factory;
    }

    public void setFactory(HandlerFactory factory) {
        this.factory = factory;
    }
}
