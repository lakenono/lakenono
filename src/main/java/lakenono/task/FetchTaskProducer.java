package lakenono.task;

import java.sql.SQLException;
import java.util.List;

import lakenono.core.GlobalComponents;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

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
		// 持久化任务——用于跟踪任务整体情况
		if (task != null) {
			// task.persistOnNotExist();
			// //TODO 数据库中Todo类型的任务会重新推送，对于还没有执行完的任务，在周期完成时会重新推送。
			// if (!task.hasCompleted()) {
			// // 推送任务
			// pushTask(task);
			// }

			boolean isExist = task.exist();
			if (!isExist) {
				// 推送
				pushTask(task);

				// 记录推送日志
				task.persist();
			}
		}

	}

	/**
	 * 发送任务
	 * 
	 * @param task
	 */
	public void pushTask(FetchTask task) {
		GlobalComponents.jedis.lpush(taskQueueName, JSON.toJSONString(task));
		log.info("{} push mq !", task);
	}

	/**
	 * 清空任务队列
	 */
	public static void cleanAllTask(String taskQueueName) {
		log.info("【{}】 queue clean tasks start !", taskQueueName);
		GlobalComponents.jedis.del(taskQueueName);
		log.info("【{}】 queue clean task finish!", taskQueueName);
	}

	public static void cleanAllTaskLog(String name, String batchName) throws SQLException {
		log.info("【{},{}】 clean task log start !", name, batchName);
		int delCount = FetchTask.cleanAllTask(name, batchName);
		log.info("【{},{}】  clean task log finish ! del : {}", name, batchName,delCount);
	}

	public void rePushTask(String keyword) throws SQLException {
		List<FetchTask> tasks = FetchTask.getTodoTasks(keyword, taskQueueName);
		for (FetchTask task : tasks) {
			pushTask(task);
		}
	}
}
