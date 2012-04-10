package com.vk.gradle.profile.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.internal.dialogs.FileExtensionDialog;

import com.vk.gradle.profile.builder.domain.ProfileExtractor;
import com.vk.gradle.profile.builder.domain.ResourceFilter;
import com.vk.gradle.profile.builder.domain.ResourceVisitor;

public class GProfileBuilder extends IncrementalProjectBuilder {

	class GProfileDeltaVisitor implements IResourceDeltaVisitor {
		private ResourceVisitor visitor;
		public GProfileDeltaVisitor(ResourceFilter filter) {
			visitor = new ResourceVisitor(filter);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource instanceof IFile) {
				if (isBuildResource((IFile) resource)) {
					switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						visitor.visitFile(resource.getLocation().toFile());
						break;
					case IResourceDelta.REMOVED: break;
					case IResourceDelta.CHANGED:
						visitor.visitFile(resource.getLocation().toFile());
						break;
					}
				}
			}
			//return true to continue visiting children.
			return true;
		}
		
		private boolean isBuildResource(IFile file) {
			IPath rel = file.getProjectRelativePath();
			boolean oldRes = rel.segment(0).equalsIgnoreCase("bin");
			boolean gradleRes = rel.segment(0).equalsIgnoreCase("build") && rel.segment(0) != null && rel.segment(1).equalsIgnoreCase("resources");
			if (oldRes || gradleRes) {
				if (file.getFileExtension() != null) {
					if (file.getFileExtension().equalsIgnoreCase("properties") || file.getFileExtension().equalsIgnoreCase("xml")) {
						return true;
					}	
				}
			}
			return false;
		}
		
	}

	class SampleResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			checkXML(resource);
			//return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = "gprofile.gprofilebuilder";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		ResourceFilter filter = createResourceFilter();
		if (filter != null) {
			if (kind == FULL_BUILD) {
				fullBuild(monitor, filter);
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(monitor, filter);
				} else {
					incrementalBuild(delta, monitor, filter);
				}
			}
		}
		return null;
	}
	
	private ResourceFilter createResourceFilter() {
		IProject project = getProject();
		// check no more than 2 levels high
		File file = project.getFile("profiles.properties").getLocation().toFile();
		if (!file.exists()) {
			file = new File(file.getParentFile().getParentFile(), "profiles.properties");
		}
		if (file.exists()) {
			ProfileExtractor extractor = new ProfileExtractor();
			Properties props = new Properties();
			try {
				InputStream is = new FileInputStream(file);
				props.load(is);
				is.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Map<String, String> profProps = extractor.loadProfileProperties(props);
			ResourceFilter filter = new ResourceFilter(profProps);
			return filter;
		}
		return null;
	}

	void checkXML(IResource resource) {
	}

	protected void fullBuild(final IProgressMonitor monitor, ResourceFilter filter)
			throws CoreException {
		File file = new File(getProject().getFile("build").getLocation().toFile(), "resources");
		if (file.exists()) {
			ResourceVisitor visitor = new ResourceVisitor(filter);
			visitor.visitNode(file);
		} else {
			// try old bin way
			ResourceVisitor visitor = new ResourceVisitor(filter);
			File bin = getProject().getFile("bin").getLocation().toFile();
			if (bin.exists()) {
				visitor.visitNode(bin);
			}
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor, ResourceFilter filter) throws CoreException {
		// the visitor does the work.
		delta.accept(new GProfileDeltaVisitor(filter));
	}
}
