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
package io.github.ilmich.tempesta.web.handler;

import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;

import io.github.ilmich.tempesta.util.DateUtil;
import io.github.ilmich.tempesta.web.http.HttpException;
import io.github.ilmich.tempesta.web.http.HttpRequest;
import io.github.ilmich.tempesta.web.http.HttpRequestHandler;
import io.github.ilmich.tempesta.web.http.HttpResponse;
import io.github.ilmich.tempesta.web.http.Request;
import io.github.ilmich.tempesta.web.http.Response;
import io.github.ilmich.tempesta.web.http.protocol.HttpStatus;


/**
 * A RequestHandler that serves static content (files) from a predefined
 * directory.
 * 
 * "Cache-Control: public" indicates that the response MAY be cached by any
 * cache, even if it would normally be non-cacheable or cacheable only within a
 * non- shared cache.
 */

public class StaticContentHandler extends HttpRequestHandler {

    private final static StaticContentHandler instance = new StaticContentHandler();

    private MimetypesFileTypeMap mimeTypeMap;    

    public static StaticContentHandler getInstance() {
        return instance;
    }    
    
    private StaticContentHandler() {
    	try {    		
			mimeTypeMap = new MimetypesFileTypeMap("META-INF/mime.types");
		} catch (IOException e) {
			mimeTypeMap = new MimetypesFileTypeMap();
		}
    }

    /** {inheritDoc} */
    @Override
    public void get(HttpRequest request, HttpResponse response) {
        perform(request, response, true);
    }

    /** {inheritDoc} */
    @Override
    public void head(final HttpRequest request, final HttpResponse response) {
        perform(request, response, false);
    }

    /**
     * @param request the <code>HttpRequest</code>
     * @param response the <code>HttpResponse</code>
     * @param hasBody <code>true</code> to write the message body;
     *            <code>false</code> otherwise.
     */
    private void perform(final Request request, final Response response, boolean hasBody) {

        final String path = request.getRequestedPath();
        final File file = new File(path.substring(1)); // remove the leading '/'
               
        if (!file.exists()) {
            throw new HttpException(HttpStatus.CLIENT_ERROR_NOT_FOUND,"File not found");
        } else if (!file.isFile()) {
            throw new HttpException(HttpStatus.CLIENT_ERROR_FORBIDDEN, path + "is not a file");
        }

        final long lastModified = file.lastModified();
        response.setHeader("Last-Modified", DateUtil.parseToRFC1123(lastModified));
        response.setHeader("Cache-Control", "public");
        String mimeType = mimeTypeMap.getContentType(file);
        if ("text/plain".equals(mimeType)) {
            mimeType += "; charset=utf-8";
        }
        response.setHeader("Content-Type", mimeType);
        final String ifModifiedSince = request.getHeader("If-Modified-Since");
        if (ifModifiedSince != null) {
            final long ims = DateUtil.parseToMilliseconds(ifModifiedSince);
            if (lastModified <= ims) {
                response.setStatus(HttpStatus.REDIRECTION_NOT_MODIFIED);
                return;
            }
        }

        if (hasBody) {
            response.write(file);
        }
    }
}
