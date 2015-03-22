package lakenono.core;

import redis.clients.jedis.Jedis;
import lakenono.db.DB;
import lakenono.fetch.DynamicFetch;
import lakenono.fetch.JsoupFetch;
import lakenono.task.TaskService;

/**
 * @author hu.xinlei
 *
 */
public class GlobalComponents
{

	public static DB db = new DB();

	public static JsoupFetch fetcher = new JsoupFetch();

	public static DynamicFetch dynamicFetch = new DynamicFetch();

	public static TaskService taskService = new TaskService();

	public static Jedis jedis = new Jedis("115.29.110.62", 8080);

}
