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
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

/**
 * 数据库访问层
 * 
 * @author shilei
 *
 */
@Slf4j
public class DBBean {

	protected String tableKey = "";

	public DBBean() {

	}

	public DBBean(String tableKey) {
		this.tableKey = tableKey;
	}

	/**
	 * 建表
	 * 
	 * @param dbBean
	 * @throws SQLException
	 */
	public static void createTable(Class<? extends DBBean> dbBean) throws SQLException {
		createTable(dbBean, "");
	}

	/**
	 * 建表
	 * 
	 * @param dbBean
	 * @throws SQLException
	 */
	public static void createTable(Class<? extends DBBean> dbBean, String tableKey) throws SQLException {
		String tableName = dbBean.getAnnotation(DBTable.class).name();
		if (StringUtils.isBlank(tableName)) {
			throw new NullPointerException("table name is null!");
		}

		if (StringUtils.isNotBlank(tableKey)) {
			tableName = tableName + "_" + tableKey;
		}

		// 删除表
		String dropSql = "DROP TABLE IF EXISTS " + tableName;
		log.info("{}", dropSql);

		GlobalComponents.db.getRunner().update(dropSql);
		log.info("Drop finish! ");

		// 创建表
		StringBuilder createSql = new StringBuilder();
		createSql.append("CREATE TABLE `").append(tableName).append("`(");

		Field[] fields = dbBean.getDeclaredFields();
		// 主键缓存
		List<String> pks = new ArrayList<String>();

		for (Field field : fields) {
			// 设置private访问权限
			field.setAccessible(true);

			// 主键操作
			if (field.getAnnotation(DBConstraintPK.class) != null) {
				pks.add(field.getName());
			}

			// 排除静态变量
			int mo = field.getModifiers();
			String priv = Modifier.toString(mo);
			if (StringUtils.contains(priv, "static")) {
				continue;
			}

			// 排除掉非序列化字段
			if (field.getAnnotation(DBField.class) != null && !field.getAnnotation(DBField.class).serialization()) {
				continue;
			}

			createSql.append('`').append(field.getName()).append('`').append(' ');

			// 对默认字段进行默认类型配置
			if (field.getAnnotation(DBField.class) == null) {
				createSql.append("varchar(200) NOT NULL DEFAULT ''");
			} else {
				createSql.append(field.getAnnotation(DBField.class).type());
			}
			createSql.append(',');
		}

		// 主键操作
		if (!pks.isEmpty()) {
			createSql.append("PRIMARY KEY (");
			for (String pkField : pks) {
				createSql.append(pkField).append(',');
			}
			createSql.deleteCharAt(createSql.length() - 1);
			createSql.append(')');
		} else {
			createSql.deleteCharAt(createSql.length() - 1);
		}

		createSql.append(')').append(' ');

		// 引擎设置
		createSql.append("ENGINE=InnoDB");

		//
		log.info("{}", createSql.toString());
		GlobalComponents.db.getRunner().update(createSql.toString());
		log.info("Create finish!");
	}

	/**
	 * 获取表名
	 * 
	 * @param c
	 * @return
	 */
	public static String getTableName(Class<?> c) {
		return c.getAnnotation(DBTable.class).name();
	}

	/**
	 * 获取表名
	 * 
	 * @param c
	 * @param tableKey
	 * @return
	 */
	public static String getTableName(Class<?> c, String tableKey) {
		return c.getAnnotation(DBTable.class).name() + "_" + tableKey;
	}

	/**
	 * 插入数据，主键重复时更新
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void saveOrUpdate() throws SQLException, IllegalArgumentException, IllegalAccessException {
		StringBuilder sql = new StringBuilder();

		String tableName = getTableName();
		sql.append("REPLACE INTO `" + tableName + "`(");

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

			// value
			params.add(field.get(this));
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		sql.append(" VALUES(");
		for (int i = 0; i < params.size(); i++) {
			sql.append("?,");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(");");

		log.debug("{}", sql.toString());

		GlobalComponents.db.getRunner().update(sql.toString(), params.toArray());

		log.debug("{} update finish ! ", this);

	}

	/**
	 * 插入，主键重复时忽略
	 * 
	 * @return true 插入完成；false 记录已存在
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 */
	public boolean saveOnNotExist() throws IllegalArgumentException, IllegalAccessException, SQLException {
		StringBuilder sql = new StringBuilder();

		String tableName = getTableName();
		sql.append("INSERT IGNORE INTO `" + tableName + "`(");

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

			// value
			params.add(field.get(this));
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(")");

		sql.append(" VALUES(");
		for (int i = 0; i < params.size(); i++) {
			sql.append("?,");
		}
		sql.deleteCharAt(sql.length() - 1);
		sql.append(");");

		log.debug("{}", sql.toString());

		int result = GlobalComponents.db.getRunner().update(sql.toString(), params.toArray());

		if (result > 0) {
			log.debug("{} insert finish ! ", this);
			return true;
		} else {
			log.debug("{} has bean exist ! ", this);
			return false;
		}
	}

	private String getTableName() {
		String tableName = this.getClass().getAnnotation(DBTable.class).name();
		if (StringUtils.isNotBlank(tableKey)) {
			tableName = tableName + "_" + tableKey;
		}
		return tableName;
	}

}
