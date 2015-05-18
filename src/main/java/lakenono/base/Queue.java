package lakenono.base;

import java.sql.SQLException;

import lakenono.core.GlobalComponents;
import lombok.extern.slf4j.Slf4j;

import com.alibaba.fastjson.JSON;

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
	public static void push(Task task) throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException
	{
		// 如果task为空 不做操作
		if (task == null)
		{
			return;
		}

		// 如果task已存在 不做操作
		if (task.exist())
		{
			return;
		}

		// 推送
		pushMQ(task);

		// 持久化task
		task.persist();
	}

	/**
	 * 拉取任务
	 * 
	 * @param queueName
	 * @return task or null
	 */
	public static Task pull(String queueName)
	{
		String json = GlobalComponents.jedis.rpop(queueName);

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
		GlobalComponents.jedis.lpush(task.getQueueName(), JSON.toJSONString(task));
		log.info("{} push mq !", task);
	}
}
