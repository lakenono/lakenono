package lakenono.fetcher;

import java.io.IOException;
import java.util.List;

import lakenono.core.GlobalComponents;
import lakenono.fetcher.utils.RandomUA;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

@Slf4j
public class JSoupFetcher implements Fetcher {

	@Override
	public String fetch(String url, String cookies, String charset) throws InterruptedException {
		Connection connect = Jsoup.connect(url);

		// cookie
		if (StringUtils.isNotBlank(cookies)) {
			connect.header("Cookie", cookies);
		}

		// ua
		connect.userAgent(RandomUA.getUA());
		connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		connect.timeout(1000 * 5);

		int retry = 5;

		for (int i = 1; i <= retry; i++) {
			try {
				if (StringUtils.isBlank(charset)) {
					return connect.ignoreContentType(true).execute().body();
				} else {
					return new String(connect.ignoreContentType(true).execute().bodyAsBytes(), charset);
				}
			} catch (IOException e) {
				log.error("JSoupFetcher fetch error : {}, 1 秒后重试第[{}]次..", e.getMessage(), i);
				Thread.sleep(1000);
			}
		}
		throw new RuntimeException("JSoupFetcher 重试[" + retry + "] 次后无法成功.");
	}

	public static void main(String[] args) throws Exception {
		List<String> cookies = GlobalComponents.db.getRunner().query("select authData from auth_data where domain = 'weibo.cn'", new ColumnListHandler<String>());
		String url = "http://weibo.cn/2525452065/info?vt=4";
		// String[] cookies = {
		// "_T_WM=b05b993fe8af77db278c3c0323fdafbb; SUB=_2A257No9aDeTxGeNI6lEQ8CzJwjSIHXVY2BESrDV6PUJbrdAKLXX-kW1-GDZAoBHI_jYUDakryIDSQj0c7A..; gsid_CTandWM=4ulF69ab1nMYvsnOLqQAMnydU9E",
		// "_T_WM=bd5234cb83f100643944d97e56ee54fc; SUB=_2A257No6XDeSRGeNL7FQW8CnIyjqIHXVY2BLfrDV6PUJbrdANLUHtkW0ucd4wL1hWAPDUnTn8OAMmdjFB6w..; gsid_CTandWM=4uSd69ab1CZQRGSz7vfjjnoKT6I",
		// "_T_WM=b57b7f8c20e795f9596d5aadab3ba89d; SUB=_2A257No4NDeSRGeNL7FQS9inIyj-IHXVY2BJFrDV6PUJbrdAKLUjVkW2HCOLKpKXe7g7Tz9mbD5Ney5jqMA..; gsid_CTandWM=4uMl69ab1u6VGvpc5XNugnoFp6F",
		// "_T_WM=45a28de7e1bdcf3ada14577f5cea7944; SUB=_2A257No5ZDeSRGeNL7FUR-C7NyjqIHXVY2BIRrDV6PUJbrdAKLU2kkW15dHYTp6bKNx_X8cPP7ibAE6PIQg..; gsid_CTandWM=4uJw69ab1qGQ1OLSTFWGKnoQU1S"
		// };
		// String cookies =
		// "_T_WM=b55909f1c458a59420d07b4b0994d4ba; SUB=_2A254tZV6DeTxGeVN7lIV9CnEzziIHXVYWTsyrDV6PUJbrdANLXD_kW1QxrUPTyX06x_N8Gbxbo_cBNeepQ..; gsid_CTandWM=4ujA0ade1kbLIkS9Wayabe3BtdC";
		for (String c : cookies) {
			System.out.println(new JSoupFetcher().fetch(url, c, "UTF-8"));
		}
	}
}
