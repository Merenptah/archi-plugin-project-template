package com.archiplugin.projectcreator.preferences;

import java.util.ArrayList;
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

	private List<LifecycleDefinition> findContainingLifecycles(String id) {
		return lifecycles.stream().filter(l -> l.getFromFolderId().equals(id)).toList();
	}

	public List<LifecycleDefinition> toList() {
		return List.copyOf(lifecycles);
	}

	public List<LifecycleDefinition> findMatchingLifecycles(IFolder folder) {
		var result = new ArrayList<LifecycleDefinition>();

		while (folder.eContainer() instanceof IFolder) {
			folder = (IFolder) folder.eContainer();

			result.addAll(findContainingLifecycles(folder.getId()));
		}

		return result;
	}

}
