package lakenono.base;

import java.io.IOException;

import org.junit.Test;

import lakenono.core.GlobalComponents;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理带有分页的任务 
 * @author Lakenono
 */
@Slf4j
public abstract class DistributedPageParser extends BaseParser
{
	public static final int FIRST_PAGE = 1;

	// 队列名称
	public abstract String getQueueName();

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
			//TODO 静态 动态 fetch判断.. 使用抽象方法. 是否使用cookie
			result = GlobalComponents.fetcher.fetch(task.getUrl());
		}
		catch (IOException | InterruptedException e)
		{
			// TODO 下载异常进行重试 推送任务到队列
			task.updateError();
			return;
		}

		// 判断错误页面
		if (this.errorPage(result))
		{
			task.updateErrorPage();
			return;
		}

		try
		{
			// 解析
			this.parse(result, task);

			// 翻页逻辑
			int pagenum = this.getMaxPage(result, task);

			// 迭代
			for (int i = FIRST_PAGE; i <= pagenum; i++)
			{
				// 创建url
				String url = buildUrl(i);

				// 创建抓取任务
				Task todoTask = this.buildTask(url, task);

				// 推送任务
				Queue.push(todoTask);
			}

		}
		catch (Exception e)
		{
			log.error("{}", e);
			task.updateError();
			return;
		}

		// 设置整体任务完成
		task.updateSuccess();
	}

	public abstract int getMaxPage(String result, Task task) throws Exception;

	/**
	 * 
	 * @param pageNum
	 *            页号，从1开始
	 * @return 请求页面的url
	 */
	protected abstract String buildUrl(int pageNum) throws Exception;

	/**
	 * 生成抓取任务
	 * 
	 * @param url
	 * @return
	 */
	protected Task buildTask(String url, Task perTask)
	{
		Task task = new Task();
		task.setProjectName(perTask.getProjectName());
		task.setQueueName(this.getQueueName());
		task.setUrl(url);
		task.setExtra(perTask.getExtra());

		return task;
	}
}
