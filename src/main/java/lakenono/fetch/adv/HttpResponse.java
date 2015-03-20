package lakenono.fetch.adv;

import java.util.Map;

import lombok.Data;

@Data
public class HttpResponse {
	private String contentType;
	private Map<String, String> cookies;
	private int status;
	private String charset;
	private byte[] content;
}
