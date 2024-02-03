package com.archiplugin.projectcreator.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.archimatetool.editor.preferences.IPreferenceConstants;
import com.archiplugin.projectcreator.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer
		implements IPreferenceConstants, ProjectCreatorPreferenceConstants {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.INSTANCE.getPreferenceStore();

		store.setDefault(PROJECT_CREATION_TEMPLATE_FOLDER, "");
	}
}
