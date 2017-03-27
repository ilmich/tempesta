/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package io.github.ilmich.tempesta.io.connectors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Logger;

import com.google.common.io.Closeables;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

import io.github.ilmich.tempesta.io.IOHandler;
import io.github.ilmich.tempesta.io.callback.CallbackManager;
import io.github.ilmich.tempesta.io.callback.JMXCallbackManager;
import io.github.ilmich.tempesta.io.timeout.JMXTimeoutManager;
import io.github.ilmich.tempesta.io.timeout.Timeout;
import io.github.ilmich.tempesta.web.http.HttpServerDescriptor;

public class ServerConnector extends AbstractExecutionThreadService {

    //private int port;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private final CallbackManager cm = new JMXCallbackManager();
    private final JMXTimeoutManager tm = new JMXTimeoutManager();
    private final Logger logger = Logger.getLogger(ServerConnector.class.getName());
    private IOHandler ioHandler;
          
    public ServerConnector() {
	super();	
    }

    public ServerConnector(IOHandler ioHandler) {
	super();
	setIoHandler(ioHandler);
    }

    /**
     * Setup connector 
     * 
     */
    protected void startUp() throws Exception {
	if (this.serverChannel == null) {
	    this.bind(8080);
	}
	selector = Selector.open();	
	serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    protected void run() throws Exception {
	long selectorTimeout = 250; // 250 ms
	while (isRunning()) {	    
	    if (selector.select(selectorTimeout) == 0) {
		long ms = tm.execute(); // execute all timeouts
		// eventually reduce selector timeout in order to execute next
		// timeout
		selectorTimeout = Math.min(ms, /* selectorTimeout */250);
		if (cm.execute()) { // execute all callback registered
		    selectorTimeout = 1;
		}
		continue;		
	    }
	    
	    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();	    
	    while (keys.hasNext()) {
		SelectionKey key = keys.next();
		keys.remove();
		try {
		    if (key.isValid() && key.isAcceptable()) {
			ioHandler.handleAccept(key);
			continue;
		    }
		    if (key.isValid() && key.isConnectable()) {
			ioHandler.handleConnect(key);
			continue;
		    }
		    if (key.isValid() && key.isReadable()) {
			ioHandler.handleRead(key);
			continue;
		    }
		    if (key.isValid() && key.isWritable()) {
			ioHandler.handleWrite(key);
			continue;
		    }
		} catch (CancelledKeyException ex) {
		    logger.severe("CancelledKeyException received: " + ex.getMessage());
		} catch (IOException ex) {
		    Closeables.closeQuietly(key.channel());
		}
	    }
	    long ms = tm.execute();
	    selectorTimeout = Math.min(ms, /* selectorTimeout */250);
	    if (cm.execute()) {
		selectorTimeout = 1;
	    }
	}
    }
    
        
    public void bind(int port) {
	this.bind(new InetSocketAddress(port));
    }
    
    public void bind(InetSocketAddress endpoint) {	
	try {
	    serverChannel = ServerSocketChannel.open();
	    boolean reuse = serverChannel.socket().getReuseAddress();
	    if (!reuse) {
		logger.config("Enabling SO_REUSEADDR (was disabled)");
		serverChannel.socket().setReuseAddress(true);
	    }
	    serverChannel.configureBlocking(false);
	} catch (IOException e) {
	    logger.severe("Error creating ServerSocketChannel: " + e.getMessage());
	}
	
	try {
	    serverChannel.socket().bind(endpoint);
	} catch (IOException e) {
	    logger.severe("Could not bind socket: " + e.getMessage());
	}
	logger.fine("Listen to " + endpoint.toString());
    }

    public void registerChannel(SocketChannel channel, int interestOps) throws IOException {
	channel.register(selector, interestOps);
	selector.wakeup();
    }

    public void registerChannel(SocketChannel channel, int interestOps, Object attachment) throws IOException {
	channel.register(selector, interestOps, attachment);
	selector.wakeup();
    }

    public void closeChannel(SocketChannel channel) {
	Closeables.closeQuietly(channel);
    }

    public void addKeepAliveTimeout(SelectableChannel channel, Timeout keepAliveTimeout) {
	tm.addKeepAliveTimeout(channel, keepAliveTimeout);
    }

    public boolean hasKeepAliveTimeout(SelectableChannel channel) {
	return tm.hasKeepAliveTimeout(channel);
    }

    public void removeKeepAliveTimeout(SelectableChannel channel) {
	tm.removeKeepAliveTimeout(channel);
    }
  
    public void prolongKeepAliveTimeout(SelectableChannel channel) {
	addKeepAliveTimeout(channel, Timeout.newKeepAliveTimeout(channel, HttpServerDescriptor.KEEP_ALIVE_TIMEOUT));
    }
    
    public void closeOrRegisterForRead(SelectionKey key, boolean keepAlive) throws IOException {
	if (key.isValid() && keepAlive) {
	    try {
		registerChannel((SocketChannel) key.channel(), SelectionKey.OP_READ);
		key.selector().wakeup();
		prolongKeepAliveTimeout(key.channel());
	    } catch (IOException ex) {
		logger.severe("IOException while registrating key for read: " + ex.getMessage());
		throw ex;
	    }
	} else {
	    closeChannel((SocketChannel) key.channel());	    
	}
    }

    public IOHandler getIoHandler() {
        return ioHandler;
    }

    public void setIoHandler(IOHandler ioHandler) {
        this.ioHandler = ioHandler;
        this.ioHandler.attachServerConnector(this);
    }
}