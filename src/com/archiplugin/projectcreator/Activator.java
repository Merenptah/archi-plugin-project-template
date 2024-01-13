/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archiplugin.projectcreator;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.archiplugin.projectcreator"; //$NON-NLS-1$

	/**
	 * The shared instance
	 */
	public static Activator INSTANCE;

	public Activator() {
		INSTANCE = this;
	}
}
