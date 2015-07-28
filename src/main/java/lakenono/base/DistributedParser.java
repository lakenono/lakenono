package lakenono.base;

import org.apache.commons.lang.StringUtils;

import lakenono.core.GlobalComponents;
import lakenono.fetcher.Fetcher;
import lombok.extern.slf4j.Slf4j;

/**
 * 普通任务
 * 
 * @author Lakenono
 */
@Slf4j
public abstract class DistributedParser extends BaseParser {
	// 队列名称
	public abstract String getQueueName();

	// 项目名称
	public String projectName;

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

			// 解析
			this.parse(result, task);

		} catch (Exception e) {
			log.error("task handle error {} , error : {}  ", task, e.getMessage(), e);
			task.updateError();
			return;
		}

		// 更新任务状态
		task.updateSuccess();
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
