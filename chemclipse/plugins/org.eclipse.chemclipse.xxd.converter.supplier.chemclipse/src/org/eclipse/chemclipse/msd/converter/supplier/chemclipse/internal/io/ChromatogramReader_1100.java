/*******************************************************************************
 * Copyright (c) 2016, 2018 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.chemclipse.internal.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.eclipse.chemclipse.converter.exceptions.FileIsEmptyException;
import org.eclipse.chemclipse.converter.exceptions.FileIsNotReadableException;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.baseline.IBaselineModel;
import org.eclipse.chemclipse.model.core.IChromatogramOverview;
import org.eclipse.chemclipse.model.core.IIntegrationEntry;
import org.eclipse.chemclipse.model.core.IMethod;
import org.eclipse.chemclipse.model.core.IPeakIntensityValues;
import org.eclipse.chemclipse.model.core.PeakType;
import org.eclipse.chemclipse.model.core.RetentionIndexType;
import org.eclipse.chemclipse.model.exceptions.AbundanceLimitExceededException;
import org.eclipse.chemclipse.model.exceptions.PeakException;
import org.eclipse.chemclipse.model.exceptions.ReferenceMustNotBeNullException;
import org.eclipse.chemclipse.model.identifier.ChromatogramComparisonResult;
import org.eclipse.chemclipse.model.identifier.ChromatogramLibraryInformation;
import org.eclipse.chemclipse.model.identifier.ComparisonResult;
import org.eclipse.chemclipse.model.identifier.IChromatogramLibraryInformation;
import org.eclipse.chemclipse.model.identifier.IComparisonResult;
import org.eclipse.chemclipse.model.identifier.IIdentificationTarget;
import org.eclipse.chemclipse.model.identifier.ILibraryInformation;
import org.eclipse.chemclipse.model.identifier.IPeakLibraryInformation;
import org.eclipse.chemclipse.model.identifier.LibraryInformation;
import org.eclipse.chemclipse.model.identifier.PeakComparisonResult;
import org.eclipse.chemclipse.model.identifier.PeakLibraryInformation;
import org.eclipse.chemclipse.model.implementation.IdentificationTarget;
import org.eclipse.chemclipse.model.implementation.PeakIntensityValues;
import org.eclipse.chemclipse.model.implementation.QuantitationEntry;
import org.eclipse.chemclipse.model.quantitation.IInternalStandard;
import org.eclipse.chemclipse.model.quantitation.IQuantitationEntry;
import org.eclipse.chemclipse.model.quantitation.InternalStandard;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.io.IChromatogramMSDZipReader;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.io.IReaderProxy;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.model.chromatogram.IVendorChromatogram;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.model.chromatogram.IVendorIon;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.model.chromatogram.IVendorScan;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.model.chromatogram.IVendorScanProxy;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.model.chromatogram.VendorChromatogram;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.model.chromatogram.VendorIon;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.model.chromatogram.VendorScan;
import org.eclipse.chemclipse.msd.converter.supplier.chemclipse.model.chromatogram.VendorScanProxy;
import org.eclipse.chemclipse.msd.model.core.AbstractIon;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IChromatogramPeakMSD;
import org.eclipse.chemclipse.msd.model.core.IIntegrationEntryMSD;
import org.eclipse.chemclipse.msd.model.core.IIonTransition;
import org.eclipse.chemclipse.msd.model.core.IIonTransitionSettings;
import org.eclipse.chemclipse.msd.model.core.IPeakMSD;
import org.eclipse.chemclipse.msd.model.core.IPeakMassSpectrum;
import org.eclipse.chemclipse.msd.model.core.IPeakModelMSD;
import org.eclipse.chemclipse.msd.model.core.IScanMSD;
import org.eclipse.chemclipse.msd.model.exceptions.IonLimitExceededException;
import org.eclipse.chemclipse.msd.model.exceptions.IonTransitionIsNullException;
import org.eclipse.chemclipse.msd.model.implementation.ChromatogramPeakMSD;
import org.eclipse.chemclipse.msd.model.implementation.IntegrationEntryMSD;
import org.eclipse.chemclipse.msd.model.implementation.PeakMassSpectrum;
import org.eclipse.chemclipse.msd.model.implementation.PeakModelMSD;
import org.eclipse.chemclipse.msd.model.implementation.QuantitationEntryMSD;
import org.eclipse.chemclipse.msd.model.implementation.ScanMSD;
import org.eclipse.chemclipse.support.history.EditInformation;
import org.eclipse.chemclipse.support.history.IEditHistory;
import org.eclipse.chemclipse.support.history.IEditInformation;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.internal.support.BaselineElement;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.internal.support.IBaselineElement;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.internal.support.IConstants;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.internal.support.IFormat;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.preferences.PreferenceSupplier;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Methods are copied to ensure that file formats are kept readable even if they contain errors.
 * This is suitable but I know, it's not the best way to achieve long term support for older formats.
 */
public class ChromatogramReader_1100 extends AbstractChromatogramReader implements IChromatogramMSDZipReader {

	private static final Logger logger = Logger.getLogger(ChromatogramReader_1100.class);

