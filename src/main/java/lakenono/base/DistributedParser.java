package lakenono.base;

import java.io.IOException;

import lakenono.core.GlobalComponents;
import lombok.extern.slf4j.Slf4j;

/**
 * 普通任务 
 * @author Lakenono
 */
@Slf4j
public abstract class DistributedParser extends BaseParser
{
	// 队列名称
	public abstract String getQueueName();

	// 项目名称
	public String projectName;

	@Override
	public void run()
	{
		// 取task
		Task task = Queue.pull(this.getQueueName());

		// 空值判断
		if (null == task)
		{
			log.debug("{} task is null. sleep...", this.getClass());
			return;
		}
		else
		{
			log.debug("task begin {}", task);
		}

		String result;

		// 爬取
		try
		{
			result = GlobalComponents.fetcher.fetch(task.getUrl());
		}
		catch (IOException | InterruptedException e)
		{
			// TODO 下载异常进行重试 推送任务到队列
			log.error("", e);
			task.updateError();
			return;
		}

		// 解析
		try
		{
			this.parse(result, task);
		}
		catch (Exception e)
		{
			// TODO 解析异常直接进入error
			log.error("", e);
			task.updateError();
			return;
		}

		// 更新任务状态
		task.updateSuccess();
	}

	protected Task buildTask(String url, String queueName, Task perTask)
	{
		Task task = new Task();
		task.setProjectName(perTask.getProjectName());
		task.setQueueName(queueName);
		task.setUrl(url);
		task.setExtra(perTask.getExtra());

		return task;
	}

}
