package com.archiplugin.projectcreator.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.archiplugin.projectcreator.preferences.messages"; //$NON-NLS-1$

	public static String ProjectCreationPreferencesPage_Settings;
	public static String ProjectCreationPreferencesPage_TemplateFolder;
	public static String ProjectCreationPreferencesPage_Choose;
	public static String ProjectCreationPreferencesPage_FolderChoice;
	public static String ProjectCreationPreferencesPage_Selection;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
