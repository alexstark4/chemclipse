/*******************************************************************************
 * Copyright (c) 2011, 2020 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.integrator.supplier.trapezoid.internal.support;

import java.util.List;

import org.eclipse.chemclipse.chromatogram.xxd.integrator.exceptions.ValueMustNotBeNullException;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.result.IPeakIntegrationResult;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.result.IPeakIntegrationResults;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.result.PeakIntegrationResults;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.supplier.trapezoid.processor.PeakIntegrator;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.supplier.trapezoid.settings.PeakIntegrationSettings;
import org.eclipse.chemclipse.csd.model.core.IChromatogramCSD;
import org.eclipse.chemclipse.csd.model.core.selection.IChromatogramSelectionCSD;
import org.eclipse.chemclipse.model.core.IPeak;
import org.eclipse.chemclipse.model.selection.IChromatogramSelection;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;
import org.eclipse.chemclipse.wsd.model.core.IChromatogramWSD;
import org.eclipse.chemclipse.wsd.model.core.selection.IChromatogramSelectionWSD;
import org.eclipse.core.runtime.IProgressMonitor;

public class PeakIntegratorSupport {

	public IPeakIntegrationResults calculatePeakIntegrationResults(List<? extends IPeak> peaks, PeakIntegrationSettings peakIntegrationSettings, IProgressMonitor monitor) throws ValueMustNotBeNullException {

		/*
		 * Get the peak integration results.
		 */
		monitor.subTask("Integrate the peaks");
		IPeakIntegrationResults peakIntegrationResults;
		if(peaks != null) {
			PeakIntegrator peakIntegrator = new PeakIntegrator();
			peakIntegrationResults = peakIntegrator.integrate(peaks, peakIntegrationSettings, monitor);
		} else {
			peakIntegrationResults = new PeakIntegrationResults();
		}
		return peakIntegrationResults;
	}

	@SuppressWarnings("rawtypes")
	public IPeakIntegrationResults calculatePeakIntegrationResults(IChromatogramSelection chromatogramSelection, PeakIntegrationSettings peakIntegrationSettings, IProgressMonitor monitor) throws ValueMustNotBeNullException {

		/*
		 * Get the chromatogram.
		 */
		List<? extends IPeak> peaks = null;
		if(chromatogramSelection instanceof IChromatogramSelectionMSD) {
			IChromatogramSelectionMSD chromatogramSelectionMSD = (IChromatogramSelectionMSD)chromatogramSelection;
			IChromatogramMSD chromatogramMSD = chromatogramSelectionMSD.getChromatogramMSD();
			peaks = chromatogramMSD.getPeaks(chromatogramSelectionMSD);
		} else if(chromatogramSelection instanceof IChromatogramSelectionCSD) {
			IChromatogramSelectionCSD chromatogramSelectionCSD = (IChromatogramSelectionCSD)chromatogramSelection;
			IChromatogramCSD chromatogramFID = chromatogramSelectionCSD.getChromatogramCSD();
			peaks = chromatogramFID.getPeaks(chromatogramSelectionCSD);
		} else if(chromatogramSelection instanceof IChromatogramSelectionWSD) {
			IChromatogramSelectionWSD chromatogramSelectionWSD = (IChromatogramSelectionWSD)chromatogramSelection;
			IChromatogramWSD chromatogramWSD = chromatogramSelectionWSD.getChromatogramWSD();
			peaks = chromatogramWSD.getPeaks(chromatogramSelectionWSD);
		}
		//
		return calculatePeakIntegrationResults(peaks, peakIntegrationSettings, monitor);
	}

	public IPeakIntegrationResult calculatePeakIntegrationResult(IPeak peak, PeakIntegrationSettings peakIntegrationSettings, IProgressMonitor monitor) throws ValueMustNotBeNullException {

		monitor.subTask("Integrate the peak");
		PeakIntegrator peakIntegrator = new PeakIntegrator();
		IPeakIntegrationResult peakIntegrationResult = peakIntegrator.integrate(peak, peakIntegrationSettings, monitor);
		return peakIntegrationResult;
	}
}
