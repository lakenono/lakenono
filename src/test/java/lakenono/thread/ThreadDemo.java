package lakenono.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ThreadDemo
{

	public static final void shutdownCallback()
	{
		System.out.println("Shutdown callback is invoked.");
	}

	public static void main(String[] args)
	{
		// 回调
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				shutdownCallback();
			}
		});

		// 注册线程
		final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("lolth-%d").setDaemon(false).build();
		ScheduledExecutorService service = Executors.newScheduledThreadPool(2, threadFactory);

		service.scheduleWithFixedDelay(new Runnable()
		{
			@Override
			public void run()
			{
				System.out.println("10");
			}
		}, 0, 10, TimeUnit.SECONDS);

		service.scheduleWithFixedDelay(new Runnable()
		{
			@Override
			public void run()
			{
				System.out.println("5");
			}
		}, 0, 5, TimeUnit.SECONDS);
	}
}
