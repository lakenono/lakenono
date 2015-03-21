package lakenono.fetch.handlers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

public interface PageFetchHandler
{
	public void run() throws IOException, InterruptedException, SQLException, Exception;

	void process(int i) throws Exception;

	public int getMaxPage() throws UnsupportedEncodingException, IOException, InterruptedException;

	public String buildUrl(int pageNum) throws UnsupportedEncodingException;

}
