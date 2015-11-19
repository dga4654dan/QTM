package com.dc.qtm.queue;

/**
 * 
 * 任务信息
 * 
 * @author Daemon
 *
 * @param <Param> 请求参数
 */
public class TaskInfo<Param> {
	
	/**
	 * 该请求的id
	 */
	public final int requestId;

	/**
	 * 处理类 的 类名
	 */
	public final String handlerInfo;
	
	/**
	 * 请求参数
	 */
	public final Param param;
	
	public TaskInfo(int requestId, String handlerInfo, Param param) {

		this.requestId = requestId;
		this.handlerInfo = handlerInfo;
		this.param = param;
	}

	/**
	 * @return 该请求的id
	 */
	public int getRequestId() {
		return requestId;
	}

	/**
	 * @return 处理类 的 信息
	 */
	public String getHandlerInfo() {
		return handlerInfo;
	}

	/**
	 * @return 请求参数
	 */
	public Param getParam() {
		return param;
	}
	
	@Override
	public String toString() {
		return "TaskInfo [requestId=" + requestId + ", handlerInfo=" + handlerInfo + ", param=" + param
				+ "]";
	}
	
	
}
