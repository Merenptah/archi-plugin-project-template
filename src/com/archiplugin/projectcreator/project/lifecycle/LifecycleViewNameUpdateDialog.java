package com.archiplugin.projectcreator.project.lifecycle;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LifecycleViewNameUpdateDialog extends Dialog {
	private final Map<String, String> viewIdToNames;
	private Map<String, Text> inputFields = new HashMap<String, Text>();
	private Map<String, String> inputFieldValues = new HashMap<String, String>();

	LifecycleViewNameUpdateDialog(Shell parentShell, Map<String, String> viewIdToNames) {
		super(parentShell);
		this.viewIdToNames = viewIdToNames;
	}

	public Map<String, String> getUpdatedViewNames() {
		return inputFieldValues;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("View Name Update");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite twoColumnArea = createTwoColumnArea(parent);

		viewIdToNames.entrySet().forEach(e -> addRowWith(twoColumnArea, e.getValue(), e.getValue(), e.getKey()));

		return twoColumnArea;
	}

	private void addRowWith(Composite twoColumnArea, String labelText, String defaultValue, String id) {
		Label label = new Label(twoColumnArea, SWT.None);
		label.setText(labelText);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		label.setLayoutData(data);

		var inputField = new Text(twoColumnArea, getInputTextStyle());
		var inputFieldLayout = new GridData(SWT.FILL, SWT.CENTER, true, false);
		inputField.setLayoutData(inputFieldLayout);
		inputField.setText(defaultValue);

		inputFields.put(id, inputField);
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
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
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
