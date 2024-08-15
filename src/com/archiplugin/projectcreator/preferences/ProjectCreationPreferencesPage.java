package com.archiplugin.projectcreator.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.archiplugin.projectcreator.Activator;
import com.archiplugin.projectcreator.preferences.ModelFolders.ModelFolder;

public class ProjectCreationPreferencesPage extends PreferencePage
		implements IWorkbenchPreferencePage, ProjectCreatorPreferenceConstants {

	private ComboViewer templateSelector;

	private List<LifecycleDefinition> lifecyclePreferences = new ArrayList<>();
	private Button lifecycleAddButton;
	private TableViewer lifecycleDefinitionTable;
	private ComboViewer firstLifeCycleFromFolderSelector;
	private ComboViewer firstLifeCycleToFolderSelector;

	public ProjectCreationPreferencesPage() {
		setPreferenceStore(Activator.INSTANCE.getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite page = createPage(parent);

		createTemplateGroup(page);

		createLifecycleGroup(page);

		return page;
	}

	private void createTemplateGroup(Composite page) {
		Group settingsGroup = settingsGroupOn(page, Messages.ProjectCreationPreferencesPage_Template_Settings);

		createTemplateSelection(settingsGroup);
	}

	private void createTemplateSelection(Group settingsGroup) {
		createLabelIn(settingsGroup, Messages.ProjectCreationPreferencesPage_Template_Folder);

		templateSelector = createPathSelectorIn(settingsGroup);
		setSelectionAndSelectableValuesOfTemplateSelector();
	}

	private void createLifecycleGroup(Composite page) {
		Group lifecycleSettingsGroup = settingsGroupOn(page,
				Messages.ProjectCreationPreferencesPage_Lifecycle_Settings);
		createFromFolderSelection(lifecycleSettingsGroup);
		createToFolderSelection(lifecycleSettingsGroup);

		createLifecyclePreferenceTable(lifecycleSettingsGroup);

		lifecycleAddButton = new Button(lifecycleSettingsGroup, SWT.PUSH);
		setButtonLayoutData(lifecycleAddButton);
		lifecycleAddButton.setText("Add lifecycle");
		lifecycleAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				var dialog = new LifecycleDefinitionDialog(getShell());
				if (dialog.open() == Window.OK) {
					dialog.getLifecycleDefinition().ifPresent(res -> {
						getPreferenceStore().setValue(PROJECT_LIFECYCLE_FROM_FOLDER, res.fromFolderId());
						getPreferenceStore().setValue(PROJECT_LIFECYCLE_TO_FOLDER, res.toFolderId());
						lifecyclePreferences.add(res);
						lifecycleDefinitionTable.refresh();
					});
				}
			}
		});
	}

	private void createLifecyclePreferenceTable(Group lifecycleSettingsGroup) {
		lifecycleDefinitionTable = new TableViewer(lifecycleSettingsGroup);
		GridDataFactory.create(GridData.FILL_BOTH).hint(SWT.DEFAULT, 200).applyTo(lifecycleDefinitionTable.getTable());
		lifecycleDefinitionTable.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				return lifecyclePreferences.toArray();
			}

		});
		lifecycleDefinitionTable.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				LifecycleDefinition entry = (LifecycleDefinition) cell.getElement();
				cell.setText(entry.fromFolderName() + " to " + entry.toFolderName());
			}
		});
		lifecycleDefinitionTable.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object[] selected = ((IStructuredSelection) lifecycleDefinitionTable.getSelection()).toArray();
				if (!(selected == null || selected.length == 0)) {
					var lifecycleDefinition = (LifecycleDefinition) selected[0];
					var dialog = new LifecycleDefinitionDialog(getShell(), lifecycleDefinition);
					if (dialog.open() == Window.OK) {
						dialog.getLifecycleDefinition().ifPresent(res -> {
							getPreferenceStore().setValue(PROJECT_LIFECYCLE_FROM_FOLDER, res.fromFolderId());
							getPreferenceStore().setValue(PROJECT_LIFECYCLE_TO_FOLDER, res.toFolderId());
							lifecyclePreferences.remove(lifecycleDefinition);
							lifecyclePreferences.add(res);
							lifecycleDefinitionTable.refresh();
						});
					}
				}
			}
		});

		ModelFolders.findFolderById(getPreferenceStore().getString(PROJECT_LIFECYCLE_FROM_FOLDER)).onSuccess(from -> {
			ModelFolders.findFolderById(getPreferenceStore().getString(PROJECT_LIFECYCLE_TO_FOLDER)).onSuccess(to -> {
				lifecyclePreferences.add(new LifecycleDefinition(from.folder().getName(), from.folder().getId(),
						to.folder().getName(), to.folder().getId()));
			});
		});
		lifecycleDefinitionTable.setInput(lifecyclePreferences);
	}

	private void createToFolderSelection(Group lifecycleSettingsGroup) {
		createLabelIn(lifecycleSettingsGroup, Messages.ProjectCreationPreferencesPage_Lifecycle_ToFolder);
		firstLifeCycleToFolderSelector = createPathSelectorIn(lifecycleSettingsGroup);
		setSelectionAndSelectableValuesOfLifecycleToFolderSelector();
	}

	private void createFromFolderSelection(Group lifecycleSettingsGroup) {
		createLabelIn(lifecycleSettingsGroup, Messages.ProjectCreationPreferencesPage_Lifecycle_FromFolder);
		firstLifeCycleFromFolderSelector = createPathSelectorIn(lifecycleSettingsGroup);
		setSelectionAndSelectableValuesOfLifecycleFromFolderSelector();
	}

	private void createLabelIn(Group settingsGroup, String text) {
		Label label = new Label(settingsGroup, SWT.NULL);
		label.setText(text);
	}

	private ComboViewer createPathSelectorIn(Group settingsGroup) {
		var result = new ComboViewer(settingsGroup, SWT.READ_ONLY);

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

	private Group settingsGroupOn(Composite page, String groupName) {
		Group settingsGroup = new Group(page, SWT.NULL);
		settingsGroup.setText(groupName);
		settingsGroup.setLayout(new GridLayout(2, false));

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 500;

		settingsGroup.setLayoutData(gd);

		return settingsGroup;
	}

	private Composite createPage(Composite parent) {
		Composite client = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;

		client.setLayout(layout);

		return client;
	}

	private GridData createHorizontalGridData(int span) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = span;
		return gd;
	}

	private void setSelectionAndSelectableValuesOfTemplateSelector() {
		ModelFolders.getAllModelFolders().onSuccessOrElse(selectableValues -> {
			templateSelector.setInput(selectableValues.toArray());
			var folder = getPreferenceStore().getString(PROJECT_LIFECYCLE_TO_FOLDER);

			ModelFolders.findFolderById(folder)
					.onSuccess(s -> templateSelector.setSelection(new StructuredSelection(s)));
		}, error -> setErrorMessage(error));
	}

	private void setSelectionAndSelectableValuesOfLifecycleFromFolderSelector() {
		ModelFolders.getAllModelFolders().onSuccessOrElse(selectableValues -> {
			firstLifeCycleFromFolderSelector.setInput(selectableValues.toArray());
			var folder = getPreferenceStore().getString(PROJECT_LIFECYCLE_FROM_FOLDER);
			ModelFolders.findFolderById(folder)
					.onSuccess(s -> firstLifeCycleFromFolderSelector.setSelection(new StructuredSelection(s)));
		}, error -> setErrorMessage(error));
	}

	private void setSelectionAndSelectableValuesOfLifecycleToFolderSelector() {
		ModelFolders.getAllModelFolders().onSuccessOrElse(selectableValues -> {
			firstLifeCycleToFolderSelector.setInput(selectableValues.toArray());
			var folder = getPreferenceStore().getString(PROJECT_LIFECYCLE_TO_FOLDER);
			ModelFolders.findFolderById(folder)
					.onSuccess(s -> firstLifeCycleToFolderSelector.setSelection(new StructuredSelection(s)));
		}, error -> setErrorMessage(error));
	}

	@Override
	public boolean performOk() {
		var selectedTemplateFolder = (ModelFolder) ((IStructuredSelection) templateSelector.getSelection())
				.getFirstElement();
		getPreferenceStore().setValue(PROJECT_CREATION_TEMPLATE_FOLDER, selectedTemplateFolder.folder().getId());

		var selectedFromFolder = (ModelFolder) ((IStructuredSelection) firstLifeCycleFromFolderSelector.getSelection())
				.getFirstElement();
		getPreferenceStore().setValue(PROJECT_LIFECYCLE_FROM_FOLDER, selectedFromFolder.folder().getId());
		var selectedToFolder = (ModelFolder) ((IStructuredSelection) firstLifeCycleToFolderSelector.getSelection())
				.getFirstElement();
		getPreferenceStore().setValue(PROJECT_LIFECYCLE_TO_FOLDER, selectedToFolder.folder().getId());
		return true;
	}

	@Override
	protected void performDefaults() {
		var templateFolder = getPreferenceStore().getString(PROJECT_CREATION_TEMPLATE_FOLDER);
		var templateFolderInput = (ModelFolder[]) templateSelector.getInput();
		List.of(templateFolderInput).stream().filter(f -> f.folder().getId().equals(templateFolder)).findFirst()
				.ifPresent(s -> templateSelector.setSelection(new StructuredSelection(s)));

		var lifecycleFromFolder = getPreferenceStore().getString(PROJECT_LIFECYCLE_FROM_FOLDER);
		var lifecycleFromFolderInput = (ModelFolder[]) firstLifeCycleFromFolderSelector.getInput();
		List.of(lifecycleFromFolderInput).stream().filter(f -> f.folder().getId().equals(lifecycleFromFolder))
				.findFirst().ifPresent(s -> firstLifeCycleFromFolderSelector.setSelection(new StructuredSelection(s)));

		var lifecycleToFolder = getPreferenceStore().getString(PROJECT_LIFECYCLE_TO_FOLDER);
		var lifecycleToFolderInput = (ModelFolder[]) firstLifeCycleToFolderSelector.getInput();
		List.of(lifecycleToFolderInput).stream().filter(f -> f.folder().getId().equals(lifecycleToFolder)).findFirst()
				.ifPresent(s -> firstLifeCycleToFolderSelector.setSelection(new StructuredSelection(s)));

		super.performDefaults();
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}