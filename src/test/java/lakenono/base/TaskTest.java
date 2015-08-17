package lakenono.base;

import java.util.List;

import lakenono.db.DBBean;

import org.junit.Assert;
import org.junit.Test;

public class TaskTest {

	@Test
	public void test() throws Exception {
		Task task = new Task();

		Assert.assertTrue("fail", task.inDateRange("2015-03-01"));

		task.parseStartDate("2015-01-01");
		task.parseEndDate("2015-07-01");

		Assert.assertTrue("fail", task.inDateRange("2015-03-01"));
		Assert.assertTrue("fail", !task.inDateRange("2014-03-01"));
		Assert.assertTrue("fail", !task.inDateRange("2016-03-01"));

		task.setStartDate(null);
		task.parseEndDate("2015-07-01");

		Assert.assertTrue("fail", task.inDateRange("2015-03-01"));
		Assert.assertTrue("fail", task.inDateRange("2014-03-01"));
		Assert.assertTrue("fail", !task.inDateRange("2016-03-01"));

		task.parseStartDate("2015-01-01");
		task.setEndDate(null);

		Assert.assertTrue("fail", task.inDateRange("2015-03-01"));
		Assert.assertTrue("fail", !task.inDateRange("2014-03-01"));
		Assert.assertTrue("fail", task.inDateRange("2016-03-01"));
	}

	@Test
	public void testDB() throws Exception {
		Task task = new Task();
		task.setProjectName("test");
		task.setQueueName("test");
		task.setUrl("url");
		task.parseStartDate("2015-01-01");
		task.parseEndDate("2016-01-01");

		task.saveOrUpdate();

		List<Task> tasks = DBBean.getAll(Task.class);
		for (Task t : tasks) {
			System.out.println(t);
		}

	}

}
