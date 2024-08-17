package com.archiplugin.projectcreator.project.lifecycle;

import java.util.List;

public record MandatoryPropertiesDefinition(List<String> properties) {

	public List<String> properties() {
		return properties;
	}

}
