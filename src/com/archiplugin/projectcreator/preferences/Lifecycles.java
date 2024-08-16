package com.archiplugin.projectcreator.preferences;

import java.util.List;
import java.util.Optional;

import com.archimatetool.model.IFolder;

public class Lifecycles {
	private List<LifecycleDefinition> lifecycles;

	protected Lifecycles(List<LifecycleDefinition> lifecycles) {
		this.lifecycles = lifecycles;
	}

	public boolean containsFromFolder(String id) {
		return lifecycles.stream().anyMatch(l -> l.getFromFolderId().equals(id));
	}

	private Optional<LifecycleDefinition> findContainingLifecycle(String id) {
		return lifecycles.stream().filter(l -> l.getFromFolderId().equals(id)).findFirst();
	}

	public List<LifecycleDefinition> toList() {
		return List.copyOf(lifecycles);
	}

	public Optional<LifecycleDefinition> findMatchingLifecycle(IFolder folder) {

		while (folder.eContainer() instanceof IFolder) {
			folder = (IFolder) folder.eContainer();

			var match = findContainingLifecycle(folder.getId());
			if (match.isPresent()) {
				return match;
			}
		}

		return Optional.empty();
	}

}
