package lakenono.fetcher;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import lakenono.fetcher.utils.CookiesUtils;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * 动态fetcher
 * 
 * @author shilei
 *
 */
@Slf4j
public class SeleniumFetcher implements Fetcher {
	private WebDriver driver;

	// 重试次数
	private final int retry = 3;

	private boolean isInit = false;

	@Override
	public String fetch(String url, String cookies) throws Exception {
		log.info("DynamicFetch fetch url:{}", url);

		for (int i = 1; i <= retry; i++) {
			if (!isInit) {
				this.init();
			}

			if (StringUtils.isNotBlank(cookies)) {
				Map<String, String> cookiePair = CookiesUtils.getCookies(cookies);
				if (cookiePair != null) {
					setCookies(cookiePair);
				}
			}

			try {
				this.driver.get(url);
				String html = driver.getPageSource();
				this.destroy();
				return html;
			} catch (Exception e) {
				log.error("Exception [1]秒后重试第[{}]次..", i);
				Thread.sleep(1000);
			}

		}

		throw new RuntimeException("DynamicFetch重试[" + retry + "]次后无法成功.");
	}

	public void init() {
		log.debug("init web driver...");

		FirefoxProfile firefoxProfile = new FirefoxProfile();

		this.driver = new FirefoxDriver(firefoxProfile);
		initTimeout();
	}

	private void initTimeout() {
		this.driver.manage().timeouts().setScriptTimeout(20, TimeUnit.SECONDS);
		this.driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
		this.driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
	}

	public void setCookies(Map<String, String> cookies) {
//		if (cookies != null && cookies.size() > 0) {
//			for (Entry<String, String> c : cookies.entrySet()) {
//				Cookie cookie = new Cookie(c.getKey(), c.getValue(),"/",null);
//				this.driver.manage().addCookie(cookie);
//			}
//		}
	}

	public void destroy() {
		this.driver.quit();
	}

	public WebDriver getDriver() {
		return driver;
	}

	public static void main(String[] args) throws Exception {
		String url = "http://weibo.cn/3350447844/info";
		String cookies = "T_WM=b55909f1c458a59420d07b4b0994d4ba; SUB=_2A254tZV6DeTxGeVN7lIV9CnEzziIHXVYWTsyrDV6PUJbrdANLXD_kW1QxrUPTyX06x_N8Gbxbo_cBNeepQ..; gsid_CTandWM=4ujA0ade1kbLIkS9Wayabe3BtdC";
		System.out.println(new SeleniumFetcher().fetch(url, cookies));
	}

}
