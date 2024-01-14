package com.archiplugin.projectcreator.project;

import java.util.Optional;

import org.eclipse.gef.commands.Command;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IFolder;

public class CreateNewProject extends Command {

	private final IFolder parentFolder;
	private final ProjectDefinition projectDefinition;
	private IFolder newFolder;

	private CreateNewProject(IFolder parentFolder, ProjectDefinition projectDefinition) {
		super();
		this.parentFolder = parentFolder;
		this.projectDefinition = projectDefinition;
	}

	public static CreateNewProject from(IFolder parent, ProjectDefinition projectDefinition) {
		return new CreateNewProject(parent, projectDefinition);
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
		IFolder newFolder = IArchimateFactory.eINSTANCE.createFolder();
		newFolder.setName(projectDefinition.name());
		newFolder.setType(FolderType.USER);
		projectDefinition.properties().entrySet().forEach(e -> {
			var prop = IArchimateFactory.eINSTANCE.createProperty();
			prop.setKey(e.getKey());
			prop.setValue(e.getValue());
			newFolder.getProperties().add(prop);
		});

		this.newFolder = newFolder;

		parentFolder.getFolders().add(newFolder);
	}

}
