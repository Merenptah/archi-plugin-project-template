package com.archiplugin.projectcreator.project.lifecycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LifecycleMandatoryPropertiesDialog extends Dialog {
	private final List<String> mandatoryProperties;
	private Map<String, Text> inputFields = new HashMap<String, Text>();
	private Map<String, String> inputFieldValues = new HashMap<String, String>();

	LifecycleMandatoryPropertiesDialog(Shell parentShell, List<String> mandatoryProperties) {
		super(parentShell);
		this.mandatoryProperties = mandatoryProperties;
	}

	public Map<String, String> getInputFieldValues() {
		return inputFieldValues;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Project Definition");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite twoColumnArea = createTwoColumnArea(parent);

		mandatoryProperties.forEach(label -> addRowWith(twoColumnArea, label, ""));

		return twoColumnArea;
	}

	private void addRowWith(Composite twoColumnArea, String labelText, String defaultValue) {
		Label label = new Label(twoColumnArea, SWT.None);
		label.setText(labelText);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		label.setLayoutData(data);

		var inputField = new Text(twoColumnArea, getInputTextStyle());
		var inputFieldLayout = new GridData(SWT.FILL, SWT.CENTER, true, false);
		inputField.setLayoutData(inputFieldLayout);
		inputField.setText(defaultValue);

		inputField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getButton(IDialogConstants.OK_ID).setEnabled(inputFields.entrySet().stream()
						.allMatch(f -> f.getValue().getText() != null && !f.getValue().getText().isBlank()));

			}
		});

		inputFields.put(labelText, inputField);
	}

	private Composite createTwoColumnArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2, false));
		return composite;
	}

	protected int getInputTextStyle() {
		return SWT.SINGLE | SWT.BORDER;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		var okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);

		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			inputFieldValues = inputFields.entrySet().stream()
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getText()));
		} else {
			inputFieldValues = null;
		}
		super.buttonPressed(buttonId);
	}
}
