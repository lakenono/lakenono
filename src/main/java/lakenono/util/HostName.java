package lakenono.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostName
{
	public static String getHostName()
	{
		try
		{
			return (InetAddress.getLocalHost()).getHostName();
		}
		catch (UnknownHostException uhe)
		{
			String host = uhe.getMessage(); // host = "hostname: hostname"  
			if (host != null)
			{
				int colon = host.indexOf(':');
				if (colon > 0)
				{
					return host.substring(0, colon);
				}
			}
			return "UnknownHost";
		}
	}

	public static void main(String[] args)
	{
		String hostNameForLiunx = HostName.getHostName();
		System.out.println(hostNameForLiunx);
	}
}
