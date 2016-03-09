package lakenono.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lakenono.core.GlobalComponents;
import lakenono.db.annotation.Column;
import lakenono.db.annotation.DBConstraintPK;
import lakenono.db.annotation.DBField;
import lakenono.db.annotation.DBTable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

/**
 * 数据库访问层
 * 
 * @author shilei
 *
 */
@Slf4j
public class DBBean {

	@Getter
	@Setter
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
	
	public boolean saveOnNotExist(String tableKey) throws IllegalArgumentException, IllegalAccessException, SQLException {
		StringBuilder sql = new StringBuilder();

		String tableName = getTableName(tableKey);
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
	
	private String getTableName(String tableKey) {
		String tableName = this.getClass().getAnnotation(DBTable.class).name();
		if (StringUtils.isNotBlank(tableKey)) {
			tableName = tableName + "_" + tableKey;
		}
		return tableName;
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
		return GlobalComponents.db.getRunner().query("SELECT * FROM " + tablename, new BeanListHandler<T>(c, new BasicRowProcessor()));
	}
	
	/**
	 * 
	 * @param dbBean
	 * @param taskId
	 * @param map
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws SQLException
	 * @author wuhao
	 */
	public static<T> String getSelectColumnDatas(Class<?> dbBean,String taskId,Map<String,Object>  searchMap,Map<String, Object> pageMap) throws SQLException{
		String tableName = getTableName(dbBean) +"_" + taskId;
		//String tableName = getTableName(dbBean);
		
		StringBuilder selectSql = new StringBuilder();
		selectSql.append("select  ");
		
		StringBuilder recordsSql = new StringBuilder();
		recordsSql.append("select  count(*) from ");
		recordsSql.append(tableName);
		
		List<Map<String,String>> columnList = new ArrayList<Map<String,String>>();
		
		Field[] fields = dbBean.getDeclaredFields();// 所有字段
		
		boolean textBool = false;
		boolean titleBool = false;
		
		for (Field field : fields) {
			// 设置private访问权限
			field.setAccessible(true);

			// 排除掉非序列化字段
			if (field.getAnnotation(DBField.class) != null && !field.getAnnotation(DBField.class).serialization()) {
				continue;
			}

			//是否查询字段
			if (field.getAnnotation(Column.class) != null && !field.getAnnotation(Column.class).selectColumn()) {
				continue;
			}

			// 排除静态变量
			int mo = field.getModifiers();
			String priv = Modifier.toString(mo);
			if (StringUtils.contains(priv, "static")) {
				continue;
			}
			
			//获取查询数据
			selectSql.append("`" + field.getName() + "`");
			selectSql.append(",");
			
			Map<String,String> columnMap = new HashMap<String,String>();
			columnMap.put(field.getName(), field.getAnnotation(Column.class).columnAs());
			columnList.add(columnMap);
			
		}
		
		selectSql.deleteCharAt(selectSql.length() - 1);
		selectSql.append(" from " + tableName + "");
		selectSql.append(" where 1 = 1");
		if(searchMap != null){
			if(searchMap.get("text") != null && !"".equals(String.valueOf(searchMap.get("text"))) ){
				selectSql.append(" and case when (select count(*) from information_schema.columns where TABLE_NAME = '" + tableName +"' and column_name = 'title') = 1  then title like '%%' else 1= 1 end");
				selectSql.append("  case when (select count(*) from information_schema.columns where TABLE_NAME = '" + tableName +"' and column_name = 'text') = 1  then or text like '%%' else 1= 1 end");
			}	
		}

		
		if(pageMap != null){
			int limit1 = (Integer.valueOf(String.valueOf(pageMap.get("currentPage"))) - 1)*Integer.valueOf(String.valueOf(pageMap.get("pageSize")));
			int limit2 = Integer.valueOf(String.valueOf(pageMap.get("pageSize")));
			selectSql.append(" limit " + limit1 + ", " + limit2);
		}
		
		Set<String> keys = searchMap.keySet();
		for(String key :keys){
		     System.out.println(key+" "+searchMap.get(key));
		}
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("column", columnList);
		
		
		log.debug("{} query dbbean  >  ", selectSql.toString());
		List<Map<String,Object>> sqllist = (List<Map<String,Object>>)GlobalComponents.db.getRunner().query(selectSql.toString(), new MapListHandler());
		
		Object obj = null;
		obj = GlobalComponents.db.getRunner().query(recordsSql.toString(), new ScalarHandler<Object>());
		String recordCount = String.valueOf(obj);

		pageMap.put("recordCount", recordCount);
		
		param.put("data", sqllist);
		param.put("page", pageMap);
		
		String str = JSON.toJSONString(param);
		
		return str;
	}
	
	public String updateTableData(Class<?> dbBean,String taskId,Map<String,String>  map){
		String tableName = getTableName(dbBean) +"_" + taskId;
		
		String id = "";
		String columnEn = "";
		String columnValue = "";
		
		if(map.get("id") != null && !"".equals(map.get("id")) && map.get("columnEn") != null && !"".equals(map.get("columnEn")) 
			&& map.get("columnValue") != null && !"".equals(map.get("columnValue"))	){
			id = String.valueOf(map.get("id"));
			columnEn = String.valueOf(map.get("id"));
			columnValue = String.valueOf(map.get("id"));
		}else{
			return null;
		}
		
		StringBuilder updateSql = new StringBuilder();
		updateSql.append(" update ");
		updateSql.append( tableName + " set  ");
		updateSql.append(columnEn + " = '" + columnValue + "' ");
		updateSql.append(" where id = '" + id + "'");
		
		int i = 0;
		try {
			i = GlobalComponents.db.getRunner().update(updateSql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return String.valueOf(i);
	}
	
	public String appendTableRemarks(Class<?> dbBean, String taskId, Map<String,String>  map){
		String tableName = getTableName(dbBean) +"_" + taskId;
		/**
		 * @reason 需要对数据 list 做操作！
		 */
		
		
		return null;
	}
	
	
	
}
