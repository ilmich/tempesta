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

import io.github.ilmich.tempesta.web.http.HttpRequest;
import io.github.ilmich.tempesta.web.http.HttpRequestHandler;
import io.github.ilmich.tempesta.web.http.HttpResponse;
import io.github.ilmich.tempesta.web.http.protocol.HttpStatus;

public class ForbiddenRequestHandler extends HttpRequestHandler {

	private final static ForbiddenRequestHandler instance = new ForbiddenRequestHandler();

	private ForbiddenRequestHandler() {
	}

	public static final ForbiddenRequestHandler getInstance() {
		return instance;
	}

	@Override
	public void get(HttpRequest request, HttpResponse response) {
		perform(request, response);
	}

	@Override
	public void post(HttpRequest request, HttpResponse response) {
		perform(request, response);
	}

	@Override
	public void put(HttpRequest request, HttpResponse response) {
		perform(request, response);
	}

	@Override
	public void delete(HttpRequest request, HttpResponse response) {
		perform(request, response);
	}

	@Override
	public void head(HttpRequest request, HttpResponse response) {
		perform(request, response);
	}

	@Override
	public void option(HttpRequest request, HttpResponse response) {
		perform(request, response);
	}

	private void perform(HttpRequest request, HttpResponse response) {
		response.setStatus(HttpStatus.CLIENT_ERROR_FORBIDDEN);
		response.setHeader("Connection", "close");
		response.write("Authentication failed");
	}
}
