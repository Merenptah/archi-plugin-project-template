package com.archiplugin.projectcreator.project;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record ProjectTemplateDefinition(Map<String, String> properties) {
	private final static String NAME_TEMPLATE_FIELD = "_NAME_TEMPLATE";

	public String resolveName(ProjectDefinition projectDefinition) {
		return Optional.ofNullable(properties.get(NAME_TEMPLATE_FIELD)).map(nameTemplate -> {
			var toBeReplaced = Pattern.compile("\\$\\{([^\\}]+)\\}");
			return toBeReplaced.matcher(nameTemplate).replaceAll(r -> {
				return projectDefinition.properties().get(r.group(1));
			});
		}).orElse("Dummy");
	}

	public Map<String, String> properties() {
		return properties.entrySet().stream().filter(e -> !NAME_TEMPLATE_FIELD.equals(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}
