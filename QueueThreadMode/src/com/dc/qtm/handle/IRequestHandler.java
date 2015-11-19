package com.dc.qtm.handle;


/**
 * 
 * 请求处理器
 * 
 * 执行顺序：  1. 先判断 是否队列满 （针对受队列长度限制的 请求），队列满则调用queueFull，不再往下调用
 *          2. before
 *          3. handlerRequest
 *          4. after
 * 
 * @author Daemon
 *
 * @param <TagetInfo> 队列对应的实体信息
 * @param <Param> 请求参数
 * 
 */
public interface IRequestHandler<TagetInfo, Param> {
	
	/**
	 * 
	 * 该请求是否受队列长度影响（如果放回true，则当队列满的时候直接调用queueFull，不会调用处理方法）
	 * （作用：一些系统处理：例如：用户掉线 应该不受队列限制，必须被处理）
	 * 
	 * @param 请求的id
	 * @param tagetInfo 队列对应的实体信息
	 * @param param 请求参数 
	 * @return 该请求是否受队列长度影响
	 */
	boolean isLimited(int requestId, TagetInfo tagetInfo, Param param);
	
	/**
	 * 
	 * 队列满（针对受队列长度限制的 请求）
	 * 
	 * @param 请求的id
	 * @param tagetInfo 队列对应的实体信息
	 * @param param 请求参数
	 */
	void queueFull(int requestId, TagetInfo tagetInfo, Param param);
	
	/**
	 * 
	 * 执行 handlerRequest 方法前调用
	 * 
	 * @param 请求的id
	 * @param tagetInfo 队列对应的实体信息
	 * @param param 请求参数
	 */
	void before(int requestId, TagetInfo tagetInfo, Param param);
	
	/**
	 * 
	 * 处理请求
	 * 
	 * @param 请求的id
	 * @param tagetInfo 队列对应的实体信息
	 * @param param 请求参数
	 */
	void handlerRequest(int requestId, TagetInfo tagetInfo, Param param);
	
	/**
	 * 
	 * 执行 handlerRequest 方法后调用
	 * 
	 * @param 请求的id
	 * @param tagetInfo 队列对应的实体信息
	 * @param param 请求参数
	 */
	void after(int requestId, TagetInfo tagetInfo, Param param);
	
}
