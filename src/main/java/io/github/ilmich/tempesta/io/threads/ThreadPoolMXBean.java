package io.github.ilmich.tempesta.io.threads;

public interface ThreadPoolMXBean {

	int getNumberOfActiveThreads();

	int getNumberOfIdleThreads();

	int getLargestNumberOfThreads();

}
