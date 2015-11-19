package com.dc.qtm.queue;

/**
 * 
 * 被中断的任务的信息
 * 
 * @author Daemon
 *
 * @param <Param> 请求参数
 */
public class TaskInterruptInfo<Param> {
	
	/**
	 * 执行结果
	 */
	public final boolean success;
	
	/**
	 * 线程名字
	 */
	public final String threadName;

	/**
	 * 执行开始时间
	 */
	public final long beginTime;
	
	
	/**
	 * 中断时间
	 */
	public final long interrupTime;
	
	/**
	 * 请求参数
	 */
	public final Param param;
	
	
	public TaskInterruptInfo(boolean success, String threadName,
			long beginTime, long interrupTime, Param param) {

		this.success = success;
		this.threadName = threadName;
		this.beginTime = beginTime;
		this.interrupTime = interrupTime;
		this.param = param;
	}

	/**
	 * @return 执行结果 
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @return 线程名字
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * @return 执行开始时间
	 */
	public long getBeginTime() {
		return beginTime;
	}

	/**
	 * @return 中断时间
	 */
	public long getInterrupTime() {
		return interrupTime;
	}

	/**
	 * @return 请求参数
	 */
	public Param getParam() {
		return param;
	}

	@Override
	public String toString() {
		return "TaskInterruptInfo [success=" + success + ", threadName=" + threadName
				+ ", beginTime=" + beginTime + ", interrupTime=" + interrupTime
				+ ", param=" + param + "]";
	}
	
	
	
}
