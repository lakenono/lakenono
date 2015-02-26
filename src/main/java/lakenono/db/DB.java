package lakenono.db;


import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

/**
 *
 * @author lakenono
 */
public final class DB
{

	private QueryRunner runner;
	private DataSource ds;

	@SuppressWarnings("rawtypes")
	public final static ScalarHandler scaleHandler = new ScalarHandler()
	{
		@Override
		public Object handle(ResultSet rs) throws SQLException
		{
			Object obj = super.handle(rs);
			if (obj instanceof BigInteger)
			{
				return ((BigInteger) obj).longValue();
			}
			return obj;
		}
	};

	public DB()
	{
		try
		{
			this.init();
		}
		catch (Exception ex)
		{
			Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void init() throws IOException, Exception
	{
		// 加载配置文件
		Properties p = new Properties();
//		p.load(new FileInputStream(this.getClass().getClassLoader().getResource("dev.properties").getPath()));
		p.load(new FileInputStream(this.getClass().getClassLoader().getResource("aliyun.properties").getPath()));

		// 构造queryRunner
		this.ds = BasicDataSourceFactory.createDataSource(p);
		this.runner = new QueryRunner(ds);
	}

	public void destory()
	{
		//todo
	}

	public QueryRunner getRunner()
	{
		return runner;
	}

}
