package lakenono.fetch;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
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

		this.driver = new FirefoxDriver();

		// 设置页面为最大
		this.driver.manage().window().maximize();

		// init cookie
		try
		{
			this.log.debug("init cookies");
		}
		catch (Exception e)
		{
			this.log.error("init error", e);
		}
	}

	public void initTimeout()
	{
		this.driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
		this.driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
		this.driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	public String fetch(String url) throws Exception
	{
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
