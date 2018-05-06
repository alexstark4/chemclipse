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
package org.eclipse.chemclipse.chromatogram.xxd.calculator.supplier.amdiscalri.ui.editors;

import org.eclipse.chemclipse.chromatogram.xxd.calculator.supplier.amdiscalri.ui.swt.ExtendedRetentionIndexListUI;
import org.eclipse.chemclipse.model.columns.ISeparationColumnIndices;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class PageCalibration {

	private Composite control;
	private ExtendedRetentionIndexListUI extendedTableViewer;

	public PageCalibration(Composite container) {
		createControl(container);
	}

	public Composite getControl() {

		return control;
	}

	public void showData(ISeparationColumnIndices separationColumnIndices) {

		extendedTableViewer.setInput(separationColumnIndices);
	}

	private void createControl(Composite container) {

		control = new Composite(container, SWT.NONE);
		control.setLayout(new FillLayout());
		control.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		//
		extendedTableViewer = new ExtendedRetentionIndexListUI(control, SWT.NONE);
	}
}
