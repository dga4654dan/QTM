package com.dc.qtm.queue;

/**
 * 
 * 执行结果
 * 
 * @author Daemon
 *
 */
public enum ExecutorResult {
	
	/**
	 * 成功
	 */
	SUCCESS,
	
	/**
	 * 队列被抛弃，可能需要从新获取这个实体的队列
	 */
	QUEUE_ABANDON,
	
	/**
	 *异常
	 */
	EXCEPTION
	
}
