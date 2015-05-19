package lakenono.base;

import java.io.IOException;

import lakenono.core.GlobalComponents;

public abstract class DistributedParser
{
	private String classname = this.getClass().getName();

	// 队列名称
	public abstract String getQueueName();

	// 项目名称
	public String projectName;

	public void run()
	{
		// 取task
		Task task = Queue.pull(this.getQueueName());

		// TODO task is null

		String result;

		// 爬取
		try
		{
			result = GlobalComponents.fetcher.fetch(task.getUrl());
		}
		catch (IOException | InterruptedException e)
		{
			// TODO 下载异常进行重试 推送任务到队列
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
			return;
		}

		// 更新任务状态
		task.updateSuccess();
	}

	public abstract void parse(String result, Task task) throws Exception;

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
