package com.dc.qtm.queue;

import java.util.List;
import java.util.concurrent.locks.Lock;

import com.dc.qtm.IAbandonQueueInvoke;
import com.dc.qtm.Task;
import com.dc.qtm.handle.IRequestHandler;

/**
 * 
 * 任务队列
 * 
 * @author Daemon
 *
 * @param <TagetInfo> 队列对应的实体信息
 * @param <QueueInfo> 队列信息
 * @param <Param> 请求参数
 */
public interface ITaskQueue<TagetInfo, QueueInfo, Param> {
	
	
	/**
	 * 
	 * 获得 任务队列 的操作锁( 除非熟悉实现,不然不建议这么操作该锁  )
	 * 
	 * eg:
	 * 业务需求: 同时添加两个任务,不能分离
	 * 执行细节: 1.获得锁后  2.添加任务A  3.添加任务B   4.释放锁
	 * 
	 * eg: 
	 * 业务需求: 在设置用户登录标志时,不允许logout入列,如果成功则login的业务必须入列
	 *     (为避免情况: 设置标志位后,其他业务认为其已登录,所以可以logout它,
	 *     而logout可能会重置了这个标志位,然后再执行login,则会出现 login了,但是标志位被清除了)
	 * 执行细节: 1.获得锁后  2.设置一个重要的标志位  3.设置成功后,紧接着添加一个任务A进去队列   4.释放锁
	 * 
	 * @return 获得 任务队列 的操作锁
	 */
	Lock getQueueOperateLock();

	/**
	 * 
	 * 添加并执行任务
	 * 
	 * @param requestId 请求的id
	 * @param tagetInfo 实体信息
	 * @param handler 具体处理器
	 * @param param 请求参数
	 * @return 执行结果 （ 成功 / 队列被抛弃，可能需要从新获取 / 异常）
	 */
	ExecutorResult execTask( int requestId, TagetInfo tagetInfo, IRequestHandler<TagetInfo, Param> handler, Param param );
	
	/**
	 * 
	 * 挂起队列
	 * 
	 * @return 执行结果 （ 成功 / 队列被抛弃，可能需要从新获取 / 异常 ）
	 */
	ExecutorResult holdQueue();
	
	/**
	 * 
	 * 恢复队列
	 * 
	 * @return 执行结果 （ 成功 / 队列被抛弃，可能需要从新获取 / 异常 ）
	 */
	ExecutorResult resumeQueue();
	
	/**
	 * 
	 * 中断当前正在执行的任务（调用 thread.interrupt，只能中断一些等待之类的操作，但是如果是一个一直运行的程序是无法被中断的）
	 * 
	 * @param tagetId 中断的任务的id
	 * @return 被中断的任务的信息
	 */
	TaskInterruptInfo<Param> interruptTaskNow( int tagetId );
	
	/**
	 * 
	 * 要求回收队列
	 * 
	 * @param abandonQueueInvoke 队列为空的时候，的回调函数
	 * @return 执行结果 （ 成功 / 队列被抛弃，可能需要从新获取 / 异常 ）
	 */
	ExecutorResult requireAbandon( IAbandonQueueInvoke<QueueInfo> abandonQueueInvoke );
	
	/**
	 * 
	 * 将除了现在运行的任务 tagetIdNow 之外的任务都移除掉
	 * （用于紧急处理，由于某一个handler陷入死循环或者时间很长的循环的情况，将返回的任务放到一个新的队列中执行，
	 * 如果是被阻塞在io，应考虑用interruptTaskNow）
	 * 
	 * @param tagetIdNow 现在运行的任务Id（如果现在运行的任务非这个，则会返回null，且不会执行清除动作）
	 * @return 除了现在运行的任务 tagetIdNow 之外的任务（如果现在运行的任务非tagetIdNow，则会返回null，且不会执行清除动作）
	 */
	List< Task<TagetInfo, QueueInfo, Param> > clearWaitTaskList(int tagetIdNow);
	
	/**
	 * 
	 * 将除了现在运行的任务之外的任务都移除掉
	 * （用于紧急处理，由于某一个handler陷入死循环或者时间很长的循环的情况，将返回的任务放到一个新的队列中执行，
	 * 如果是被阻塞在io，应考虑用interruptTaskNow）
	 * 
	 * @return 除了现在运行的任务 tagetIdNow 之外的任务
	 */
	List<Task<TagetInfo, QueueInfo, Param>> clearWaitTaskList();
	
	/**
	 * 
	 * 获得队列的信息
	 * 
	 * @return 队列的信息
	 */
	QueueRuningInfo<QueueInfo, Param> getQueueRuningInfo( boolean needThreadTrackInfo );

	/**
	 * 
	 * 队列是否被要求挂起(holdQueue)
	 * 
	 * @return 队列是否被要求挂起
	 */
	boolean isRequireHold();

	/**
	 * 
	 * 队列是否被要求回收
	 * 
	 * @return 队列是否被要求回收(requireAbandon)
	 */
	boolean isRequireAbandon();

	/**
	 * 线程开始执行当前任务的时间，没有被执行时=0
	 * 
	 * @return 线程开始执行当前任务的时间，没有被执行时=0
	 */
	long getThreadStartTime();

	

	

	
}
