package com.archiplugin.projectcreator.extension;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import com.archimatetool.editor.views.tree.ITreeModelView;
import com.archimatetool.model.FolderType;
import com.archimatetool.model.IFolder;
import com.archimatetool.model.IProperties;
import com.archiplugin.projectcreator.preferences.LifecycleDefinition;
import com.archiplugin.projectcreator.preferences.Preferences;
import com.archiplugin.projectcreator.project.lifecycle.MandatoryPropertiesDefinition;
import com.archiplugin.projectcreator.project.lifecycle.MoveProject;

public class ProjectLifecycleMenuContributionItem extends ContributionItem implements IWorkbenchContribution {

	private MenuManager menuManager;

	private ISelectionService selectionService;

	@Override
	public void initialize(IServiceLocator serviceLocator) {
		this.selectionService = serviceLocator.getService(ISelectionService.class);
	}

	@Override
	public void fill(Menu menu, int index) {
		if (menuManager != null) {
			menuManager.dispose();
		}

		IStructuredSelection selection = getCurrentSelection();
		if (selection == null) {
			return;
		}

		menuManager = new MenuManager();

		currentFolder(selection).ifPresent(currentFolder -> {
			var matchingLifecycles = Preferences.getPreferenceLifecycles().toLifecycles()
					.findMatchingLifecycles(currentFolder);

			matchingLifecycles.forEach(lc -> {
				var moveProjectToNextStage = new MoveProjectToNextStageAction(lc, currentFolder);
				menuManager.add(moveProjectToNextStage);
			});
		});

		for (IContributionItem item : menuManager.getItems()) {
			item.fill(menu, index++);
		}
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	private IStructuredSelection getCurrentSelection() {
		return (IStructuredSelection) selectionService.getSelection(ITreeModelView.ID);
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
			return lifecycleDefinition.getToFolderName();
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
