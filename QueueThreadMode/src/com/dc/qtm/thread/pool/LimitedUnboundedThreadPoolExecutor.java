package com.dc.qtm.thread.pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * 线程池，提供两种添加任务到处理队列的方式：
 * 1. 受队列最大数量限制的添加
 * 2. 不受队列最大数量限制的添加（用于处理重要的消息）
 * 
 * @author Daemon
 *
 */
public class LimitedUnboundedThreadPoolExecutor extends ThreadPoolExecutor {

	private final int limitedQueueSize;
	private final BlockingQueue<Runnable> workQueue;
	
	public LimitedUnboundedThreadPoolExecutor(int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue, int limitedQueueSize) {
	
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		
		this.limitedQueueSize = limitedQueueSize;
		this.workQueue = workQueue;
	}

	/**
	 * 
	 * 受队列最大数量限制的添加
	 * 
	 * @param command 任务
	 * @return 添加成功货失败
	 */
	public boolean executeLimited(Runnable command) {
		
		if( workQueue.size() > limitedQueueSize ) {
			
			return false;
			
		} else {
			
			super.execute(command);
			
			return true;
		}
	}
	
	/**
	 * 
	 * 不受队列最大数量限制的添加（用于处理重要的消息）
	 * 
	 * @param command 任务
	 */
	public void executeUnbounded(Runnable command) {
		
		super.execute(command);
	}
	
}
