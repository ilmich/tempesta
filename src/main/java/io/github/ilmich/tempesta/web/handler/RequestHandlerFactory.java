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

import java.util.logging.Logger;

/**
 * Factory for the creation and retrieval of <code>RequestHandler</code> types.
 */
public class RequestHandlerFactory {

	/**
	 * The <code>Logger</code>.
	 */
	private final static Logger logger = Logger.getLogger(RequestHandlerFactory.class.getName());

	/**
	 * Clone the given instance of <code>RequestHandler</code>.
	 * 
	 * @param handler the <code>RequestHandler</code> to clone.
	 * @return a new instance, or <code>null</code> on any problem.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends RequestHandler> T cloneHandler(T handler) {

		if (handler != null) {
			try {
				return (T) handler.clone();
			} catch (CloneNotSupportedException e) {
				logger.severe("Could not clone RequestHandler: " + e.getMessage());
			}
		}
		return null;
	}
}
