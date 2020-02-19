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
package io.github.ilmich.tempesta.web.http;

import java.io.File;
import java.nio.channels.FileChannel;

import io.github.ilmich.tempesta.io.buffer.DynamicByteBuffer;
import io.github.ilmich.tempesta.web.http.protocol.HttpStatus;


/**
 * An HTTP response build and sent to a client in response to a
 * {@link Request}
 */
public interface Response {
    
    /**
     * The given data data will be sent as the HTTP response upon next flush or
     * when the response is finished.
     * 
     * @return this for chaining purposes.
     */
    Response write(String data);

    /**
     * The given data data will be sent as the HTTP response upon next flush or
     * when the response is finished.http://mail.google.com/mail/?shva=1#inbox
     * 
     * @param data the data to write.
     * @return <code>this</code>, for chaining.
     */
    Response write(byte[] data);

    /**
     * Experimental support.
     */
    long write(File file);

    /**
     * Explicit flush.
     * 
     * @return the number of bytes that were actually written as the result of
     *         this flush.
     */
    long flush();

    /**
     * Should only be invoked by third party asynchronous request handlers (or
     * by the AWF framework for synchronous request handlers). If no previous
     * (explicit) flush is invoked, the "Content-Length" and (where configured)
     * "ETag" header will be calculated and inserted to the HTTP response.
     * 
     * @see #setCreateETag(boolean)
     */
    long finish();
    
    public void reset();
    
    public void prepare();
    
    public DynamicByteBuffer getResponseData();
    
    public FileChannel getFile();
    
    public boolean isKeepAlive();
    
    public Response setStatus(HttpStatus status);

    public Response setHeader(String header, String value);

    public void setCookie(String name, String value);

    public void setCookie(String name, String value, long expiration);

    public void setCookie(String name, String value, String domain);

    public void setCookie(String name, String value, String domain, String path);

    public void setCookie(String name, String value, long expiration, String domain);

    public void setCookie(String name, String value, long expiration, String domain, String path);

    public void clearCookie(String name);

}
