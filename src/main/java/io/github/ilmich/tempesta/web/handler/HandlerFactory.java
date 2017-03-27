package io.github.ilmich.tempesta.web.handler;


import io.github.ilmich.tempesta.web.handler.RequestHandler;
import io.github.ilmich.tempesta.web.http.Request;

public interface HandlerFactory {
    
    public RequestHandler getHandler(Request request);

}
