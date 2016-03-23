package com.dc.qtm.queue.lock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dc.qtm.IAbandonQueueInvoke;
import com.dc.qtm.IExceptionListener;
import com.dc.qtm.Task;
import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ExecutorResult;
import com.dc.qtm.queue.ITaskQueue;
import com.dc.qtm.queue.QueueRuningInfo;
import com.dc.qtm.queue.QueueState;
import com.dc.qtm.queue.QueueStatus;
import com.dc.qtm.queue.RunState;
import com.dc.qtm.queue.TaskInfo;
import com.dc.qtm.queue.TaskInterruptInfo;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;

/**
 * 
 * 基于锁的 任务队列
 * 
 * @author Daemon
 *
 * @param <TagetInfo> 队列对应的实体信息
 * @param <QueueInfo> 队列信息
 * @param <Param> 请求参数
 */
public class LockTaskQueue<TagetInfo, QueueInfo, Param> 
	implements ITaskQueue<TagetInfo, QueueInfo, Param>, Runnable {
	
	protected final QueueInfo queueInfo;
	
	/**
	 * 运行状态
	 */
	protected RunState runState = RunState.STOP;
	
	/**
	 * 要求挂起队列
	 */
	protected boolean requireHold = false;
	
	/**
	 * 要求回收队列
	 */
	protected boolean requireAbandon = false;
	
	/**
	 * 队列状态
	 */
	protected QueueState queueState = QueueState.NORMAL;

	/**
	 * 队列 状态 数组的操作锁
	 */
	protected final ReentrantLock operateLock = new ReentrantLock();
	
	/**
	 * 任务列表
	 */
	protected final LinkedList<Task<TagetInfo, QueueInfo, Param>> waitTaskList = new LinkedList<Task<TagetInfo, QueueInfo, Param>>();
	
	/**
	 * 当前执行的任务
	 * 
	 * 注意：此变量在 run 方法中访问不加锁，所以此处设置为可 volatile，
	 *      在设置此变量的时候需要保证 ：
	 *      1 run方法没有被执行（没有线程真正处理queue的信息（runState = STOP））
	 *      或者：
	 *      2  改变变量的的正是run方法的线程（eg：execNextJob）
	 * 
	 */
	protected volatile Task<TagetInfo, QueueInfo, Param> taskNow = null;
	
	/**
	 * 线程开始执行当前任的务时间，没有被执行时=0
	 */
	protected volatile long threadStartTime = 0;
	
	
	/**
	 * 当前线程
	 */
	protected volatile Thread threadNow = null;
	
	/**
	 * 线程池
	 */
	protected final LimitedUnboundedThreadPoolExecutor pool;
	
	/**
	 * 异常监听器
	 */
	protected final IExceptionListener exceptionListener;
	
	/**
	 * 回收队列的回调
	 */
	protected IAbandonQueueInvoke<QueueInfo> abandonQueueInvoke;
	
	public LockTaskQueue( QueueInfo queueInfo, LimitedUnboundedThreadPoolExecutor pool, 
			IExceptionListener exceptionListener ) {
		
		this.queueInfo = queueInfo;
		this.pool = pool;
		this.exceptionListener = exceptionListener;
		
	}
	
	@Override
	public Lock getQueueOperateLock() {
		
		return operateLock;
	}
	
	@Override
	public boolean isRequireHold() {
		return requireHold;
	}

	@Override
	public boolean isRequireAbandon() {
		return requireAbandon;
	}

	@Override
	public void run() {
		
		try {
			
			this.threadStartTime = System.currentTimeMillis();
			this.threadNow = Thread.currentThread();
			
			Task<TagetInfo, QueueInfo, Param> taskNow = this.taskNow;
			IRequestHandler<TagetInfo, Param> handler =  taskNow.handler;
			TagetInfo tagetInfo = taskNow.tagetInfo;
			Param param = taskNow.param;
			int requestId = taskNow.requestId;
			
			try {
				
				handler.before(requestId, tagetInfo, param);
				
			} catch (Exception e) {
				exceptionListener.exception(e);
			}
			
			try {
				
				handler.handlerRequest(requestId, tagetInfo, param);
				
			} catch (Exception e) {
				exceptionListener.exception(e);
			}

			try {
				
				handler.after(requestId, tagetInfo, param);
				
			} catch (Exception e) {
				exceptionListener.exception(e);
			}
			
		} catch (Exception e) {
			
			exceptionListener.exception(e);
			
		} finally {
			
			this.taskNow = null;
			this.threadNow = null;
			this.threadStartTime = 0;
			
			execNextJob();
		}
		
	}
	
	private void execNextJob() {
		
		operateLock.lock();
		try {
			
			if( queueState == QueueState.NORMAL && runState == RunState.RUNNING ) {
				
				if( requireHold ) {

					requireHold = false;
					
					// 状态3 下次挂起 => 4 挂起
					taskNow = null;
					runState = RunState.STOP;
					queueState = QueueState.HOLD;
					
					if( waitTaskList.size() == 0 && requireAbandon ) {
						
						requireAbandon = false;
						
						if( abandonQueueInvoke.queueEmptyNowSureToAbandon(queueInfo) ) {
							
							// 状态4 挂起 => 5 销毁
							queueState = QueueState.ABANDON;
							
						}
						
					}
					
					return;
					
				} else {
					
					// 取得下一个任务，并启动线程执行
					for( ;; ) {
						
						if( waitTaskList.size() > 0 ) {
							
							taskNow = waitTaskList.removeFirst();
							if( taskNow.handler.isLimited(taskNow.requestId, taskNow.tagetInfo, taskNow.param) ) {
								
								if( pool.executeLimited(this) ) {
									
									return;
									
								} else {
									
									// 队列满
									taskNow.handler.queueFull(taskNow.requestId, taskNow.tagetInfo, taskNow.param);
								}
								
							} else {
								
								pool.executeUnbounded(this);
								return;
							}
							
						} else {
							
							// 状态2  运行 => 1 等待
							taskNow = null;
							runState = RunState.STOP;
							
							if( requireAbandon ) {
								
								requireAbandon = false;
								
								if( abandonQueueInvoke.queueEmptyNowSureToAbandon(queueInfo) ) {
									
									// 状态1 等待 => 5 销毁
									queueState = QueueState.ABANDON;
								}
								
							}
							
							return;
						}
					}
					
				}
				
				
			}
			
			exceptionListener.erroState("unexpect queueState state:" + queueState + " in execNextJob 130");
			
		} finally {
			
			operateLock.unlock();
		}
	}
	
	@Override
	public ExecutorResult execTask(int requestId, TagetInfo tagetInfo, IRequestHandler<TagetInfo, Param> handler, Param param) {
		
		operateLock.lock();
		try {
			
			switch (queueState) {
			
			case NORMAL:
				
				//队列状态 = 正常

				if( runState == RunState.STOP ) {
					
					// 运行状态 = 停止
					// 添加任务到任务列表，并启动线程运行当前queue
					
					if( handler.isLimited(requestId, tagetInfo, param) ) {
						
						// 队列状态 = 正常 & 运行状态 = 停止，则队列中一定没有内容，所以只需要设置taskNow
						// 由于 taskNow 是 volatile的，队列里的线程执行时就可以看到这个变量
						taskNow = new Task<TagetInfo, QueueInfo, Param>(requestId, tagetInfo, handler, param);
						
						if( pool.executeLimited(this) ) {
							
							// 状态1  等待 => 2 运行
							runState = RunState.RUNNING;
							
						} else {
							
							// 队列满，重置taskNow
							taskNow = null;
							handler.queueFull(requestId, tagetInfo, param);
						}
						
					} else {
						
						// 队列状态 = 正常 & 运行状态 = 停止，则队列中一定没有内容，所以只需要设置taskNow
						// 由于 taskNow 是 volatile的，队列里的线程执行时就可以看到这个变量
						taskNow = new Task<TagetInfo, QueueInfo, Param>(requestId, tagetInfo, handler, param);
						
						pool.executeUnbounded(this);
						
						// 状态1  等待 => 2 运行
						runState = RunState.RUNNING;
						
					}
					
				} else {
					
					// 运行状态 = 执行
					// 添加任务到任务列表
					
					waitTaskList.add( new Task<TagetInfo, QueueInfo, Param>(requestId, tagetInfo, handler, param) );
					
				}
				
				return ExecutorResult.SUCCESS;

			case HOLD:
				
				// 添加任务到任务列表
				waitTaskList.add( new Task<TagetInfo, QueueInfo, Param>(requestId, tagetInfo, handler, param) );
				
				return ExecutorResult.SUCCESS;
				
			case ABANDON:
				
				return ExecutorResult.QUEUE_ABANDON;
				
			}
			
		} finally {
			
			operateLock.unlock();
		}
		
		exceptionListener.erroState("unexpect queueState state:" + queueState + " in execTask 218");
		
		return ExecutorResult.EXCEPTION;
		
	}

	@Override
	public ExecutorResult holdQueue() {

		operateLock.lock();
		try {
			
			switch (queueState) {
			
			case NORMAL:
				
				requireHold = true;
				
			case HOLD:
				
				return ExecutorResult.SUCCESS;
				
			case ABANDON:
				
				return ExecutorResult.QUEUE_ABANDON;
				
			}
			
		} finally {
			
			operateLock.unlock();
		}
		
		exceptionListener.erroState("unexpect queueState state:" + queueState + " in resumeQueue 218");
		
		return ExecutorResult.EXCEPTION;
	}

	@Override
	public ExecutorResult resumeQueue() {
		
		operateLock.lock();
		try {
			
			switch (queueState) {
			
			case NORMAL:
				
				return ExecutorResult.SUCCESS;
				
			case HOLD:
				
				if( runState == RunState.STOP ) {
					
					// 取得下一个任务，并启动线程执行
					for( ;; ) {
						
						if( waitTaskList.size() > 0 ) {
							
							taskNow = waitTaskList.removeFirst();
							if( taskNow.handler.isLimited(taskNow.requestId, taskNow.tagetInfo, taskNow.param) ) {
								
								if( pool.executeLimited(this) ) {
									
									// 状态4 挂起  => 2 运行
									runState = RunState.RUNNING;
									break;
									
								} else {
									
									// 队列满
									taskNow.handler.queueFull(taskNow.requestId, taskNow.tagetInfo, taskNow.param);
								}
								
							} else {
								
								pool.executeUnbounded(this);
								runState = RunState.RUNNING;
								break;
							}
							
						} else {
							
							// 状态4 挂起  => 1 等待
							taskNow = null;
							break;
						}
					}
					
					queueState = QueueState.NORMAL;
					return ExecutorResult.SUCCESS;
					
				} else {
					
					// 状态3  下次挂起 => 2 运行
					queueState = QueueState.NORMAL;
					return ExecutorResult.SUCCESS;
				}
				
			case ABANDON:
				
				return ExecutorResult.QUEUE_ABANDON;
				
			}
			
		} finally {
			
			operateLock.unlock();
		}
		
		exceptionListener.erroState("unexpect queueState state:" + queueState + " in resumeQueue 218");
		
		return ExecutorResult.EXCEPTION;
	}
	
	@Override
	public TaskInterruptInfo<Param> interruptTaskNow(int tagetId) {
		
		boolean success = false;
		long beginTime=0, interrupTime=0;
		String threadName="";
		Param param = null;
		
		operateLock.lock();
		try {
			
			Task<TagetInfo, QueueInfo, Param> taskNow = this.taskNow;
			if( taskNow != null && taskNow.requestId == tagetId ) {
				
				beginTime = this.threadStartTime;
				Thread threadNow = this.threadNow;
				if( threadNow != null ) {
					
					interrupTime = System.currentTimeMillis();
					threadName = threadNow.getName();
					param = taskNow.param;
					
					threadNow.interrupt();
					
					success = true;
				}
			}
			
		} finally {
			
			operateLock.unlock();
		}
		
		TaskInterruptInfo<Param> taskInterruptInfo = 
				new TaskInterruptInfo<Param>(success, threadName, beginTime, interrupTime, param);
		
		return taskInterruptInfo;
	}
	
	@Override
	public List< Task<TagetInfo, QueueInfo, Param> > clearWaitTaskList(int tagetIdNow) {
		
		operateLock.lock();
		try {
			
			Task<TagetInfo, QueueInfo, Param> task = this.taskNow;
			if( task != null && task.requestId == tagetIdNow ) {
				
				ArrayList<Task<TagetInfo, QueueInfo, Param>> list = 
						new ArrayList<Task<TagetInfo, QueueInfo, Param>>(waitTaskList);
				
				waitTaskList.clear();
				
				return list;
			}
			
			return null;
			
		} finally {
			
			operateLock.unlock();
		}
		
	}
	
	@Override
	public List< Task<TagetInfo, QueueInfo, Param> > clearWaitTaskList() {
		
		operateLock.lock();
		try {
			
			ArrayList<Task<TagetInfo, QueueInfo, Param>> list = 
					new ArrayList<Task<TagetInfo, QueueInfo, Param>>(waitTaskList);
			
			waitTaskList.clear();
			
			return list;
			
		} finally {
			
			operateLock.unlock();
		}
		
	}
	
	@Override
	public ExecutorResult requireAbandon( IAbandonQueueInvoke<QueueInfo> abandonQueueInvoke ) {

		operateLock.lock();
		try {
			
			if( queueState == QueueState.ABANDON )
				return ExecutorResult.QUEUE_ABANDON;
			
			if( waitTaskList.size() == 0 && runState == RunState.STOP ) {
				
				requireAbandon = false;
				
				if( abandonQueueInvoke.queueEmptyNowSureToAbandon(queueInfo) ) {
					
					queueState = QueueState.ABANDON;
				}
				
			} else {
				
				this.abandonQueueInvoke = abandonQueueInvoke;
				requireAbandon = true;
			}
			
		} finally {
			
			operateLock.unlock();
		}
		
		return ExecutorResult.SUCCESS;
	}
	
	@Override
	public long getThreadStartTime() {
		
		return threadStartTime;
	}

	@Override
	public QueueRuningInfo<QueueInfo, Param> getQueueRuningInfo( boolean needThreadTrackInfo ) {
		
		operateLock.lock();
		try {
			
			QueueStatus queueStatus = new QueueStatus(runState, queueState, requireHold, requireAbandon, threadStartTime);
			
			List<TaskInfo<Param>> taskInfoList = new ArrayList<TaskInfo<Param>>( waitTaskList.size() + 1 );
			
			Task<TagetInfo, QueueInfo, Param> taskNow = this.taskNow;
			if( taskNow != null ) {
				
				taskInfoList.add( new TaskInfo<Param>(taskNow.requestId, taskNow.handler.toString(), taskNow.param) );
				
			}
			
			for( Task<TagetInfo, QueueInfo, Param> task : waitTaskList )
				taskInfoList.add( new TaskInfo<Param>(task.requestId, task.handler.toString(), task.param) );
			
			String threadTrackInfo = "";
			if( needThreadTrackInfo ) {
				
				Thread t = this.threadNow;
				if( t != null ) {
					
					StackTraceElement[] elements = t.getStackTrace();
					StringBuilder builder = new StringBuilder( elements.length << 5 );
					for( StackTraceElement element : elements )
						builder.append(element).append("\n");
					
					threadTrackInfo = builder.toString();
					
				} else {
					
					threadTrackInfo = "null";
				}
				
			}
			
			return new QueueRuningInfo<QueueInfo, Param>(queueStatus, queueInfo, threadTrackInfo, taskInfoList);
			
		} finally {
			
			operateLock.unlock();
		}
		
	}

}
