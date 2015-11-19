package com.dc.qtm.queue;

import java.util.List;

/**
 * 
 * 队列 的运行信息
 * 
 * @author Daemon
 *
 * @param <QueueInfo> 队列的基本信息
 * @param <Param> 请求参数
 */
public class QueueRuningInfo<QueueInfo, Param> {
	
	/**
	 * 队列状态信息
	 */
	public final QueueStatus queueStatus;

	/**
	 * 队列的基本信息
	 */
	public final QueueInfo queueInfo;
	
	public final String threadTrackInfo;
	
	/**
	 * 任务信息列表
	 */
	public final List<TaskInfo<Param>> taskInfoList;
	
	public QueueRuningInfo(QueueStatus queueStatus, QueueInfo queueInfo, 
			String threadTrackInfo, List<TaskInfo<Param>> taskInfoList) {
		
		this.queueStatus = queueStatus;
		this.queueInfo = queueInfo;
		this.threadTrackInfo = threadTrackInfo;
		this.taskInfoList = taskInfoList;
	}

	/**
	 * @return 队列状态信息
	 */
	public QueueStatus getQueueStatus() {
		return queueStatus;
	}

	/**
	 * @return 队列的基本信息
	 */
	public QueueInfo getQueueInfo() {
		return queueInfo;
	}

	public String getThreadTrackInfo() {
		return threadTrackInfo;
	}

	/**
	 * @return 任务信息列表
	 */
	public List<TaskInfo<Param>> getTaskInfoList() {
		return taskInfoList;
	}

	@Override
	public String toString() {
		return "QueueRuningInfo [\nqueueStatus=" + queueStatus + ", \nqueueInfo=" + queueInfo 
				+ ", \nthreadTrackInfo=\n" + threadTrackInfo + ", \ntaskInfoList=\n" + taskInfoList + "\n]";
	}
	
	
}
