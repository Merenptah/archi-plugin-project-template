package com.archiplugin.projectcreator.extension;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.archiplugin.projectcreator.project.ProjectTemplateDefinition;
import com.archiplugin.projectcreator.project.ViewDefinition;

public class ProjectCreationMenuExtensionContributionFactory extends ExtensionContributionFactory {

	public ProjectCreationMenuExtensionContributionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		var selectionService = serviceLocator.getService(ISelectionService.class);
		var selection = (IStructuredSelection) selectionService.getSelection();

		currentFolder(selection).ifPresent(currentFolder -> {
			findViewFolder(currentFolder).flatMap(views -> findProjectTemplateIn(views)).ifPresent(templateFolder -> {
				additions.addContributionItem(new Separator(), null);

				var newProjectFromTemplate = new ActionContributionItem(
						new NewProjectFromTemplateAction(templateFolder, currentFolder));
				additions.addContributionItem(newProjectFromTemplate, null);

				templateFolder.getElements().forEach(o -> {
					if (o instanceof IDiagramModel) {
						IDiagramModel diagramTemplate = (IDiagramModel) o;

						var newViewFromTemplate = new ActionContributionItem(
								new NewViewFromTemplateAction(diagramTemplate, currentFolder));
						additions.addContributionItem(newViewFromTemplate, null);
					}
				});
			});
		});

	}

	private class NewProjectFromTemplateAction extends Action {
		private final IFolder templateFolder;
		private final IFolder parentFolder;

		NewProjectFromTemplateAction(IFolder templateFolder, IFolder parentFolder) {
			this.templateFolder = templateFolder;
			this.parentFolder = parentFolder;
		}

		@Override
		public String getText() {
			return Messages.NewProjectFromTemplateMenuEntry;
		}

		@Override
		public void run() {
			Command cmd = CreateNewProject.from(parentFolder,
					new ProjectTemplateDefinition(propertiesOf(templateFolder)));
			CommandStack commandStack = (CommandStack) parentFolder.getAdapter(CommandStack.class);
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
		private final IFolder parentFolder;

		NewViewFromTemplateAction(IDiagramModel templateDiagram, IFolder parentFolder) {
			this.templateDiagram = templateDiagram;
			this.parentFolder = parentFolder;
		}

		@Override
		public String getText() {
			return Messages.NewViewFromTemplateMenuEntry + ": " + templateDiagram.getName();
		}

		@Override
		public void run() {
			Command cmd = CreateViewFromTemplate.from(parentFolder, templateDiagram,
					new ViewDefinition("New View", propertiesOf(templateDiagram)));
			CommandStack commandStack = (CommandStack) parentFolder.getAdapter(CommandStack.class);
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

	private Optional<IFolder> currentFolder(IStructuredSelection selection) {
		var firstElement = selection.getFirstElement();

		if (firstElement instanceof IFolder && isInDiagramFolder((IFolder) firstElement)) {
			return Optional.of((IFolder) firstElement);
		} else if (firstElement instanceof IDiagramModel) {
			return Optional.of((IFolder) ((IDiagramModel) firstElement).eContainer());
		}

		return Optional.empty();
	}

	private Optional<IFolder> findViewFolder(IFolder selectedFolder) {
		return Optional.ofNullable(selectedFolder).map(f -> {
			var currentFolder = f;
			while (!FolderType.DIAGRAMS.equals(currentFolder.getType())) {
				currentFolder = (IFolder) currentFolder.eContainer();
			}

			return currentFolder;
		});
	}

	public Map<String, String> propertiesOf(IProperties template) {
		return template.getProperties().stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
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
