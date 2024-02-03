package com.archiplugin.projectcreator.project;

import java.util.Map;

public record ProjectDefinition(ProjectTemplateDefinition template, Map<String, String> properties) {

	public String name() {
		return template.resolveName(this);
	}
}
