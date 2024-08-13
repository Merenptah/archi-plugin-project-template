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
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperties;
import com.archiplugin.projectcreator.Activator;
import com.archiplugin.projectcreator.preferences.ProjectCreatorPreferenceConstants;
import com.archiplugin.projectcreator.project.MoveProject;

public class ProjectLifecycleMenuExtensionContributionFactory extends ExtensionContributionFactory {

	public ProjectLifecycleMenuExtensionContributionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		var selectionService = serviceLocator.getService(ISelectionService.class);
		var selection = (IStructuredSelection) selectionService.getSelection();

		currentFolder(selection).ifPresent(currentFolder -> {
			findViewsFolder(currentFolder).flatMap(views -> findTargetFolderIn(views)).ifPresent(targetFolder -> {
				additions.addContributionItem(new Separator(), null);

				var moveProjectToNextStage = new ActionContributionItem(
						new MoveProjectToNextStageAction(targetFolder, currentFolder));
				additions.addContributionItem(moveProjectToNextStage, null);
			});

		});

	}

	private class MoveProjectToNextStageAction extends Action {
		private final IFolder currentFolder;
		private final IFolder newParentFolder;

		MoveProjectToNextStageAction(IFolder newParentFolder, IFolder currentFolder) {
			this.currentFolder = currentFolder;
			this.newParentFolder = newParentFolder;
		}

		@Override
		public String getText() {
			return Messages.MoveProjectToNextStageMenuEntry;
		}

		@Override
		public void run() {
			Command cmd = MoveProject.to(newParentFolder, currentFolder);
			CommandStack commandStack = (CommandStack) newParentFolder.getAdapter(CommandStack.class);
			commandStack.execute(cmd);

		}

		@Override
		public String getId() {
			return "moveProjectToNextStageAction"; //$NON-NLS-1$
		};

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.projectImage();
		}
	};

	private Optional<IFolder> currentFolder(IStructuredSelection selection) {
		var firstElement = selection.getFirstElement();

		if (!(firstElement instanceof IFolder)) {
			return Optional.empty();
		}

		var currentFolder = (IFolder) firstElement;
		if (isInDiagramFolder(currentFolder) && currentFolder.getType() == FolderType.USER) {
			return Optional.of(currentFolder);
		}

		return Optional.empty();
	}

	public Map<String, String> propertiesOf(IProperties template) {
		return template.getProperties().stream().collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
	}

	private Optional<IFolder> findViewsFolder(IFolder currentFolder) {
		var folderIterator = currentFolder;

		if (folderIterator.getType() == FolderType.DIAGRAMS) {
			return Optional.of(folderIterator);
		}

		while (folderIterator.eContainer() instanceof IFolder) {
			folderIterator = (IFolder) folderIterator.eContainer();
			if (folderIterator.getType() == FolderType.DIAGRAMS) {
				return Optional.of(folderIterator);
			}
		}

		return Optional.empty();
	}

	private Optional<IFolder> findTargetFolderIn(IFolder viewsFolder) {
		var folderId = Activator.INSTANCE.getPreferenceStore()
				.getString(ProjectCreatorPreferenceConstants.PROJECT_LIFECYCLE_TO_FOLDER);

		for (Iterator<IFolder> iterator = viewsFolder.getFolders().iterator(); iterator.hasNext();) {
			IFolder f = iterator.next();

			if (f.getId().equals(folderId)) {
				return Optional.of(f);
			}

			var intermediateResult = findTargetFolderIn(f);
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