	@Override
	public IChromatogramMSD read(File file, IProgressMonitor monitor) throws FileNotFoundException, FileIsNotReadableException, FileIsEmptyException, IOException {

		IChromatogramMSD chromatogram = null;
		ZipFile zipFile = new ZipFile(file);
		try {
			if(isValidFileFormat(zipFile)) {
				// monitor.subTask(IConstants.IMPORT_CHROMATOGRAM);
				chromatogram = readFromZipFile(zipFile, "", file, monitor);
			}
		} finally {
			zipFile.close();
		}
		//
		return chromatogram;
	}

	@Override
	public IChromatogramOverview readOverview(File file, IProgressMonitor monitor) throws FileNotFoundException, FileIsNotReadableException, FileIsEmptyException, IOException {

		IChromatogramOverview chromatogramOverview = null;
		ZipFile zipFile = new ZipFile(file);
		try {
			if(isValidFileFormat(zipFile)) {
				chromatogramOverview = readOverviewFromZipFile(zipFile, "", monitor);
			}
		} finally {
			zipFile.close();
		}
		//
		return chromatogramOverview;
	}

	@Override
	public IChromatogramMSD read(ZipInputStream zipInputStream, String directoryPrefix, IProgressMonitor monitor) throws IOException {

		return readZipData(zipInputStream, directoryPrefix, null, monitor);
	}

	@Override
	public IChromatogramMSD read(ZipFile zipFile, String directoryPrefix, IProgressMonitor monitor) throws IOException {

		return readFromZipFile(zipFile, directoryPrefix, null, monitor);
	}

	private IChromatogramMSD readFromZipFile(ZipFile zipFile, String directoryPrefix, File file, IProgressMonitor monitor) throws IOException {

		return readZipData(zipFile, directoryPrefix, file, monitor);
	}

	/*
	 * Object = ZipFile or ZipInputStream
	 * @param object
	 * @param file
	 * @return
	 */
	private IChromatogramMSD readZipData(Object object, String directoryPrefix, File file, IProgressMonitor monitor) throws IOException {

		IVendorChromatogram chromatogram = new VendorChromatogram();
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Read compressed data", 100);
		try {
			boolean closeStream;
			boolean useScanProxies;
			//
			if(object instanceof ZipFile) {
				/*
				 * ZipFile
				 */
				closeStream = true;
				IEclipsePreferences preferences = PreferenceSupplier.INSTANCE().getPreferences();
				useScanProxies = preferences.getBoolean(PreferenceSupplier.P_USE_SCAN_PROXIES, PreferenceSupplier.DEF_USE_SCAN_PROXIES);
			} else if(object instanceof ZipInputStream) {
				/*
				 * ZipInputStream
				 */
				closeStream = false;
				useScanProxies = false;
			} else {
				return null;
			}
			subMonitor.worked(10);
			//
			readMethod(getDataInputStream(object, directoryPrefix + IFormat.FILE_SYSTEM_SETTINGS_MSD), closeStream, chromatogram, subMonitor.split(10));
			if(useScanProxies) {
				readScanProxies((ZipFile)object, directoryPrefix, file, chromatogram, subMonitor.split(10));
			} else {
				readScans(getDataInputStream(object, directoryPrefix + IFormat.FILE_SCANS_MSD), closeStream, chromatogram, subMonitor.split(10));
			}
			readBaseline(getDataInputStream(object, directoryPrefix + IFormat.FILE_BASELINE_MSD), closeStream, chromatogram, subMonitor.split(10));
			readPeaks(getDataInputStream(object, directoryPrefix + IFormat.FILE_PEAKS_MSD), closeStream, chromatogram, subMonitor.split(10));
			readArea(getDataInputStream(object, directoryPrefix + IFormat.FILE_AREA_MSD), closeStream, chromatogram, subMonitor.split(10));
			readIdentification(getDataInputStream(object, directoryPrefix + IFormat.FILE_IDENTIFICATION_MSD), closeStream, chromatogram, subMonitor.split(10));
			readHistory(getDataInputStream(object, directoryPrefix + IFormat.FILE_HISTORY_MSD), closeStream, chromatogram, subMonitor.split(10));
			readMiscellaneous(getDataInputStream(object, directoryPrefix + IFormat.FILE_MISC_MSD), closeStream, chromatogram, subMonitor.split(10));
			setAdditionalInformation(file, chromatogram, subMonitor.split(10));
		} finally {
			SubMonitor.done(subMonitor);
		}
		//
		return chromatogram;
	}

	private void readMethod(DataInputStream dataInputStream, boolean closeStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		IMethod method = chromatogram.getMethod();
		//
		method.setInstrumentName(readString(dataInputStream));
		method.setIonSource(readString(dataInputStream));
		method.setSamplingRate(dataInputStream.readDouble());
		method.setSolventDelay(dataInputStream.readInt());
		method.setSourceHeater(dataInputStream.readDouble());
		method.setStopMode(readString(dataInputStream));
		method.setStopTime(dataInputStream.readInt());
		method.setTimeFilterPeakWidth(dataInputStream.readInt());
		//
		if(closeStream) {
			dataInputStream.close();
		}
	}

