package com.archiplugin.projectcreator.project;

import java.util.Optional;

import org.eclipse.gef.commands.Command;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IFolder;

public class CreateNewProject extends Command {

	private final IFolder parentFolder;
	private IFolder newFolder;

	private CreateNewProject(IFolder parentFolder) {
		super();
		this.parentFolder = parentFolder;
	}

	public static CreateNewProject from(IFolder parent) {
		return new CreateNewProject(parent);
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
		newFolder.setName("Dummy");
		newFolder.setType(FolderType.USER);

		this.newFolder = newFolder;

		parentFolder.getFolders().add(newFolder);
	}

}
