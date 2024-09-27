package com.archiplugin.projectcreator.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IFolder;

public class Folders {

	public static Result<Folder, String> findFolderById(String id) {
		var models = IEditorModelManager.INSTANCE.getModels();

		var idToFolders = models.stream().map(model -> {
			var viewsFolder = model.getFolder(FolderType.DIAGRAMS);

			return transitiveCollect(Map.of(viewsFolder.getId(), new Folder(model.getName(), viewsFolder)));
		}).reduce(new HashMap<>(), (m, n) -> {
			m.putAll(n);
			return m;
		});

		return Optional.ofNullable(idToFolders.get(id)).map(f -> Result.<Folder, String>succeededWith(f))
				.orElse(Result.failedWith("There is no folder with ID: " + id));
	}

	public static Views getAllViewsIn(IFolder folder) {
		var result = new ArrayList<IDiagramModel>();

		var transitiveFolders = transitiveCollect(Map.of(folder.getId(), new Folder("", folder)));

		transitiveFolders.entrySet().forEach(entry -> {
			entry.getValue().folder().getElements().stream().filter(e -> e instanceof IDiagramModel)
					.forEach(e -> result.add((IDiagramModel) e));
		});

		return new Views(result);
	}

	private static Map<String, Folder> transitiveCollect(Map<String, Folder> idToFolders) {
		var result = new HashMap<String, Folder>(idToFolders);

		idToFolders.entrySet().forEach(idToFolder -> {
			if (!idToFolder.getValue().getSubfolders().isEmpty()) {
				idToFolder.getValue().getSubfolders().stream().forEach(f -> result.putAll(
						transitiveCollect(Map.of(f.getId(), new Folder(idToFolder.getValue().modelname(), f)))));
			}

		});

		return result;
	}

	public static record Folder(String modelname, IFolder folder) {
		private List<IFolder> getSubfolders() {
			return folder.getFolders();
		}
	};

	public static record Views(List<IDiagramModel> views) {
		public boolean areEmpty() {
			return views.isEmpty();
		}

		public Map<String, String> viewIdsToName() {
			return views.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getName()));
		}
		
		public void rename(Map<String, String> viewIdsToNewNames) {
			views.stream().forEach(v -> {
				var newName = viewIdsToNewNames.get(v.getId());
				if (newName != null) {
					v.setName(newName);
				}
			});
		}
	};
}
