/*******************************************************************************
 * Copyright (c) 2011, 2018 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.msd.process.supplier.peakidentification.internal.report;

import java.io.PrintWriter;

import org.eclipse.chemclipse.model.core.IPeak;
import org.eclipse.chemclipse.model.core.IPeaks;
import org.eclipse.chemclipse.model.identifier.IComparisonResult;
import org.eclipse.chemclipse.model.identifier.ILibraryInformation;
import org.eclipse.chemclipse.model.targets.IPeakTarget;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IPeakMSD;

public class PeakReport {

	/**
	 * This class has only static methods.
	 */
	private PeakReport() {
	}

	public static void writeResults(IPeaks peaks, PrintWriter printWriter, String integrator, String identifier) {

		if(peaks == null || printWriter == null) {
			return;
		}
		/*
		 * Integrator Name
		 */
		printWriter.println("########");
		printWriter.println("Integrator: " + integrator);
		printWriter.println("Identifier: " + identifier);
		printWriter.println("########");
		/*
		 * Results
		 */
		for(IPeak peak : peaks.getPeaks()) {
			if(peak instanceof IPeakMSD) {
				writeResult((IPeakMSD)peak, printWriter);
			}
		}
		printWriter.println("");
	}

	private static void writeResult(IPeakMSD peak, PrintWriter printWriter) {

		printWriter.println("----------------------------------------------");
		printWriter.println("Model Description: " + peak.getModelDescription());
		printWriter.println("----------------------------------------------");
		/*
		 * Values
		 */
		printWriter.println("~~~~~~~~");
		printWriter.println("Integration Results");
		printWriter.println("~~~~~~~~");
		printIntegrationResults(peak, printWriter);
		printWriter.println("");
		printWriter.println("~~~~~~~~");
		printWriter.println("Identification Results");
		printWriter.println("~~~~~~~~");
		printIdentificationResults(peak, printWriter);
		printWriter.println("");
	}

	private static void printIntegrationResults(IPeakMSD peak, PrintWriter printWriter) {

		printWriter.println("Start RT (Minutes): " + (peak.getPeakModel().getStartRetentionTime() / IChromatogramMSD.MINUTE_CORRELATION_FACTOR));
		printWriter.println("Stop RT (Minutes): " + (peak.getPeakModel().getStopRetentionTime() / IChromatogramMSD.MINUTE_CORRELATION_FACTOR));
		printWriter.println("Tailing: " + peak.getPeakModel().getTailing());
		printWriter.println("Width (Minutes): " + (peak.getPeakModel().getWidthByInflectionPoints(0.5f) / IChromatogramMSD.MINUTE_CORRELATION_FACTOR));
		printWriter.println("Integrated Area: " + peak.getIntegratedArea());
	}

	private static void printIdentificationResults(IPeakMSD peak, PrintWriter printWriter) {

		printWriter.print("Name");
		printWriter.print("\t");
		printWriter.print("CAS");
		printWriter.print("\t");
		printWriter.print("MatchFactor");
		printWriter.print("\t");
		printWriter.print("ReverseMatchFactor");
		printWriter.print("\t");
		printWriter.print("Probability");
		printWriter.print("\t");
		printWriter.print("Identifier");
		printWriter.print("\t");
		printWriter.print("Miscellaneous");
		printWriter.print("\t");
		printWriter.print("Comments");
		printWriter.println("");
		for(IPeakTarget peakTarget : peak.getTargets()) {
			/*
			 * Get the hits.
			 */
			if(peakTarget instanceof IPeakTarget) {
				IPeakTarget peakIdentificationEntry = (IPeakTarget)peakTarget;
				IComparisonResult comparisonResult = peakIdentificationEntry.getComparisonResult();
				ILibraryInformation libraryInformation = peakIdentificationEntry.getLibraryInformation();
				/*
				 * Print the result.
				 */
				printWriter.print(libraryInformation.getName());
				printWriter.print("\t");
				printWriter.print(libraryInformation.getCasNumber());
				printWriter.print("\t");
				printWriter.print(comparisonResult.getMatchFactor());
				printWriter.print("\t");
				printWriter.print(comparisonResult.getReverseMatchFactor());
				printWriter.print("\t");
				printWriter.print(comparisonResult.getProbability());
				printWriter.print("\t");
				printWriter.print(peakIdentificationEntry.getIdentifier());
				printWriter.print("\t");
				printWriter.print(libraryInformation.getMiscellaneous());
				printWriter.print("\t");
				printWriter.print(libraryInformation.getComments());
				printWriter.println("");
			}
		}
	}
}
