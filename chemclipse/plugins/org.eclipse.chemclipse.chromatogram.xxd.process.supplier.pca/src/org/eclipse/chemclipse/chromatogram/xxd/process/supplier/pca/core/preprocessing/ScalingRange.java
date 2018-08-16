/*******************************************************************************
 * Copyright (c) 2017, 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jan Holy - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.core.preprocessing;

import java.util.List;

import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.ISample;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.ISampleData;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.ISamples;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.IVariable;

public class ScalingRange extends AbstaractScaling {

	public ScalingRange(int centeringType) {
		super(centeringType);
	}

	@Override
	public String getDescription() {

		return "";
	}

	private <S extends ISample<? extends ISampleData>> double getMax(List<S> samples, int index) {

		boolean onlySelected = isOnlySelected();
		return samples.stream().filter(s -> s.isSelected() || !onlySelected).map(s -> s.getSampleData().get(index)).mapToDouble(s -> getData(s)).summaryStatistics().getMax();
	}

	private <S extends ISample<? extends ISampleData>> double getMin(List<S> samples, int index) {

		boolean onlySelected = isOnlySelected();
		return samples.stream().filter(s -> s.isSelected() || !onlySelected).map(s -> s.getSampleData().get(index)).mapToDouble(s -> getData(s)).summaryStatistics().getMin();
	}

	@Override
	public String getName() {

		return "Range scaling";
	}

	@Override
	public <V extends IVariable, S extends ISample<? extends ISampleData>> void process(ISamples<V, S> samples) {

		boolean onlySelected = isOnlySelected();
		int centeringType = getCenteringType();
		List<V> variables = samples.getVariables();
		List<S> samplesList = samples.getSampleList();
		for(int i = 0; i < variables.size(); i++) {
			final double mean = getCenteringValue(samplesList, i, centeringType);
			final double max = getMax(samplesList, i);
			final double min = getMin(samplesList, i);
			for(ISample<?> sample : samplesList) {
				ISampleData sampleData = sample.getSampleData().get(i);
				if((sample.isSelected() || !onlySelected)) {
					double data = getData(sampleData);
					double scaleData = (data - mean) / (max - min);
					sampleData.setModifiedData(scaleData);
				}
			}
		}
	}
}
