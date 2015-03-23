package lakenono.task;

import java.sql.SQLException;
import java.text.MessageFormat;

import lakenono.core.GlobalComponents;
import lakenono.db.BaseBean;
import lakenono.db.DB;
import lakenono.log.BaseLog;

/**
 * 
 * 	String taskname = MessageFormat.format("weixin-{0}-{1}-{2}", this.keyword, maxPage, i + 1);
 *
 *	if (GlobalComponents.taskService.isCompleted(taskname))
 *	{
 *		this.log.info("task {} is completed", taskname);
 *		continue;
 *	}
 * 
 *执行逻辑
 * 
 * GlobalComponents.taskService.success(taskname);
 * 
 * 
 * @author hu.xinlei
 *
 */
public class TaskService extends BaseLog
{
	public void success(String taskname) throws IllegalArgumentException, IllegalAccessException, InstantiationException, SQLException
	{
		TaskBean bean = new TaskBean();
		bean.setTaskname(taskname);
		bean.setTaskstatus("success");
		bean.persist();

		this.log.info("task {} success", taskname);
	}

	public boolean isCompleted(String taskname) throws SQLException
	{
		@SuppressWarnings("unchecked")
		long count = (long) GlobalComponents.db.getRunner().query("select count(*) from " + BaseBean.getTableName(TaskBean.class) + " where taskname=?", DB.scaleHandler, taskname);

		if (count > 0)
		{
			this.log.info("task {} is completed..", taskname);
			return true;
		}
		else
		{
			this.log.info("task {} is not completed..", taskname);
			return false;
		}
	}
}
