package com.vk.gradle.profile.builder.domain;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResourceFilterTest {
	private ResourceFilter filter;
	
	@Before
	public void before() {
		Map<String, String> placeholders = new HashMap<String, String>();
		placeholders.put("db.server", "localhost");
		placeholders.put("db.port", "1234");
		placeholders.put("db.user", "user1");
		filter = new ResourceFilter(placeholders);
	}

	@Test
	public void testFilterString() {
		String res = filter.filterString("jdbc://@db.server@//content");
		Assert.assertEquals(res, "jdbc://localhost//content");
		Assert.assertEquals("localhost", filter.filterString("@db.server@"));
		Assert.assertEquals("1localhost", filter.filterString("1@db.server@"));
		Assert.assertEquals("localhost1", filter.filterString("@db.server@1"));
		res = filter.filterString("jdbc://@another@db.server@//content");
		Assert.assertEquals(res, "jdbc://@anotherlocalhost//content");
		res = filter.filterString("jdbc://@another@db.server@//@an@an2@db.port@o@an3@content");
		Assert.assertEquals(res, "jdbc://@anotherlocalhost//@an@an21234o@an3@content");
	}
	
	@Test
	public void testFilterStringMultiple() {
		Assert.assertEquals("localhostat1234", filter.filterString("@db.server@at@db.port@"));
		Assert.assertEquals("1234localhostat1234onuser112", filter.filterString("1234@db.server@at@db.port@on@db.user@12"));
	}

	@Test
	public void testFilterResource() {
		InputStream is = ResourceFilterTest.class.getClassLoader().getResourceAsStream("filtersample.txt");
		List<String> result = filter.filterResource(is);
		Assert.assertEquals(result.get(0), "Hello, localhost:1234/user1");
		Assert.assertEquals(result.get(1), "Some non-empty line to fill space");
		Assert.assertEquals(result.get(2), "@db.serve@onlocalhost");
	}
}
