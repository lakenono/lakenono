package lakenono.task;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 列表页任务生成器,定义分页处理模板，推送任务
 * 
 * @author shi.lei
 *
 */
@Slf4j
public abstract class PagingFetchTaskProducer extends FetchTaskProducer {

	public PagingFetchTaskProducer(String taskQueueName) {
		super(taskQueueName);
	}

	public static final int FIRST_PAGE = 1;

	@Setter
	protected long sleep = 3000;

	public void run() {
		log.info("FetchTask Producer start ...");

//		super.cleanAllTask();

		int pages = getMaxPage();

		// 提取最大页失败
		if (pages == 0) {
			log.error("Get max page fail ! ");
			return;
		}
		
		log.info("Get max page : " + pages);

		// 迭代
		for (int i = 1; i <= pages; i++) {
			// 创建url
			String url = buildUrl(i);
			// 创建抓取任务
			FetchTask task = buildTask(url);
			// 推送任务
			try {
				super.saveAndPushTask(task);
			} catch (Exception e) {
				log.error("{} push error!", task, e);
			}
		}

		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * 获取最大页数
	 * 
	 * @return 0：获取失败，1:1页；n：n页
	 */
	protected abstract int getMaxPage();

	/**
	 * 
	 * @param pageNum
	 *            页号，从1开始
	 * @return 请求页面的url
	 */
	protected abstract String buildUrl(int pageNum);

	/**
	 * 生成抓取任务
	 * 
	 * @param url
	 * @return
	 */
	protected abstract FetchTask buildTask(String url);

}
