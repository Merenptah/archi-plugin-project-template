package com.archiplugin.projectcreator.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "com.archiplugin.projectcreator.preferences.messages"; //$NON-NLS-1$

	public static String ProjectCreationPreferencesPage_Template_Settings;
	public static String ProjectCreationPreferencesPage_Template_Folder;

	public static String ProjectCreationPreferencesPage_Lifecycle_Settings;
	public static String ProjectCreationPreferencesPage_Lifecycle_FromFolder;
	public static String ProjectCreationPreferencesPage_Lifecycle_ToFolder;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
