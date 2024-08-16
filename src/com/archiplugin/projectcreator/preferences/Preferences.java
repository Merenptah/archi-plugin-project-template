package com.archiplugin.projectcreator.preferences;

import org.eclipse.jface.preference.IPreferenceStore;

import com.archiplugin.projectcreator.Activator;

public class Preferences {
	private static String PROJECT_CREATION_TEMPLATE_FOLDER = "ProjectCreationTemplateFolder";
	private static String PROJECT_LIFECYCLE_FROM_FOLDER = "ProjectLifeCycleFromFolder";
	private static String PROJECT_LIFECYCLE_TO_FOLDER = "ProjectLifeCycleToFolder";

	static void setDefault() {
		preferenceStore().setDefault(PROJECT_CREATION_TEMPLATE_FOLDER, "");
	}

	private static IPreferenceStore preferenceStore() {
		return Activator.INSTANCE.getPreferenceStore();
	}

	public static String getTemplateFolderId() {
		return preferenceStore().getString(PROJECT_CREATION_TEMPLATE_FOLDER);
	}

	public static void setTemplateFolderId(String id) {
		preferenceStore().setValue(PROJECT_CREATION_TEMPLATE_FOLDER, id);
	}

	public static String getLifecycleFromFolderId() {
		return preferenceStore().getString(PROJECT_LIFECYCLE_FROM_FOLDER);
	}

	public static void setLifecycleFromFolderId(String id) {
		preferenceStore().setValue(PROJECT_LIFECYCLE_FROM_FOLDER, id);
	}

	public static String getLifecycleToFolderId() {
		return preferenceStore().getString(PROJECT_LIFECYCLE_TO_FOLDER);
	}

	public static void setLifecycleToFolderId(String id) {
		preferenceStore().setValue(PROJECT_LIFECYCLE_TO_FOLDER, id);
	}

}
