package lakenono.task;

import java.sql.SQLException;

import lakenono.core.GlobalComponents;
import lakenono.db.BaseBean;
import lakenono.db.DB;
import lakenono.log.BaseLog;

public class TaskService extends BaseLog
{
	public void success(String taskname) throws IllegalArgumentException, IllegalAccessException, InstantiationException, SQLException
	{
		TaskBean bean = new TaskBean();
		bean.setTaskname(taskname);
		bean.setTaskstatus("success");
		bean.persist();

		this.log.info(bean.toString());
	}

	public boolean isCompleted(String taskname) throws SQLException
	{
		@SuppressWarnings("unchecked")
		long count = (long) GlobalComponents.db.getRunner().query("select count(*) from " + BaseBean.getTableName(TaskBean.class) + " where taskname=?", DB.scaleHandler, taskname);

		if (count > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
