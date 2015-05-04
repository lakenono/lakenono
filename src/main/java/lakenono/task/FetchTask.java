package lakenono.task;

import java.sql.SQLException;
import java.util.List;

import lakenono.core.GlobalComponents;
import lakenono.db.BaseBean;
import lakenono.db.DB;
import lakenono.db.annotation.DBConstraintPK;
import lakenono.db.annotation.DBTable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.dbutils.handlers.BeanListHandler;

@DBTable(name = "lakenono_fetch_task")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
@Slf4j
public class FetchTask extends BaseBean {
	public static final String STATUS_TODO = "todo";
	public static final String STATUS_SUCCESS = "success";
	public static final String STATUS_ERROR = "error";

	// 总任务名
	@DBConstraintPK
	protected String name;
	// 爬取地址
	@DBConstraintPK
	protected String url;
	// 批次名
	@DBConstraintPK
	protected String batchName;

	protected String status = STATUS_TODO;
	// 提供给爬虫节点的附加信息
	protected String extra;

	public void updateSuccess() throws SQLException {
		updateStatus(STATUS_SUCCESS);
	}

	public void updateError() throws SQLException {
		updateStatus(STATUS_ERROR);
	}

	private static List<FetchTask> getStatusTasks(String name, String batchName, String status) throws SQLException {
		List<FetchTask> tasks = GlobalComponents.db.getRunner().query("select * from " + BaseBean.getTableName(FetchTask.class) + " where name=? and batchName=? and status=? ", new BeanListHandler<>(FetchTask.class), name, batchName, status);
		return tasks;
	}

	public static List<FetchTask> getTodoTasks(String name, String batchName) throws SQLException {
		return getStatusTasks(name, batchName, STATUS_TODO);
	}

	public static List<FetchTask> getErrorTasks(String name, String batchName) throws SQLException {
		return getStatusTasks(name, batchName, STATUS_ERROR);
	}

	/**
	 * 更新状态
	 * 
	 * @param status
	 * @throws SQLException
	 */
	public void updateStatus(String status) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(BaseBean.getTableName(FetchTask.class)).append(' ');
		sql.append("SET status=? WHERE name=? and url=? and batchName=?");

		GlobalComponents.db.getRunner().update(sql.toString(), status, this.name, this.url, this.batchName);

		log.debug("task {} is updateStatue ..", this);
	}

	/**
	 * 查看任务是否完成
	 * 
	 * @return
	 * @throws SQLException
	 */
	public boolean hasCompleted() throws SQLException {
		@SuppressWarnings("unchecked")
		long count = (long) GlobalComponents.db.getRunner().query("select count(*) from " + BaseBean.getTableName(FetchTask.class) + " where name=? and url=? and batchName=? and status!=? ", DB.scaleHandler, this.name, this.url, this.batchName, STATUS_TODO);
		if (count > 0) {
			log.debug("{} is completed..", this);
			return true;
		} else {
			log.debug("{} is not completed..", this);
			return false;
		}
	}

	/**
	 * 删除任务队里
	 * 
	 * @param name
	 * @param batchName
	 * @throws SQLException
	 */
	public static int cleanAllTask(String name, String batchName) throws SQLException {
		log.debug("delete fetch task name={},batch={}", name, batchName);
		int delCount = GlobalComponents.db.getRunner().update("DELETE FROM " + BaseBean.getTableName(FetchTask.class) + " WHERE name=? and batchName=?", name, batchName);
		log.debug("delete fetch task name={},batch={} finish ，delete count ：{} ", name, batchName, delCount);
		return delCount;
	}

	public static void main(String[] args) throws Exception {
		FetchTask task = new FetchTask();
		task.setName("testname");
		task.setBatchName("testBatchName");
		task.setUrl("url");
		task.setStatus("todo");
		task.setExtra("extra");

		task.persistOnNotExist();

		int delCount = FetchTask.cleanAllTask(task.getName(), task.getBatchName());
		System.out.println(delCount);

	}
}
