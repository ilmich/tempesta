package io.github.ilmich.tempesta.io.threads;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TempestaThreadPoolExecutor extends ThreadPoolExecutor implements ThreadPoolMXBean {

    public TempestaThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
	    BlockingQueue<Runnable> workQueue) {
	super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	// MXBeanUtil.registerMXBean(this,
	// "TempestaThreadPool",this.getClass().getSimpleName());
    }

    public TempestaThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
	    BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
	super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	// MXBeanUtil.registerMXBean(this,
	// "TempestaThreadPool",this.getClass().getSimpleName());
    }

    public TempestaThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
	    BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
	super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	// MXBeanUtil.registerMXBean(this,
	// "TempestaThreadPool",this.getClass().getSimpleName());
    }

    public TempestaThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
	    BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
	super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	// MXBeanUtil.registerMXBean(this,
	// "TempestaThreadPool",this.getClass().getSimpleName());
    }

    @Override
    public int getNumberOfActiveThreads() {
	return getActiveCount();
    }

    @Override
    public int getNumberOfIdleThreads() {
	return getPoolSize() - getActiveCount();
    }

    public int getLargestNumberOfThreads() {
	return getLargestNumberOfThreads();
    }

}
