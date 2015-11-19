package com.dc.qtm.queue;

/**
 * 队列状态信息
 * 
 * @author Daemon
 *
 */
public class QueueStatus {

	/**
	 * 运行状态
	 */
	public final RunState runState;
	
	/**
	 * 队列状态
	 */
	public final QueueState queueState;
	
	/**
	 * 要求挂起队列
	 */
	public final boolean requireHold;
	
	/**
	 * 要求回收队列
	 */
	public final boolean requireAbandon;
	
	public QueueStatus( RunState runState, QueueState queueState,
			boolean requireHold, boolean requireAbandon ) {

		this.runState = runState;
		this.queueState = queueState;
		this.requireHold = requireHold;
		this.requireAbandon = requireAbandon;
	}

	/**
	 * @return 运行状态
	 */
	public RunState getRunState() {
		return runState;
	}

	/**
	 * @return 队列状态
	 */
	public QueueState getQueueState() {
		return queueState;
	}

	/**
	 * @return 要求挂起队列
	 */
	public boolean isRequireHold() {
		return requireHold;
	}

	/**
	 * @return 要求回收队列
	 */
	public boolean isRequireAbandon() {
		return requireAbandon;
	}

	@Override
	public String toString() {
		return "QueueStatus [runState=" + runState + ", queueState=" + queueState 
				+ ", requireHold=" + requireHold + ", requireAbandon=" + requireAbandon + "]";
	}
	
	
}
