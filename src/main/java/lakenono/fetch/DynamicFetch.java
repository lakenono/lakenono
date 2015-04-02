package lakenono.fetch;

import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicFetch
{
	private final Logger log = LoggerFactory.getLogger(DynamicFetch.class);

	private WebDriver driver;

	// 重试次数
	private final int retry = 3;

	private boolean isInit = false;

	public void init()
	{
		this.log.debug("init web driver...");

		// 设置页面为最大
		// this.driver.manage().window().maximize();

		FirefoxProfile firefoxProfile = new FirefoxProfile();
		// 去掉css
		firefoxProfile.setPreference("permissions.default.stylesheet", 2);
		this.log.debug("init web driver... 去掉css");
		// 去掉图片
		firefoxProfile.setPreference("permissions.default.image", 2);
		this.log.debug("init web driver... 去掉图片");
		// 去掉flash
		firefoxProfile.setPreference("dom.ipc.plugins.enabled.libflashplayer.so", false);
		this.log.debug("init web driver... 去掉flash");

		this.driver = new FirefoxDriver(firefoxProfile);
	}

	public void initTimeout()
	{
		this.driver.manage().timeouts().setScriptTimeout(20, TimeUnit.SECONDS);
		this.driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
		this.driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
	}

	public Document document(String url) throws Exception
	{
		String html = this.fetch(url);
		return Jsoup.parse(html);
	}

	public String fetch(String url) throws Exception
	{
		this.log.info("DynamicFetch fetch url:{}", url);

		for (int i = 1; i <= retry; i++)
		{
			if (!isInit)
			{
				this.init();
			}

			try
			{
				this.driver.get(url);
				String html = driver.getPageSource();
				this.destroy();
				return html;
			}
			catch (Exception e)
			{
				log.error("Exception [1]秒后重试第[{}]次..", i);
				Thread.sleep(1000);
			}

		}

		throw new RuntimeException("DynamicFetch重试[" + retry + "]次后无法成功.");
	}

	public String[] fetchExtraCurrentUrl(String url) throws Exception
	{
		String html = this.fetch(url);
		String currentUrl = this.driver.getCurrentUrl();

		return new String[] { html, currentUrl };
	}

	public void destroy()
	{
		this.driver.quit();
	}

	public WebDriver getDriver()
	{
		return driver;
	}

	public static void main(String[] args) throws Exception
	{
		DynamicFetch dynamicFetch = new DynamicFetch();
		String html = dynamicFetch.fetch("http://www.baidu.com");
		System.out.println(html);
	}

}
