package lakenono.task;

import lakenono.db.BaseBean;
import lakenono.db.annotation.DBTable;

@DBTable(name = "lakenono_task")
public class TaskBean extends BaseBean
{
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
