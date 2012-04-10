package com.vk.gradle.profile.builder.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResourceFilter {
	private String startSymbol = "@";
	private String endSymbol = "@";
	
	private Map<String, String> placeholderVals;
	
	public ResourceFilter(Map<String, String> placeholderVals) {
		this.placeholderVals = placeholderVals;
	}
	
	public List<String> filterResource(InputStream is) {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<String> list = new ArrayList<String>();
		try {
			String line = br.readLine();
			while(line != null) {
				list.add(filterString(line));
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public String filterString(String str) {
		StringBuilder sb = new StringBuilder();
		filterString(str, sb);
		return sb.toString();
	}
	
	// if return == true then no string modified
	private void filterString(String str, StringBuilder sb) {
		int idx = str.indexOf(startSymbol);
		if (idx >= 0 && idx < str.length() - 1) {
			sb.append(str.substring(0, idx));
			filterString(str, sb, idx);
		} else {
			sb.append(str);
		}
	}
	
	private void filterString(String str, StringBuilder sb, int idx) {
		int endIdx = str.indexOf(endSymbol, idx + startSymbol.length());
		if (endIdx > 0) {
			String propName = str.substring(idx + startSymbol.length(), endIdx);
			String propVal = placeholderVals.get(propName);
			if (propVal != null) {
				sb.append(propVal);
				filterString(str.substring(endIdx + endSymbol.length()), sb);
			} else {
				sb.append(str.substring(idx, endIdx));
				filterString(str, sb, endIdx);
			}
		} else {
			sb.append(str.substring(idx));
		}
	}
}
