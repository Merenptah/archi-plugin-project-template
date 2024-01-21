package com.archiplugin.projectcreator.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.PreferencePage;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IFolder;
import com.archiplugin.projectcreator.Activator;

public class ProjectCreationPreferencesPage extends PreferencePage
		implements IWorkbenchPreferencePage, ProjectCreatorPreferenceConstants {

	private ComboViewer templateSelector;

	public ProjectCreationPreferencesPage() {
		setPreferenceStore(Activator.INSTANCE.getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite page = createPage(parent);

		Group settingsGroup = settingsGroupOn(page, Messages.ProjectCreationPreferencesPage_Settings);

		createLabelIn(settingsGroup, Messages.ProjectCreationPreferencesPage_TemplateFolder);

		templateSelector = createTemplateSelectorIn(settingsGroup);
		setSelectionAndSelectableValuesOfTemplateSelector();

		return page;
	}

	private void createLabelIn(Group settingsGroup, String text) {
		Label label = new Label(settingsGroup, SWT.NULL);
		label.setText(text);
	}

	private ComboViewer createTemplateSelectorIn(Group settingsGroup) {
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
				var folder = (ModelViewFolder) element;

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
		settingsGroup.setLayout(new GridLayout(3, false));

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
		var models = IEditorModelManager.INSTANCE.getModels();

		Map<String, List<IFolder>> modelNameToTopLevelFolders = models.stream()
				.collect(Collectors.toMap(m -> m.getName(), m -> m.getFolders()));
		var selectableValues = flattenHierarchy(modelNameToTopLevelFolders);

		templateSelector.setInput(selectableValues.toArray());

		var templateFolder = getPreferenceStore().getString(PROJECT_CREATION_TEMPLATE_FOLDER);
		selectableValues.stream().filter(f -> f.folder().getId().equals(templateFolder)).findFirst()
				.ifPresent(s -> templateSelector.setSelection(new StructuredSelection(s)));
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
	public boolean performOk() {
		var selectedFolder = (ModelViewFolder) ((IStructuredSelection) templateSelector.getSelection())
				.getFirstElement();
		getPreferenceStore().setValue(PROJECT_CREATION_TEMPLATE_FOLDER, selectedFolder.folder.getId());
		return true;
	}

	@Override
	protected void performDefaults() {
		var templateFolder = getPreferenceStore().getString(PROJECT_CREATION_TEMPLATE_FOLDER);
		var input = (ModelViewFolder[]) templateSelector.getInput();
		List.of(input).stream().filter(f -> f.folder().getId().equals(templateFolder)).findFirst()
				.ifPresent(s -> templateSelector.setSelection(new StructuredSelection(s)));

		super.performDefaults();
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}