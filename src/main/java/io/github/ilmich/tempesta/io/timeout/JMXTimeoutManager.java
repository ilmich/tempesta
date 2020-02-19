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
package io.github.ilmich.tempesta.io.timeout;

import java.nio.channels.SelectableChannel;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import io.github.ilmich.tempesta.util.MXBeanUtil;
import io.github.ilmich.tempesta.web.AsyncCallback;

public class JMXTimeoutManager implements TimeoutManager, TimeoutManagerMXBean {

	private final Logger logger = Logger.getLogger(JMXTimeoutManager.class.getName());

	private final TreeSet<Timeout> timeouts = new TreeSet<Timeout>(new TimeoutComparator());
	private final TreeSet<DecoratedTimeout> keepAliveTimeouts = new TreeSet<JMXTimeoutManager.DecoratedTimeout>();
	private final Map<SelectableChannel, DecoratedTimeout> index = new ConcurrentHashMap<SelectableChannel, JMXTimeoutManager.DecoratedTimeout>();

	public JMXTimeoutManager() { // instance initialization block
		// MXBeanUtil.registerMXBean(this, "TimeoutManager",
		// this.getClass().getSimpleName());
	}

	@Override
	public void addKeepAliveTimeout(SelectableChannel channel, Timeout timeout) {
		DecoratedTimeout decorated = index.get(channel);
		if (decorated == null) {
			decorated = new DecoratedTimeout(channel, timeout);
			index.put(channel, decorated);
		}
		keepAliveTimeouts.remove(decorated);
		decorated.setTimeout(timeout);
		keepAliveTimeouts.add(decorated);
	}

	public void removeKeepAliveTimeout(SelectableChannel channel) {
		DecoratedTimeout sc = index.get(channel);
		if (sc == null)
			return;

		keepAliveTimeouts.remove(index.get(channel));
		index.remove(channel);
	}

	@Override
	public void addTimeout(Timeout timeout) {
		timeouts.add(timeout);
	}

	@Override
	public boolean hasKeepAliveTimeout(SelectableChannel channel) {
		return index.containsKey(channel);
	}

	@Override
	public long execute() {
		return Math.min(executeKeepAliveTimeouts(), executeTimeouts());
	}

	private long executeKeepAliveTimeouts() {
		// makes a defensive copy to avoid (1) CME (new timeouts are added this
		// iteration) and (2) IO starvation.
		long now = System.currentTimeMillis();
		SortedSet<DecoratedTimeout> defensive = new TreeSet<JMXTimeoutManager.DecoratedTimeout>(keepAliveTimeouts)
				.headSet(new DecoratedTimeout(null, new Timeout(now, AsyncCallback.nopCb)));

		keepAliveTimeouts.removeAll(defensive);
		for (DecoratedTimeout decoratedTimeout : defensive) {
			decoratedTimeout.timeout.getCallback().onCallback();
			index.remove(decoratedTimeout.channel);
			logger.fine("Keepalive Timeout triggered: ");
		}

		return keepAliveTimeouts.isEmpty() ? Long.MAX_VALUE
				: Math.max(1, keepAliveTimeouts.iterator().next().timeout.getTimeout() - now);
	}

	private long executeTimeouts() {
		// makes a defensive copy to avoid (1) CME (new timeouts are added this
		// iteration) and (2) IO starvation.
		TreeSet<Timeout> defensive = new TreeSet<Timeout>(timeouts);
		Iterator<Timeout> iter = defensive.iterator();
		final long now = System.currentTimeMillis();
		while (iter.hasNext()) {
			Timeout candidate = iter.next();
			if (candidate.getTimeout() > now) {
				break;
			}
			candidate.getCallback().onCallback();
			iter.remove();
			timeouts.remove(candidate);
			logger.fine("Timeout triggered: ");
		}
		return timeouts.isEmpty() ? Long.MAX_VALUE : Math.max(1, timeouts.iterator().next().getTimeout() - now);
	}

	// implements TimoutMXBean
	@Override
	public int getNumberOfKeepAliveTimeouts() {
		return index.size();
	}

	@Override
	public int getNumberOfTimeouts() {
		return keepAliveTimeouts.size() + timeouts.size();
	}

	private class DecoratedTimeout implements Comparable<DecoratedTimeout> {

		public final SelectableChannel channel;
		public Timeout timeout;

		public DecoratedTimeout(SelectableChannel channel, Timeout timeout) {
			this.channel = channel;
			this.timeout = timeout;
		}

		@Override
		public int compareTo(DecoratedTimeout that) {
			long diff = timeout.getTimeout() - that.timeout.getTimeout();
			if (diff < 0) {
				return -1;
			} else if (diff > 0) {
				return 1;
			}
			if (channel != null && that.channel != null) {
				return channel.hashCode() - that.channel.hashCode();
			} else if (channel == null && that.channel != null) {
				return -1;
			} else if (channel != null && that.channel == null) {
				return -1;
			} else {
				return 0;
			}
		}

		public void setTimeout(Timeout timeout) {
			this.timeout = timeout;
		}

	}

	private class TimeoutComparator implements Comparator<Timeout> {

		@Override
		public int compare(Timeout lhs, Timeout rhs) {
			if (lhs == rhs) {
				return 0;
			}
			long diff = lhs.getTimeout() - rhs.getTimeout();
			if (diff <= 0) {
				return -1;
			}
			return 1; // / else if (diff > 0) {
		}
	}

}
