package lakenono.fetch.adv.httpclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lakenono.fetch.adv.HttpFetcher;
import lakenono.fetch.adv.HttpProxy;
import lakenono.fetch.adv.HttpRequest;
import lakenono.fetch.adv.HttpResponse;
import lakenono.fetch.adv.utils.HttpURLUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class HttpClientFetcher implements HttpFetcher {
	private CloseableHttpClient httpclient;

	private int socketTimeout = 50000;
	private int connectionTimeout = 50000;
	private int connectionRequestTimeout = 50000;

	public HttpClientFetcher() {
		this(null, null);
	}

	/**
	 * 代理方式
	 * 
	 * @param proxy
	 */
	public HttpClientFetcher(HttpProxy proxy) {
		this(null, proxy);
	}

	public HttpClientFetcher(Map<String, String> globalCookies, HttpProxy proxy) {
		HttpClientBuilder builder = HttpClients.custom();
		if (proxy != null) {
			buildProxy(proxy, builder);
		}
		if (globalCookies != null && globalCookies.size() > 0) {
			buildGlobalCookie(globalCookies, builder);
		}
		httpclient = builder.build();
	}

	// 设置代理
	private void buildProxy(HttpProxy proxy, HttpClientBuilder builder) {
		HttpHost proxyHost = new HttpHost(proxy.getHost(), proxy.getPort());
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(proxy.getHost(), proxy.getPort()), new UsernamePasswordCredentials(proxy.getUser(), proxy.getPassword()));

		builder.setProxy(proxyHost).setDefaultCredentialsProvider(credentialsProvider);
	}

	// 设置全局cookie
	private void buildGlobalCookie(Map<String, String> globalCookies, HttpClientBuilder builder) {
		BasicCookieStore cookieStore = new BasicCookieStore();

		for (Entry<String, String> c : globalCookies.entrySet()) {
			BasicClientCookie cookie = new BasicClientCookie(c.getKey(), c.getValue());
			cookieStore.addCookie(cookie);
		}

		builder.setDefaultCookieStore(cookieStore);
	}

	@Override
	public HttpResponse run(HttpRequest httpRequest) throws Exception {
		HttpResponse httpResponse = new HttpResponse();
		// 创建请求
		HttpRequestBase httpClientRequest = buildHttpMehodRequest(httpRequest);
		// 设置超时时间
		configRequest(httpClientRequest, httpRequest);
		// 设置cookies
		setHeaders(httpClientRequest, httpRequest);

		CloseableHttpResponse httpClientResponse = httpclient.execute(httpClientRequest);
		if (httpRequest.isNeedCookies()) {
			// 读取cookies信息
			Map<String, String> cookies = getResponseCookies(httpClientResponse);
			httpResponse.setCookies(cookies);
		}

		httpResponse.setStatus(httpClientResponse.getStatusLine().getStatusCode());
		try {
			HttpEntity httClientEntity = httpClientResponse.getEntity();

			if (httClientEntity.getContentEncoding() != null) {
				httpResponse.setCharset(httClientEntity.getContentEncoding().getValue());
			}

			if (httClientEntity.getContentType() != null) {
				httpResponse.setContentType(httClientEntity.getContentType().getValue());
			}

			// 读取数据内容
			if (httpResponse.getStatus() == HttpStatus.SC_OK && httpRequest.isNeedContent()) {
				httpResponse.setContent(EntityUtils.toByteArray(httClientEntity));
			}
			// 关闭响应流
			EntityUtils.consume(httClientEntity);
		} finally {
			httpClientResponse.close();
		}

		return httpResponse;
	}

	// 根据类型创建
	private static HttpRequestBase buildHttpMehodRequest(HttpRequest httpRequest) throws UnsupportedEncodingException {
		HttpRequestBase httpClientRequest = null;
		switch (httpRequest.getMethod()) {
		case GET:
			httpClientRequest = buildGetRequest(httpRequest);
			break;
		case POST:
			httpClientRequest = buildPostRequest(httpRequest);
			break;
		}
		return httpClientRequest;
	}

	// 创建Get请求
	private static HttpRequestBase buildGetRequest(HttpRequest httpRequest) {
		return new HttpGet(HttpURLUtils.buildUrlWithParams(httpRequest.getUrl(), httpRequest.getParams()));
	}

	// 创建Post请求
	private static HttpRequestBase buildPostRequest(HttpRequest httpRequest) throws UnsupportedEncodingException {
		HttpPost httpPost = new HttpPost(httpRequest.getUrl());
		Map<String, String> params = httpRequest.getParams();
		if (params != null && params.size() > 0) {
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Entry<String, String> param : params.entrySet()) {
				nvps.add(new BasicNameValuePair(param.getKey(), param.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		}
		return httpPost;
	}

	// 超时配置
	private void configRequest(HttpRequestBase httpClientRequest, HttpRequest httpRequest) {
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectionTimeout).setConnectionRequestTimeout(connectionRequestTimeout).setRedirectsEnabled(httpRequest.isRedirectsEnabled()).build();
		httpClientRequest.setConfig(requestConfig);
	}

	// 配置Header
	private void setHeaders(HttpRequestBase httpClientRequest, HttpRequest httpRequest) {
		// 配置cookie
		Map<String, String> cookies = httpRequest.getCookies();
		if (cookies != null && cookies.size() > 0) {
			httpClientRequest.addHeader("Cookie", buildCookieStr(cookies));
		}
		// 配置User-Agent
		String userAgent = httpRequest.getUserAgent();
		if (!Strings.isNullOrEmpty(userAgent)) {
			httpClientRequest.addHeader("User-Agent", userAgent);
		}
	}

	/**
	 * 创建cookie 串
	 * 
	 * @param cookies
	 * @return
	 */
	public static String buildCookieStr(Map<String, String> cookies) {
		StringBuilder cookieStr = new StringBuilder();
		for (Entry<String, String> cookie : cookies.entrySet()) {
			cookieStr.append(cookie.getKey()).append('=').append(cookie.getValue()).append(';');
		}
		cookieStr.deleteCharAt(cookieStr.length() - 1);
		return cookieStr.toString();
	}

	private Map<String, String> getResponseCookies(CloseableHttpResponse httpClientResponse) {
		Map<String, String> cookies = Maps.newHashMap();
		Header[] headers = httpClientResponse.getHeaders("Set-Cookie");
		for (Header h : headers) {
			String cookie = h.getValue();

			String cookieRecord = cookie.substring(0, cookie.indexOf(';'));
			int equalMarkIndex = cookieRecord.indexOf('=');
			if (equalMarkIndex > 0) {
				cookies.put(cookieRecord.substring(0, equalMarkIndex), cookieRecord.substring(equalMarkIndex + 1, cookieRecord.length()));
			}
		}

		return cookies;
	}

	@Override
	public void close() throws IOException {
		httpclient.close();
	}

	public static void main(String[] args) throws Exception {
		// String url = "http://www.baidu.com";
		// HttpProxy proxy = new HttpProxy();
		// proxy.setHost("proxy.asiainfo.com");
		// proxy.setPort(8080);
		// proxy.setUser("shilei15");
		// proxy.setPassword("");
		//
		// HttpRequest httpRequest = new HttpRequest(url);
		// httpRequest.setNeedCookies(true);
		// httpRequest.setNeedContent(true);
		// HttpFetcher httpExecutor = new HttpClientFetcher(null, null);
		// HttpResponse resp = httpExecutor.run(httpRequest);
		// System.out.println(new String(resp.getContent()));
		// httpExecutor.close();

		String url = "http://www.sohu.com";
		HttpRequest req = new HttpRequest(url);
		req.setNeedCookies(true);
		req.setNeedCookies(false);

		HttpFetcher httpExecutor = new HttpClientFetcher();
		HttpResponse resp = httpExecutor.run(req);

		Map<String, String> cookies = resp.getCookies();
		if (cookies != null) {
			for (Entry<String, String> cEntry : cookies.entrySet()) {
				System.out.println(cEntry.getKey() + " | " + cEntry.getValue());
			}
		}
		httpExecutor.close();
	}

}
