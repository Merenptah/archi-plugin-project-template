package com.archiplugin.projectcreator.project.lifecycle;

import java.util.Optional;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.archimatetool.editor.views.tree.commands.MoveFolderCommand;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperty;

public class MoveProject extends Command {

	private IFolder newParent;
	private IFolder projectFolder;
	private MandatoryPropertiesDefinition mandatoryPropertiesDefinition;
	private Command moveFolderCommand;

	private MoveProject(IFolder newParent, IFolder projectFolder,
			MandatoryPropertiesDefinition mandatoryPropertiesDefinition) {
		super();
		this.newParent = newParent;
		this.projectFolder = projectFolder;
		this.mandatoryPropertiesDefinition = mandatoryPropertiesDefinition;
	}

	public static MoveProject to(IFolder newParent, IFolder projectFolder,
			MandatoryPropertiesDefinition mandatoryPropertiesDefinition) {
		return new MoveProject(newParent, projectFolder, mandatoryPropertiesDefinition);
	}

	@Override
	public void execute() {
		var mandatoryPropertiesPopup = new LifecycleMandatoryPropertiesDialog(shell(), mandatoryPropertiesDefinition);
		if (mandatoryPropertiesPopup.open() == Window.OK) {
			var folderProps = this.projectFolder.getProperties();
			mandatoryPropertiesPopup.getInputFieldValues().entrySet().forEach(e -> {
				findProperty(e.getKey(), folderProps).ifPresentOrElse(p -> {
					p.setValue(e.getValue());
				}, () -> {
					var prop = IArchimateFactory.eINSTANCE.createProperty();
					prop.setKey(e.getKey());
					prop.setValue(e.getValue());
					folderProps.add(prop);
				});
			});
		}
		this.moveFolderCommand = new MoveFolderCommand(this.newParent, this.projectFolder);
		this.moveFolderCommand.execute();
	}

	private Shell shell() {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		Shell shell = (activeWindow != null) ? activeWindow.getShell() : null;
		return shell;
	}

	private Optional<IProperty> findProperty(String key, EList<IProperty> props) {
		return props.stream().filter(p -> p.getKey().equals(key)).findFirst();
	}

	@Override
	public void redo() {
		if (this.moveFolderCommand != null) {
			this.moveFolderCommand.execute();
		}
	}

	@Override
	public void undo() {
		if (this.moveFolderCommand != null) {
			this.moveFolderCommand.undo();
		}
	}
}
