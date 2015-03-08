package lakenono.task;

import java.sql.SQLException;

import lakenono.db.BaseBean;
import lakenono.db.annotation.DBTable;

@DBTable(name = "lakenono_task")
public class TaskBean extends BaseBean
{
	public static void main(String[] args) throws SQLException
	{
		new TaskBean().buildTable();
	}

	@Override
	public String toString()
	{
		return "TaskBean [taskname=" + taskname + ", taskstatus=" + taskstatus + "]";
	}

	private String taskname;
	private String taskstatus;

	public String getTaskname()
	{
		return taskname;
	}

	public void setTaskname(String taskname)
	{
		this.taskname = taskname;
	}

	public String getTaskstatus()
	{
		return taskstatus;
	}

	public void setTaskstatus(String taskstatus)
	{
		this.taskstatus = taskstatus;
	}

}
