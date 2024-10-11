package com.archiplugin.projectcreator.project.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.gef.commands.Command;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IFolder;

public class NewRecursiveFolderCommand extends Command {

	private IFolder fParent;
	private List<IFolder> fFolders;
	private List<String> folderNameHierarchy;

	public NewRecursiveFolderCommand(IFolder parent, List<String> folderNameHierarchy) {
		fParent = parent;
		this.folderNameHierarchy = folderNameHierarchy;
	}

	@Override
	public void execute() {
		fFolders = new ArrayList<IFolder>();
		var currentParent = fParent;

		for (String folderName : folderNameHierarchy) {
			IFolder newFolder = IArchimateFactory.eINSTANCE.createFolder();
			newFolder.setName(folderName);
			newFolder.setType(FolderType.USER);

			fFolders.add(newFolder);
			currentParent.getFolders().add(newFolder);
			currentParent = newFolder;
		}
	}
	
	public Optional<IFolder> getLeaf() {
		if (fFolders == null) {
			return Optional.empty();
		}
		
		return Optional.of(fFolders.get(fFolders.size() - 1));
	}

	@Override
	public void redo() {
		super.execute();
	}

	@Override
	public void undo() {
		if (fFolders == null || fFolders.isEmpty()) {
			return;
		}

		var currentParent = fParent;
		for (var child : fFolders) {
			currentParent.getFolders().remove(child);
			currentParent = child;
		}

	}

}
