package com.archiplugin.projectcreator.project;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.archimatetool.editor.ui.textrender.TextRenderer;
import com.archimatetool.model.IArchimateModelObject;

public record ViewTemplateDefinition(Map<String, String> properties) {
	private final static String NAME_TEMPLATE_FIELD = "_NAME_TEMPLATE";

	public String resolveName(ViewDefinition viewDefinition, IArchimateModelObject object, String defaultName) {
		return Optional.ofNullable(properties.get(NAME_TEMPLATE_FIELD)).map(nameTemplate -> {
			return TextRenderer.getDefault().renderWithExpression(object, nameTemplate);
		}).orElse(defaultName);
	}

	public Map<String, String> properties() {
		return properties.entrySet().stream().filter(e -> !NAME_TEMPLATE_FIELD.equals(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
