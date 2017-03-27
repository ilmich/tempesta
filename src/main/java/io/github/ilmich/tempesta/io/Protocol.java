package io.github.ilmich.tempesta.io;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import io.github.ilmich.tempesta.web.http.Request;
import io.github.ilmich.tempesta.web.http.Response;

public abstract class Protocol {

    public abstract Request onRead(final ByteBuffer buffer, SocketChannel client);

    public abstract Response processRequest(final Request request);
    
}
