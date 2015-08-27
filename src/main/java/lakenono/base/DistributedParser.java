package lakenono.base;

import lakenono.core.GlobalComponents;
import lakenono.fetcher.Fetcher;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 普通任务
 * 
 * @author Lakenono
 */
@Slf4j
public abstract class DistributedParser extends BaseParser {
	
	private static final Logger taskLogger = LoggerFactory.getLogger("task_log");
	
	// 队列名称
	public abstract String getQueueName();

	// 项目名称
	public String projectName;

	@Override
	public void run() {
		String classname = this.getClass().getName();
		
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

			String charset = getCharset();

			// 下载内容
			result = fetch(fetcher, cookies, charset, task);

			// 解析
			this.parse(result, task);

		} catch (Exception e) {
			log.error("task handle error {} , error : {}  ", task, e.getMessage(), e);
			task.updateError();
			taskLogger.info("{},{},{},{},{},{}",classname,task.getProjectName(),task.getQueueName(),"error",e.getMessage(),task.getUrl());
			return;
		}

		// 更新任务状态
		task.updateSuccess();
		
		taskLogger.info("{},{},{},{},{},{}",classname,task.getProjectName(),task.getQueueName(),"success","",task.getUrl());
	}

	/**
	 * 抓取
	 * 
	 * @param fetcher
	 * @param cookies
	 * @param charset
	 * @param task
	 * @return
	 * @throws Exception
	 */
	protected String fetch(Fetcher fetcher, String cookies, String charset, Task task) throws Exception {
		return fetcher.fetch(task.getUrl(), cookies, charset);
	}

	/**
	 * 设置字符集
	 * 
	 * @return
	 */
	protected String getCharset() {
		return "";
	}

	protected Task buildTask(String url, String queueName, Task perTask) {
		Task task = new Task();
		task.setProjectName(perTask.getProjectName());
		task.setQueueName(queueName);
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
