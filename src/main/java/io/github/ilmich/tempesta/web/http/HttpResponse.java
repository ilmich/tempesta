/*
 *  or more contributor license agreements.  See the NOTICE file
 *  Licensed to the Apache Software Foundation (ASF) under one
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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import io.github.ilmich.tempesta.io.buffer.DynamicByteBuffer;
import io.github.ilmich.tempesta.util.Closeables;
import io.github.ilmich.tempesta.util.CookieUtil;
import io.github.ilmich.tempesta.util.DateUtil;
import io.github.ilmich.tempesta.util.HttpUtil;
import io.github.ilmich.tempesta.util.Strings;
import io.github.ilmich.tempesta.web.http.protocol.HttpStatus;

public class HttpResponse implements Response {

    private final static Logger logger = Logger.getLogger(HttpResponse.class.getName());

    private HttpStatus status = HttpStatus.SUCCESS_OK;

    private final Map<String, String> headers = new HashMap<String, String>();
    private final Map<String, String> cookies = new HashMap<String, String>();
    private boolean headersCreated = false;
    private DynamicByteBuffer responseData = DynamicByteBuffer.allocate(HttpServerDescriptor.WRITE_BUFFER_SIZE);
    private FileChannel file;
    private Charset mainCharset = Charset.forName("ASCII");

    private boolean createETag;

    @Override
    public DynamicByteBuffer getResponseData() {
		return responseData;
	}
    
    public HttpResponse(boolean keepAlive) {
    	//headers.put("Server", "Tempesta/0.5.0-SNAPSHOT");
        headers.put("Date", DateUtil.getCurrentAsString());
        setKeepAlive(keepAlive);
    }    
    
    public void setKeepAlive(boolean keepAlive) {
    	headers.put("Connection", keepAlive ? "Keep-Alive" : "close");
    }
    
    public boolean isKeepAlive() {
	if (headers.containsKey("Connection"))
    		return (headers.get("Connection").equals("Keep-Alive") ? true : false);
	return false;
    }

    public Response setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }
    
    public void setCreateETag(boolean create) {
        createETag = create;
    }

    public Response setHeader(String header, String value) {
        headers.put(header, value);
        return this;
    }

    public void setCookie(String name, String value) {
        setCookie(name, value, -1, null, null, false, false);
    }

    public void setCookie(String name, String value, long expiration) {
        setCookie(name, value, expiration, null, null, false, false);
    }

    public void setCookie(String name, String value, String domain) {
        setCookie(name, value, -1, domain, null, false, false);
    }

    public void setCookie(String name, String value, String domain, String path) {
        setCookie(name, value, -1, domain, path, false, false);
    }

    public void setCookie(String name, String value, long expiration, String domain) {
        setCookie(name, value, expiration, domain, null, false, false);
    }

    public void setCookie(String name, String value, long expiration, String domain, String path) {
        setCookie(name, value, expiration, domain, path, false, false);
    }

    public void setCookie(String name, String value, long expiration, String domain, String path, boolean secure,
            boolean httpOnly) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Cookie name is empty");
        }
        if (name.trim().startsWith("$")) {
            throw new IllegalArgumentException("Cookie name is not valid");
        }
        StringBuffer sb = new StringBuffer(name.trim() + "=" + Strings.nullToEmpty(value).trim() + "; ");
        
        /*if (CharMatcher.JAVA_ISO_CONTROL.countIn(sb) > 0) {
            throw new IllegalArgumentException("Invalid cookie " + name + ": " + value);
        }*/
        
        if (expiration >= 0) {
            if (expiration == 0) {
                sb.append("Expires=" + DateUtil.getDateAsString(new Date(0)) + "; ");
            } else {
                sb.append("Expires=" + CookieUtil.maxAgeToExpires(expiration) + "; ");
            }
        }
        if (!Strings.isNullOrEmpty(domain)) {
            sb.append("Domain=" + domain.trim() + "; ");
        }
        if (!Strings.isNullOrEmpty(path)) {
            sb.append("Path=" + path.trim() + "; ");
        }
        if (secure) {
            sb.append("Secure; ");
        }
        if (httpOnly) {
            sb.append("HttpOnly; ");
        }
        cookies.put(name, sb.toString());
    }

    public void clearCookie(String name) {
        if (Strings.emptyToNull(name) != null) {
            setCookie(name, null, 0);
        }
    }

    public Response write(String data) {
        return write(data.getBytes(mainCharset));
    }

    @Override
    public Response write(byte[] data) {
        responseData.put(data);
        return this;
    }
    
    public Response setContentType(String contentType) {
    	return setHeader("Content-Type", contentType);    	
    }
    
    public void prepare() {
    	setEtagAndContentLength();
    	if (!headersCreated) {
            String initial = createInitalLineAndHeaders();
            responseData.prepend(initial);
            headersCreated = true;
        }
    	responseData.flip();
    }
    
    private void setEtagAndContentLength() {
    	if (responseData.position() > 0) {
    		if (createETag) {
    			setHeader("Etag", HttpUtil.getEtag(responseData.array()));
    		}        
    		setHeader("Content-Length", String.valueOf(responseData.position()));
    	}
    }

    private String createInitalLineAndHeaders() {
        StringBuilder sb = new StringBuilder(status.line());
        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey());
            sb.append(": ");
            sb.append(header.getValue());
            sb.append("\r\n");
        }
        for (String cookie : cookies.values()) {
            sb.append("Set-Cookie: " + cookie + "\r\n");
        }

        sb.append("\r\n");
        return sb.toString();
    }

    /**
     * Experimental support.
     */
    @Override
    public long write(File file) {
        // setHeader("Etag", HttpUtil.getEtag(file));
        setHeader("Content-Length", String.valueOf(file.length()));    	
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            this.file = in.getChannel();        
        } catch (IOException e) {
            logger.severe("Error writing (static file "+file.getAbsolutePath()+") to response: "+ e.getMessage());
            // If an exception occurs here we should ensure that file is closed
            Closeables.closeQuietly(in);
        }

        return 0;
    }

    	@Override
	public FileChannel getFile() {
	    return file;
	}
    
	/*
	 * Reset response
	 * 
	 */
    public void reset() {
    	this.responseData.clear();
    	this.headers.clear();
    	this.headersCreated = false;
    	this.cookies.clear();    	
    	this.file = null;
    }

	@Override
	public long flush() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long finish() {
		// TODO Auto-generated method stub
		return 0;
	}
}
