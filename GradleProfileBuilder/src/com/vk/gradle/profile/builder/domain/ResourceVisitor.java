package com.vk.gradle.profile.builder.domain;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ResourceVisitor {
	private ResourceFilter filter;

	public ResourceVisitor(ResourceFilter filter) {
		this.filter = filter;
	}

	public void visit(String baseDir) {
		visitNode(new File(baseDir));
	}

	public void visitNode(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				for (File ch : children) {
					visitNode(ch);
				}
			} else {
				if (file.getName().endsWith(".xml") || file.getName().endsWith(".properties")) {
					visitFile(file);
				}
			}
		}
	}

	public void visitFile(File file) {
		if (file.exists()) {
			try {
				List<String> filtered = filter.filterResource(new FileInputStream(file));
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				for (String line : filtered) {
					writer.write(line);
				}
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
