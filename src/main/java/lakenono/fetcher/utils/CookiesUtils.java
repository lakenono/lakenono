package lakenono.fetcher.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

public class CookiesUtils {
	
	public static Map<String, String> getCookies(String cookieStr) {
		Map<String, String> cookies = new HashMap<>();
		String[] cookiePairs = StringUtils.splitByWholeSeparator(cookieStr, ";");
		for (String cookiePair : cookiePairs) {
			if (StringUtils.contains(cookiePair, "=")) {
				String[] kv = StringUtils.splitByWholeSeparator(cookiePair, "=");
				cookies.put(kv[0], kv[1]);
			}
		}
		if(MapUtils.isEmpty(cookies)){
			return null;
		}
		return cookies;
	}
}
