package com.archiplugin.projectcreator.project;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.commands.Command;

import com.archimatetool.editor.ui.services.EditorManager;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.util.UUIDFactory;

public class CreateViewFromTemplate extends Command {

	private final IFolder parentFolder;
	private final ViewDefinition viewDefinition;

	private final IDiagramModel originalDiagramModel;
	private IDiagramModel newDiagramModel;

	private CreateViewFromTemplate(IFolder parentFolder, IDiagramModel originalDiagramModel,
			ViewDefinition viewDefinition) {
		this.parentFolder = parentFolder;
		this.viewDefinition = viewDefinition;
		this.originalDiagramModel = originalDiagramModel;
	}

	public static CreateViewFromTemplate from(IFolder parentFolder, IDiagramModel originalDiagramModel,
			ViewDefinition viewDefinition) {
		return new CreateViewFromTemplate(parentFolder, originalDiagramModel, viewDefinition);
	}

	@Override
	public void execute() {
		newDiagramModel = EcoreUtil.copy(originalDiagramModel);
		UUIDFactory.generateNewIDs(newDiagramModel);
		newDiagramModel.setName(viewDefinition.name()); // $NON-NLS-1$

		parentFolder.getElements().add(newDiagramModel);
	}

	@Override
	public void undo() {
		EditorManager.closeDiagramEditor(newDiagramModel);

		parentFolder.getElements().remove(newDiagramModel);
	}

	@Override
	public void redo() {
		parentFolder.getElements().add(newDiagramModel);
	}

	@Override
	public void dispose() {
		newDiagramModel = null;
	}
}
