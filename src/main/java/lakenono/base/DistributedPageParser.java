package lakenono.base;

import java.io.IOException;

import lakenono.core.GlobalComponents;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DistributedPageParser
{
	private String classname = this.getClass().getName();

	public static final int FIRST_PAGE = 1;

	// 队列名称
	public abstract String getQueueName();

	public void run()
	{
		// 取task
		Task task = Queue.pull(this.getQueueName());

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
			return;
		}

		// 设置整体任务完成
		task.updateSuccess();
	}

	public abstract void parse(String result, Task task) throws Exception;

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

	// 处理url正常页面无数据 例如: "该贴以删除."
	protected boolean errorPage(String result)
	{
		return false;
	}

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
