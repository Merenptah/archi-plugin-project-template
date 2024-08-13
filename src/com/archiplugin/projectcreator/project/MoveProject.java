package com.archiplugin.projectcreator.project;

import org.eclipse.gef.commands.Command;

import com.archimatetool.editor.views.tree.commands.MoveFolderCommand;
import com.archimatetool.model.IFolder;

public class MoveProject extends Command {

	private IFolder newParent;
	private IFolder projectFolder;
	private Command moveFolderCommand;

	private MoveProject(IFolder newParent, IFolder projectFolder) {
		super();
		this.newParent = newParent;
		this.projectFolder = projectFolder;
	}

	public static MoveProject to(IFolder newParent, IFolder projectFolder) {
		return new MoveProject(newParent, projectFolder);
	}

	@Override
	public void execute() {
		this.moveFolderCommand = new MoveFolderCommand(this.newParent, this.projectFolder);
		this.moveFolderCommand.execute();
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
