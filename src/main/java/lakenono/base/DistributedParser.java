package lakenono.base;

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
			if(this.fetchType.equals(FETCH_TYPE_DYNAMIC)){
				result = GlobalComponents.dynamicFetch.fetch(task.getUrl());
			} else if (this.fetchType.equals(FETCH_TYPE_JSON)) {
				result = GlobalComponents.jsonFetch.text(task.getUrl());
			}else{
				result = GlobalComponents.fetcher.fetch(task.getUrl());
			}
		}
		catch (Exception e)
//		catch (IOException | InterruptedException e)
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
