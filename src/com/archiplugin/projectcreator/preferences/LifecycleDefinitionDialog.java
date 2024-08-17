package com.archiplugin.projectcreator.preferences;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.archiplugin.projectcreator.preferences.ModelFolders.ModelFolder;

public class LifecycleDefinitionDialog extends Dialog {
	private ComboViewer lifecycleFromFolderSelector;
	private ComboViewer lifecycleToFolderSelector;
	private Optional<LifecycleDefinition> lifecycleDefinition = Optional.empty();

	public LifecycleDefinitionDialog(Shell parentShell) {
		super(parentShell);
	}

	public LifecycleDefinitionDialog(Shell parentShell, LifecycleDefinition lifecycleDefinition) {
		super(parentShell);

		this.lifecycleDefinition = Optional.of(lifecycleDefinition);
	}

	public Optional<LifecycleDefinition> getLifecycleDefinition() {
		return lifecycleDefinition;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Lifecycle Configuration");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite twoColumnArea = createTwoColumnArea(parent);

		createFromFolderSelection(twoColumnArea);
		createToFolderSelection(twoColumnArea);

		return twoColumnArea;
	}

	private Composite createTwoColumnArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2, false));
		return composite;
	}

	private void createToFolderSelection(Composite parent) {
		createLabelIn(parent,
				com.archiplugin.projectcreator.preferences.Messages.ProjectCreationPreferencesPage_Lifecycle_ToFolder);
		lifecycleToFolderSelector = createPathSelectorIn(parent);
		setSelectionAndSelectableValuesOfLifecycleToFolderSelector();
	}

	private void createFromFolderSelection(Composite parent) {
		createLabelIn(parent,
				com.archiplugin.projectcreator.preferences.Messages.ProjectCreationPreferencesPage_Lifecycle_FromFolder);
		lifecycleFromFolderSelector = createPathSelectorIn(parent);
		setSelectionAndSelectableValuesOfLifecycleFromFolderSelector();
	}

	private void createLabelIn(Composite parent, String text) {
		Label label = new Label(parent, SWT.NULL);
		label.setText(text);
	}

	private ComboViewer createPathSelectorIn(Composite parent) {
		var result = new ComboViewer(parent, SWT.READ_ONLY);

		result.getCombo().setLayoutData(createHorizontalGridData(1));
		result.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
		});
		result.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				var folder = (ModelFolder) element;

				var name = folder.modelname() + ": " + folder.folderPath();

				return name;
			}
		});
		result.setComparator(new ViewerComparator());

		return result;
	}

	private GridData createHorizontalGridData(int span) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		return gd;
	}

	private void setSelectionAndSelectableValuesOfLifecycleFromFolderSelector() {
		ModelFolders.getAllModelFolders().onSuccessOrElse(selectableValues -> {
			lifecycleFromFolderSelector.setInput(selectableValues.toArray());
			lifecycleDefinition.ifPresent(def -> ModelFolders.findFolderById(def.getFromFolderId())
					.onSuccess(s -> lifecycleFromFolderSelector.setSelection(new StructuredSelection(s))));
		}, error -> MessageDialog.openError(getShell(), "Error", error));
	}

	private void setSelectionAndSelectableValuesOfLifecycleToFolderSelector() {
		ModelFolders.getAllModelFolders().onSuccessOrElse(selectableValues -> {
			lifecycleToFolderSelector.setInput(selectableValues.toArray());
			lifecycleDefinition.ifPresent(def -> ModelFolders.findFolderById(def.getToFolderId())
					.onSuccess(s -> lifecycleToFolderSelector.setSelection(new StructuredSelection(s))));
		}, error -> MessageDialog.openError(getShell(), "Error", error));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			var selectedFromFolder = (ModelFolder) ((IStructuredSelection) lifecycleFromFolderSelector.getSelection())
					.getFirstElement();
			var selectedToFolder = (ModelFolder) ((IStructuredSelection) lifecycleToFolderSelector.getSelection())
					.getFirstElement();
			lifecycleDefinition = Optional
					.of(new LifecycleDefinition(selectedFromFolder.folder(), selectedToFolder.folder(), List.of()));
		} else {
			lifecycleDefinition = Optional.empty();
		}
		super.buttonPressed(buttonId);
	}
}
