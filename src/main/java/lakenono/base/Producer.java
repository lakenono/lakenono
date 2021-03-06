package lakenono.base;

import lombok.extern.slf4j.Slf4j;

/**
 * 单机.
 * 作为任务入口
 * 列表页任务生成器,定义分页处理模板，推送任务
 * 
 * @author lakenono
 */
@Slf4j
public abstract class Producer implements Runnable
{
	public static final int FIRST_PAGE = 1;

	private String classname = this.getClass().getName();

	// 队列名称
	public abstract String getQueueName();

	// 项目名称
	public String projectName;

	public Producer(String projectName)
	{
		super();
		this.projectName = projectName;
	}

	@Override
	public void run() 
	{
		try {
			log.info("{} Producer start ...", this.classname);

			//1 解析获得最大页
			int pagenum = this.parse();

			log.info("{} Get max page : {}", this.classname, pagenum);

			// 2 迭代创建任务
			for (int i = FIRST_PAGE; i <= pagenum; i++)
			{
				try {
					// 2.1 创建url
					String url = buildUrl(i);

					// 创建抓取任务
					Task task = buildTask(url);

					// 推送任务
					Queue.push(task);
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			log.error("get maxpage error : {}",e.getMessage(),e);
		}
	}

	/**
	 * 获取最大页数
	 * 
	 * @return 0：获取失败，1:1页；n：n页
	 */
	protected abstract int parse() throws Exception;

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
	protected Task buildTask(String url)
	{
		Task task = new Task();
		task.setProjectName(this.projectName);
		task.setQueueName(this.getQueueName());
		task.setUrl(url);

		return task;
	}
}
