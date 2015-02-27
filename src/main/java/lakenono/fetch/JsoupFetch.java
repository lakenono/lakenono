package lakenono.fetch;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupFetch
{
	private final Logger log = LoggerFactory.getLogger(JsoupFetch.class);

	private final int retry = 3;

	public String fetch(String url) throws IOException, InterruptedException
	{
		log.debug("fetch - {}", url);

		for (int i = 1; i <= retry; i++)
		{ 
			try
			{
				return this.connect(url);
			}
			catch (java.net.SocketTimeoutException e)
			{
				log.error("SocketTimeoutException [1]秒后重试第[{}]次..", i);
				Thread.sleep(1000);
			}
			catch (java.net.ConnectException e)
			{
				log.error("SocketTimeoutException [1]秒后重试第[{}]次..", i);
				Thread.sleep(1000);
			}

		}

		throw new RuntimeException("JsoupFetch重试[" + retry + "]次后无法成功.");
	}

	private String connect(String url) throws IOException
	{
		Connection connect = Jsoup.connect(url);
		connect.userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
		connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		connect.timeout(1000 * 10);

		return connect.get().html();
	}

	public static void main(String[] args) throws IOException, InterruptedException
	{
		String html = new JsoupFetch().fetch("http://www.baidu.com");
		System.out.println(html);
	}
}
