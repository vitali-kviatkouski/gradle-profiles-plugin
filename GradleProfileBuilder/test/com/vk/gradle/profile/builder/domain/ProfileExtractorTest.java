package com.vk.gradle.profile.builder.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProfileExtractorTest {
	private Properties testProps;
	private ProfileExtractor extractor;

	@Before
	public void before() {
		testProps = new Properties();
		testProps.setProperty("profile.default", "local");
		
		testProps.setProperty("local.db.server", "localhost");
		testProps.setProperty("local.db.port", "1234");
		testProps.setProperty("local.db.user", "user1");
		
		testProps.setProperty("stage.db.server", "staging");
		testProps.setProperty("stage.db.port", "80");
		testProps.setProperty("stage.db.user", "user2");
		
		extractor = new ProfileExtractor();
	}

	@Test
	public void testExtractPropsForProfile() {
		assertExtractPropsForProfile(testProps);
	}
	
	@Test
	public void testExtractPropsForProfileFromFile() {
		Properties props = new Properties();
		InputStream is = ProfileExtractorTest.class.getClassLoader().getResourceAsStream("profiles.properties");
		try {
			props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		assertExtractPropsForProfile(props);
	}
	
	private void assertExtractPropsForProfile(Properties props) {
		Map<String, String> prProps = extractor.loadProfileProperties(props, "stage");
		Assert.assertEquals(prProps.size(), 3);
		Assert.assertEquals(prProps.get("db.server"), "staging");
		Assert.assertEquals(prProps.get("db.port"), "80");
		Assert.assertEquals(prProps.get("db.user"), "user2");
	}
	
	@Test
	public void testExtractCurProfile() {
		String curProfile = extractor.loadCurrentProfile(testProps);
		Assert.assertEquals(curProfile, "local");
	}
}
