package lakenono.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegexURLFilter {

	private List<RegexRule> rules;

	public RegexURLFilter() {
		this(RegexURLFilter.class.getResource("/regex.urls").getPath());
	}

	public RegexURLFilter(String filename) {
		this(new File(filename));
	}

	public RegexURLFilter(File filename) {
		init(filename);
	}

	protected RegexURLFilter(Reader reader) throws IllegalArgumentException, IOException {
		rules = readRules(reader);
	}

	private void init(File filename) {
		try {
			rules = readRules(new FileReader(filename));
		} catch (IllegalArgumentException | IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 匹配url正则
	 * 
	 * @param url
	 * @return
	 */
	public String filterToString(String url) {
		return filterToBoolean(url) ? url : null;
	}

	public boolean filterToBoolean(String url) {
		if(rules == null){
			return false;
		}
		for (RegexRule rule : rules) {
			if (rule.match(url)) {
				return true;
			}
		}
		return false;
	}

	private List<RegexRule> readRules(Reader reader) throws IOException, IllegalArgumentException {

		BufferedReader in = new BufferedReader(reader);
		List<RegexRule> rules = new ArrayList<RegexRule>();
		String line;

		while ((line = in.readLine()) != null) {
			if (line.length() == 0) {
				continue;
			}
			if (line.startsWith("#")) {
				continue;
			}
			String regex = line.substring(1);
			log.info("Adding rule {}", regex);
			RegexRule rule = createRule(regex);
			rules.add(rule);
		}
		return rules;
	}

	protected RegexRule createRule(String regex) {
		return new RegexRule(regex);
	}

	private class RegexRule {

		private Pattern pattern;

		protected RegexRule(String regex) {
			pattern = Pattern.compile(regex);
		}

		protected boolean match(String url) {
			return pattern.matcher(url).find();
		}
	}
}
