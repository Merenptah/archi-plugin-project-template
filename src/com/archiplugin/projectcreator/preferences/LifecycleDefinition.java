package com.archiplugin.projectcreator.preferences;

import java.util.List;

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
		return this.fromFolder.getArchimateModel().getName() + ":" + this.fromFolder.getName();
	}

	public String getFromFolderId() {
		return this.fromFolder.getId();
	}

	public String getToFolderName() {
		return this.toFolder.getArchimateModel().getName() + ":" + this.toFolder.getName();
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
