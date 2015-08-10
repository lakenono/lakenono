package lakenono.fetcher;

public interface Fetcher {
	/**
	 * 普通抓取
	 * 
	 * @param url
	 * @return
	 */
	default public String fetch(String url) throws Exception {
		return fetch(url, "", "");
	}

	/**
	 * 登陆抓取
	 * 
	 * @param url
	 * @param cookie
	 * @return
	 */
	public String fetch(String url, String cookies, String charset) throws Exception;
}
