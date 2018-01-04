/*******************************************************************************
 * Copyright (c) 2016, 2018 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.calculator.supplier.amdiscalri.ui.wizards;

import java.io.File;

import org.eclipse.chemclipse.support.ui.wizards.AbstractExtendedWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PageCalibrationSettings extends AbstractExtendedWizardPage {

	private IRetentionIndexWizardElements wizardElements;
	private String[] availableStandards;
	//
	private Button checkBoxUseExistingCalibrationFile;
	private Text textPathRetentionIndexFile;
	private Button buttonSelectCalibrationFile;
	//
	private Button buttonMSD;
	private Button buttonCSD;
	//
	private Combo comboStartIndexName;
	private Combo comboStopIndexName;
	private Button checkBoxUseExistingPeaks;

	public PageCalibrationSettings(IRetentionIndexWizardElements wizardElements) {
		//
		super(PageCalibrationSettings.class.getName());
		setTitle("Calibration Settings");
		setDescription("Please select the calibration settings.");
		this.wizardElements = wizardElements;
		availableStandards = wizardElements.getAvailableStandards();
	}

	@Override
	public boolean canFinish() {

		if(getMessage() == null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setDefaultValues() {

	}

	@Override
	public void setVisible(boolean visible) {

		super.setVisible(visible);
		if(visible) {
			checkBoxUseExistingCalibrationFile.setSelection(wizardElements.isUseExistingRetentionIndexFile());
			setCalibrationFileSelection(wizardElements.isUseExistingRetentionIndexFile());
			comboStartIndexName.select(getComboIndex(wizardElements.getStartIndexName()));
			comboStopIndexName.select(getComboIndex(wizardElements.getStopIndexName()));
			checkBoxUseExistingPeaks.setSelection(wizardElements.isUseAlreadyDetectedPeaks());
			validateSelection();
		}
	}

	@Override
	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		//
		createCalibrationFileField(composite);
		createLabelDataField(composite);
		createButtonMSDField(composite);
		createButtonCSDField(composite);
		createStartRetentionIndexField(composite);
		createStopRetentionIndexField(composite);
		createPeakIdentificationField(composite);
		//
		validateSelection();
		setControl(composite);
	}

	private void createCalibrationFileField(Composite composite) {

		checkBoxUseExistingCalibrationFile = new Button(composite, SWT.CHECK);
		checkBoxUseExistingCalibrationFile.setText("Use existing *.cal file for improved detection");
		checkBoxUseExistingCalibrationFile.setEnabled(true);
		checkBoxUseExistingCalibrationFile.setLayoutData(getGridData());
		checkBoxUseExistingCalibrationFile.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				boolean useExistingCalibrationFile = checkBoxUseExistingCalibrationFile.getSelection();
				wizardElements.setUseExistingRetentionIndexFile(useExistingCalibrationFile);
				setCalibrationFileSelection(useExistingCalibrationFile);
				validateSelection();
			}
		});
		//
		textPathRetentionIndexFile = new Text(composite, SWT.BORDER);
		textPathRetentionIndexFile.setText("");
		textPathRetentionIndexFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textPathRetentionIndexFile.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				wizardElements.setPathRetentionIndexFile(textPathRetentionIndexFile.getText().trim());
				validateSelection();
			}
		});
		//
		buttonSelectCalibrationFile = new Button(composite, SWT.PUSH);
		buttonSelectCalibrationFile.setText("Select *.cal");
		buttonSelectCalibrationFile.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				FileDialog fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.READ_ONLY);
				fileDialog.setText("Select an existing *.cal template file.");
				fileDialog.setFilterExtensions(new String[]{"*.CAL", "*.cal"});
				fileDialog.setFilterNames(new String[]{"AMDIS Calibration *.CAL", "AMDIS Calibration *.cal"});
				fileDialog.setFilterPath(wizardElements.getFilterPathCalibrationFile());
				String pathRetentionIndexFile = fileDialog.open();
				if(pathRetentionIndexFile != null) {
					textPathRetentionIndexFile.setText(pathRetentionIndexFile);
					wizardElements.setFilterPathCalibrationFile(fileDialog.getFilterPath());
					wizardElements.setPathRetentionIndexFile(pathRetentionIndexFile);
					validateSelection();
				}
			}
		});
	}

	private void createLabelDataField(Composite composite) {

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select whether to use MSD or CSD data to detect the RI pattern.");
		label.setLayoutData(getGridData());
	}

	private void createButtonMSDField(Composite composite) {

		buttonMSD = new Button(composite, SWT.RADIO);
		buttonMSD.setText("MSD (Quadrupole, IonTrap, ...) data");
		buttonMSD.setSelection(wizardElements.isUseMassSpectrometryData());
		buttonMSD.setLayoutData(getGridData());
		buttonMSD.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				wizardElements.setUseMassSpectrometryData(true);
			}
		});
	}

	private void createButtonCSDField(Composite composite) {

		buttonCSD = new Button(composite, SWT.RADIO);
		buttonCSD.setText("CSD (FID, ECD, ...) data");
		buttonCSD.setSelection(!wizardElements.isUseMassSpectrometryData());
		buttonCSD.setLayoutData(getGridData());
		buttonCSD.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				wizardElements.setUseMassSpectrometryData(false);
			}
		});
	}

	private void createStartRetentionIndexField(Composite composite) {

		Label label = new Label(composite, SWT.NONE);
		label.setText("Start Index");
		label.setLayoutData(getGridData());
		//
		comboStartIndexName = new Combo(composite, SWT.NONE);
		comboStartIndexName.setLayoutData(getGridData());
		comboStartIndexName.setItems(availableStandards);
		comboStartIndexName.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				wizardElements.setStartIndexName(comboStartIndexName.getText().trim());
				validateSelection();
			}
		});
	}

	private void createStopRetentionIndexField(Composite composite) {

		Label label = new Label(composite, SWT.NONE);
		label.setText("Stop Index");
		label.setLayoutData(getGridData());
		//
		comboStopIndexName = new Combo(composite, SWT.NONE);
		comboStopIndexName.setLayoutData(getGridData());
		comboStopIndexName.setItems(availableStandards);
		comboStopIndexName.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				wizardElements.setStopIndexName(comboStopIndexName.getText().trim());
				validateSelection();
			}
		});
	}

	private void createPeakIdentificationField(Composite composite) {

		checkBoxUseExistingPeaks = new Button(composite, SWT.CHECK);
		checkBoxUseExistingPeaks.setText("Use existing peaks in chromatogram if available.");
		checkBoxUseExistingPeaks.setEnabled(true);
		checkBoxUseExistingPeaks.setLayoutData(getGridData());
		checkBoxUseExistingPeaks.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				wizardElements.setUseAlreadyDetectedPeaks(checkBoxUseExistingPeaks.getSelection());
			}
		});
	}

	private GridData getGridData() {

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		gridData.verticalIndent = 5;
		return gridData;
	}

	private void setCalibrationFileSelection(boolean enabled) {

		textPathRetentionIndexFile.setEnabled(enabled);
		buttonSelectCalibrationFile.setEnabled(enabled);
	}

	private int getComboIndex(String name) {

		for(int i = 0; i < availableStandards.length; i++) {
			if(availableStandards[i].equals(name)) {
				return i;
			}
		}
		return -1;
	}

	private void validateSelection() {

		String message = null;
		/*
		 * *.cal file
		 */
		if(wizardElements.isUseExistingRetentionIndexFile()) {
			String pathCalibrationFile = wizardElements.getPathRetentionIndexFile();
			if(pathCalibrationFile.equals("")) {
				message = "Please select an existing calibration (*.cal) file.";
			} else {
				File file = new File(pathCalibrationFile);
				if(!file.exists()) {
					message = "The selected *.cal doesn't exist.";
				}
			}
		}
		/*
		 * Start index
		 */
		if(message == null) {
			String startIndexName = wizardElements.getStartIndexName();
			if(startIndexName.equals("")) {
				message = "Please select and start index.";
			} else {
				if(getComboIndex(startIndexName) == -1) {
					message = "The select start index is not valid.";
				}
			}
		}
		/*
		 * Stop index
		 */
		if(message == null) {
			String stopIndexName = wizardElements.getStopIndexName();
			if(stopIndexName.equals("")) {
				message = "Please select and stop index.";
			} else {
				if(getComboIndex(stopIndexName) == -1) {
					message = "The select stop index is not valid.";
				}
			}
		}
		/*
		 * Updates the status
		 */
		updateStatus(message);
	}
}
