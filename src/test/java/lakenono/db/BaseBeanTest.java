package lakenono.db;

import java.sql.SQLException;
import java.util.Date;

import lakenono.core.GlobalComponents;
import lakenono.db.annotation.DBField;
import lakenono.db.annotation.DBTable;

@DBTable(name = "test_base_bean")
public class BaseBeanTest extends BaseBean
{
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException
	{
		GlobalComponents.db.getRunner().update("drop table test_base_bean");
		new BaseBeanTest().buildTable();

		for (int i = 0; i < 5; i++)
		{
			BaseBeanTest bean = new BaseBeanTest();
			bean.setUsername("u" + i);
			bean.setPassword("p" + i);
			bean.setCreateTime(new Date());
			bean.setText("t" + i);
			bean.setAge(i);
			bean.setStatus(i + "");
			bean.persist();
		}

	}

	private String username;
	private String password;

	@DBField(type = "datetime")
	private Date createTime;

	@DBField(type = "Text")
	private String text;

	@DBField(type = "int")
	private int age;

	@DBField(serialization = false)
	private String status;

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public Date getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Date createTime)
	{
		this.createTime = createTime;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public int getAge()
	{
		return age;
	}

	public void setAge(int age)
	{
		this.age = age;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
}
