package com.archiplugin.projectcreator.project.lifecycle;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.views.tree.commands.MoveFolderCommand;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperty;
import com.archiplugin.projectcreator.preferences.Preferences;
import com.archiplugin.projectcreator.project.Folders;
import com.archiplugin.projectcreator.project.Folders.Views;
import com.archiplugin.projectcreator.project.creation.ProjectDefinition;
import com.archiplugin.projectcreator.project.creation.ProjectTemplateDefinition;

public class MoveProject extends Command {

	private IFolder newParent;
	private IFolder projectFolder;
	private Views views;
	private MandatoryPropertiesDefinition mandatoryPropertiesDefinition;
	private Command moveFolderCommand;

	private List<IProperty> oldProperties;
	private String oldName;
	private Map<String, String> oldViewNames;

	private MoveProject(IFolder newParent, IFolder projectFolder,
			MandatoryPropertiesDefinition mandatoryPropertiesDefinition) {
		super();
		this.newParent = newParent;
		this.projectFolder = projectFolder;
		this.mandatoryPropertiesDefinition = mandatoryPropertiesDefinition;
		this.views = Folders.getAllViewsIn(projectFolder);

		this.oldProperties = projectFolder.getProperties().stream().map(p -> {
			var prop = IArchimateFactory.eINSTANCE.createProperty();
			prop.setKey(p.getKey());
			prop.setValue(p.getValue());
			return prop;
		}).collect(Collectors.toList());

		this.oldName = projectFolder.getName();
		this.oldViewNames = views.viewIdsToName();
	}

	public static MoveProject to(IFolder newParent, IFolder projectFolder,
			MandatoryPropertiesDefinition mandatoryPropertiesDefinition) {
		return new MoveProject(newParent, projectFolder, mandatoryPropertiesDefinition);
	}

	@Override
	public void execute() {
		propertyUpdate().ifPresent(propertyUpdater -> {
			viewNamesUpdate().ifPresent(viewNamesUpdater -> {
				this.moveFolderCommand = new MoveFolderCommand(this.newParent, this.projectFolder);
				this.moveFolderCommand.execute();

				propertyUpdater.run();
				this.projectFolder.setName(this.createNewName(this.projectFolder));
				viewNamesUpdater.run();
			});
		});

	}

	private Optional<Runnable> viewNamesUpdate() {
		var views = Folders.getAllViewsIn(projectFolder);

		if (views.areEmpty()) {
			return Optional.of(() -> {
			});
		}

		var updatedViewNamesPopup = new LifecycleViewNameUpdateDialog(shell(), views.viewIdsToName());
		if (updatedViewNamesPopup.open() == Window.OK) {
			return Optional.of(() -> views.rename(updatedViewNamesPopup.getUpdatedViewNames()));
		}
		return Optional.empty();
	}

	private Optional<Runnable> propertyUpdate() {
		var folderProps = this.projectFolder.getProperties();
		var propertiesWithSetValues = folderProps.stream().filter(p -> p.getValue() != null && !p.getValue().isBlank())
				.map(p -> p.getKey()).collect(Collectors.toList());

		var mandatoryProperties = mandatoryPropertiesDefinition.without(propertiesWithSetValues);

		if (mandatoryProperties.isEmpty()) {
			return Optional.of(() -> {
			});
		}

		var mandatoryPropertiesPopup = new LifecycleMandatoryPropertiesDialog(shell(), mandatoryProperties);
		if (mandatoryPropertiesPopup.open() == Window.OK) {
			return Optional.of(() -> updateProperties(folderProps, mandatoryPropertiesPopup));
		}

		return Optional.empty();
	}

	private void updateProperties(EList<IProperty> folderProps,
			LifecycleMandatoryPropertiesDialog mandatoryPropertiesPopup) {
		mandatoryPropertiesPopup.getInputFieldValues().entrySet().forEach(e -> {
			findProperty(e.getKey(), folderProps).ifPresentOrElse(p -> {
				p.setValue(e.getValue());
			}, () -> {
				var prop = IArchimateFactory.eINSTANCE.createProperty();
				prop.setKey(e.getKey());
				prop.setValue(e.getValue());
				folderProps.add(prop);
			});
		});
	}

	private String createNewName(IFolder projectFolder) {
		return Folders.findFolderById(Preferences.getTemplateFolderId()).mapSuccess(f -> {
			var templateProperties = f.folder().getProperties().stream()
					.collect(Collectors.toMap(IProperty::getKey, IProperty::getValue));
			var projectsProperties = projectFolder.getProperties().stream()
					.collect(Collectors.toMap(IProperty::getKey, IProperty::getValue));
			return new ProjectDefinition(new ProjectTemplateDefinition(templateProperties), projectsProperties);
		}).mapSuccess(def -> def.name(projectFolder, projectFolder.getName())).recover(projectFolder.getName());
	}

	private Shell shell() {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Shell shell = (activeWindow != null) ? activeWindow.getShell() : null;
		return shell;
	}

	private Optional<IProperty> findProperty(String key, EList<IProperty> props) {
		return props.stream().filter(p -> p.getKey().equals(key)).findFirst();
	}

	@Override
	public void redo() {
		if (this.moveFolderCommand != null) {
			this.moveFolderCommand.execute();
		}
	}

	@Override
	public void undo() {
		if (this.moveFolderCommand != null) {
			this.moveFolderCommand.undo();
			this.projectFolder.getProperties().clear();
			this.projectFolder.getProperties().addAll(oldProperties);
			this.projectFolder.setName(oldName);
			this.views.rename(oldViewNames);
		}
	}
}
