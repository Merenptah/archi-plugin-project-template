package com.archiplugin.projectcreator.project;

import java.util.Map;
import java.util.function.Consumer;

import com.archimatetool.model.IArchimateModelObject;

public class ProjectDefinition {
	private final ProjectTemplateDefinition template;
	private final Map<String, String> properties;

	public ProjectDefinition(ProjectTemplateDefinition template, Map<String, String> properties) {
		this.template = template;
		this.properties = properties;
	}

	public String name(IArchimateModelObject object) {
		return template.resolveName(this, object);
	}

	public void updatePropertiesAndName(Consumer<Map<String, String>> propertiesUpdater, Consumer<String> nameUpdater,
			IArchimateModelObject object) {

		propertiesUpdater.accept(properties);
		nameUpdater.accept(template.resolveName(this, object));
	}
}
