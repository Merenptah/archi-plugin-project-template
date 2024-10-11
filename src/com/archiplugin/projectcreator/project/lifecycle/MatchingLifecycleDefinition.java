package com.archiplugin.projectcreator.project.lifecycle;

import java.util.List;

import com.archiplugin.projectcreator.preferences.LifecycleDefinition;

public record MatchingLifecycleDefinition(LifecycleDefinition lifecycleDefinition, List<String> subPath) {

}
