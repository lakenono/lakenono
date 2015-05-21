package lakenono.base;

/**
 * 集群parser的父类
 * 
 * 默认使用jsoup fetch
 * 默认不使用cookie
 * 
 * @author Lakenono
 */
public abstract class BaseParser
{
	public String fetchType = "default";
	public String cookie = "no";

	public static String FETCH_TYPE_DEFAULT = "default";
	public static String FETCH_TYPE_DYNAMIC = "dynamic";

	public abstract void run();

	public abstract void parse(String result, Task task) throws Exception;

	// 处理url正常页面无数据 例如: "该贴以删除."
	protected boolean errorPage(String result)
	{
		return false;
	}

	protected void useDynamicFetch()
	{
		this.fetchType = FETCH_TYPE_DYNAMIC;
	}

	protected void useCookie()
	{
		this.cookie = "yes";
	}
}
