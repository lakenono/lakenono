package lakenono.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lakenono.core.GlobalComponents;
import lakenono.db.annotation.DBConstraintPK;
import lakenono.db.annotation.DBField;
import lakenono.db.annotation.DBTable;
import lakenono.log.BaseLog;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

public class BaseBean extends BaseLog {

	public static String getTableName(Class<?> c) {
		return c.getAnnotation(DBTable.class).name();
	}

	/**
	 * "@DBConstraintPK" 修饰列对应的记录在数据库是否存在
	 * 
	 * @return true：记录在数据库中存在，false 不存在
	 */
	public boolean exist() throws IllegalArgumentException,
			IllegalAccessException, SQLException {
		StringBuilder sql = new StringBuilder();

		String tablename = this.getClass().getAnnotation(DBTable.class).name();
		sql.append("SELECT COUNT(1) FROM ").append(tablename);
		sql.append(" WHERE ");

		Field[] fields = this.getClass().getDeclaredFields(); // 所有字段
		List<Object> pkFields = new ArrayList<>();// 持久化字段值

		for (Field field : fields) {
			// 设置private访问权限
			field.setAccessible(true);

			// 添加PK 字段
			if (field.getAnnotation(DBConstraintPK.class) != null) {
				sql.append(field.getName()).append('=').append('?')
						.append(" and ");
				pkFields.add(field.get(this));
			}
		}

		// 未定义逐渐
		if (pkFields.size() == 0) {
			this.log.debug("{} pk not defined ! ", this);
			return false;
		}

		sql.delete(sql.length() - " and ".length(), sql.length());

		@SuppressWarnings("unchecked")
		long count = (long) GlobalComponents.db.getRunner().query(
				sql.toString(), DB.scaleHandler, pkFields.toArray());
		if (count > 0) {
			this.log.debug("{} has been exist!", this);
			return true;
		} else {
			this.log.debug("{} not exist!", this);
			return false;
		}
	}

	/**
	 * "@DBConstraintPK" 修饰列对应的记录在数据库不错存在，则执行插入操作
	 * 
	 * @return true：插入记录，false：未插入记录
	 */
	public boolean persistOnNotExist() throws IllegalArgumentException,
			IllegalAccessException, SQLException, InstantiationException {
		if (exist()) {
			return false;
		}
		persist();
		return true;
	}

	/**
	 * 持久化
	 * 
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 * @throws InstantiationException
	 */
	public void persist() throws IllegalArgumentException,
			IllegalAccessException, SQLException, InstantiationException {
		StringBuilder sql = new StringBuilder();

		String tablename = this.getClass().getAnnotation(DBTable.class).name();
		sql.append("insert into `" + tablename + "`(");

		Field[] fields = this.getClass().getDeclaredFields(); // 所有字段
		ArrayList<Object> params = new ArrayList<Object>();// 持久化字段值
		for (Field field : fields) {
			// 设置private访问权限
			field.setAccessible(true);

			// 排除掉非序列化字段
			if (field.getAnnotation(DBField.class) != null
					&& !field.getAnnotation(DBField.class).serialization()) {
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

			// value
			params.add(serializeFiledValue(field));
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
		GlobalComponents.db.getRunner()
				.update(sql.toString(), params.toArray());
	}

	/**
	 * 序列化字段
	 * 
	 * @param field
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	protected Object serializeFiledValue(Field field)
			throws IllegalArgumentException, IllegalAccessException {
		if (field.getType() == Map.class) {
			return JSON.toJSONString(field.get(this));
		}
		return field.get(this);
	}

	/**
	 * 获得所有数据
	 * 
	 * @param c
	 * @return
	 * @throws SQLException
	 */
	public static <T> List<T> getAll(Class<T> c) throws SQLException {
		String tablename = getTableName(c);
		return GlobalComponents.db.getRunner().query(
				"SELECT * FROM " + tablename,
				new BeanListHandler<T>(c, new BasicRowProcessor(
						new CustomBeanProcesser())));
	}

	static class CustomBeanProcesser extends BeanProcessor {

		@Override
		protected Object processColumn(ResultSet rs, int index,
				Class<?> propType) throws SQLException {
			if (propType == Map.class) {
				return JSON.parseObject(rs.getString(index), Map.class);
			}
			return super.processColumn(rs, index, propType);
		}

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
		List<String> pk = new ArrayList<String>();

		for (Field field : fields) {
			// 设置private访问权限
			field.setAccessible(true);

			// 排除掉非序列化字段
			if (field.getAnnotation(DBField.class) != null
					&& !field.getAnnotation(DBField.class).serialization()) {
				continue;
			}

			// 排除静态变量
			int mo = field.getModifiers();
			String priv = Modifier.toString(mo);
			if (StringUtils.contains(priv, "static")) {
				continue;
			}

			if (field.getAnnotation(DBField.class) != null
					&& !field.getAnnotation(DBField.class).type()
							.equals("varchar")) {
				sql.append("`" + field.getName() + "` ").append(
						field.getAnnotation(DBField.class).type() + " NULL");
			} else {
				sql.append("`" + field.getName() + "` ").append(
						"varchar(200) NULL");
			}
			// 主键字段
			if (field.getAnnotation(DBConstraintPK.class) != null) {
				pk.add(field.getName());
			}

			sql.append(", ");
		}
		sql.delete(sql.length() - 2, sql.length());
		// 添加索引
		if (pk.size() > 0) {
			sql.append(" , INDEX ");
			StringBuilder index_name = new StringBuilder();
			for (String name : pk) {
				index_name.append(name).append("_");
			}
			index_name.deleteCharAt(index_name.length() - 1);
			sql.append("`" + index_name + "`").append(" (");
			for (String field : pk) {
				// 限定索引长度
//				sql.append(field).append("(200) ,");
//				如果是url字段，索引长度设置200，其他字段索引长度10
				if(field.equals("url")){
					sql.append(field).append("(200) ,");
				}else{
					sql.append(field).append("(10) ,");
				}
			}
			sql.deleteCharAt(sql.length() - 1);
			// 更新表时，延迟更新索引
			sql.append(")) DELAY_KEY_WRITE= 1;");
		} else {
			sql.append(");");
		}
		log.debug(sql.toString());
		GlobalComponents.db.getRunner().update(sql.toString());
	}

	public static class UUID {
		public static String generateId() {
			return java.util.UUID.randomUUID().toString();
		}
	}
}
