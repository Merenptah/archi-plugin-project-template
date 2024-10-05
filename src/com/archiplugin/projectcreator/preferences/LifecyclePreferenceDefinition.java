package com.archiplugin.projectcreator.preferences;

import java.util.List;

public record LifecyclePreferenceDefinition(String fromFolderId, String toFolderId, List<String> mandatoryProperties) {

}
