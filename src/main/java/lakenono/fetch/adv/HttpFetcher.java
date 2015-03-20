package lakenono.fetch.adv;

import java.io.Closeable;

/**
 * Http 请求驱动器
 * 
 * @author shilei
 * 
 */
public interface HttpFetcher extends Closeable {
	/**
	 * Http请求驱动器
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public HttpResponse run(HttpRequest request) throws Exception;

	/**
	 * 下载指定url的内容
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	default public byte[] run(String url) throws Exception {
		HttpRequest httpRequest = new HttpRequest(url);
		httpRequest.setNeedCookies(false);
		httpRequest.setNeedContent(true);
		HttpResponse httpResponse = this.run(httpRequest);
		return httpResponse.getContent();
	}
}
