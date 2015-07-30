package lakenono.base;

import org.apache.commons.lang.StringUtils;

import lakenono.core.GlobalComponents;
import lakenono.fetcher.Fetcher;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理带有分页的任务 
 * 
 * @author Lakenono
 */
@Slf4j
public abstract class DistributedPageParser extends BaseParser {
	public static final int FIRST_PAGE = 1;

	// 队列名称
	public abstract String getQueueName();

	@Override
	public void run() {
		// 取task
		Task task = Queue.pull(this.getQueueName());

		// 空值判断
		if (null == task) {
			log.debug("{} task is null. sleep...", this.getClass());
			return;
		} else {
			log.debug("task begin {}", task);
		}

		String result;

		// 爬取
		try {
			Fetcher fetcher = selectFetcher();

			// 获得cookie
			String cookies = null;
			String cookieDomain = getCookieDomain();

			if (StringUtils.isNotBlank(cookieDomain)) {
				// 做获取cookie的逻辑
				cookies = GlobalComponents.authService.getCookies(cookieDomain);
				log.debug("get cookie : {} = {}", cookieDomain, cookies);
			}

			// 下载内容
			result = fetcher.fetch(task.getUrl(), cookies);
		} catch (Exception e)
		// catch (IOException | InterruptedException e)
		{
			// TODO 下载异常进行重试 推送任务到队列
			task.updateError();
			return;
		}

		// 判断错误页面
		if (this.errorPage(result)) {
			task.updateErrorPage();
			return;
		}

		try {
			// 解析
			this.parse(result, task);

			// 翻页逻辑
			int pagenum = this.getMaxPage(result, task);

			// 迭代
			for (int i = FIRST_PAGE; i <= pagenum; i++) {
				// 创建url
				String url = buildUrl(i);

				// 创建抓取任务
				Task todoTask = this.buildTask(url, task);

				// 推送任务
				Queue.push(todoTask);
			}

		} catch (Exception e) {
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
	protected Task buildTask(String url, Task perTask) {
		Task task = new Task();
		task.setProjectName(perTask.getProjectName());
		task.setQueueName(perTask.getQueueName());
		task.setUrl(url);
		task.setExtra(perTask.getExtra());

		return task;
	}
	
	/**
	 * 选择Fetcher
	 * 
	 * @return
	 */
	protected Fetcher selectFetcher() {
		Fetcher fetcher = null;

		if (this.fetchType.equals(FETCH_TYPE_DYNAMIC)) {
			fetcher = GlobalComponents.seleniumFetcher;
		} else {
			fetcher = GlobalComponents.jsoupFetcher;
		}

		return fetcher;
	}

	/**
	 * 获取cookie 域名
	 * 
	 * @return
	 */
	protected String getCookieDomain() {
		return "";
	}
}
