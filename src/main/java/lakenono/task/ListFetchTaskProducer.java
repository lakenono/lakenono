package lakenono.task;

import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ListFetchTaskProducer<A> extends FetchTaskProducer {
	@Setter
	protected long waitTaskTime = 30 * 60 * 1000;

	public ListFetchTaskProducer(String taskQueueName) {
		super(taskQueueName);
	}

	public void run() throws Exception {
		cleanAllTask();

		while (true) {
			try {
				List<A> tasks = getTaskArgs();

				if (tasks == null || tasks.isEmpty()) {
					Thread.sleep(waitTaskTime);
					log.debug("wait for push task");
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
