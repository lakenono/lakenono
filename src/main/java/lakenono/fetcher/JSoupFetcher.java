package lakenono.fetcher;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

@Slf4j
public class JSoupFetcher implements Fetcher {

	@Override
	public String fetch(String url, String cookies) throws InterruptedException {
		Connection connect = Jsoup.connect(url);

		// cookie
		if (StringUtils.isNotBlank(cookies)) {
			connect.header("Cookie", cookies);
		}

		// ua
		connect.userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
		connect.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		connect.timeout(1000 * 5);

		int retry = 5;

		for (int i = 1; i <= retry; i++) {
			try {
				return connect.execute().body();
			} catch (IOException e) {
				log.error("JSoupFetcher fetch error : {}, 1 秒后重试第[{}]次..", e.getMessage(), i);
				Thread.sleep(1000);
			}
		}
		throw new RuntimeException("JSoupFetcher 重试[" + retry + "] 次后无法成功.");
	}

	public static void main(String[] args) throws Exception {
		String url = "http://weibo.cn/3350447844/info";
		String cookies = "_T_WM=b55909f1c458a59420d07b4b0994d4ba; SUB=_2A254tZV6DeTxGeVN7lIV9CnEzziIHXVYWTsyrDV6PUJbrdANLXD_kW1QxrUPTyX06x_N8Gbxbo_cBNeepQ..; gsid_CTandWM=4ujA0ade1kbLIkS9Wayabe3BtdC";
		System.out.println(new JSoupFetcher().fetch(url, cookies));
	}
}
