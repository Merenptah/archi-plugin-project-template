package com.archiplugin.projectcreator.project.lifecycle;

import java.util.List;
import java.util.stream.Collectors;

public record MandatoryPropertiesDefinition(List<String> properties) {

	public List<String> properties() {
		return properties;
	}

	public List<String> without(List<String> propertyNames) {
		return properties.stream().filter(p -> !propertyNames.contains(p)).collect(Collectors.toList());
	}

}
