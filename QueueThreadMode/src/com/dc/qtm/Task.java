package com.dc.qtm;

import com.dc.qtm.handle.IRequestHandler;

public class Task<TagetInfo, QueueInfo, Param> {
		
	/**
	 * 该请求的id
	 */
	public final int requestId;
	
	/**
	 * 实体信息
	 */
	public final TagetInfo tagetInfo;

	/**
	 * 处理类
	 */
	public final IRequestHandler<TagetInfo, Param> handler;
	
	/**
	 * 请求参数
	 */
	public final Param param;
	
	public Task(int requestId, TagetInfo tagetInfo, 
			IRequestHandler<TagetInfo, Param> handler, Param param) {

		this.requestId = requestId;
		this.tagetInfo = tagetInfo;
		this.handler = handler;
		this.param = param;
	}

	/**
	 * @return 该请求的id
	 */
	public int getRequestId() {
		return requestId;
	}

	/**
	 * @return 实体信息
	 */
	public TagetInfo getTagetInfo() {
		return tagetInfo;
	}

	/**
	 * @return 处理类
	 */
	public IRequestHandler<TagetInfo, Param> getHandler() {
		return handler;
	}

	/**
	 * @return 请求参数
	 */
	public Param getParam() {
		return param;
	}
	
	@Override
	public String toString() {
		return "TaskInfo [requestId=" + requestId + ", handler=" + handler.toString() + ", param=" + param
				+ "]";
	}
}

