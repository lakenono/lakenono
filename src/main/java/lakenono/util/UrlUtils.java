package lakenono.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtils {
	public static String getDomain(String url) {
		String p1 = "com\\b|cn\\b|net\\b|org\\b|biz\\b|info\\b|cc\\b|tv\\b|edu\\b|ac\\b|mil\\b|arpa\\b|pro\\b|coop\\b|aero\\b|museum\\b|mobi\\b|asia\\b|tel\\b|int\\b|us\\b|travel\\b";
		String p2 = "ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci" + "|ck|cl|cm|cn|co|cr|cu|cv|cx|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|" + "gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|" + "md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|" + "qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|" + "uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw";
		Pattern p = Pattern.compile("(?<=http://|\\.)([^.]*?\\.(" + p1 + ")(\\.(" + p2 + "))?)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(url);

		while (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	/*
	 * 对字符编译
	 */
	public static String encode(String... args) {
		String value = args[0];
		try {
			value = URLEncoder.encode(value, (args.length > 1 ? args[1] : "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return value;
	}
}
