/*******************************************************************************
 * Copyright (c) 2013, 2016 Dr. Philip Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.svg.converter;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.chemclipse.converter.processing.chromatogram.ChromatogramOverviewImportConverterProcessingInfo;
import org.eclipse.chemclipse.converter.processing.chromatogram.IChromatogramOverviewImportConverterProcessingInfo;
import org.eclipse.chemclipse.msd.converter.chromatogram.AbstractChromatogramMSDImportConverter;
import org.eclipse.chemclipse.msd.converter.processing.chromatogram.ChromatogramMSDImportConverterProcessingInfo;
import org.eclipse.chemclipse.msd.converter.processing.chromatogram.IChromatogramMSDImportConverterProcessingInfo;

public class ChromatogramImportConverter extends AbstractChromatogramMSDImportConverter {

	private static final String DESCRIPTION = "SVG Import Converter";
	private static final String EXPORT_ERROR = "The SVG converter is not able to import chromatograms.";

	@Override
	public IChromatogramMSDImportConverterProcessingInfo convert(File file, IProgressMonitor monitor) {

		IChromatogramMSDImportConverterProcessingInfo processingInfo = new ChromatogramMSDImportConverterProcessingInfo();
		processingInfo.addErrorMessage(DESCRIPTION, EXPORT_ERROR);
		return processingInfo;
	}

	@Override
	public IChromatogramOverviewImportConverterProcessingInfo convertOverview(File file, IProgressMonitor monitor) {

		IChromatogramOverviewImportConverterProcessingInfo processingInfo = new ChromatogramOverviewImportConverterProcessingInfo();
		processingInfo.addErrorMessage(DESCRIPTION, EXPORT_ERROR);
		return processingInfo;
	}
}