	private IChromatogramOverview readOverviewFromZipFile(ZipFile zipFile, String directoryPrefix, IProgressMonitor monitor) throws IOException {

		DataInputStream dataInputStream = getDataInputStream(zipFile, directoryPrefix + IFormat.FILE_TIC_MSD);
		//
		IVendorChromatogram chromatogram = new VendorChromatogram();
		readScansOverview(dataInputStream, chromatogram, monitor);
		//
		dataInputStream.close();
		//
		return chromatogram;
	}

	private void readScansOverview(DataInputStream dataInputStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		IVendorScan massSpectrum;
		IVendorIon ion;
		/*
		 * Retention Times - Total Signals
		 */
		int scans = dataInputStream.readInt();
		for(int scan = 1; scan <= scans; scan++) {
			massSpectrum = new VendorScan();
			int retentionTime = dataInputStream.readInt(); // Retention Time
			float abundance = dataInputStream.readFloat(); // Total Signal
			try {
				ion = new VendorIon(AbstractIon.TIC_ION, abundance);
				massSpectrum.setRetentionTime(retentionTime);
				massSpectrum.addIon(ion);
				chromatogram.addScan(massSpectrum);
			} catch(AbundanceLimitExceededException e) {
				logger.warn(e);
			} catch(IonLimitExceededException e) {
				logger.warn(e);
			}
		}
	}

	private void setAdditionalInformation(File file, IChromatogramMSD chromatogram, IProgressMonitor monitor) {

		chromatogram.setConverterId(IFormat.CONVERTER_ID_CHROMATOGRAM);
		chromatogram.setFile(file);
		// Delay
		int startRetentionTime = chromatogram.getStartRetentionTime();
		int scanDelay = startRetentionTime;
		chromatogram.setScanDelay(scanDelay);
		// Interval
		int endRetentionTime = chromatogram.getStopRetentionTime();
		int scanInterval = endRetentionTime / chromatogram.getNumberOfScans();
		chromatogram.setScanInterval(scanInterval);
	}

	private void readScanProxies(ZipFile zipFile, String directoryPrefix, File file, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		IIonTransitionSettings ionTransitionSettings = chromatogram.getIonTransitionSettings();
		//
		DataInputStream dataInputStream = getDataInputStream(zipFile, directoryPrefix + IFormat.FILE_SCANPROXIES_MSD);
		/*
		 * Scans
		 */
		int scans = dataInputStream.readInt();
		SubMonitor subMonitor = SubMonitor.convert(monitor, IConstants.IMPORT_SCAN, scans);
		try {
			for(int scan = 1; scan <= scans; scan++) {
				//
				int offset = dataInputStream.readInt(); // Offset
				int retentionTime = dataInputStream.readInt(); // Retention Time
				int numberOfIons = dataInputStream.readInt(); // Number of Ions
				float totalSignal = dataInputStream.readFloat(); // Total Signal
				float retentionIndex = dataInputStream.readFloat(); // Retention Index
				int timeSegmentId = dataInputStream.readInt(); // Time Segment Id
				int cycleNumber = dataInputStream.readInt(); // Cycle Number
				//
				IVendorScanProxy massSpectrum = new VendorScanProxy(file, offset, IFormat.CHROMATOGRAM_VERSION_1100, ionTransitionSettings);
				massSpectrum.setRetentionTime(retentionTime);
				massSpectrum.setNumberOfIons(numberOfIons);
				massSpectrum.setTotalSignal(totalSignal);
				massSpectrum.setRetentionIndex(retentionIndex);
				massSpectrum.setTimeSegmentId(timeSegmentId);
				massSpectrum.setCycleNumber(cycleNumber);
				chromatogram.addScan(massSpectrum);
				subMonitor.worked(1);
			}
			//
			dataInputStream.close();
		} finally {
			SubMonitor.done(subMonitor);
		}
	}

	private void readScans(DataInputStream dataInputStream, boolean closeStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		IIonTransitionSettings ionTransitionSettings = chromatogram.getIonTransitionSettings();
		/*
		 * Scans
		 */
		IReaderProxy readerProxy = new ReaderProxy_1100();
		int scans = dataInputStream.readInt();
		SubMonitor subMonitor = SubMonitor.convert(monitor, IConstants.IMPORT_SCAN, scans * 2);
		try {
			for(int scan = 1; scan <= scans; scan++) {
				IVendorScan massSpectrum = new VendorScan();
				readerProxy.readMassSpectrum(massSpectrum, dataInputStream, ionTransitionSettings);
				chromatogram.addScan(massSpectrum);
				subMonitor.worked(1);
			}
			//
			if(closeStream) {
				dataInputStream.close();
			}
		} finally {
			SubMonitor.done(subMonitor);
		}
	}

