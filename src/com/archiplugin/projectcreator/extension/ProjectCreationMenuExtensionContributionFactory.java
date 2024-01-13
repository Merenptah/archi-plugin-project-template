/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archiplugin.projectcreator.extension;

import java.util.List;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import com.archimatetool.editor.views.tree.commands.NewFolderCommand;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IArchimateFactory;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IFolder;

public class ProjectCreationMenuExtensionContributionFactory extends ExtensionContributionFactory {

	private IFolder fCurrentFolder;

	public ProjectCreationMenuExtensionContributionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		var item = new ActionContributionItem(new NewProjectFromTemplateAction());
		additions.addContributionItem(new Separator(), isVisibibleIfDiagramFolder);
		additions.addContributionItem(item, isVisibibleIfDiagramFolder);
	}

	private class NewProjectFromTemplateAction extends Action {
		@Override
		public String getText() {
			return Messages.NewProjectFromTemplateMenuEntry;
		}

		@Override
		public void run() {
			// Execute Command
			Command cmd = new NewFolderCommand(fCurrentFolder, createFolder());
			CommandStack commandStack = (CommandStack) fCurrentFolder.getAdapter(CommandStack.class);
			commandStack.execute(cmd);
		}

		private IFolder createFolder() {
			IFolder result = IArchimateFactory.eINSTANCE.createFolder();
			result.setName("Dummy");
			result.setType(FolderType.USER);

			return result;
		}

		@Override
		public String getId() {
			return "newProjectFromTemplateAction"; //$NON-NLS-1$
		};

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.projectImage();
		}
	};

	private Expression isVisibibleIfDiagramFolder = new Expression() {
		@Override
		public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
			Object o = context.getDefaultVariable();

			fCurrentFolder = null;

			if (o instanceof List<?> && ((List<?>) o).size() > 0) {
				o = ((List<?>) o).get(0);

				if (o instanceof IFolder && isDiagramFolder((IFolder) o)) {
					fCurrentFolder = (IFolder) o;
				} else if (o instanceof IDiagramModel) {
					fCurrentFolder = (IFolder) ((IDiagramModel) o).eContainer();
				}
			}

			return fCurrentFolder != null ? EvaluationResult.TRUE : EvaluationResult.FALSE;
		}
	};

	private boolean isDiagramFolder(IFolder folder) {
		if (folder.getType() == FolderType.DIAGRAMS) {
			return true;
		}

		while (folder.eContainer() instanceof IFolder) {
			folder = (IFolder) folder.eContainer();
			if (folder.getType() == FolderType.DIAGRAMS) {
				return true;
			}
		}

		return false;
	}
}
