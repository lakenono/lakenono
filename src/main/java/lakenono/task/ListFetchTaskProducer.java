package lakenono.task;

import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ListFetchTaskProducer<A> extends FetchTaskProducer {
	@Setter
	protected long waitTaskTime = 30 * 60 * 1000;
	
	@Setter
	protected long batchSleep = 15 * 60 *1000;

	public ListFetchTaskProducer(String taskQueueName) {
		super(taskQueueName);
	}

	public void run() throws Exception {
		while (true) {
			try {
				//TODO 当消费者处理速度赶不上生产者推送速度，二次循环容易产生堆积
				List<A> tasks = getTaskArgs();

				if (tasks == null || tasks.isEmpty()) {
					log.debug("wait for push task");
					Thread.sleep(waitTaskTime);
					continue;
				}

				for (A a : tasks) {
					try {
						FetchTask task = buildTask(a);
						saveAndPushTask(task);
					} catch (Exception e) {
						log.error("{}|{} push error : ", a, e);
					}
				}
				
				//休息一段时间，等待客户端处理
				log.debug("wait for client handler");
				Thread.sleep(batchSleep);
			} catch (Exception e) {
				log.error("Get task args error! ",e);
			}
		}

	}

	/**
	 * 创建Task
	 * 
	 * @param t
	 * @return
	 */
	protected abstract FetchTask buildTask(A a);

	/**
	 * 获得task参数
	 * 
	 * @return
	 */
	protected abstract List<A> getTaskArgs() throws Exception;

}
