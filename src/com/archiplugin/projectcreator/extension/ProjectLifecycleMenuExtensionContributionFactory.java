package com.archiplugin.projectcreator.extension;

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
import com.archiplugin.projectcreator.preferences.LifecycleDefinition;
import com.archiplugin.projectcreator.preferences.Preferences;
import com.archiplugin.projectcreator.project.lifecycle.MandatoryPropertiesDefinition;
import com.archiplugin.projectcreator.project.lifecycle.MoveProject;

public class ProjectLifecycleMenuExtensionContributionFactory extends ExtensionContributionFactory {

	public ProjectLifecycleMenuExtensionContributionFactory() {
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		var selectionService = serviceLocator.getService(ISelectionService.class);
		var selection = (IStructuredSelection) selectionService.getSelection();

		currentFolder(selection).ifPresent(currentFolder -> {
			var matchingLifecycle = Preferences.getPreferenceLifecycles().toLifecycles()
					.findMatchingLifecycle(currentFolder);

			matchingLifecycle.ifPresent(lc -> {
				additions.addContributionItem(new Separator(), null);

				var moveProjectToNextStage = new ActionContributionItem(
						new MoveProjectToNextStageAction(lc, currentFolder));
				additions.addContributionItem(moveProjectToNextStage, null);
			});

		});

	}

	private class MoveProjectToNextStageAction extends Action {
		private final IFolder currentFolder;
		private final LifecycleDefinition lifecycleDefinition;

		MoveProjectToNextStageAction(LifecycleDefinition lifecycle, IFolder currentFolder) {
			this.currentFolder = currentFolder;
			this.lifecycleDefinition = lifecycle;
		}

		@Override
		public String getText() {
			return Messages.MoveProjectToNextStageMenuEntry;
		}

		@Override
		public void run() {
			Command cmd = MoveProject.to(lifecycleDefinition.getToFolder(), currentFolder,
					new MandatoryPropertiesDefinition(lifecycleDefinition.getMandatoryProperties()));
			CommandStack commandStack = (CommandStack) lifecycleDefinition.getToFolder().getAdapter(CommandStack.class);
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
