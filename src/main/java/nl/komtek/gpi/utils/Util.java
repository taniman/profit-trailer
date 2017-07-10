package nl.komtek.gpi.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by Elroy on 10-7-2017.
 */
@Component
public class Util {

	@Autowired
	private Environment environment;
	
	public String getEnvProperty(String key) {
		String value = StringUtils.trimAllWhitespace(environment.getProperty(key));
		return StringUtils.replace(value, "\"", "");
	}
}
