package lakenono.db;

import java.sql.SQLException;

import lakenono.db.annotation.DBConstraintPK;
import lakenono.db.annotation.DBField;
import lakenono.db.annotation.DBTable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.junit.Test;

public class DBBeanTest {

	@Test
	public void testCreateTable() throws SQLException {
		DBBean.createTable(DBTestBean.class, "oppo");
	}

	@Test
	public void testSaveOnNotExist() throws IllegalArgumentException, IllegalAccessException, SQLException {
		DBTestBean b = new DBTestBean("oppo");
		b.setId(111);
		b.setUrl("url");
		b.setContent("");

		for (int i = 0; i < 5; i++) {
			b.setUa("ua_" + i);
			b.saveOnNotExist();
		}
	}

	@Test
	public void testSaveOrUpdate() throws IllegalArgumentException, IllegalAccessException, SQLException {
		DBTestBean b = new DBTestBean("oppo");
		b.setId(222);
		b.setUrl("url");
		b.setContent("");

		for (int i = 0; i < 5; i++) {
			b.setUa("ua_" + i);
			b.saveOrUpdate();
		}
	}

	@DBTable(name = "test_table")
	@Data
	@EqualsAndHashCode(callSuper = false)
	static class DBTestBean extends DBBean {

		public DBTestBean(String tableKey) {
			super(tableKey);
		}

		@DBConstraintPK
		@DBField(type = "int")
		private int id;

		@DBConstraintPK
		private String url;

		@DBField(serialization = false)
		private String content;

		private String ua;
	}

}
