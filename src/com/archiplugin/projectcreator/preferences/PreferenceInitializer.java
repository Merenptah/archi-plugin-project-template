/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archiplugin.projectcreator.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.archimatetool.editor.preferences.IPreferenceConstants;

public class PreferenceInitializer extends AbstractPreferenceInitializer implements IPreferenceConstants {

	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefault();
	}
}
