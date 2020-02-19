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
package io.github.ilmich.tempesta.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import io.github.ilmich.tempesta.web.http.HttpRequest;

public class UrlUtil {

	/**
	 * Example:
	 * 
	 * <pre>
	 * {@code 
	 * url: http://tt.se/                 Location: /start              =>  http://tt.se/start
	 * url: http://localhost/moved_perm   Location: /                   =>  http://localhost/
	 * url: http://github.com/            Location: http://github.com/  =>  https://github.com/
	 * }
	 * 
	 * (If the new url throws a MalformedURLException the url String representation
	 * will be returned.)
	 */
	public static String urlJoin(URL url, String locationHeader) {
		try {
			if (locationHeader.startsWith("http")) {
				return new URL(locationHeader).toString();
			}
			return new URL(url.getProtocol() + "://" + url.getAuthority() + locationHeader).toString();
		} catch (MalformedURLException e) {
			return url.toString();
		}
	}

	public static Map<String, String> parseUrlParams(String req) {
		// codice preso da deft per parsare parametri che sono all'interno di
		// richieste POST e PUT
		// provare a sviluppare questa implementazione all'interno di deft.. se
		// quello si sveglia:)
		Map<String, String> builder = new HashMap<String, String>();
		String[] paramArray;
		try {
			paramArray = HttpRequest.PARAM_STRING_PATTERN.split(URLDecoder.decode(req, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		for (String keyValue : paramArray) {
			String[] keyValueArray = HttpRequest.KEY_VALUE_PATTERN.split(keyValue);
			// We need to check if the parameter has a value associated with it.
			if (keyValueArray.length > 1) {
				builder.put(keyValueArray[0], keyValueArray[1]); // name, value
			}
		}

		return builder;
	}

}
