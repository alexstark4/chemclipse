/*******************************************************************************
 * Copyright (c) 2018 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.xxd.process.supplier;

import org.eclipse.chemclipse.chromatogram.csd.identifier.peak.IPeakIdentifierSupplierCSD;
import org.eclipse.chemclipse.chromatogram.csd.identifier.peak.IPeakIdentifierSupportCSD;
import org.eclipse.chemclipse.chromatogram.csd.identifier.peak.PeakIdentifierCSD;
import org.eclipse.chemclipse.chromatogram.csd.identifier.settings.IPeakIdentifierSettingsCSD;
import org.eclipse.chemclipse.csd.model.core.selection.IChromatogramSelectionCSD;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.exceptions.NoIdentifierAvailableException;
import org.eclipse.chemclipse.model.selection.IChromatogramSelection;
import org.eclipse.chemclipse.model.settings.IProcessSettings;
import org.eclipse.chemclipse.model.types.DataType;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.ProcessingInfo;
import org.eclipse.chemclipse.xxd.process.support.IProcessTypeSupplier;
import org.eclipse.core.runtime.IProgressMonitor;

public class PeakIdentifierTypeSupplierCSD extends AbstractProcessTypeSupplier implements IProcessTypeSupplier {

	public static final String CATEGORY = "Peak Identifier [CSD]";
	private static final Logger logger = Logger.getLogger(PeakIdentifierTypeSupplierCSD.class);

	public PeakIdentifierTypeSupplierCSD() {
		super(CATEGORY, new DataType[]{DataType.CSD});
		try {
			IPeakIdentifierSupportCSD support = PeakIdentifierCSD.getPeakIdentifierSupport();
			for(String processorId : support.getAvailableIdentifierIds()) {
				IPeakIdentifierSupplierCSD supplier = support.getIdentifierSupplier(processorId);
				addProcessorId(processorId);
				// addProcessorSettingsClass(processorId, supplier.getSettingsClass()); // TODO
				addProcessorName(processorId, supplier.getIdentifierName());
				addProcessorDescription(processorId, supplier.getDescription());
			}
		} catch(NoIdentifierAvailableException e) {
			logger.warn(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public IProcessingInfo applyProcessor(IChromatogramSelection chromatogramSelection, String processorId, IProcessSettings processSettings, IProgressMonitor monitor) {

		IProcessingInfo processingInfo;
		if(chromatogramSelection instanceof IChromatogramSelectionCSD) {
			IChromatogramSelectionCSD chromatogramSelectionCSD = (IChromatogramSelectionCSD)chromatogramSelection;
			if(processSettings instanceof IPeakIdentifierSettingsCSD) {
				processingInfo = new ProcessingInfo(); // TODO REMOVE
				// processingInfo = PeakIdentifierCSD.identify(chromatogramSelectionCSD, (IPeakIdentifierSettingsCSD)processSettings, processorId, monitor); // TODO
			} else {
				processingInfo = PeakIdentifierCSD.identify(chromatogramSelectionCSD, processorId, monitor);
			}
		} else {
			processingInfo = new ProcessingInfo();
			processingInfo.addErrorMessage(processorId, "The data is not supported by the processor.");
		}
		return processingInfo;
	}
}
