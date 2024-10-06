package com.archiplugin.projectcreator.preferences;

import java.util.List;

public class LifecyclePreferenceDefinition {
	private String fromFolderId;
	private String toFolderId;
	private List<String> mandatoryProperties;

	LifecyclePreferenceDefinition(String fromFolderId, String toFolderId, List<String> mandatoryProperties) {
		this.fromFolderId = fromFolderId;
		this.toFolderId = toFolderId;
		this.mandatoryProperties = mandatoryProperties;
	}

	public String fromFolderId() {
		return fromFolderId;
	}

	public String toFolderId() {
		return toFolderId;
	}

	public List<String> mandatoryProperties() {
		return mandatoryProperties;
	}

}
