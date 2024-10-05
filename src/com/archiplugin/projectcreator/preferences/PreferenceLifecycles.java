package com.archiplugin.projectcreator.preferences;

import java.util.ArrayList;
import java.util.Collection;

public class PreferenceLifecycles {
	private Collection<LifecyclePreferenceDefinition> lifecycles;

	protected PreferenceLifecycles(Collection<LifecyclePreferenceDefinition> lifecycles) {
		this.lifecycles = lifecycles;
	}

	public boolean containFromFolder(String id) {
		return lifecycles.stream().anyMatch(l -> l.fromFolderId().equals(id));
	}

	public Lifecycles toLifecycles() {
		var lifecycleDefs = new ArrayList<LifecycleDefinition>();

		this.lifecycles.stream().forEach(prefDef -> {
			ModelFolders.findFolderById(prefDef.fromFolderId())
					.onSuccess(from -> ModelFolders.findFolderById(prefDef.toFolderId()).onSuccess(to -> {
						lifecycleDefs.add(
								new LifecycleDefinition(from.folder(), to.folder(), prefDef.mandatoryProperties()));
					}));
		});

		return new Lifecycles(lifecycleDefs);
	}

}
