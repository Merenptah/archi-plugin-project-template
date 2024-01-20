package com.archiplugin.projectcreator.project;

import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.gef.commands.Command;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperty;

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

		parentFolder.getFolders().add(configured(newFolder));

		this.newFolder = newFolder;
	}

	private IFolder configured(IFolder newFolder) {
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
