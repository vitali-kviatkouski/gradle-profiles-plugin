package com.vk.gradle.profile.builder.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class ProfileExtractor {
	private static final String PROFILE_DEFAULT = "profile.default";
	
	public String loadCurrentProfile(Properties props) {
		return props.getProperty(PROFILE_DEFAULT);
	}
	
	public Map<String, String> loadProfileProperties(Properties props, String profile) {
		Map<String, String> profileProps = new HashMap<String, String>();
		for (Entry<Object, Object> entry : props.entrySet()) {
			String key = entry.getKey().toString();
			if (key.startsWith(profile + ".")) {
				profileProps.put(key.replace(profile + ".", ""), entry.getValue().toString());
			}
		}
		return profileProps;
	}
	
	public Map<String, String> loadProfileProperties(Properties props) {
		return loadProfileProperties(props, loadCurrentProfile(props));
	}
}
