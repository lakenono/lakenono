package lakenono.core;

import lakenono.auth.AuthService;
import lakenono.auth.AuthServiceClient;
import lakenono.db.DB;
import lakenono.fetch.DynamicFetch;
import lakenono.fetch.JsoupFetch;
import lakenono.fetch.adv.HttpFetcher;
import lakenono.fetch.adv.httpclient.HttpClientFetcher;
import lakenono.fetcher.JSoupFetcher;
import lakenono.fetcher.SeleniumFetcher;
import lakenono.task.TaskService;
import lakenono.util.RedisAPI;
import lakenono.util.RegexURLFilter;
import redis.clients.jedis.Jedis;

/**
 * @author hu.xinlei
 *
 */
public class GlobalComponents {

	public static DB db = new DB();

	public static JsoupFetch fetcher = new JsoupFetch();

	public static HttpFetcher jsonFetch = new HttpClientFetcher();

	public static DynamicFetch dynamicFetch = new DynamicFetch();

	public static TaskService taskService = new TaskService();

	public static Jedis jedis = new Jedis("115.29.110.62", 8080);

	public static RedisAPI redisAPI = new RedisAPI();
	
	
	
	//new 
	public static JSoupFetcher jsoupFetcher = new JSoupFetcher();
	
	public static SeleniumFetcher seleniumFetcher = new SeleniumFetcher();
	
	public static AuthService.Iface authService = new AuthServiceClient("115.28.72.146",8080);
	
	public static RegexURLFilter urlFilter = new RegexURLFilter();
}
