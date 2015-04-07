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
//			task.persistOnNotExist();
//			//TODO 数据库中Todo类型的任务会重新推送，对于还没有执行完的任务，在周期完成时会重新推送。
//			if (!task.hasCompleted()) {
//				// 推送任务
//				pushTask(task);
//			}
			
			boolean isNotPush = task.persistOnNotExist();
			if(isNotPush){
				pushTask(task);
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
	public static  void cleanAllTask(String taskQueueName) {
		GlobalComponents.jedis.del(taskQueueName);
		log.info("【{}】 queue clean all tasks !", taskQueueName);
	}

	public void rePushTask(String keyword) throws SQLException {
		List<FetchTask> tasks = FetchTask.getTodoTasks(keyword, taskQueueName);
		for (FetchTask task : tasks) {
			pushTask(task);
		}
	}
}
