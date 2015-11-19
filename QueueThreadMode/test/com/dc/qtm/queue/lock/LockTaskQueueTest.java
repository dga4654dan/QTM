package com.dc.qtm.queue.lock;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.dc.qtm.IAbandonQueueInvoke;
import com.dc.qtm.IExceptionListener;
import com.dc.qtm.handle.IRequestHandler;
import com.dc.qtm.queue.ExecutorResult;
import com.dc.qtm.queue.QueueRuningInfo;
import com.dc.qtm.queue.TaskInfo;
import com.dc.qtm.queue.TaskInterruptInfo;
import com.dc.qtm.thread.pool.LimitedUnboundedThreadPoolExecutor;

public class LockTaskQueueTest {

	public static void main(String[] args) {
		
		LimitedUnboundedThreadPoolExecutor pool = new LimitedUnboundedThreadPoolExecutor( 5,
				5, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), 10 );

		IExceptionListener exceptionListener = new IExceptionListener() {
			
			@Override
			public void exception(Exception e) {
				
				e.printStackTrace();
			}
			
			@Override
			public void erroState(String erroInfo) {
				
				System.out.println(erroInfo);
			}
		};
		
		LockTaskQueue<Integer, String, Object> queue = 
				new LockTaskQueue<Integer, String, Object>("test 1", pool, exceptionListener);
		
		
//		testAbandon(queue);
		
//		testAddHoldResumeInterrupt(queue);
		
