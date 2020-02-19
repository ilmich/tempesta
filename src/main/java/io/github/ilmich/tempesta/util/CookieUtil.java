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

/**
 * Utility type providing cookie-related functionality.
 */
public class CookieUtil {

	/**
	 * Utility method to calculate the expiration date of a cookie, starting with a
	 * time of validity in seconds.
	 * 
	 * @param seconds time of validity
	 * @return expiry date
	 */
	public static String maxAgeToExpires(Long seconds) {
		return DateUtil.parseToRFC1123(System.currentTimeMillis() + seconds * 1000);
	}

}