	private void readBaseline(DataInputStream dataInputStream, boolean closeStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		int scans = dataInputStream.readInt(); // Number of Scans
		List<IBaselineElement> baselineElements = new ArrayList<IBaselineElement>();
		SubMonitor subMonitor = SubMonitor.convert(monitor, IConstants.IMPORT_BASELINE, scans * 2);
		//
		try {
			/*
			 * Get the Baseline
			 */
			for(int scan = 1; scan <= scans; scan++) {
				int retentionTime = dataInputStream.readInt(); // Retention Time
				float backgroundAbundance = dataInputStream.readFloat(); // Background Abundance
				IBaselineElement baselineElement = new BaselineElement(retentionTime, backgroundAbundance);
				baselineElements.add(baselineElement);
				subMonitor.worked(1);
			}
			/*
			 * Set the Baseline
			 */
			IBaselineModel baselineModel = chromatogram.getBaselineModel();
			for(int index = 0; index < (scans - 1); index++) {
				/*
				 * Retention times and background abundances.
				 * Set the baseline.
				 */
				IBaselineElement baselineElement = baselineElements.get(index);
				IBaselineElement baselineElementNext = baselineElements.get(index + 1);
				int startRetentionTime = baselineElement.getRetentionTime();
				float startBackgroundAbundance = baselineElement.getBackgroundAbundance();
				int stopRetentionTime = baselineElementNext.getRetentionTime();
				float stopBackgroundAbundance = baselineElementNext.getBackgroundAbundance();
				/*
				 * Add the baseline in validate == false modus, cause we know, that the baseline
				 * has been saved without overlapping segments.
				 */
				baselineModel.addBaseline(startRetentionTime, stopRetentionTime, startBackgroundAbundance, stopBackgroundAbundance, false);
				subMonitor.worked(1);
			}
			//
			if(closeStream) {
				dataInputStream.close();
			}
		} finally {
			SubMonitor.done(subMonitor);
		}
	}

	private void readPeaks(DataInputStream dataInputStream, boolean closeStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		int numberOfPeaks = dataInputStream.readInt(); // Number of Peaks
		SubMonitor subMonitor = SubMonitor.convert(monitor, IConstants.IMPORT_PEAK, numberOfPeaks);
		try {
			for(int i = 1; i <= numberOfPeaks; i++) {
				try {
					IChromatogramPeakMSD peak = readPeak(dataInputStream, chromatogram, subMonitor.split(1));
					chromatogram.addPeak(peak);
				} catch(IllegalArgumentException e) {
					logger.warn(e);
				} catch(PeakException e) {
					logger.warn(e);
				}
			}
			//
			if(closeStream) {
				dataInputStream.close();
			}
		} finally {
			SubMonitor.done(subMonitor);
		}
	}

