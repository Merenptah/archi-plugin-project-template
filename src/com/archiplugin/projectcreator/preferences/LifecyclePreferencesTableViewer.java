package com.archiplugin.projectcreator.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class LifecyclePreferencesTableViewer extends TableViewer {

	public LifecyclePreferencesTableViewer(Composite parent) {
		super(parent);
		GridDataFactory.create(GridData.FILL_BOTH).hint(SWT.DEFAULT, 200).applyTo(this.getTable());
		this.setContentProvider(new ArrayContentProvider());
		this.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				LifecycleDefinition entry = (LifecycleDefinition) cell.getElement();
				cell.setText(entry.getFromFolderName() + Messages.ProjectCreationPreferencesPage_LifecycleNameSeparator + entry.getToFolderName());
			}
		});
	}

}
