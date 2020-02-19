package io.github.ilmich.tempesta.web.http;

import io.github.ilmich.tempesta.io.PlainIOHandler;
import io.github.ilmich.tempesta.io.connectors.ServerConnector;
import io.github.ilmich.tempesta.web.handler.HandlerFactory;

public class HttpServerBuilder {

	private HttpServer instance = new HttpServer();
	private HttpProtocol protocol = new HttpProtocol();

	public HttpServerBuilder bindPlain(int port) {
		PlainIOHandler hndl = new PlainIOHandler(protocol);
		ServerConnector conn = new ServerConnector();
		conn.bind(port);
		conn.setIoHandler(hndl);
		hndl.setConnector(conn);

		instance.addConnector(conn);

		return this;
	}

	public HttpServerBuilder addRoute(String route, HttpRequestHandler handler) {
		if (this.protocol.getFactory() == null) {
			this.protocol.setFactory(new HttpHandlerFactory());
		}
		this.protocol.getFactory().addRoute(route, handler);
		return this;
	}

	public HttpServerBuilder setHandlerFactory(HandlerFactory factory) {
		this.protocol.setFactory(factory);
		return this;
	}

	public HttpServer build() {
		return this.instance;
	}

}
