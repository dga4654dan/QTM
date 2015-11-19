package com.dc.qtm.queue;

/**
 * 
 * 队列状态
 * 
 * @author Daemon
 *
 */
public enum QueueState {

	/**
	 * 正常
	 */
	NORMAL,
	
	/**
	 * 挂起
	 */
	HOLD,
	
	/**
	 * 队列已经被抛弃（回收）
	 */
	ABANDON
	
}
