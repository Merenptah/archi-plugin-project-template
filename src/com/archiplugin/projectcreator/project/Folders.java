package com.archiplugin.projectcreator.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.model.FolderType;
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
}
