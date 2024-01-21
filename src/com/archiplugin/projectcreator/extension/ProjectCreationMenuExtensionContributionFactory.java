/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archiplugin.projectcreator.extension;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IFolder;
import com.archiplugin.projectcreator.Activator;
import com.archiplugin.projectcreator.preferences.ProjectCreatorPreferenceConstants;
import com.archiplugin.projectcreator.project.CreateNewProject;
import com.archiplugin.projectcreator.project.ProjectDefinition;

public class ProjectCreationMenuExtensionContributionFactory extends ExtensionContributionFactory {

	private IFolder fCurrentFolder;
	private Optional<IFolder> viewsFolder;

	public ProjectCreationMenuExtensionContributionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		additions.addContributionItem(new Separator(), isVisibibleIfDiagramFolder);

		var newProjectFromTemplate = new ActionContributionItem(new NewProjectFromTemplateAction());
		additions.addContributionItem(newProjectFromTemplate, isVisibibleIfDiagramFolder);
	}

	private class NewProjectFromTemplateAction extends Action {
		@Override
		public String getText() {
			return Messages.NewProjectFromTemplateMenuEntry;
		}

		@Override
		public void run() {
			findViewFolder().ifPresent(views -> {
				findProjectTemplateIn(views).ifPresent(template -> {
					var templatePropertyKeys = propertyKeysOf(template);
					// Execute Command
					Command cmd = CreateNewProject.from(fCurrentFolder, new ProjectDefinition("Dummy",
							templatePropertyKeys.stream().collect(Collectors.toMap(k -> k, k -> ""))));
					CommandStack commandStack = (CommandStack) fCurrentFolder.getAdapter(CommandStack.class);
					commandStack.execute(cmd);
				});

			});

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
			Object defaultVariable = context.getDefaultVariable();

			fCurrentFolder = null;

			if (!isNonEmptyList(defaultVariable)) {
				return EvaluationResult.FALSE;
			}

			Object firstElement = ((List<?>) defaultVariable).get(0);

			if (firstElement instanceof IFolder && isInDiagramFolder((IFolder) firstElement)) {
				fCurrentFolder = (IFolder) defaultVariable;
			} else if (firstElement instanceof IDiagramModel) {
				fCurrentFolder = (IFolder) ((IDiagramModel) firstElement).eContainer();
			}

			return fCurrentFolder != null ? EvaluationResult.TRUE : EvaluationResult.FALSE;
		}

		private boolean isNonEmptyList(Object o) {
			return o instanceof List<?> && ((List<?>) o).size() > 0;
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

	public List<String> propertyKeysOf(IFolder template) {
		return template.getProperties().stream().map(p -> p.getKey()).toList();
	}

	private Optional<IFolder> findProjectTemplateIn(IFolder viewsFolder) {
		var templateFolderId = Activator.INSTANCE.getPreferenceStore()
				.getString(ProjectCreatorPreferenceConstants.PROJECT_CREATION_TEMPLATE_FOLDER);

		for (Iterator iterator = viewsFolder.getFolders().iterator(); iterator.hasNext();) {
			IFolder f = (IFolder) iterator.next();

			if (f.getId().equals(templateFolderId)) {
				return Optional.of(f);
			}

			var intermediateResult = findProjectTemplateIn(f);
			if (intermediateResult.isPresent()) {
				return intermediateResult;
			}

		}

		return Optional.empty();
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
