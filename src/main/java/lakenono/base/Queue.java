package lakenono.base;

import java.sql.SQLException;
import java.util.List;

import lakenono.core.GlobalComponents;
import lombok.extern.slf4j.Slf4j;

import com.alibaba.fastjson.JSON;

/**
 * 队列工具类 
 * @author Lakenono
 */
@Slf4j
public class Queue 
{
	/**
	 * 推送任务
	 * 
	 * @param task
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SQLException
	 */
	public static void push(Task task)
	{
		// 如果task为空 不做操作
		if (task == null)
		{
			return;
		}

		// 如果task不存在 持久化
		try
		{
			if (!task.exist())
			{
				task.persist();
			}
		}
		catch (IllegalArgumentException | IllegalAccessException | SQLException | InstantiationException e)
		{
			log.error("{}", e);
		}

		// 推送
		pushMQ(task);
	}

	/**
	 * 拉取任务
	 * 
	 * @param queueName
	 * @return task or null
	 */
	public static Task pull(String queueName)
	{
		String json = GlobalComponents.redisAPI.rpop(queueName);
//		String json = GlobalComponents.jedis.rpop(queueName);
		if (json == null)
		{
			return null;
		}
		else
		{
			Task task = JSON.parseObject(json, Task.class);
			return task;
		}
	}

	/**
	 * 发送任务
	 *  
	 * @param task
	 */
	public static void pushMQ(Task task)
	{
//		GlobalComponents.jedis.lpush(task.getQueueName(), JSON.toJSONString(task));
		GlobalComponents.redisAPI.lpush(task.getQueueName(), JSON.toJSONString(task));
		log.info("{} push mq !", task);
	}
	/**
	 * 查看当前redis某个队列有多少个数据
	 * @param queueName 队列名
	 * @return 返回数字
	 */
	public static long viewQueueNum(String queueName){
		long count = GlobalComponents.jedis.llen(queueName);
		log.debug("{} redis queuename={},count={}", queueName, count);
		return count;
	}
	
	/**
	 * 清空当前redis某个队列数据
	 * @param queueName 队列名
	 */
	public static long cleanQueue(String queueName){
		long count = GlobalComponents.jedis.del(queueName);
		log.debug("{} redis clean queuename={},count={}",queueName,count);
		return count;
	}
	
	/**
	 * 重推todo状态的抓取任务
	 * @param projectName 总任务名
	 * @param queueName 队列名
	 * @throws SQLException 
	 */
	public static void pushTodoTask(String projectName,String queueName) throws SQLException{
		List<Task> taks = Task.getTaks(projectName, queueName, Task.TODO);
		if(taks==null){
			return;
		}
		for(Task task:taks){
			pushMQ(task);
		}
	}
}
