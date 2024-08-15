package com.archiplugin.projectcreator.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IFolder;

public class LifecycleDefinitionDialog extends Dialog {
	private ComboViewer lifecycleFromFolderSelector;
	private ComboViewer lifecycleToFolderSelector;
	private Optional<LifecycleDefinition> lifecycleDefinition = Optional.empty();

	public LifecycleDefinitionDialog(Shell parentShell) {
		super(parentShell);
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
				var folder = (ModelViewFolder) element;

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
		var models = IEditorModelManager.INSTANCE.getModels();

		var dups = duplicateModelsIn(models);
		if (!dups.isEmpty()) {
			MessageDialog.openError(getShell(), "Error",
					"Cannot select project lifecycle folder, duplicate models: " + dups);
			return;
		}

		Map<String, List<IFolder>> modelNameToTopLevelFolders = models.stream()
				.collect(Collectors.toMap(m -> m.getName(), m -> m.getFolders()));
		var selectableValues = flattenHierarchy(modelNameToTopLevelFolders);

		lifecycleFromFolderSelector.setInput(selectableValues.toArray());
	}

	private void setSelectionAndSelectableValuesOfLifecycleToFolderSelector() {
		var models = IEditorModelManager.INSTANCE.getModels();

		var dups = duplicateModelsIn(models);
		if (!dups.isEmpty()) {
			MessageDialog.openError(getShell(), "Error",
					"Cannot select project lifecycle folder, duplicate models: " + dups);
			return;
		}

		Map<String, List<IFolder>> modelNameToTopLevelFolders = models.stream()
				.collect(Collectors.toMap(m -> m.getName(), m -> m.getFolders()));
		var selectableValues = flattenHierarchy(modelNameToTopLevelFolders);

		lifecycleToFolderSelector.setInput(selectableValues.toArray());
	}

	private List<String> duplicateModelsIn(List<IArchimateModel> models) {
		var modelAppearances = models.stream().collect(Collectors.groupingBy(e -> e.getName(), Collectors.counting()));

		return modelAppearances.entrySet().stream().filter(e -> e.getValue() > 1).map(e -> e.getKey())
				.collect(Collectors.toList());

	}

	private List<ModelViewFolder> flattenHierarchy(Map<String, List<IFolder>> input) {
		return input.entrySet().stream().flatMap(e -> {
			return e.getValue().stream().filter(v -> v.getType().equals(FolderType.DIAGRAMS)).flatMap(
					v -> dive(v.getFolders().stream().collect(Collectors.toMap(f -> f.getName(), Function.identity())))
							.entrySet().stream().map(d -> new ModelViewFolder(e.getKey(), d.getKey(), d.getValue())));
		}).toList();
	}

	private Map<String, IFolder> dive(Map<String, IFolder> pathsToFolders) {
		var result = new HashMap<String, IFolder>();

		pathsToFolders.entrySet().forEach(pathToFolder -> {
			result.put(pathToFolder.getKey(), pathToFolder.getValue());
			if (pathToFolder.getValue().getFolders().isEmpty()) {
				return;
			}

			result.putAll(pathToFolder.getValue().getFolders().stream()
					.map(f -> dive(Map.of(pathToFolder.getKey() + "." + f.getName(), f)))
					.reduce(new HashMap<>(), (a, b) -> {
						a.putAll(b);
						return a;
					}));
		});

		return result;
	}

	private static record ModelViewFolder(String modelname, String folderPath, IFolder folder) {
	};

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			var selectedFromFolder = (ModelViewFolder) ((IStructuredSelection) lifecycleFromFolderSelector
					.getSelection()).getFirstElement();
			var selectedToFolder = (ModelViewFolder) ((IStructuredSelection) lifecycleToFolderSelector.getSelection())
					.getFirstElement();
			lifecycleDefinition = Optional.of(new LifecycleDefinition(selectedFromFolder.folderPath,
					selectedFromFolder.folder.getId(), selectedToFolder.folderPath, selectedToFolder.folder().getId()));
		} else {
			lifecycleDefinition = Optional.empty();
		}
		super.buttonPressed(buttonId);
	}
}
