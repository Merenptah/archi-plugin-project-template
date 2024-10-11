package com.archiplugin.projectcreator.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IFolder;

public class LifecycleDefinition {
	private IFolder fromFolder;
	private IFolder toFolder;
	private List<String> mandatoryProperties;

	LifecycleDefinition(IFolder fromFolder, IFolder toFolder, List<String> mandatoryProperties) {
		this.fromFolder = fromFolder;
		this.toFolder = toFolder;
		this.mandatoryProperties = mandatoryProperties;
	}

	public String getFromFolderName() {
		return this.fromFolder.getArchimateModel().getName() + ":" + getFolderPath(this.fromFolder);
	}

	public String getFromFolderId() {
		return this.fromFolder.getId();
	}

	public String getToFolderName() {
		var folderName = this.toFolder.eContainer();
		
		return this.toFolder.getArchimateModel().getName() + ":" + getFolderPath(this.toFolder);
	}

	private String getFolderPath(IFolder folder) {
		var folderStack = new ArrayList<String>();
		
		folderStack.add(folder.getName());
		while (folder.eContainer() instanceof IFolder && !((IFolder) folder.eContainer()).getType().equals(FolderType.DIAGRAMS)) {
			folder = (IFolder) folder.eContainer();
			folderStack.add(folder.getName());
		}
		
		Collections.reverse(folderStack);
		
		return folderStack.stream().reduce("", (a,b) -> a + "/" + b);
	}
	
	public String getToFolderId() {
		return this.toFolder.getId();
	}

	public IFolder getToFolder() {
		return toFolder;
	}

	public List<String> getMandatoryProperties() {
		return mandatoryProperties;
	}

}
