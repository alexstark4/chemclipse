/*******************************************************************************
 * Copyright (c) 2020 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alexander Stark - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.xxd.model.filter.peaks;

import java.util.Collection;
import java.util.function.BiPredicate;

import org.eclipse.chemclipse.model.core.IPeak;
import org.eclipse.chemclipse.model.core.IPeakModel;
import org.eclipse.chemclipse.model.filter.IPeakFilter;
import org.eclipse.chemclipse.processing.Processor;
import org.eclipse.chemclipse.processing.core.MessageConsumer;
import org.eclipse.chemclipse.processing.filter.CRUDListener;
import org.eclipse.chemclipse.processing.filter.Filter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.service.component.annotations.Component;

@Component(service = { IPeakFilter.class, Filter.class, Processor.class })
public class AsymmetryFilter implements IPeakFilter<AsymmetryFilterSettings> {

	private static BiPredicate<Double, Double> ASYMMETRY_FACTOR_SMALLER_THAN_LIMIT_COMPARATOR = (factor, factorSetting) -> (factor < factorSetting);
	private static BiPredicate<Double, Double> ASYMMETRY_FACTOR_GREATER_THAN_LIMIT_COMPARATOR = (factor, factorSetting) -> (factor > factorSetting);
	
	private static class FactorPredicate<T> {

		private final BiPredicate<Double, T> predicate;
		private final T factorSetting;

		public FactorPredicate(BiPredicate<Double, T> predicate, T factorSetting) {

			super();
			this.predicate = predicate;
			this.factorSetting = factorSetting;
		}

		public FactorPredicate<?> negate() {

			return new FactorPredicate<T>(predicate.negate(), factorSetting);
		}

		public boolean test(double factor) {

			boolean result = predicate.test(factor, factorSetting);
			return result;
		}
	}
	
	@Override
	public String getName() {

		return "Asymmetry Filter";
	}

	@Override
	public String getDescription() {

		return "Filter peaks by peak asymmetry factor (As)";
	}

	@Override
	public Class<AsymmetryFilterSettings> getConfigClass() {

		return AsymmetryFilterSettings.class;
	}

	@Override
	public boolean acceptsIPeaks(Collection<? extends IPeak> items) {

		return true;
	}

	@Override
	public <X extends IPeak> void filterIPeaks(CRUDListener<X, IPeakModel> listener, AsymmetryFilterSettings configuration, MessageConsumer messageConsumer, IProgressMonitor monitor) throws IllegalArgumentException {

		Collection<X> read = listener.read();
		if(configuration == null) {
			configuration = createConfiguration(read);
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, read.size());

		for(X peak : read) {
			processPeak(configuration, listener, peak, getPredicate(configuration));
			subMonitor.worked(1);
		}
	}

	private static FactorPredicate<?> getPredicate(AsymmetryFilterSettings configuration) {

		switch(configuration.getFilterSelectionCriterion()) {
		case ASYMMETRY_FACTOR_SMALLER_THAN_LIMIT:
			return new FactorPredicate<>(ASYMMETRY_FACTOR_SMALLER_THAN_LIMIT_COMPARATOR, configuration.getPeakAsymmetryFactor());
		case ASYMMETRY_FACTOR_GREATER_THAN_LIMIT:
			return new FactorPredicate<>(ASYMMETRY_FACTOR_GREATER_THAN_LIMIT_COMPARATOR, configuration.getPeakAsymmetryFactor());
		default:
			throw new IllegalArgumentException("Unsupported Peak Filter Selection Criterion!");
		}
	}

	private static <X extends IPeak> void processPeak(AsymmetryFilterSettings configuration, CRUDListener<X, IPeakModel> listener, X peak, FactorPredicate<?> predicate) {

		double  peakAsymmetryFactor = peak.getPeakModel().getTailing()/peak.getPeakModel().getLeading();
		switch(configuration.getFilterTreatmentOption()) {
		case ENABLE_PEAK:
			if(predicate.test(peakAsymmetryFactor)) {
				peak.setActiveForAnalysis(true);
				listener.updated(peak);
			}
			break;
		case DEACTIVATE_PEAK:
			if(predicate.test(peakAsymmetryFactor)) {
				peak.setActiveForAnalysis(false);
				listener.updated(peak);
			}
			break;
		case KEEP_PEAK:
			if(predicate.negate().test(peakAsymmetryFactor))
				listener.delete(peak);
			break;
		case DELETE_PEAK:
			if(predicate.test(peakAsymmetryFactor))
				listener.delete(peak);
			break;
		default:
			throw new IllegalArgumentException("Unsupported Peak Filter Treatment Option!");
		}
	}
}