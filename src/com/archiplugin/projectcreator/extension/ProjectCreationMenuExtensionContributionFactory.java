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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import com.archimatetool.model.FolderType;
import com.archimatetool.model.IDiagramModel;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperties;
import com.archiplugin.projectcreator.Activator;
import com.archiplugin.projectcreator.preferences.ProjectCreatorPreferenceConstants;
import com.archiplugin.projectcreator.project.CreateNewProject;
import com.archiplugin.projectcreator.project.CreateViewFromTemplate;
import com.archiplugin.projectcreator.project.ProjectDefinition;
import com.archiplugin.projectcreator.project.ViewDefinition;

public class ProjectCreationMenuExtensionContributionFactory extends ExtensionContributionFactory {

	private IFolder fCurrentFolder;
	private IFolder templateFolder;

	public ProjectCreationMenuExtensionContributionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		var selectionService = serviceLocator.getService(ISelectionService.class);
		var selection = (IStructuredSelection) selectionService.getSelection();

		fCurrentFolder = currentFolder(selection).orElse(null);

		findViewFolder().flatMap(views -> findProjectTemplateIn(views)).ifPresent(folder -> {
			templateFolder = folder;
			additions.addContributionItem(new Separator(), isVisibibleIfDiagramFolder);

			var newProjectFromTemplate = new ActionContributionItem(new NewProjectFromTemplateAction(templateFolder));
			additions.addContributionItem(newProjectFromTemplate, isVisibibleIfDiagramFolder);

			if (templateFolder == null) {
				return;
			}

			templateFolder.getElements().forEach(o -> {
				if (o instanceof IDiagramModel) {
					IDiagramModel diagramTemplate = (IDiagramModel) o;

					var newViewFromTemplate = new ActionContributionItem(
							new NewViewFromTemplateAction(diagramTemplate));
					additions.addContributionItem(newViewFromTemplate, isVisibibleIfDiagramFolder);
				}
			});
		});

	}

	private class NewProjectFromTemplateAction extends Action {
		private final IFolder templateFolder;

		NewProjectFromTemplateAction(IFolder templateFolder) {
			this.templateFolder = templateFolder;
		}

		@Override
		public String getText() {
			return Messages.NewProjectFromTemplateMenuEntry;
		}

		@Override
		public void run() {
			var templatePropertyKeys = propertyKeysOf(templateFolder);

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

	private class NewViewFromTemplateAction extends Action {
		private final IDiagramModel templateDiagram;

		NewViewFromTemplateAction(IDiagramModel templateDiagram) {
			this.templateDiagram = templateDiagram;
		}

		@Override
		public String getText() {
			return Messages.NewViewFromTemplateMenuEntry + ": " + templateDiagram.getName();
		}

		@Override
		public void run() {
			var templatePropertyKeys = propertyKeysOf(templateDiagram);

			Command cmd = CreateViewFromTemplate.from(fCurrentFolder, templateDiagram, new ViewDefinition("New View",
					templatePropertyKeys.stream().collect(Collectors.toMap(k -> k, k -> ""))));
			CommandStack commandStack = (CommandStack) fCurrentFolder.getAdapter(CommandStack.class);
			commandStack.execute(cmd);

		}

		@Override
		public String getId() {
			return "newViewFromTemplateAction"; //$NON-NLS-1$
		};

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.projectImage();
		}
	};

	private Expression isVisibibleIfDiagramFolder = new Expression() {
		@Override
		public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
			return fCurrentFolder != null ? EvaluationResult.TRUE : EvaluationResult.FALSE;
		}
	};

	private Optional<IFolder> currentFolder(IStructuredSelection selection) {
		var firstElement = selection.getFirstElement();

		if (firstElement instanceof IFolder && isInDiagramFolder((IFolder) firstElement)) {
			return Optional.of((IFolder) firstElement);
		} else if (firstElement instanceof IDiagramModel) {
			return Optional.of((IFolder) ((IDiagramModel) firstElement).eContainer());
		}

		return Optional.empty();
	}

	private boolean isNonEmptyList(Object o) {
		return o instanceof List<?> && ((List<?>) o).size() > 0;
	}

	private Optional<IFolder> findViewFolder() {
		return Optional.ofNullable(fCurrentFolder).map(f -> {
			var currentFolder = f;
			while (!FolderType.DIAGRAMS.equals(currentFolder.getType())) {
				currentFolder = (IFolder) currentFolder.eContainer();
			}

			return currentFolder;
		});
	}

	public List<String> propertyKeysOf(IProperties template) {
		return template.getProperties().stream().map(p -> p.getKey()).toList();
	}

	private Optional<IFolder> findProjectTemplateIn(IFolder viewsFolder) {
		var templateFolderId = Activator.INSTANCE.getPreferenceStore()
				.getString(ProjectCreatorPreferenceConstants.PROJECT_CREATION_TEMPLATE_FOLDER);

		for (Iterator<IFolder> iterator = viewsFolder.getFolders().iterator(); iterator.hasNext();) {
			IFolder f = iterator.next();

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
