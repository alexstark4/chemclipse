/*******************************************************************************
 * Copyright (c) 2008, 2016 Dr. Philip Wenig.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.msd.filter.supplier.ionremover.core;

import java.io.File;

import org.eclipse.chemclipse.chromatogram.msd.filter.core.chromatogram.IChromatogramFilterMSD;
import org.eclipse.chemclipse.chromatogram.msd.filter.supplier.ionremover.settings.ISupplierFilterSettings;
import org.eclipse.chemclipse.chromatogram.msd.filter.supplier.ionremover.settings.SupplierFilterSettings;
import org.eclipse.chemclipse.msd.converter.chromatogram.ChromatogramConverterMSD;
import org.eclipse.chemclipse.msd.converter.processing.chromatogram.IChromatogramMSDImportConverterProcessingInfo;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.selection.ChromatogramSelectionMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;
import org.eclipse.chemclipse.msd.model.core.support.IMarkedIons;
import org.eclipse.chemclipse.msd.model.core.support.MarkedIon;
import org.eclipse.chemclipse.msd.model.exceptions.NoExtractedIonSignalStoredException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class Show_IonRemoverFilter extends ChromatogramImporterTestCase {

	private final static String EXTENSION_POINT_ID = "org.eclipse.chemclipse.msd.converter.supplier.agilent";
	protected IChromatogramMSD chromatogram;
	protected IChromatogramSelectionMSD chromatogramSelection;
	private IChromatogramFilterMSD chromatogramFilter;
	private ISupplierFilterSettings chromatogramFilterSettings;
	private IMarkedIons excludedIons;
	private File fileImport;
	private File fileExport;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		/*
		 * Import
		 */
		// fileImport = new
		// File(TestPathHelper.getAbsolutePath(TestPathHelper.TESTFILE_IMPORT_OP21705));
		fileImport = new File("E:\\Dissertation\\Pyrolyseläufe\\OP\\OP21680-707\\OP21705.D\\DATA.MS");
		IChromatogramMSDImportConverterProcessingInfo processingInfo = ChromatogramConverterMSD.convert(fileImport, EXTENSION_POINT_ID, new NullProgressMonitor());
		chromatogram = processingInfo.getChromatogram();
		chromatogramSelection = new ChromatogramSelectionMSD(chromatogram);
		chromatogramFilter = new ChromatogramFilter();
		chromatogramFilterSettings = new SupplierFilterSettings();
		excludedIons = chromatogramFilterSettings.getIonsToRemove();
		excludedIons.add(new MarkedIon(28));
		excludedIons.add(new MarkedIon(32));
		excludedIons.add(new MarkedIon(207));
		excludedIons.add(new MarkedIon(281));
	}

	@Override
	protected void tearDown() throws Exception {

		chromatogram = null;
		chromatogramSelection = null;
		chromatogramFilter = null;
		chromatogramFilterSettings = null;
		super.tearDown();
	}

	public void testApplyFilter_1() throws NoExtractedIonSignalStoredException {

		int scan = 1;
		float sumExcludedIons = 2294055.0f;
		float result;
		result = chromatogram.getScan(scan).getTotalSignal() - sumExcludedIons;
		chromatogramFilter.applyFilter(chromatogramSelection, chromatogramFilterSettings, new NullProgressMonitor());
		assertEquals("total signal", result, chromatogram.getScan(scan).getTotalSignal());
		// fileExport = new File("/home/eselmeister/tmp/OPTest.D/DATA.MS");
		fileExport = new File("E:\\Dissertation\\Pyrolyseläufe\\OP\\OP21680-707\\OP21705-R.D\\DATA.MS");
		ChromatogramConverterMSD.convert(fileExport, chromatogram, EXTENSION_POINT_ID, new NullProgressMonitor());
	}
}
