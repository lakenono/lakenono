package lakenono.fetch.adv;


/**
 * 请求增强器
 * 
 * @author shilei
 * 
 */
public interface HttpRequestEnhancer {

	/**
	 * http请求增强器，对HttpRequest做响应的增强操作，如增加登录所需信息等
	 * 
	 * @param httpRequest
	 * @return
	 */
	public HttpRequest enhanceHttpRequest(HttpRequest httpRequest);
}