	private IChromatogramPeakMSD readPeak(DataInputStream dataInputStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException, IllegalArgumentException, PeakException {

		SubMonitor subMonitor = SubMonitor.convert(monitor, 40);
		try {
			// TODO monitor handling...
			IIonTransitionSettings ionTransitionSettings = chromatogram.getIonTransitionSettings();
			//
			String detectorDescription = readString(dataInputStream); // Detector Description
			String quantifierDescription = readString(dataInputStream);
			boolean activeForAnalysis = dataInputStream.readBoolean();
			String integratorDescription = readString(dataInputStream); // Integrator Description
			String modelDescription = readString(dataInputStream); // Model Description
			PeakType peakType = PeakType.valueOf(readString(dataInputStream)); // Peak Type
			int suggestedNumberOfComponents = dataInputStream.readInt(); // Suggest Number Of Components
			//
			float startBackgroundAbundance = dataInputStream.readFloat(); // Start Background Abundance
			float stopBackgroundAbundance = dataInputStream.readFloat(); // Stop Background Abundance
			//
			IPeakMassSpectrum peakMaximum = readPeakMassSpectrum(dataInputStream, ionTransitionSettings, subMonitor.split(10));
			//
			int numberOfRetentionTimes = dataInputStream.readInt(); // Number Retention Times
			IPeakIntensityValues intensityValues = new PeakIntensityValues(Float.MAX_VALUE);
			for(int i = 1; i <= numberOfRetentionTimes; i++) {
				int retentionTime = dataInputStream.readInt(); // Retention Time
				float relativeIntensity = dataInputStream.readFloat(); // Intensity
				intensityValues.addIntensityValue(retentionTime, relativeIntensity);
			}
			intensityValues.normalize();
			//
			IPeakModelMSD peakModel = new PeakModelMSD(peakMaximum, intensityValues, startBackgroundAbundance, stopBackgroundAbundance);
			IChromatogramPeakMSD peak = new ChromatogramPeakMSD(peakModel, chromatogram);
			peak.setDetectorDescription(detectorDescription);
			peak.setQuantifierDescription(quantifierDescription);
			peak.setActiveForAnalysis(activeForAnalysis);
			peak.setIntegratorDescription(integratorDescription);
			peak.setModelDescription(modelDescription);
			peak.setPeakType(peakType);
			peak.setSuggestedNumberOfComponents(suggestedNumberOfComponents);
			//
			List<IIntegrationEntry> integrationEntries = readIntegrationEntries(dataInputStream);
			peak.setIntegratedArea(integrationEntries, integratorDescription);
			/*
			 * Identification Results
			 */
			readPeakIdentificationTargets(dataInputStream, peak, subMonitor.split(10));
			/*
			 * Quantitation Results
			 */
			readPeakQuantitationEntries(dataInputStream, peak, subMonitor.split(10));
			/*
			 * Optimized Mass Spectrum
			 */
			boolean readOptimizedMassSpectrum = dataInputStream.readBoolean();
			if(readOptimizedMassSpectrum) {
				IScanMSD optimizedMassSpectrum = new ScanMSD();
				readNormalMassSpectrum(optimizedMassSpectrum, dataInputStream, ionTransitionSettings, subMonitor.split(10));
				peakMaximum.setOptimizedMassSpectrum(optimizedMassSpectrum);
			}
			//
			List<IInternalStandard> internalStandards = readInternalStandards(dataInputStream);
			peak.addInternalStandards(internalStandards);
			//
			return peak;
		} finally {
			SubMonitor.done(subMonitor);
		}
	}

	private List<IInternalStandard> readInternalStandards(DataInputStream dataInputStream) throws IOException {

		List<IInternalStandard> internalStandards = new ArrayList<IInternalStandard>();
		int numberOfInternalStandards = dataInputStream.readInt();
		for(int i = 1; i <= numberOfInternalStandards; i++) {
			String name = readString(dataInputStream);
			double concentration = dataInputStream.readDouble();
			String concentrationUnit = readString(dataInputStream);
			double responseFactor = dataInputStream.readDouble();
			String chemicalClass = readString(dataInputStream);
			IInternalStandard internalStandard = new InternalStandard(name, concentration, concentrationUnit, responseFactor);
			internalStandard.setChemicalClass(chemicalClass);
			internalStandards.add(internalStandard);
		}
		return internalStandards;
	}

	private List<IIntegrationEntry> readIntegrationEntries(DataInputStream dataInputStream) throws IOException {

		List<IIntegrationEntry> integrationEntries = new ArrayList<IIntegrationEntry>();
		int numberOfIntegrationEntries = dataInputStream.readInt(); // Number Integration Entries
		for(int i = 1; i <= numberOfIntegrationEntries; i++) {
			double ion = dataInputStream.readDouble(); // m/z
			double integratedArea = dataInputStream.readDouble(); // Integrated Area
			IIntegrationEntryMSD integrationEntry = new IntegrationEntryMSD(ion, integratedArea);
			integrationEntries.add(integrationEntry);
		}
		return integrationEntries;
	}

	private void readPeakIdentificationTargets(DataInputStream dataInputStream, IPeakMSD peak, IProgressMonitor monitor) throws IOException {

		int numberOfPeakTargets = dataInputStream.readInt(); // Number Peak Targets
		for(int i = 1; i <= numberOfPeakTargets; i++) {
			//
			String identifier = readString(dataInputStream); // Identifier
			boolean manuallyVerified = dataInputStream.readBoolean();
			//
			String casNumber = readString(dataInputStream); // CAS-Number
			String comments = readString(dataInputStream); // Comments
			String referenceIdentifier = readString(dataInputStream);
			String miscellaneous = readString(dataInputStream); // Miscellaneous
			String database = readString(dataInputStream);
			String contributor = readString(dataInputStream);
			String name = readString(dataInputStream); // Name
			Set<String> synonyms = new HashSet<String>(); // Synonyms
			int numberOfSynonyms = dataInputStream.readInt();
			for(int j = 0; j < numberOfSynonyms; j++) {
				synonyms.add(readString(dataInputStream));
			}
			String formula = readString(dataInputStream); // Formula
			String smiles = readString(dataInputStream); // SMILES
			String inChI = readString(dataInputStream); // InChI
			double molWeight = dataInputStream.readDouble(); // Mol Weight
			float matchFactor = dataInputStream.readFloat(); // Match Factor
			float matchFactorDirect = dataInputStream.readFloat(); // Match Factor Direct
			float reverseMatchFactor = dataInputStream.readFloat(); // Reverse Match Factor
			float reverseMatchFactorDirect = dataInputStream.readFloat(); // Reverse Match Factor Direct
			float probability = dataInputStream.readFloat(); // Probability
			boolean isMatch = dataInputStream.readBoolean();
			//
			IPeakLibraryInformation libraryInformation = new PeakLibraryInformation();
			libraryInformation.setCasNumber(casNumber);
			libraryInformation.setComments(comments);
			libraryInformation.setReferenceIdentifier(referenceIdentifier);
			libraryInformation.setMiscellaneous(miscellaneous);
			libraryInformation.setDatabase(database);
			libraryInformation.setContributor(contributor);
			libraryInformation.setName(name);
			libraryInformation.setSynonyms(synonyms);
			libraryInformation.setFormula(formula);
			libraryInformation.setSmiles(smiles);
			libraryInformation.setInChI(inChI);
			libraryInformation.setMolWeight(molWeight);
			//
			IComparisonResult comparisonResult = new PeakComparisonResult(matchFactor, reverseMatchFactor, matchFactorDirect, reverseMatchFactorDirect, probability);
			comparisonResult.setMatch(isMatch);
			//
			try {
				IIdentificationTarget identificationEntry = new IdentificationTarget(libraryInformation, comparisonResult);
				identificationEntry.setIdentifier(identifier);
				identificationEntry.setManuallyVerified(manuallyVerified);
				peak.getTargets().add(identificationEntry);
			} catch(ReferenceMustNotBeNullException e) {
				logger.warn(e);
			}
		}
	}

	private void readMassSpectrumIdentificationTargets(DataInputStream dataInputStream, IScanMSD massSpectrum, IProgressMonitor monitor) throws IOException {

		int numberOfMassSpectrumTargets = dataInputStream.readInt(); // Number Mass Spectrum Targets
		for(int i = 1; i <= numberOfMassSpectrumTargets; i++) {
			//
			String identifier = readString(dataInputStream); // Identifier
			boolean manuallyVerified = dataInputStream.readBoolean();
			//
			String casNumber = readString(dataInputStream); // CAS-Number
			String comments = readString(dataInputStream); // Comments
			String referenceIdentifier = readString(dataInputStream);
			String miscellaneous = readString(dataInputStream); // Miscellaneous
			String database = readString(dataInputStream);
			String contributor = readString(dataInputStream);
			String name = readString(dataInputStream); // Name
			Set<String> synonyms = new HashSet<String>(); // Synonyms
			int numberOfSynonyms = dataInputStream.readInt();
			for(int j = 0; j < numberOfSynonyms; j++) {
				synonyms.add(readString(dataInputStream));
			}
			String formula = readString(dataInputStream); // Formula
			String smiles = readString(dataInputStream); // SMILES
			String inChI = readString(dataInputStream); // InChI
			double molWeight = dataInputStream.readDouble(); // Mol Weight
			float matchFactor = dataInputStream.readFloat(); // Match Factor
			float matchFactorDirect = dataInputStream.readFloat(); // Match Factor Direct
			float reverseMatchFactor = dataInputStream.readFloat(); // Reverse Match Factor
			float reverseMatchFactorDirect = dataInputStream.readFloat(); // Reverse Match Factor Direct
			float probability = dataInputStream.readFloat(); // Probability
			boolean isMatch = dataInputStream.readBoolean();
			//
			ILibraryInformation libraryInformation = new LibraryInformation();
			libraryInformation.setCasNumber(casNumber);
			libraryInformation.setComments(comments);
			libraryInformation.setReferenceIdentifier(referenceIdentifier);
			libraryInformation.setMiscellaneous(miscellaneous);
			libraryInformation.setDatabase(database);
			libraryInformation.setContributor(contributor);
			libraryInformation.setName(name);
			libraryInformation.setSynonyms(synonyms);
			libraryInformation.setFormula(formula);
			libraryInformation.setSmiles(smiles);
			libraryInformation.setInChI(inChI);
			libraryInformation.setMolWeight(molWeight);
			//
			IComparisonResult comparisonResult = new ComparisonResult(matchFactor, reverseMatchFactor, matchFactorDirect, reverseMatchFactorDirect, probability);
			comparisonResult.setMatch(isMatch);
			//
			try {
				IIdentificationTarget identificationEntry = new IdentificationTarget(libraryInformation, comparisonResult);
				identificationEntry.setIdentifier(identifier);
				identificationEntry.setManuallyVerified(manuallyVerified);
				massSpectrum.getTargets().add(identificationEntry);
			} catch(ReferenceMustNotBeNullException e) {
				logger.warn(e);
			}
		}
	}

	private void readPeakQuantitationEntries(DataInputStream dataInputStream, IPeakMSD peak, IProgressMonitor monitor) throws IOException {

		int numberOfQuantitationEntries = dataInputStream.readInt(); // Number Quantitation Entries
		for(int i = 1; i <= numberOfQuantitationEntries; i++) {
			//
			String name = readString(dataInputStream); // Name
			String chemicalClass = readString(dataInputStream); // Chemical Class
			double concentration = dataInputStream.readDouble(); // Concentration
			String concentrationUnit = readString(dataInputStream); // Concentration Unit
			double area = dataInputStream.readDouble(); // Area
			String calibrationMethod = readString(dataInputStream); // Calibration Method
			boolean usedCrossZero = dataInputStream.readBoolean(); // Used Cross Zero
			String description = readString(dataInputStream); // Description
			/*
			 * Only MSD stores an ion.
			 */
			IQuantitationEntry quantitationEntry;
			boolean isMSD = dataInputStream.readBoolean(); // Ion value is stored or not.
			if(isMSD) {
				double ion = dataInputStream.readDouble(); // Ion
				quantitationEntry = new QuantitationEntryMSD(name, concentration, concentrationUnit, area, ion);
			} else {
				quantitationEntry = new QuantitationEntry(name, concentration, concentrationUnit, area);
			}
			//
			quantitationEntry.setChemicalClass(chemicalClass);
			quantitationEntry.setCalibrationMethod(calibrationMethod);
			quantitationEntry.setUsedCrossZero(usedCrossZero);
			quantitationEntry.setDescription(description);
			//
			peak.addQuantitationEntry(quantitationEntry);
		}
	}

	private void readArea(DataInputStream dataInputStream, boolean closeStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		String chromatogramIntegratorDescription = readString(dataInputStream); // Chromatogram Integrator Description
		List<IIntegrationEntry> chromatogramIntegrationEntries = readIntegrationEntries(dataInputStream);
		chromatogram.setChromatogramIntegratedArea(chromatogramIntegrationEntries, chromatogramIntegratorDescription);
		//
		String backgroundIntegratorDescription = readString(dataInputStream); // Background Integrator Description
		List<IIntegrationEntry> backgroundIntegrationEntries = readIntegrationEntries(dataInputStream);
		chromatogram.setBackgroundIntegratedArea(backgroundIntegrationEntries, backgroundIntegratorDescription);
		//
		if(closeStream) {
			dataInputStream.close();
		}
	}

	private void readIdentification(DataInputStream dataInputStream, boolean closeStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		int numberOfTargets = dataInputStream.readInt(); // Number of Targets
		SubMonitor subMonitor = SubMonitor.convert(monitor, numberOfTargets);
		try {
			for(int i = 1; i <= numberOfTargets; i++) {
				//
				String identifier = readString(dataInputStream); // Identifier
				boolean manuallyVerified = dataInputStream.readBoolean();
				//
				String casNumber = readString(dataInputStream); // CAS-Number
				String comments = readString(dataInputStream); // Comments
				String referenceIdentifier = readString(dataInputStream);
				String miscellaneous = readString(dataInputStream); // Miscellaneous
				String database = readString(dataInputStream);
				String contributor = readString(dataInputStream);
				String name = readString(dataInputStream); // Name
				Set<String> synonyms = new HashSet<String>(); // Synonyms
				int numberOfSynonyms = dataInputStream.readInt();
				for(int j = 0; j < numberOfSynonyms; j++) {
					synonyms.add(readString(dataInputStream));
				}
				String formula = readString(dataInputStream); // Formula
				String smiles = readString(dataInputStream); // SMILES
				String inChI = readString(dataInputStream); // InChI
				double molWeight = dataInputStream.readDouble(); // Mol Weight
				float matchFactor = dataInputStream.readFloat(); // Match Factor
				float matchFactorDirect = dataInputStream.readFloat(); // Match Factor Direct
				float reverseMatchFactor = dataInputStream.readFloat(); // Reverse Match Factor
				float reverseMatchFactorDirect = dataInputStream.readFloat(); // Reverse Match Factor Direct
				float probability = dataInputStream.readFloat(); // Probability
				boolean isMatch = dataInputStream.readBoolean();
				//
				IChromatogramLibraryInformation libraryInformation = new ChromatogramLibraryInformation();
				libraryInformation.setCasNumber(casNumber);
				libraryInformation.setComments(comments);
				libraryInformation.setReferenceIdentifier(referenceIdentifier);
				libraryInformation.setMiscellaneous(miscellaneous);
				libraryInformation.setDatabase(database);
				libraryInformation.setContributor(contributor);
				libraryInformation.setName(name);
				libraryInformation.setSynonyms(synonyms);
				libraryInformation.setFormula(formula);
				libraryInformation.setSmiles(smiles);
				libraryInformation.setInChI(inChI);
				libraryInformation.setMolWeight(molWeight);
				//
				IComparisonResult comparisonResult = new ChromatogramComparisonResult(matchFactor, reverseMatchFactor, matchFactorDirect, reverseMatchFactorDirect, probability);
				comparisonResult.setMatch(isMatch);
				//
				try {
					IIdentificationTarget identificationEntry = new IdentificationTarget(libraryInformation, comparisonResult);
					identificationEntry.setIdentifier(identifier);
					identificationEntry.setManuallyVerified(manuallyVerified);
					chromatogram.getTargets().add(identificationEntry);
				} catch(ReferenceMustNotBeNullException e) {
					logger.warn(e);
				}
				subMonitor.worked(1);
			}
			//
			if(closeStream) {
				dataInputStream.close();
			}
		} finally {
			SubMonitor.done(subMonitor);
		}
	}

	private void readHistory(DataInputStream dataInputStream, boolean closeStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		IEditHistory editHistory = chromatogram.getEditHistory();
		int numberOfEntries = dataInputStream.readInt(); // Number of entries
		SubMonitor subMonitor = SubMonitor.convert(monitor, numberOfEntries);
		try {
			for(int i = 1; i <= numberOfEntries; i++) {
				long time = dataInputStream.readLong(); // Date
				String description = readString(dataInputStream); // Description
				//
				Date date = new Date(time);
				IEditInformation editInformation = new EditInformation(date, description);
				editHistory.add(editInformation);
				subMonitor.worked(1);
			}
			//
			if(closeStream) {
				dataInputStream.close();
			}
		} finally {
			SubMonitor.done(subMonitor);
		}
	}

	private void readMiscellaneous(DataInputStream dataInputStream, boolean closeStream, IChromatogramMSD chromatogram, IProgressMonitor monitor) throws IOException {

		long time = dataInputStream.readLong(); // Date
		String miscInfo = readString(dataInputStream); // Miscellaneous Info
		String miscInfoSeparated = readString(dataInputStream);
		String dataName = readString(dataInputStream);
		String operator = readString(dataInputStream); // Operator
		//
		Date date = new Date(time);
		chromatogram.setDate(date);
		chromatogram.setMiscInfo(miscInfo);
		chromatogram.setMiscInfoSeparated(miscInfoSeparated);
		chromatogram.setDataName(dataName);
		chromatogram.setOperator(operator);
		//
		if(closeStream) {
			dataInputStream.close();
		}
	}

	private IPeakMassSpectrum readPeakMassSpectrum(DataInputStream dataInputStream, IIonTransitionSettings ionTransitionSettings, IProgressMonitor monitor) throws IOException {

		short massSpectrometer = dataInputStream.readShort(); // Mass Spectrometer
		short massSpectrumType = dataInputStream.readShort(); // Mass Spectrum Type
		double precursorIon = dataInputStream.readDouble(); // Precursor Ion (0 if MS1 or none has been selected)
		//
		IPeakMassSpectrum massSpectrum = new PeakMassSpectrum();
		massSpectrum.setMassSpectrometer(massSpectrometer);
		massSpectrum.setMassSpectrumType(massSpectrumType);
		massSpectrum.setPrecursorIon(precursorIon);
		//
		readNormalMassSpectrum(massSpectrum, dataInputStream, ionTransitionSettings, monitor);
		//
		return massSpectrum;
	}

	private void readNormalMassSpectrum(IScanMSD massSpectrum, DataInputStream dataInputStream, IIonTransitionSettings ionTransitionSettings, IProgressMonitor monitor) throws IOException {

		int retentionTime = dataInputStream.readInt(); // Retention Time
		int retentionTimeColumn1 = dataInputStream.readInt();
		int retentionTimeColumn2 = dataInputStream.readInt();
		float retentionIndex = dataInputStream.readFloat(); // Retention Index
		if(dataInputStream.readBoolean()) {
			int size = dataInputStream.readInt();
			for(int i = 0; i < size; i++) {
				RetentionIndexType retentionIndexType = RetentionIndexType.valueOf(readString(dataInputStream));
				float retentionIndexAdditional = dataInputStream.readFloat();
				massSpectrum.setRetentionIndex(retentionIndexType, retentionIndexAdditional);
			}
		}
		int timeSegmentId = dataInputStream.readInt(); // Time Segment Id
		int cycleNumber = dataInputStream.readInt(); // Cycle Number
		massSpectrum.setRetentionTime(retentionTime);
		massSpectrum.setRetentionTimeColumn1(retentionTimeColumn1);
		massSpectrum.setRetentionTimeColumn2(retentionTimeColumn2);
		massSpectrum.setRetentionIndex(retentionIndex);
		massSpectrum.setTimeSegmentId(timeSegmentId);
		massSpectrum.setCycleNumber(cycleNumber);
		//
		int numberOfIons = dataInputStream.readInt(); // Number of ions
		for(int i = 1; i <= numberOfIons; i++) {
			/*
			 * Read Ions
			 */
			try {
				IVendorIon ion = readIon(dataInputStream, ionTransitionSettings);
				massSpectrum.addIon(ion);
			} catch(AbundanceLimitExceededException e) {
				logger.warn(e);
			} catch(IonLimitExceededException e) {
				logger.warn(e);
			} catch(IonTransitionIsNullException e) {
				logger.warn(e);
			}
		}
		/*
		 * Identification Results
		 */
		readMassSpectrumIdentificationTargets(dataInputStream, massSpectrum, monitor);
	}

	private IVendorIon readIon(DataInputStream dataInputStream, IIonTransitionSettings ionTransitionSettings) throws IOException, AbundanceLimitExceededException, IonLimitExceededException, IonTransitionIsNullException {

		IVendorIon ion;
		//
		double mz = dataInputStream.readDouble(); // m/z
		float abundance = dataInputStream.readFloat(); // Abundance
		/*
		 * Ion Transition
		 */
		int transition = dataInputStream.readInt();
		if(transition == 0) {
			ion = new VendorIon(mz, abundance);
		} else {
			/*
			 * parent m/z start, ...
			 */
			String compoundName = readString(dataInputStream); // compound name
			double filter1FirstIon = dataInputStream.readDouble(); // parent m/z start
			double filter1LastIon = dataInputStream.readDouble(); // parent m/z stop
			double filter3FirstIon = dataInputStream.readDouble(); // daughter m/z start
			double filter3LastIon = dataInputStream.readDouble(); // daughter m/z stop
			double collisionEnergy = dataInputStream.readDouble(); // collision energy
			double filter1Resolution = dataInputStream.readDouble(); // q1 resolution
			double filter3Resolution = dataInputStream.readDouble(); // q3 resolution
			int transitionGroup = dataInputStream.readInt(); // transition group
			int dwell = dataInputStream.readInt(); // dwell
			//
			IIonTransition ionTransition = ionTransitionSettings.getIonTransition(compoundName, filter1FirstIon, filter1LastIon, filter3FirstIon, filter3LastIon, collisionEnergy, filter1Resolution, filter3Resolution, transitionGroup);
			ionTransition.setDwell(dwell);
			ion = new VendorIon(mz, abundance, ionTransition);
		}
		return ion;
	}

	private boolean isValidFileFormat(ZipFile zipFile) throws IOException {

		boolean isValid = false;
		DataInputStream dataInputStream;
		//
		dataInputStream = getDataInputStream(zipFile, IFormat.FILE_VERSION);
		String version = readString(dataInputStream);
		if(version.equals(IFormat.CHROMATOGRAM_VERSION_1100)) {
			isValid = true;
		}
		//
		dataInputStream.close();
		//
		return isValid;
	}
}
