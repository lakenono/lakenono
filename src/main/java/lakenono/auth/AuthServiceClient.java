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
	private TTransport transport;
	private AuthService.Iface client;

	public AuthServiceClient(String serverIp, int port) {
		// 设置传输通道 - 普通IO流通道
		try {
			transport = new TSocket(serverIp, port);
			transport.open();

			// 使用高密度二进制协议
			TProtocol protocol = new TBinaryProtocol(transport);

			// 创建Client
			client = new AuthService.Client(protocol);
		} catch (Exception e) {
			log.error("AuthServiceClient init error : {}", e.getMessage(), e);
		}
	}

	@Override
	public String getCookies(String domain) throws TException {
		return client.getCookies(domain);
	}

	public void close() {
		transport.close();
	}
	
	public static void main(String[] args) throws TException {
			String cookies = GlobalComponents.authService.getCookies("weibo.cn");
			System.out.println(cookies);
	}

}
