/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archiplugin.projectcreator.extension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IFolder;
import com.archiplugin.projectcreator.project.CreateNewProject;
import com.archiplugin.projectcreator.project.ProjectDefinition;

public class ProjectCreationMenuExtensionContributionFactory extends ExtensionContributionFactory {

	private IFolder fCurrentFolder;
	private Optional<IFolder> viewsFolder;

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
			viewsFolder = findViewFolder();
			var templates = findProjectTemplates();

			var templatePropertyKeys = propertyKeysOf(templates);
			// Execute Command
			Command cmd = CreateNewProject.from(fCurrentFolder, new ProjectDefinition("Dummy",
					templatePropertyKeys.stream().collect(Collectors.toMap(k -> k, k -> ""))));
			CommandStack commandStack = (CommandStack) fCurrentFolder.getAdapter(CommandStack.class);
			commandStack.execute(cmd);
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

				if (o instanceof IFolder && isInDiagramFolder((IFolder) o)) {
					fCurrentFolder = (IFolder) o;
				} else if (o instanceof IDiagramModel) {
					fCurrentFolder = (IFolder) ((IDiagramModel) o).eContainer();
				}
			}

			return fCurrentFolder != null ? EvaluationResult.TRUE : EvaluationResult.FALSE;
		}
	};

	private Optional<IFolder> findViewFolder() {
		return Optional.ofNullable(fCurrentFolder).map(f -> {
			var currentFolder = f;
			while (!FolderType.DIAGRAMS.equals(currentFolder.getType())) {
				currentFolder = (IFolder) currentFolder.eContainer();
			}

			return currentFolder;
		});
	}

	public List<String> propertyKeysOf(List<IFolder> templates) {
		return Optional.ofNullable(templates.get(0)).map(t -> t.getProperties())
				.map(props -> props.stream().map(p -> p.getKey()).toList()).orElse(List.of());
	}

	private List<IFolder> findProjectTemplates() {
		return viewsFolder.flatMap((IFolder views) -> views.getFolders().stream()
				.filter(f -> f.getName().contains("Templates")).findFirst()).map(f -> f.getFolders())
				.orElse(new BasicEList<IFolder>());
	}

	private boolean isInDiagramFolder(IFolder folder) {
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
