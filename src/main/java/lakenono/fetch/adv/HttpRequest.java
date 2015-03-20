package lakenono.fetch.adv;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpRequest {
	private HttpRequestMethod method = HttpRequestMethod.GET;
	private String baseUrl;
	private String userAgent;
	private String url;
	private Map<String, String> cookies;
	private Map<String, String> params;

	// 是否需要内容
	private boolean isNeedContent = true;
	private boolean isNeedCookies = false;
	private boolean isRedirectsEnabled = true;

	public HttpRequest(String url) {
		this.url = url;
	}
}
