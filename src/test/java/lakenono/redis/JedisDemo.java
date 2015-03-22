package lakenono.redis;

import lakenono.core.GlobalComponents;

public class JedisDemo
{
	public static void main(String[] args)
	{
		GlobalComponents.jedis.set("name", "lakenono");
		String name = GlobalComponents.jedis.get("name");
		System.out.println(name);
	}
}
