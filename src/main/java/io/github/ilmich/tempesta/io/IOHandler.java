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
package io.github.ilmich.tempesta.io;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import io.github.ilmich.tempesta.io.connectors.ServerConnector;

/**
 * {@code IOHandler}s are added to the IOLoop via {@link IOLoop#addHandler}
 * method. The callbacks defined in the {@code IOHandler} will be invoked by the
 * {@code IOLoop} when io is ready.
 * 
 */
public interface IOHandler {

    void handleAccept(SelectionKey key) throws IOException;

    void handleConnect(SelectionKey key) throws IOException;

    void handleRead(SelectionKey key) throws IOException;

    void handleWrite(SelectionKey key) throws IOException;
    
    void attachServerConnector(ServerConnector conn);

}
