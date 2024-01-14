package com.archiplugin.projectcreator.project;

import java.util.Map;

public record ProjectDefinition(String name, Map<String, String> properties) {

}
