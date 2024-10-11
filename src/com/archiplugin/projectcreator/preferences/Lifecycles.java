package com.archiplugin.projectcreator.preferences;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.archimatetool.model.IFolder;
import com.archiplugin.projectcreator.project.lifecycle.MatchingLifecycleDefinition;

public class Lifecycles {
	private List<LifecycleDefinition> lifecycles;

	protected Lifecycles(List<LifecycleDefinition> lifecycles) {
		this.lifecycles = lifecycles;
	}

	public boolean containsFromFolder(String id) {
		return lifecycles.stream().anyMatch(l -> l.getFromFolderId().equals(id));
	}

	public List<LifecycleDefinition> toList() {
		return List.copyOf(lifecycles);
	}

	public List<MatchingLifecycleDefinition> findMatchingLifecycles(IFolder folder) {
		var result = new ArrayList<MatchingLifecycleDefinition>();

		var subPath = new LinkedList<String>();
		while (folder.eContainer() instanceof IFolder) {
			folder = (IFolder) folder.eContainer();

			result.addAll(findContainingLifecycles(folder.getId(), new ArrayList<>(subPath)));
			subPath.addFirst(folder.getName());
		}

		return result;
	}
	
	private List<MatchingLifecycleDefinition> findContainingLifecycles(String id, List<String> subPath) {
		return lifecycles.stream()
				.filter(l -> l.getFromFolderId().equals(id))
				.map(l -> new MatchingLifecycleDefinition(l, subPath))
				.toList();
	}

}
