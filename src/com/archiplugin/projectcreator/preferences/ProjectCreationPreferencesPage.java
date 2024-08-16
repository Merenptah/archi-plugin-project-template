package com.archiplugin.projectcreator.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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

public class ProjectCreationPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private ComboViewer templateSelector;

	private List<LifecycleDefinition> lifecyclePreferences = new ArrayList<>();
	private Button lifecycleAddButton;
	private Button lifecycleDeleteButton;
	private TableViewer lifecycleDefinitionTable;

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
		Group settingsGroup = settingsGroupOn(page, Messages.ProjectCreationPreferencesPage_Template_Settings, 2);

		createTemplateSelection(settingsGroup);
	}

	private void createTemplateSelection(Group settingsGroup) {
		createLabelIn(settingsGroup, Messages.ProjectCreationPreferencesPage_Template_Folder);

		templateSelector = createPathSelectorIn(settingsGroup);
		setSelectionAndSelectableValues(templateSelector, Preferences.getTemplateFolderId());
	}

	private void createLifecycleGroup(Composite page) {
		Group lifecycleSettingsGroup = settingsGroupOn(page, Messages.ProjectCreationPreferencesPage_Lifecycle_Settings,
				1);

		createLifecyclePreferenceTable(lifecycleSettingsGroup);

		createAddButton(lifecycleSettingsGroup);

		createDeleteButton(lifecycleSettingsGroup);

	}

	private void createDeleteButton(Group lifecycleSettingsGroup) {
		lifecycleDeleteButton = new Button(lifecycleSettingsGroup, SWT.PUSH);
		setButtonLayoutData(lifecycleDeleteButton);
		lifecycleDeleteButton.setText("Remove lifecycle");
		lifecycleDeleteButton.setEnabled(false);
		lifecycleDeleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				var selectedItem = (LifecycleDefinition) ((IStructuredSelection) lifecycleDefinitionTable
						.getSelection()).getFirstElement();
				lifecyclePreferences.remove(selectedItem);
				lifecycleDefinitionTable.refresh();
			}
		});

		lifecycleDefinitionTable.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				lifecycleDeleteButton.setEnabled(event.getSelection() != null);
			}
		});
	}

	private void createAddButton(Group lifecycleSettingsGroup) {
		lifecycleAddButton = new Button(lifecycleSettingsGroup, SWT.PUSH);
		setButtonLayoutData(lifecycleAddButton);
		lifecycleAddButton.setText("Add lifecycle");
		lifecycleAddButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				var dialog = new LifecycleDefinitionDialog(getShell());
				if (dialog.open() == Window.OK) {
					dialog.getLifecycleDefinition().ifPresent(res -> {
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
				cell.setText(entry.getFromFolderName() + " to " + entry.getToFolderName());
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
							lifecyclePreferences.remove(lifecycleDefinition);
							lifecyclePreferences.add(res);
							lifecycleDefinitionTable.refresh();
						});
					}
				}
			}
		});

		lifecyclePreferences = new ArrayList<>(Preferences.getPreferenceLifecycles().toLifecycles().toList());

		lifecycleDefinitionTable.setInput(lifecyclePreferences);
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

	private Group settingsGroupOn(Composite page, String groupName, int columns) {
		Group settingsGroup = new Group(page, SWT.NULL);
		settingsGroup.setText(groupName);
		settingsGroup.setLayout(new GridLayout(columns, false));

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

	private void setSelectionAndSelectableValues(ComboViewer selector, String folderId) {
		ModelFolders.getAllModelFolders().onSuccessOrElse(selectableValues -> {
			selector.setInput(selectableValues.toArray());
			ModelFolders.findFolderById(folderId).onSuccess(s -> selector.setSelection(new StructuredSelection(s)));
		}, error -> setErrorMessage(error));
	}

	@Override
	public boolean performOk() {
		var selectedTemplateFolder = (ModelFolder) ((IStructuredSelection) templateSelector.getSelection())
				.getFirstElement();
		Preferences.setTemplateFolderId(selectedTemplateFolder.folder().getId());

		Preferences.setPreferenceLifecycles(this.lifecyclePreferences);

		return true;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}