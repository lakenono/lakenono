package lakenono.base.demo.baidu.zhidao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import lakenono.base.PageParser;
import lakenono.base.Task;
import lakenono.core.GlobalComponents;
import lakenono.fetch.adv.utils.HttpURLUtils;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class BaiduZhidaoListPageParser extends PageParser
{
	public BaiduZhidaoListPageParser(String projectName, String keyword, String dateType)
	{
		super(projectName);
		this.keyword = keyword;
		this.dateType = dateType;
	}

	public static void main(String[] args)
	{
	}

	private String keyword;
	private String dateType; //时间周期 0: 全部 2： 1周 3: 1月 4: 1年

	@Override
	public String getQueueName()
	{
		return "baidu_zhidao_list_fetch";
	}

	private static final String BAIDU_ZHIDAO_LIST_URL_TEMPLATE = "http://zhidao.baidu.com/search?word={0}&ie=gbk&site=-1&sites=0&date={1}&pn={2}";

	@Override
	protected int parse() throws IOException, InterruptedException
	{
		Document document = GlobalComponents.fetcher.document(buildUrl(1));

		// 超过10页——查看尾页标签
		Elements pages = document.select("div.pager a.pager-last");
		if (!pages.isEmpty())
		{
			String url = pages.first().absUrl("href");
			String maxPage = HttpURLUtils.getUrlParams(url, "").get("pn");
			if (StringUtils.isNumeric(maxPage))
			{
				return Integer.parseInt(maxPage) / 10 + 1;
			}
			else
			{
				return 0;
			}
		}

		// 2~10 页——查看下一页标签
		pages = document.select("div.pager a.pager-next");
		if (!pages.isEmpty())
		{
			String maxPage = pages.first().previousElementSibling().text();
			if (StringUtils.isNumeric(maxPage))
			{
				return Integer.parseInt(maxPage);
			}
			else
			{
				return 0;
			}
		}

		// 1 页——根据记录数查看1
		Elements records = document.select("div.list-inner dl.dl");
		if (!records.isEmpty())
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	@Override
	protected Task buildTask(String url)
	{
		Task task = super.buildTask(url);
		task.setExtra(this.keyword);
		return task;
	}

	@Override
	protected String buildUrl(int pageNum) throws UnsupportedEncodingException
	{
		String keywordEncode = URLEncoder.encode(this.keyword, "gbk");
		return MessageFormat.format(BAIDU_ZHIDAO_LIST_URL_TEMPLATE, keywordEncode, dateType, String.valueOf((pageNum - 1) * 10));
	}

}
