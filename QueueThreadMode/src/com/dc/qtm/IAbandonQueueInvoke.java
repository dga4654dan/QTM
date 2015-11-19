package com.dc.qtm;

public interface IAbandonQueueInvoke<QueueInfo> {

	/**
	 * 
	 * 队列现在为空，是否把队列状态设置为抛弃
	 * 
	 * @param queueInfo 队列信息
	 * @return 是否把队列状态设置为抛弃
	 */
	boolean queueEmptyNowSureToAbandon( QueueInfo queueInfo );
}
