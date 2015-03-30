package lakenono.task;

import java.io.IOException;

import lakenono.core.GlobalComponents;

import org.jsoup.nodes.Document;

/**
 * 页面解析任务处理器
 * 
 * @author shi.lei
 *
 */
public abstract class PageParseFetchTaskHandler extends FetchTaskHandler{
	
	public PageParseFetchTaskHandler(String taskQueueName) {
		super(taskQueueName);
	}

	/**
	 * 处理任务
	 * 
	 * @param task
	 * @throws InterruptedException
	 * @throws IOException
	 */
	protected void handleTask(FetchTask task) throws IOException, InterruptedException ,Exception{
		Document doc = GlobalComponents.fetcher.document(task.getUrl());
		parsePage(doc, task);
		Thread.sleep(sleep);
	}

	/**
	 * 解析页面
	 * 
	 * @param doc
	 * @param task
	 */
	protected abstract void parsePage(Document doc, FetchTask task)throws Exception;
}
