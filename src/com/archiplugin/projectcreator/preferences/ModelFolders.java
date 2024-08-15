package com.archiplugin.projectcreator.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.archimatetool.editor.model.IEditorModelManager;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IFolder;

public class ModelFolders {
	public static Result<List<ModelFolder>, String> getAllModelFolders() {
		var models = IEditorModelManager.INSTANCE.getModels();

		var dups = duplicateModelsIn(models);
		if (!dups.isEmpty()) {
			return Result.failedWith("Cannot select folder, duplicate models: " + dups);
		}

		Map<String, List<IFolder>> modelNameToTopLevelFolders = models.stream()
				.collect(Collectors.toMap(m -> m.getName(), m -> m.getFolders()));
		return Result.succeededWith(flattenHierarchy(modelNameToTopLevelFolders));
	}

	private static List<String> duplicateModelsIn(List<IArchimateModel> models) {
		var modelAppearances = models.stream().collect(Collectors.groupingBy(e -> e.getName(), Collectors.counting()));

		return modelAppearances.entrySet().stream().filter(e -> e.getValue() > 1).map(e -> e.getKey())
				.collect(Collectors.toList());

	}

	private static List<ModelFolder> flattenHierarchy(Map<String, List<IFolder>> input) {
		return input.entrySet().stream().flatMap(e -> {
			return e.getValue().stream().filter(v -> v.getType().equals(FolderType.DIAGRAMS)).flatMap(
					v -> dive(v.getFolders().stream().collect(Collectors.toMap(f -> f.getName(), Function.identity())))
							.entrySet().stream().map(d -> new ModelFolder(e.getKey(), d.getKey(), d.getValue())));
		}).toList();
	}

	private static Map<String, IFolder> dive(Map<String, IFolder> pathsToFolders) {
		var result = new HashMap<String, IFolder>();

		pathsToFolders.entrySet().forEach(pathToFolder -> {
			result.put(pathToFolder.getKey(), pathToFolder.getValue());
			if (pathToFolder.getValue().getFolders().isEmpty()) {
				return;
			}

			result.putAll(pathToFolder.getValue().getFolders().stream()
					.map(f -> dive(Map.of(pathToFolder.getKey() + "." + f.getName(), f)))
					.reduce(new HashMap<>(), (a, b) -> {
						a.putAll(b);
						return a;
					}));
		});

		return result;
	}

	static record ModelFolder(String modelname, String folderPath, IFolder folder) {
	};
}
