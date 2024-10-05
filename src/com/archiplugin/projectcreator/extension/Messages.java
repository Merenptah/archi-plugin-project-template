/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archiplugin.projectcreator.extension;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.archiplugin.projectcreator.extension.messages"; //$NON-NLS-1$

	public static String NewProjectFromTemplateMenuEntry;
	public static String NewViewFromTemplateMenuEntry;
	public static String MoveProjectToNextStageMenuEntry;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
