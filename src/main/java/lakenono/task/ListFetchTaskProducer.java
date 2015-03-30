package lakenono.task;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ListFetchTaskProducer<A> extends FetchTaskProducer {

	public ListFetchTaskProducer(String taskQueueName) {
		super(taskQueueName);
	}

	public void run() throws Exception {
		cleanAllTask();
		
		List<A> tasks = getTaskArgs();
		
		for (A a : tasks) {
			try {
				FetchTask task = buildTask(a);
				saveAndPushTask(task);
			} catch (Exception e) {
				log.error("{}|{} push error : ", a, e);
			}
		}
	}

	/**
	 * 创建Task
	 * @param t
	 * @return
	 */
	protected abstract FetchTask buildTask(A a);

	/**
	 * 获得task参数
	 * @return
	 */
	protected abstract List<A> getTaskArgs() throws Exception;

}
