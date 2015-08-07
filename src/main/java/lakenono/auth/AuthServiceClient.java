package lakenono.auth;

import lakenono.core.GlobalComponents;
import lombok.extern.slf4j.Slf4j;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

@Slf4j
public class AuthServiceClient implements AuthService.Iface {
	private String serverIp;
	private int port;

	public AuthServiceClient(String serverIp, int port) {
		this.serverIp = serverIp;
		this.port = port;
	}

	@Override
	public String getCookies(String domain) throws TException {
		String cookies = "";
		// 设置传输通道 - 普通IO流通道
		try {
			TTransport transport = new TSocket(serverIp, port);
			transport.open();

			// 使用高密度二进制协议
			TProtocol protocol = new TBinaryProtocol(transport);

			// 创建Client
			AuthService.Iface client = new AuthService.Client(protocol);
			cookies = client.getCookies(domain);

			transport.close();
		} catch (Exception e) {
			log.error("AuthServiceClient init error : {}", e.getMessage(), e);
		}
		return cookies;
	}

	public void close() {
	}

	public static void main(String[] args) throws TException {
		Thread[] threads = new Thread[30];

		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < 100000; i++) {
						try {
							String cookies = GlobalComponents.authService.getCookies("weibo.cn");
							System.out.println(cookies);
						} catch (TException e) {
							e.printStackTrace();
						}
					}

				}

			});
		}

		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}

	}

}
