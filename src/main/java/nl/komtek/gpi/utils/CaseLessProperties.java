package nl.komtek.gpi.utils;

/**
 * Created by Elroy on 13-7-2017.
 */
import java.util.Properties;

public class CaseLessProperties extends Properties {

	public Object put(Object key, Object value) {
		String lowercase = ((String) key).toLowerCase();
		return super.put(lowercase, value);
	}

	public String getProperty(String key) {
		String lowercase = key.toLowerCase();
		return super.getProperty(lowercase);
	}

	public String getProperty(String key, String defaultValue) {
		String lowercase = key.toLowerCase();
		return super.getProperty(lowercase, defaultValue);
	}
}