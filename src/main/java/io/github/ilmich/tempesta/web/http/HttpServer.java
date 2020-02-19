package io.github.ilmich.tempesta.web.http;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.ilmich.tempesta.io.connectors.ServerConnector;

public class HttpServer {

	private List<ServerConnector> connectors = new ArrayList<ServerConnector>();

	public void startAndWait() {
		Iterator<ServerConnector> iter = connectors.iterator();
		ServerConnector conn = iter.next();
		while (iter.hasNext()) {
			iter.next().start();
		}
		;
		conn.startAndWait();
	}

	public void start() {
		Iterator<ServerConnector> iter = connectors.iterator();
		while (iter.hasNext()) {
			iter.next().start();
		}
		;
	}

	public void stop() {
		Iterator<ServerConnector> iter = connectors.iterator();
		while (iter.hasNext()) {
			iter.next().shutDown();
		}
		;
	}

	public void setConnectors(List<ServerConnector> connectors) {
		this.connectors = connectors;
	}

	public HttpServer addConnector(ServerConnector conn) {
		this.connectors.add(conn);
		return this;
	}
}
