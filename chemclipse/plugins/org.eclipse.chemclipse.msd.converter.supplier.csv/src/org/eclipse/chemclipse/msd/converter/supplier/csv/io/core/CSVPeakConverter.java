/*******************************************************************************
 * Copyright (c) 2020 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.csv.io.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.chemclipse.converter.core.IMagicNumberMatcher;
import org.eclipse.chemclipse.model.core.IChromatogramOverview;
import org.eclipse.chemclipse.model.core.IChromatogramPeak;
import org.eclipse.chemclipse.model.core.IPeakIntensityValues;
import org.eclipse.chemclipse.model.core.IPeaks;
import org.eclipse.chemclipse.model.core.IScan;
import org.eclipse.chemclipse.model.exceptions.AbundanceLimitExceededException;
import org.eclipse.chemclipse.model.identifier.IIdentificationTarget;
import org.eclipse.chemclipse.model.identifier.ILibraryInformation;
import org.eclipse.chemclipse.model.implementation.IntegrationEntry;
import org.eclipse.chemclipse.model.implementation.PeakIntensityValues;
import org.eclipse.chemclipse.model.implementation.Peaks;
import org.eclipse.chemclipse.msd.converter.peak.IPeakExportConverter;
import org.eclipse.chemclipse.msd.converter.peak.IPeakImportConverter;
import org.eclipse.chemclipse.msd.model.core.IIon;
import org.eclipse.chemclipse.msd.model.core.IIonProvider;
import org.eclipse.chemclipse.msd.model.core.IPeakMSD;
import org.eclipse.chemclipse.msd.model.core.IPeakMassSpectrum;
import org.eclipse.chemclipse.msd.model.core.IPeakModelMSD;
import org.eclipse.chemclipse.msd.model.exceptions.IonLimitExceededException;
import org.eclipse.chemclipse.msd.model.implementation.Ion;
import org.eclipse.chemclipse.msd.model.implementation.PeakMSD;
import org.eclipse.chemclipse.msd.model.implementation.PeakMassSpectrum;
import org.eclipse.chemclipse.msd.model.implementation.PeakModelMSD;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.ProcessingInfo;
import org.eclipse.core.runtime.IProgressMonitor;

public class CSVPeakConverter implements IPeakExportConverter, IPeakImportConverter, IMagicNumberMatcher {

	private static final String HEADER_AREA = "Area";
	private static final String HEADER_RRT = "RRT (min)";
	private static final String HEADER_RI = "RI";
	private static final String HEADER_INTENSITIES = "intensities";
	private static final String HEADER_MZ = "m/z";
	private static final String HEADER_RT = "RT (min)";
	private static final char SEPERATOR_VALUE = ':';
	private static final char SEPERATOR_RECORD = ' ';
	private static final Pattern SEPERATOR_VALUE_PATTERN = Pattern.compile(String.valueOf(SEPERATOR_VALUE), Pattern.LITERAL);
	private static final Pattern SEPERATOR_RECORD_PATTERN = Pattern.compile(String.valueOf(SEPERATOR_RECORD), Pattern.LITERAL);
	private static final String NAME = "CSV Peak Export";
	private static final String[] HEADERS = {"Name", HEADER_RT, HEADER_RRT, HEADER_RI, HEADER_AREA, "S/N", "CAS", HEADER_MZ, HEADER_INTENSITIES};
	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.000");

	// export
	@Override
	public IProcessingInfo<?> convert(File file, IPeaks<? extends IPeakMSD> peaks, boolean append, IProgressMonitor monitor) {

		try {
			try (CSVPrinter csv = new CSVPrinter(new FileWriter(file, append), CSVFormat.EXCEL.withNullString(""))) {
				if(!append) {
					csv.printRecord(Arrays.asList(HEADERS));
				}
				NumberFormat nf;
				synchronized(NUMBER_FORMAT) {
					nf = (NumberFormat)NUMBER_FORMAT.clone();
				}
				for(IPeakMSD peak : peaks.getPeaks()) {
					IIdentificationTarget target = IIdentificationTarget.getBestIdentificationTarget(peak.getTargets());
					IPeakModelMSD peakModel = peak.getPeakModel();
					// Name
					csv.print(getLibInfo(target, ILibraryInformation::getName));
					// RT
					csv.print(nf.format(peakModel.getRetentionTimeAtPeakMaximum() / IChromatogramOverview.MINUTE_CORRELATION_FACTOR));
					// RRT
					csv.print(nf.format(peakModel.getPeakMaximum().getRelativeRetentionTime() / IChromatogramOverview.MINUTE_CORRELATION_FACTOR));
					// RI
					csv.print(nf.format(peakModel.getPeakMaximum().getRetentionIndex()));
					// Area
					csv.print(nf.format(peak.getIntegratedArea()));
					// S/N
					if(peak instanceof IChromatogramPeak) {
						csv.print(nf.format(((IChromatogramPeak)peak).getSignalToNoiseRatio()));
					} else {
						csv.print("-");
					}
					// CAS
					csv.print(getLibInfo(target, ILibraryInformation::getCasNumber));
					// mass spectrum
					csv.print(writeMassSpectrum(peak.getPeakModel().getPeakMassSpectrum()));
					// intensities
					csv.print(writeIntensities(peak.getPeakModel()));
					csv.println();
				}
			}
		} catch(IOException e) {
			ProcessingInfo<Object> error = new ProcessingInfo<>();
			error.addErrorMessage(NAME, "Export to CSV failed", e);
			return error;
		}
		return new ProcessingInfo<>(file);
	}

	private StringBuilder writeIntensities(IPeakModelMSD peakModel) {

		StringBuilder sb = new StringBuilder();
		if(peakModel != null) {
			List<Integer> retentionTimes = peakModel.getRetentionTimes();
			for(Integer rt : retentionTimes) {
				if(sb.length() > 0) {
					sb.append(SEPERATOR_RECORD);
				}
				sb.append(rt);
				sb.append(SEPERATOR_VALUE);
				sb.append(peakModel.getPeakAbundance(rt));
			}
		}
		return sb;
	}

	private static StringBuilder writeMassSpectrum(IIonProvider provider) {

		StringBuilder sb = new StringBuilder();
		if(provider != null) {
			for(final IIon ion : provider.getIons()) {
				if(sb.length() > 0) {
					sb.append(SEPERATOR_RECORD);
				}
				sb.append(ion.getIon());
				sb.append(':');
				sb.append(ion.getAbundance());
			}
		}
		return sb;
	}

	private <R> R getLibInfo(IIdentificationTarget target, Function<ILibraryInformation, R> fkt) {

		if(target != null) {
			ILibraryInformation information = target.getLibraryInformation();
			return fkt.apply(information);
		}
		return null;
	}

	// import
	@Override
	public IProcessingInfo<IPeaks<?>> convert(File file, IProgressMonitor monitor) {

		try {
			Peaks result = new Peaks();
			try (CSVParser parser = new CSVParser(new FileReader(file), CSVFormat.EXCEL.withHeader(HEADERS).withSkipHeaderRecord())) {
				NumberFormat nf;
				synchronized(NUMBER_FORMAT) {
					nf = (NumberFormat)NUMBER_FORMAT.clone();
				}
				for(CSVRecord record : parser) {
					PeakModelMSD peakModel = new PeakModelMSD(parseMassSpectrum(HEADER_MZ), parseIntensityValues(record.get(HEADER_INTENSITIES)));
					IScan maximum = peakModel.getPeakMaximum();
					maximum.setRetentionTime((int)(nf.parse(record.get(HEADER_RT)).doubleValue() * IChromatogramOverview.MINUTE_CORRELATION_FACTOR));
					maximum.setRelativeRetentionTime((int)(nf.parse(record.get(HEADER_RRT)).doubleValue() * IChromatogramOverview.MINUTE_CORRELATION_FACTOR));
					maximum.setRetentionIndex(nf.parse(record.get(HEADER_RI)).floatValue());
					PeakMSD peakMSD = new PeakMSD(peakModel);
					peakMSD.addAllIntegrationEntries(new IntegrationEntry(nf.parse(record.get(HEADER_AREA)).doubleValue()));
					result.addPeak(peakMSD);
				}
			}
			return new ProcessingInfo<>(result);
		} catch(ParseException | IOException e) {
			ProcessingInfo<IPeaks<?>> error = new ProcessingInfo<>();
			error.addErrorMessage(NAME, "Import failed", e);
			return error;
		}
	}

	private IPeakMassSpectrum parseMassSpectrum(String headerMz) {

		PeakMassSpectrum massSpectrum = new PeakMassSpectrum();
		SEPERATOR_RECORD_PATTERN.splitAsStream(headerMz).spliterator().forEachRemaining(record -> {
			String[] values = SEPERATOR_VALUE_PATTERN.split(record, 2);
			double ion = Integer.parseInt(values[0]);
			float intensity = Float.parseFloat(values[1]);
			try {
				massSpectrum.addIon(new Ion(ion, intensity));
			} catch(AbundanceLimitExceededException
					| IonLimitExceededException e) {
				throw new RuntimeException("can't read ion", e);
			}
		});
		return massSpectrum;
	}

	private IPeakIntensityValues parseIntensityValues(String headerIntensity) {

		PeakIntensityValues intensityValues = new PeakIntensityValues(Float.MAX_VALUE);
		SEPERATOR_RECORD_PATTERN.splitAsStream(headerIntensity).spliterator().forEachRemaining(record -> {
			String[] values = SEPERATOR_VALUE_PATTERN.split(record, 2);
			int rt = Integer.parseInt(values[0]);
			float abundance = Float.parseFloat(values[1]);
			intensityValues.addIntensityValue(rt, abundance);
		});
		intensityValues.normalize();
		return intensityValues;
	}

	// magic number
	@Override
	public boolean checkFileFormat(File file) {

		return file.getName().toLowerCase().endsWith(".csv") && matchHeader(file);
	}

	private boolean matchHeader(File file) {

		try {
			try (CSVParser parser = new CSVParser(new FileReader(file), CSVFormat.EXCEL.withHeader())) {
				String[] array = parser.getHeaderMap().keySet().toArray(new String[0]);
				return Arrays.equals(array, HEADERS);
			}
		} catch(IOException e) {
		}
		return false;
	}
}
