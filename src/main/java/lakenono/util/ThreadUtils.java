package lakenono.util;

import lakenono.log.BaseLog;

public class ThreadUtils
{
	public static void sleep(long durationMillis)
	{
		try
		{
			Thread.sleep(durationMillis);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * 保证不会有Exception抛出到线程池的Runnable，防止用户没有捕捉异常导致中断了线程池中的线程。
	 */
	public static class WrapExceptionRunnable extends BaseLog implements Runnable
	{
		private Runnable runnable;

		public WrapExceptionRunnable(Runnable runnable)
		{
			this.runnable = runnable;
		}

		@Override
		public void run()
		{
			try
			{
				runnable.run();
			}
			catch (Throwable e)
			{
				log.error("Unexpected error occurred in task", e);
			}
		}
	}
}
