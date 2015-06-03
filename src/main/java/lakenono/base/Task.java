package lakenono.base;

import java.sql.SQLException;
import java.util.List;

import lakenono.core.GlobalComponents;
import lakenono.db.BaseBean;
import lakenono.db.DB;
import lakenono.db.annotation.DBConstraintPK;
import lakenono.db.annotation.DBTable;
import lakenono.task.FetchTask;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.dbutils.handlers.BeanListHandler;

/**
 * 任务实体类 
 * @author Lakenono
 */
@DBTable(name = "lakenono_lolth_task")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
@Slf4j
public class Task extends BaseBean
{
	public static final String TODO = "todo";
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final String ERRORPAGE = "errorpage";

	// 总任务名 - 根据不同的任务变化 例如: "惠氏" "雪佛兰"
	@DBConstraintPK
	protected String projectName;

	// 队列名 
	@DBConstraintPK
	protected String queueName;

	// 爬取地址
	@DBConstraintPK
	protected String url;

	// 提供给爬虫节点的附加信息
	protected String extra = "";

	// 任务状态
	protected String status = TODO;

	// 重试次数
	protected int retry = 0;

	// 优先级
	protected int Priority = 0;

	public void updateSuccess()
	{
		updateStatus(SUCCESS);
	}

	public void updateError()
	{
		updateStatus(ERROR);
	}

	public void updateErrorPage()
	{
		updateStatus(ERRORPAGE);
	}

	/**
	 * 查看任务是否完成
	 * 
	 * @return
	 * @throws SQLException
	 */
	public boolean hasCompleted() throws SQLException
	{
		@SuppressWarnings("unchecked")
		long count = (long) GlobalComponents.db.getRunner().query("select count(*) from " + BaseBean.getTableName(FetchTask.class) + " where projectName=? and url=? and queueName=? and status!=? ", DB.scaleHandler, this.projectName, this.url, this.queueName, TODO);
		if (count > 0)
		{
			log.debug("{} is completed..", this);
			return true;
		}
		else
		{
			log.debug("{} is not completed..", this);
			return false;
		}
	}

	public static void main(String[] args) throws Exception
	{
		new FetchTask().buildTable();
	}

	// TODO 增加重试

	/**
	 * 更新状态
	 * 
	 * @param status
	 * @throws SQLException
	 */
	public void updateStatus(String status)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(BaseBean.getTableName(Task.class)).append(' ');
		sql.append("SET status=? WHERE projectName=? and url=? and queueName=?");

		int records;
		try
		{
			records = GlobalComponents.db.getRunner().update(sql.toString(), status, this.projectName, this.url, this.queueName);
			log.debug("task {} is updateStatue {} records ", this, records);
		}
		catch (SQLException e)
		{
			log.error("{}", e);
		}
	}
	
	/**
	 * 数据库里面删除抓取日志纪录
	 * @param projectName 总任务名
	 * @param queueName 队列名
	 * @throws SQLException 
	 */
	public static int deleteLog(String projectName,String queueName) throws SQLException{
		log.debug("delete task projectName={},queueName={}", projectName, queueName);
		int delCount = GlobalComponents.db.getRunner().update("DELETE FROM " + BaseBean.getTableName(Task.class) + " WHERE projectName=? and queueName=?", projectName, queueName);
		log.debug("delete task queueName={},queueName={} finish ，delete count ：{} ", queueName, queueName, delCount);
		return delCount;
	}
	
	/**
	 * 获取抓取日志莫个状态的集合数据
	 * @param projectName 总任务名
	 * @param queueName 队列名
	 * @param status 任务状态
	 * @return 返回集合
	 * @throws SQLException 
	 */
	public static List<Task> getTaks(String projectName,String queueName,String status) throws SQLException{
		return GlobalComponents.db.getRunner().query("select * from " + BaseBean.getTableName(Task.class) + " where projectName=? and queueName=? and status=? ", new BeanListHandler<>(Task.class), projectName, queueName, status);
	}

}