		testHoldAbandonCleaner(queue);
		
	}
	
	public static void testAbandon( final LockTaskQueue<Integer, String, Object> queue ) {
		
		for( int i=0; i<10; i++ ) {
			
			int id = i;
			
			System.out.println();
			System.out.println("入队: >>>>>> " + id);
			System.out.println();
			
			queue.execTask(id, 10010, new IRequestHandler<Integer, Object>() {

				@Override
				public boolean isLimited(int requestId, Integer tagetInfo, Object param) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void queueFull(int requestId, Integer tagetInfo, Object param) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void before(int requestId, Integer tagetInfo, Object param) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void handlerRequest(int requestId, Integer tagetInfo, Object param) {
					
					System.out.println();
					System.out.println("handle-" + param + "-begin");
					
					try {
						TimeUnit.SECONDS.sleep( 1 );
					} catch (InterruptedException e) {
						System.out.println("---------sleep 被中断----------");
					}
					
					System.out.println("handle-" + param + "-end");
					
				}

				@Override
				public void after(int requestId, Integer tagetInfo, Object param) {
					// TODO Auto-generated method stub
					
				}
			}, id);
			
		}
		
		IAbandonQueueInvoke<String> abandonQueueInvoke = new IAbandonQueueInvoke<String>() {

			@Override
			public boolean queueEmptyNowSureToAbandon(String queueInfo) {
				
				return true;
			}
		};
		
		
		System.out.println( "requireAbandon: " + queue.requireAbandon(abandonQueueInvoke) );
		System.out.println( "requireAbandon: " + queue.requireAbandon(abandonQueueInvoke) );
		
		
		try {
			TimeUnit.SECONDS.sleep( 12 );
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
		
		ExecutorResult resut = queue.execTask(100101111, 10010, new IRequestHandler<Integer, Object>() {

			@Override
			public boolean isLimited(int requestId, Integer tagetInfo, Object param) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void queueFull(int requestId, Integer tagetInfo, Object param) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void before(int requestId, Integer tagetInfo, Object param) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void handlerRequest(int requestId, Integer tagetInfo, Object param) {
				
				System.out.println();
				System.out.println("handle-" + param + "-begin");
				
				try {
					TimeUnit.SECONDS.sleep( 1 );
				} catch (InterruptedException e) {
					System.out.println("---------sleep 被中断----------");
				}
				
				System.out.println("handle-" + param + "-end");
				
			}

			@Override
			public void after(int requestId, Integer tagetInfo, Object param) {
				// TODO Auto-generated method stub
				
			}
		}, 100101111);
		
		
		System.out.println("\n\ntry add:" + resut);
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
		System.out.println("\n\ntry hold:" + queue.holdQueue());
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
		System.out.println("\n\ntry resume:" + queue.resumeQueue());
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
		System.out.println("\n\ntry interrupt:" + queue.interruptTaskNow(100101111));
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
		System.out.println("\n\ntry abandon:" + queue.requireAbandon(abandonQueueInvoke));
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
		System.out.println("\n\ntry clearWaitTaskList:" + queue.clearWaitTaskList(100101111));
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
		System.out.println("\n\ntry clearWaitTaskList:" + queue.clearWaitTaskList());
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
	}
	
	public static void testHoldAbandonCleaner( final LockTaskQueue<Integer, String, Object> queue ) {
		
		for( int i=0; i<10; i++ ) {
			
			int id = i;
			
			System.out.println();
			System.out.println("入队: >>>>>> " + id);
			System.out.println();
			
			queue.execTask(id, 10010, new IRequestHandler<Integer, Object>() {

				@Override
				public boolean isLimited(int requestId, Integer tagetInfo, Object param) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void queueFull(int requestId, Integer tagetInfo, Object param) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void before(int requestId, Integer tagetInfo, Object param) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void handlerRequest(int requestId, Integer tagetInfo, Object param) {
					
					System.out.println();
					System.out.println("handle-" + param + "-begin");
					
					try {
						TimeUnit.SECONDS.sleep( 1 );
					} catch (InterruptedException e) {
						System.out.println("---------sleep 被中断----------");
					}
					
					System.out.println("handle-" + param + "-end");
					
				}

				@Override
				public void after(int requestId, Integer tagetInfo, Object param) {
					// TODO Auto-generated method stub
					
				}
			}, id);
			
		}

		
		try {
			TimeUnit.SECONDS.sleep( 3 );
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println( "holdQueue: " + queue.holdQueue() );
		
		try {
			TimeUnit.SECONDS.sleep( 2 );
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
		IAbandonQueueInvoke<String> abandonQueueInvoke = new IAbandonQueueInvoke<String>() {

			@Override
			public boolean queueEmptyNowSureToAbandon(String queueInfo) {
				
				return true;
			}
		};
		
		System.out.println("\n\ntry clearWaitTaskList:" + queue.clearWaitTaskList());
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
		System.out.println( "\n\nabandon: " + queue.requireAbandon(abandonQueueInvoke) );
		System.out.println( "QueueRuningInfo: " + queue.getQueueRuningInfo(false) );
		
	}
	
	public static void testAddHoldResumeInterrupt( final LockTaskQueue<Integer, String, Object> queue ) {
		
		final AtomicInteger count = new AtomicInteger();
		
		final ReentrantLock lock = new ReentrantLock();
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				for(;;) {
					
					try {
						TimeUnit.SECONDS.sleep( (int)(20 * Math.random()) );
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					lock.lock();
					
					int id = count.incrementAndGet();
					
					System.out.println();
					System.out.println("入队: >>>>>> " + id);
					System.out.println();
					
					queue.execTask(id, 10010, new IRequestHandler<Integer, Object>() {

						@Override
						public boolean isLimited(int requestId, Integer tagetInfo, Object param) {
							// TODO Auto-generated method stub
							return false;
						}

						@Override
						public void queueFull(int requestId, Integer tagetInfo, Object param) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void before(int requestId, Integer tagetInfo, Object param) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void handlerRequest(int requestId, Integer tagetInfo, Object param) {
							
							System.out.println();
							System.out.println("handle-" + param + "-begin");
							
							try {
								TimeUnit.SECONDS.sleep( (int)(18 * Math.random()) );
							} catch (InterruptedException e) {
								System.out.println("---------sleep 被中断----------");
							}
							
							System.out.println("handle-" + param + "-end");
							
						}

						@Override
						public void after(int requestId, Integer tagetInfo, Object param) {
							// TODO Auto-generated method stub
							
						}
					}, id);
					
					lock.unlock();
				}
			}
		} ).start();
		
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				for(;;) {
					
					try {
						TimeUnit.SECONDS.sleep( (int)(16 * Math.random()) );
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					lock.lock();
					
					int id = count.incrementAndGet();
					
					System.out.println();
					System.out.println("入队: >>>>>> " + id);
					System.out.println();
					
					queue.execTask(id, 10010, new IRequestHandler<Integer, Object>() {

						@Override
						public boolean isLimited(int requestId, Integer tagetInfo, Object param) {
							// TODO Auto-generated method stub
							return false;
						}

						@Override
						public void queueFull(int requestId, Integer tagetInfo, Object param) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void before(int requestId, Integer tagetInfo, Object param) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void handlerRequest(int requestId, Integer tagetInfo, Object param) {
							
							System.out.println();
							System.out.println("handle-" + param + "-start");
							
							double d = 0;
							for( int i=0; i<30000000; i++)
								d = d + Math.random();
							
							System.out.println("handle-" + param + "-end " + (int)d/10000);
							System.out.println();
						}

						@Override
						public void after(int requestId, Integer tagetInfo, Object param) {
							// TODO Auto-generated method stub
							
						}
					}, id);
					
					lock.unlock();
				}
				
				
			}
		} ).start();
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				for(;;) {
					
					try {
						TimeUnit.SECONDS.sleep( (int)(150 * Math.random()) );
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					queue.holdQueue();
					System.out.println();
					System.out.println("^^^^^^^^^^^^^^^^^^holdQueue^^^^^^^^^^^^^^^^^");
					System.out.println();
					
				}
				
				
			}
		} ).start();
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				for(;;) {
					
					try {
						TimeUnit.SECONDS.sleep( (int)(60 * Math.random()) );
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					queue.resumeQueue();
					System.out.println();
					System.out.println(">>>>>>>>>>>>>>>>>>resumeQueue>>>>>>>>>>>>>>>>>>");
					System.out.println();
					
				}
				
				
			}
		} ).start();
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				for(;;) {
					
					try {
						TimeUnit.SECONDS.sleep( (int)(120 * Math.random()) );
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					List<TaskInfo<Object>> list = queue.getQueueRuningInfo(false).getTaskInfoList();
					if( list.size() > 0 ) {
						
						TaskInterruptInfo<Object> info = queue.interruptTaskNow( list.get(0).requestId );
						
						System.out.println();
						System.out.println( "interruptTaskNow:" );
						System.out.println( info );
						System.out.println();
					}
					
				}
				
				
			}
		} ).start();
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				for(;;) {
					
					try {
						TimeUnit.SECONDS.sleep( 7 );
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					QueueRuningInfo<String, Object> queueRuningInfo = queue.getQueueRuningInfo(false);
					
					StringBuilder builder = new StringBuilder();
					builder.append("状态: ").append( queueRuningInfo.queueStatus ).append("\n");
					
					builder.append("任务列表: ").append("\n");
					
					for( TaskInfo<Object> taskInfo : queueRuningInfo.taskInfoList )
						builder.append( taskInfo.requestId ).append(" ").append( taskInfo.param ).append("\n");
					
					System.out.println();
					System.out.println( builder.toString() );
					System.out.println();
					
					
				}
				
				
			}
		} ).start();
		
	}
	
}



