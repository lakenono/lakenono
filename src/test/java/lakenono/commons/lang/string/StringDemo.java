package lakenono.commons.lang.string;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class StringDemo
{
	@Test
	public void leftPadDemo()
	{
		// 左边补0
		assertThat(StringUtils.leftPad("1", 3, '0')).isEqualTo("001");
		assertThat(StringUtils.leftPad("12", 3, '0')).isEqualTo("012");

	}

	@Test
	public void abbreviateDemo()
	{
		// 超长部分变省略号
		assertThat(StringUtils.abbreviate("abcdefg", 7)).isEqualTo("abcdefg");
		assertThat(StringUtils.abbreviate("abcdefg", 6)).isEqualTo("abc...");

	}
}
