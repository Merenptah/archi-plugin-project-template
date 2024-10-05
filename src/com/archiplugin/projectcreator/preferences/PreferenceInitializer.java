package com.archiplugin.projectcreator.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.archimatetool.editor.preferences.IPreferenceConstants;

public class PreferenceInitializer extends AbstractPreferenceInitializer implements IPreferenceConstants {

	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefault();
	}
}
