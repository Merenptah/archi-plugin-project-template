/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archiplugin.projectcreator.extension;

import org.eclipse.jface.resource.ImageDescriptor;

import com.archimatetool.editor.ui.ImageFactory;
import com.archiplugin.projectcreator.Activator;

public abstract class Images {

	private static String IMGPATH = "img/"; //$NON-NLS-1$

	private static String ICON_NEW_PROJECT = IMGPATH + "new_project.png"; //$NON-NLS-1$
	private static String ICON_NEW_VIEW = IMGPATH + "diagram.png"; //$NON-NLS-1$

	public static ImageDescriptor projectImage() {
		return new ImageFactory(Activator.INSTANCE).getImageDescriptor(ICON_NEW_PROJECT);
	}

	public static ImageDescriptor viewImage() {
		return new ImageFactory(Activator.INSTANCE).getImageDescriptor(ICON_NEW_VIEW);
	}
}
