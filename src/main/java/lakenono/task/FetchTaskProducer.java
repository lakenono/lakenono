package lakenono.task;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import lakenono.core.GlobalComponents;
import lombok.extern.slf4j.Slf4j;

import com.alibaba.fastjson.JSON;

@Slf4j
public class FetchTaskProducer {
	protected String taskQueueName;

	public FetchTaskProducer(String taskQueueName) {
		if (StringUtils.isBlank(taskQueueName)) {
			throw new IllegalArgumentException("Task queue name can not be blank");
		}
		this.taskQueueName = taskQueueName;
	}

	// 处理任务
	public void saveAndPushTask(FetchTask task) throws IllegalArgumentException, IllegalAccessException, InstantiationException, SQLException {
		if (task != null && !task.isFinish()) {
			// 持久化任务——用于跟踪任务整体情况
			task.persist();
			// 推送任务
			pushTask(task);
		}
	}

	/**
	 * 发送任务
	 * 
	 * @param task
	 */
	public void pushTask(FetchTask task) {
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
		GlobalComponents.jedis.lpush(taskQueueName, JSON.toJSONString(task));
		System.out.println("8888888888888888888888888888888888888888888888888");
		log.info("{} push mq !", task);
	}

	/**
	 * 清空任务队列
	 */
	public void cleanAllTask() {
		GlobalComponents.jedis.del(taskQueueName);
		log.info("【{}】 queue clean all tasks !", taskQueueName);
	}
}
