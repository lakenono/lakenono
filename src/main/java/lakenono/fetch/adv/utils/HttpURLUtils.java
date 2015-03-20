package lakenono.fetch.adv.utils;

import java.util.Map;
import java.util.Map.Entry;

public class HttpURLUtils {
	/**
	 * url 追加参数
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String buildUrlWithParams(String url, Map<String, String> params) {
		StringBuilder urlBuilder = new StringBuilder();
		if (params != null && params.size() > 0) {
			for (Entry<String, String> p : params.entrySet()) {
				urlBuilder.append(p.getKey()).append('=').append(p.getValue()).append('&');
			}
			urlBuilder.deleteCharAt(urlBuilder.length() - 1);

			// 判断原有url是否有参数
			int questionMarkIndex = url.indexOf('?');
			if (questionMarkIndex > 0) {
				if (questionMarkIndex < url.length() - 1) {
					urlBuilder.insert(0, '&');
				}

			} else {
				urlBuilder.insert(0, '?');
			}
		}
		urlBuilder.insert(0, url);
		return urlBuilder.toString();
	}
}
