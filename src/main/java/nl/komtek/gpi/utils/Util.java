package nl.komtek.gpi.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by Elroy on 10-7-2017.
 */
@Component
public class Util {

	@Autowired
	private Environment environment;
	private Logger logger = LogManager.getLogger(Util.class);

	public String getEnvProperty(String key) {
		String value = StringUtils.trimAllWhitespace(environment.getProperty(key));
		return StringUtils.replace(value, "\"", "");
	}

	public String getEnvProperty(String key, String defaultValue) {
		String value = getEnvProperty(key);
		return (value == null) ? defaultValue : value;
	}

	public String getConfigurationProperty(String key, String defaultValue) {
		String value = getConfigurationProperty(key);
		return (value == null) ? defaultValue : value;
	}

	public String getConfigurationProperty(String key) {
		CaseLessProperties prop = new CaseLessProperties();
		String value = null;
		try (InputStream input = new FileInputStream("configuration.properties")) {
			prop.load(input);
			value = prop.getProperty(key);
			logger.debug(String.format("reading property key %s -- value %s", key, value));
		} catch (Exception e) {
			logger.debug("Error reading configuration file", e);
		}
		return value;
	}
}
