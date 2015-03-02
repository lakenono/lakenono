package lakenono.db;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;

import lakenono.core.GlobalComponents;
import lakenono.db.annotation.DBField;
import lakenono.db.annotation.DBTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseBean
{
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	// TODO 反射版本的tostring

	public void persist() throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException
	{
		StringBuilder sql = new StringBuilder();

		String tablename = this.getClass().getAnnotation(DBTable.class).name();
		sql.append("insert into `" + tablename + "`(");

		Field[] fields = this.getClass().getDeclaredFields(); // 所有字段
		ArrayList<Object> params = new ArrayList<Object>();// 持久化字段值
		for (Field field : fields)
		{
			// 设置private访问权限
			field.setAccessible(true);

			// 排除掉非序列化字段
			if (field.getAnnotation(DBField.class) != null && !field.getAnnotation(DBField.class).serialization())
			{
				continue;
			}
			sql.append("`" + field.getName() + "`");
			sql.append(",");

			params.add(field.get(this));
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		sql.append(" values(");
		for (int i = 0; i < params.size(); i++)
		{
			sql.append("?,");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(");");

		this.log.info(sql.toString());

		GlobalComponents.db.getRunner().update(sql.toString(), params.toArray());
	}

	public void buildTable() throws SQLException
	{
		StringBuilder sql = new StringBuilder();

		String tablename = this.getClass().getAnnotation(DBTable.class).name();
		sql.append("CREATE TABLE `" + tablename + "` (");

		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields)
		{
			// 设置private访问权限
			field.setAccessible(true);

			// 排除掉非序列化字段
			if (field.getAnnotation(DBField.class) != null && !field.getAnnotation(DBField.class).serialization())
			{
				continue;
			}

			if (field.getAnnotation(DBField.class) != null && !field.getAnnotation(DBField.class).type().equals("varchar"))
			{
				sql.append("`" + field.getName() + "` ").append(field.getAnnotation(DBField.class).type() + " NULL");
			}
			else
			{
				sql.append("`" + field.getName() + "` ").append("varchar(1000) NULL");
			}

			sql.append(", ");
		}
		sql.delete(sql.length() - 2, sql.length());
		sql.append(");");

		this.log.info(sql.toString());

		GlobalComponents.db.getRunner().update(sql.toString());
	}
}
