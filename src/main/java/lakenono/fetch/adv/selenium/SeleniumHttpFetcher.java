package lakenono.fetch.adv.selenium;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lakenono.fetch.adv.HttpFetcher;
import lakenono.fetch.adv.HttpRequest;
import lakenono.fetch.adv.HttpResponse;
import lakenono.fetch.adv.utils.HttpURLUtils;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * 
 * @author shilei
 * 
 */
public class SeleniumHttpFetcher implements HttpFetcher {
	private int scriptTimeout = 20;
	private int pageLoadTimeout = 20;
	private int implicitlyWait = 20;

	private WebDriver webDriver;

	private SeleniumWebAction webAction;

	public SeleniumHttpFetcher() {
		this(null, null);
	}

	public SeleniumHttpFetcher(SeleniumWebAction webAction) {
		this(null, webAction);
	}

	/**
	 * 创建火狐浏览器
	 * 
	 * @param firefoxPath
	 *            火狐路径
	 * @param webAction
	 *            动作
	 */
	public SeleniumHttpFetcher(String firefoxPath, SeleniumWebAction webAction) {
		if (!Strings.isNullOrEmpty(firefoxPath)) {
			System.setProperty("webdriver.firefox.bin", firefoxPath);
		}

		webDriver = getNoResouceWebDriver();

		webDriver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);
		webDriver.manage().timeouts().pageLoadTimeout(pageLoadTimeout, TimeUnit.SECONDS);
		webDriver.manage().timeouts().implicitlyWait(implicitlyWait, TimeUnit.SECONDS);

		this.webAction = webAction;
	}

	/**
	 * 获得不加载 css，图片，flash的浏览器
	 * 
	 * @return
	 */
	public WebDriver getNoResouceWebDriver() {
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		// 去掉css
		// firefoxProfile.setPreference("permissions.default.stylesheet", 2);
		// 去掉图片
		// firefoxProfile.setPreference("permissions.default.image", 2);
		// 去掉flash
		// firefoxProfile.setPreference("dom.ipc.plugins.enabled.libflashplayer.so",
		// false);

		return new FirefoxDriver(firefoxProfile);
	}

	@Override
	public HttpResponse run(HttpRequest httpRequest) throws Exception {
		HttpResponse httpResponse = new HttpResponse();

		// 根据参数创建url
		String url = HttpURLUtils.buildUrlWithParams(httpRequest.getUrl(), httpRequest.getParams());

		// 设置cookies
		setCookies(httpRequest.getCookies());

		// 请求
		webDriver.get(url);

		// 处理自动化行为
		boolean doWebSuccess = false;
		if (webAction != null) {
			doWebSuccess = webAction.doWeb(webDriver, httpRequest, httpResponse);
		}

		if (!doWebSuccess) {
			return httpResponse;
		}
		// 处理响应
		if (httpRequest.isNeedCookies()) {
			Map<String, String> responseCookies = getCookies();
			httpResponse.setCookies(responseCookies);
		}
		if (httpRequest.isNeedContent()) {
			httpResponse.setContent(webDriver.getPageSource().getBytes());
		}

		return httpResponse;
	}

	public Map<String, String> getCookies() {
		Set<Cookie> cookies = webDriver.manage().getCookies();
		Map<String, String> responseCookies = Maps.newHashMap();
		for (Cookie c : cookies) {

			responseCookies.put(c.getName(), c.getValue());
		}
		return responseCookies;
	}

	/**
	 * 清除所有cookie
	 */
	public void clearCookies() {
		webDriver.manage().deleteAllCookies();
	}

	/**
	 * 获取webDriver
	 * 
	 * @return
	 */
	public WebDriver getWebDriver() {
		return webDriver;
	}

	public void setCookies(Map<String, String> cookies) {
		webDriver.manage().deleteAllCookies();
		if (cookies != null && cookies.size() > 0) {
			for (Entry<String, String> c : cookies.entrySet()) {
				Cookie cookie = new Cookie(c.getKey(), c.getValue());
				webDriver.manage().addCookie(cookie);
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (webDriver != null) {
			webDriver.quit();
		}
	}

	/**
	 * 行为
	 * 
	 * @author shilei
	 * 
	 */
	public interface SeleniumWebAction {
		/**
		 * 操作页面动作
		 * 
		 * @param webDriver
		 * @return doWeb执行情况
		 */
		public boolean doWeb(WebDriver webDriver, HttpRequest req, HttpResponse resp);
	}

	public static void main(String[] args) throws Exception {
		String url = "http://www.baidu.com";

		HttpRequest httpRequest = new HttpRequest(url);
		httpRequest.setNeedCookies(true);
		httpRequest.setNeedContent(false);
		HttpFetcher httpExecutor = new SeleniumHttpFetcher();
		System.out.println(httpExecutor.run(httpRequest));
		httpExecutor.close();
	}

}
