package com.archiplugin.projectcreator.project.lifecycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.gef.commands.Command;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IFolder;

public class NewRecursiveFolderCommand extends Command {

	private IFolder fParent;
	private Map<IFolder, IFolder> parentFolderToCreatedFolders = new HashMap<IFolder, IFolder>();
	private List<String> folderNameHierarchy;
	private Optional<IFolder> leafFolder = Optional.empty();

	public NewRecursiveFolderCommand(IFolder parent, List<String> folderNameHierarchy) {
		fParent = parent;
		this.folderNameHierarchy = folderNameHierarchy;
	}

	@Override
	public void execute() {
		var currentParent = fParent;

		for (String folderName : folderNameHierarchy) {
			var existingFolder = currentParent.getFolders().stream()
					.filter(folder -> folder.getName().equals(folderName)).findFirst();

			if (existingFolder.isPresent()) {
				currentParent = existingFolder.get();
				continue;
			}

			IFolder newFolder = IArchimateFactory.eINSTANCE.createFolder();
			newFolder.setName(folderName);
			newFolder.setType(FolderType.USER);

			parentFolderToCreatedFolders.put(currentParent, newFolder);
			currentParent.getFolders().add(newFolder);
			currentParent = newFolder;
		}

		if (!folderNameHierarchy.isEmpty()) {
			leafFolder = Optional.of(currentParent);
		}
	}

	public Optional<IFolder> getLeaf() {
		return leafFolder;
	}

	@Override
	public void redo() {
		super.execute();
	}

	@Override
	public void undo() {
		parentFolderToCreatedFolders.forEach((parent, child) -> parent.getFolders().remove(child));
	}

}
