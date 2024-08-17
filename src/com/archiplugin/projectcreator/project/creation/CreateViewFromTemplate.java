package com.archiplugin.projectcreator.project.creation;

import java.util.Map.Entry;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.ui.services.EditorManager;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperty;
import com.archimatetool.model.util.UUIDFactory;

public class CreateViewFromTemplate extends Command {

	private final IFolder parentFolder;
	private final ViewTemplateDefinition viewTemplateDefinition;

	private final IDiagramModel originalDiagramModel;
	private IDiagramModel newDiagramModel;

	private CreateViewFromTemplate(IFolder parentFolder, IDiagramModel originalDiagramModel,
			ViewTemplateDefinition viewDefinition) {
		this.parentFolder = parentFolder;
		this.viewTemplateDefinition = viewDefinition;
		this.originalDiagramModel = originalDiagramModel;
	}

	public static CreateViewFromTemplate from(IFolder parentFolder, IDiagramModel originalDiagramModel,
			ViewTemplateDefinition viewDefinition) {
		return new CreateViewFromTemplate(parentFolder, originalDiagramModel, viewDefinition);
	}

	@Override
	public void execute() {
		var creationPopup = new ViewDefinitionDialog(shell(), viewTemplateDefinition);
		if (creationPopup.open() == Window.OK) {
			newDiagramModel = EcoreUtil.copy(originalDiagramModel);
			UUIDFactory.generateNewIDs(newDiagramModel);
			parentFolder.getElements().add(newDiagramModel);
			newDiagramModel.getProperties().clear();

			creationPopup.viewDefinition().updatePropertiesAndName(p -> p.entrySet().forEach(e -> {
				var prop = IArchimateFactory.eINSTANCE.createProperty();

				newDiagramModel.getProperties().add(configured(e, prop));
			}), name -> newDiagramModel.setName(name), newDiagramModel);

		}

	}

	private Shell shell() {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Shell shell = (activeWindow != null) ? activeWindow.getShell() : null;
		return shell;
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

	private IProperty configured(Entry<String, String> e, IProperty prop) {
		prop.setKey(e.getKey());
		prop.setValue(e.getValue());

		return prop;
	}
}
