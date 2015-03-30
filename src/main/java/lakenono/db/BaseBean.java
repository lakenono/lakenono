package lakenono.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lakenono.core.GlobalComponents;
import lakenono.db.annotation.DBConstraintPK;
import lakenono.db.annotation.DBField;
import lakenono.db.annotation.DBTable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseBean {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	// TODO 反射版本的tostring

	// TODO varchar增加长度功能

	public static String getTableName(Class<?> c) {
		return c.getAnnotation(DBTable.class).name();
	}

	/**
	 *  "@DBConstraintPK" 修饰列对应的记录在数据库是否存在
	 * 
	 * @return true：记录在数据库中存在，false 不存在
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 */
	public boolean exist() throws IllegalArgumentException, IllegalAccessException, SQLException {
		StringBuilder sql = new StringBuilder();

		String tablename = this.getClass().getAnnotation(DBTable.class).name();
		sql.append("SELECT COUNT(*) FROM ").append(tablename);
		sql.append(" WHERE ");

		Field[] fields = this.getClass().getDeclaredFields(); // 所有字段
		List<Object> pkFields = new ArrayList<>();// 持久化字段值

		for (Field field : fields) {
			// 设置private访问权限
			field.setAccessible(true);

			// 添加PK 字段
			if (field.getAnnotation(DBConstraintPK.class) != null) {
				sql.append(field.getName()).append('=').append('?').append(" and ");
				pkFields.add(field.get(this));
			}
		}

		// 未定义逐渐
		if (pkFields.size() == 0) {
			log.debug("{} pk not defined ! ", this);
			return false;
		}
		
		sql.delete(sql.length()-" and ".length(), sql.length());

		@SuppressWarnings("unchecked")
		long count = (long) GlobalComponents.db.getRunner().query(sql.toString(), DB.scaleHandler, pkFields.toArray());
		if (count > 0) {
			log.debug("{} has bean exist!", this);
			return true;
		} else {
			log.debug("{} not exist!", this);
			return false;
		}
	}

	/**
	 * "@DBConstraintPK" 修饰列对应的记录在数据库不错存在，则执行插入操作
	 * 
	 * @return true：插入记录，false：未插入记录
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 * @throws InstantiationException
	 */
	public boolean persistOnNotExist() throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException {
		if (exist()) {
			return false;
		}
		persist();
		return true;
	}

	public void persist() throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException {
		StringBuilder sql = new StringBuilder();

		String tablename = this.getClass().getAnnotation(DBTable.class).name();
		sql.append("insert into `" + tablename + "`(");

		Field[] fields = this.getClass().getDeclaredFields(); // 所有字段
		ArrayList<Object> params = new ArrayList<Object>();// 持久化字段值
		for (Field field : fields) {
			// 设置private访问权限
			field.setAccessible(true);

			// 排除掉非序列化字段
			if (field.getAnnotation(DBField.class) != null && !field.getAnnotation(DBField.class).serialization()) {
				continue;
			}

			// 排除静态变量
			int mo = field.getModifiers();
			String priv = Modifier.toString(mo);
			if (StringUtils.contains(priv, "static")) {
				continue;
			}

			sql.append("`" + field.getName() + "`");
			sql.append(",");

			params.add(field.get(this));
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		sql.append(" values(");
		for (int i = 0; i < params.size(); i++) {
			sql.append("?,");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(");");

		this.log.debug(sql.toString());

		log.info("{} persist .", this);
		GlobalComponents.db.getRunner().update(sql.toString(), params.toArray());
	}

	public void buildTable() throws SQLException {
		String tablename = this.getClass().getAnnotation(DBTable.class).name();

		// drop table
		String dropSql = "DROP TABLE IF EXISTS " + tablename;
		this.log.info(dropSql);
		GlobalComponents.db.getRunner().update(dropSql);

		// create table
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE `" + tablename + "` (");

		Field[] fields = this.getClass().getDeclaredFields();

		for (Field field : fields) {
			// 设置private访问权限
			field.setAccessible(true);

			// 排除掉非序列化字段
			if (field.getAnnotation(DBField.class) != null && !field.getAnnotation(DBField.class).serialization()) {
				continue;
			}

			// 排除静态变量
			int mo = field.getModifiers();
			String priv = Modifier.toString(mo);
			if (StringUtils.contains(priv, "static")) {
				continue;
			}

			if (field.getAnnotation(DBField.class) != null && !field.getAnnotation(DBField.class).type().equals("varchar")) {
				sql.append("`" + field.getName() + "` ").append(field.getAnnotation(DBField.class).type() + " NULL");
			} else {
				sql.append("`" + field.getName() + "` ").append("varchar(200) NULL");
			}

			sql.append(", ");
		}
		sql.delete(sql.length() - 2, sql.length());
		sql.append(");");

		log.debug(sql.toString());

		GlobalComponents.db.getRunner().update(sql.toString());
	}
}
