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

import io.github.ilmich.tempesta.util.ExceptionUtils;
import io.github.ilmich.tempesta.util.Log;
import io.github.ilmich.tempesta.web.http.protocol.HttpStatus;
import io.github.ilmich.tempesta.web.http.protocol.HttpVerb;

/**
 * The <code>RequestDispatcher</code> is responsible for invoking the
 * appropriate <code>RequestHandler</code> method for the current
 * <code>HttpRequest</code>.
 */
public class HttpRequestDispatcher {

	private static final String TAG = "HttpRequestDispatcher";

	public void dispatch(HttpRequestHandler rh, HttpRequest request, HttpResponse response) {
		if (rh != null) {
			HttpVerb method = request.getMethod();
			try {
				switch (method) {
				case GET:
					rh.get(request, response);
					break;
				case POST:
					rh.post(request, response);
					break;
				case HEAD:
					rh.head(request, response);
					break;
				case PUT:
					rh.put(request, response);
					break;
				case PATCH:
					rh.patch(request, response);
					break;
				case DELETE:
					rh.delete(request, response);
					break;
				case OPTIONS:
					rh.option(request, response);
					break;
				case TRACE:
				case CONNECT:
				default:
					Log.warn(TAG, "Unimplemented Http metod received: " + method);
					response.reset();
					response.setStatus(HttpStatus.CLIENT_ERROR_METHOD_NOT_ALLOWED);
				}
			} catch (HttpException he) {
				response.reset();
				response.setStatus(he.getStatus());
				Log.error(TAG, ExceptionUtils.getStackTrace(he));
				Log.error(TAG, request.toString());
				response.write(ExceptionUtils.getStackTrace(he));
			} catch (Exception ex) {
				response.reset();
				response.setStatus(HttpStatus.SERVER_ERROR_INTERNAL_SERVER_ERROR);
				Log.error(TAG, ExceptionUtils.getStackTrace(ex));
				Log.error(TAG, request.toString());
				response.write(ExceptionUtils.getStackTrace(ex));
			}
		}
	}
}
