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
package io.github.ilmich.tempesta.io.callback;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.github.ilmich.tempesta.util.MXBeanUtil;
import io.github.ilmich.tempesta.web.AsyncCallback;

public class JMXCallbackManager implements CallbackManager, CallbackManagerMXBean {

	private final AbstractCollection<AsyncCallback> callbacks = new ConcurrentLinkedQueue<AsyncCallback>();

	public JMXCallbackManager() { // instance initialization block
		//MXBeanUtil.registerMXBean(this, "CallbackManager", this.getClass().getSimpleName());
	}

	@Override
	public int getNumberOfCallbacks() {
		return callbacks.size();
	}

	@Override
	public void addCallback(AsyncCallback callback) {
		callbacks.add(callback);
	}

	@Override
	public boolean execute() {
		// makes a defensive copy to avoid (1) CME (new callbacks are added this
		// iteration) and (2) IO starvation.
		List<AsyncCallback> defensive = new ArrayList<AsyncCallback>(callbacks);
		callbacks.clear();
		for (AsyncCallback callback : defensive) {
			callback.onCallback();
		}
		return !callbacks.isEmpty();
	}
}
