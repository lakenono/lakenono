package lakenono.auth;

import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class AuthServiceClient implements AuthService.Iface {
	private TTransport transport;
	private AuthService.Iface authService;

	public AuthServiceClient(String server, int port) throws TTransportException {
		transport = new TSocket(server, port);

		transport.open();

		TProtocol protocol = new TBinaryProtocol(transport);

		authService = new AuthService.Client(protocol);
	}

	@Override
	public Map<String, String> getAuthData(String domain, String clientIp) throws TException {
		return authService.getAuthData(domain, clientIp);
	}

	public void close() {
		transport.close();
	}
}
