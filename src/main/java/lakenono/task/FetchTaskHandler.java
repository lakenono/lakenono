package lakenono.task;

import java.io.IOException;

import lakenono.core.GlobalComponents;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

@Slf4j
public abstract class FetchTaskHandler {
	protected String taskQueueName;
	
	@Setter
	protected long sleep=3000;
	
	public FetchTaskHandler(String taskQueueName){
		if(StringUtils.isBlank(taskQueueName)){
			throw new IllegalArgumentException( "Queue name can not blank!");
		}
		this.taskQueueName = taskQueueName;
	}
	
	protected long waitForNextTask = 60000;

	public void run() {
		while (true) {
			FetchTask task;
			try {
				task = getTask();
				if (task == null) {
					Thread.sleep(waitForNextTask);
					log.debug("wait for task ...");
					continue;
				}
				try {
					if (!task.hasCompleted()) {
						handleTask(task);
						task.updateSuccess();
						Thread.sleep(sleep);
					} else {
						log.info("{} has bean finish!", task);
					}
				} catch (Exception e) {
					log.error("task {} error : ", task, e);
					task.updateError();
				}

			} catch (Exception e1) {
				log.error("Get task error : ", e1);
			}

		}
	}

	/**
	 * 处理任务
	 * 
	 * @param task
	 * @throws InterruptedException
	 * @throws IOException
	 */
	protected abstract void handleTask(FetchTask task) throws Exception;

	private FetchTask getTask() {
		String json = GlobalComponents.jedis.rpop(taskQueueName);

		if (json == null) {
			return null;
		}

		FetchTask task = JSON.parseObject(json, FetchTask.class);
		return task;
	}
}
