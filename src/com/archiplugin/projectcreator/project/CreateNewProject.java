package com.archiplugin.projectcreator.project;

import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperty;

public class CreateNewProject extends Command {

	private final IFolder parentFolder;
	private final ProjectTemplateDefinition projectTemplateDefinition;
	private IFolder newFolder;

	private CreateNewProject(IFolder parentFolder, ProjectTemplateDefinition projectTemplateDefinition) {
		super();
		this.parentFolder = parentFolder;
		this.projectTemplateDefinition = projectTemplateDefinition;
	}

	public static CreateNewProject from(IFolder parent, ProjectTemplateDefinition projectTemplateDefinition) {
		return new CreateNewProject(parent, projectTemplateDefinition);
	}

	@Override
	public void execute() {
		createFolder();
	}

	@Override
	public void redo() {
		createFolder();
	}

	@Override
	public void undo() {
		Optional.ofNullable(newFolder).ifPresent(nf -> parentFolder.getFolders().remove(nf));
	}

	private void createFolder() {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Shell shell = (activeWindow != null) ? activeWindow.getShell() : null;
		var creationPopup = new ProjectDefinitionDialog(shell, projectTemplateDefinition);
		if (creationPopup.open() == Window.OK) {
			IFolder newFolder = IArchimateFactory.eINSTANCE.createFolder();

			parentFolder.getFolders().add(configured(newFolder, creationPopup.projectDefinition()));

			this.newFolder = newFolder;
		}

	}

	private IFolder configured(IFolder newFolder, ProjectDefinition projectDefinition) {
		newFolder.setName(projectDefinition.name());
		newFolder.setType(FolderType.USER);

		projectDefinition.properties().entrySet().forEach(e -> {
			var prop = IArchimateFactory.eINSTANCE.createProperty();

			newFolder.getProperties().add(configured(e, prop));
		});

		return newFolder;
	}

	private IProperty configured(Entry<String, String> e, IProperty prop) {
		prop.setKey(e.getKey());
		prop.setValue(e.getValue());

		return prop;
	}

}
