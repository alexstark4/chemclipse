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
package org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.core.filters.IFilter;
import org.eclipse.chemclipse.model.statistics.ISample;
import org.eclipse.chemclipse.model.statistics.ISamples;
import org.eclipse.chemclipse.model.statistics.IVariable;
import org.eclipse.core.runtime.IProgressMonitor;

public class PcaFiltrationData implements IDataModification {

	private List<IFilter> filters;
	private boolean onlySelected = true;
	private boolean resetSelectedRetentionTimes = true;

	public PcaFiltrationData() {

		filters = new ArrayList<>();
	}

	@Override
	public boolean availableModification() {

		return !filters.isEmpty();
	}

	public List<IFilter> getFilters() {

		return filters;
	}

	@Override
	public boolean isOnlySelected() {

		return onlySelected;
	}

	public boolean isResetSelectedRetentionTimes() {

		return resetSelectedRetentionTimes;
	}

	@Override
	public <V extends IVariable, S extends ISample> void process(ISamples<V, S> samples, IProgressMonitor monitor) {

		List<V> variables = samples.getVariables();
		if(resetSelectedRetentionTimes) {
			setSelectAllRow(samples, true);
		}
		if(filters != null && !filters.isEmpty()) {
			for(int i = 0; i < filters.size(); i++) {
				filters.get(i).setOnlySelected(onlySelected);
				List<Boolean> result = filters.get(i).filter(samples);
				for(int j = 0; j < result.size(); j++) {
					variables.get(j).setSelected(variables.get(j).isSelected() && result.get(j));
				}
			}
		}
	}

	@Override
	public void setOnlySelected(boolean onlySelected) {

		this.onlySelected = onlySelected;
	}

	public void setResetSelectedRetentionTimes(boolean resetSelectedRetentionTimes) {

		this.resetSelectedRetentionTimes = resetSelectedRetentionTimes;
	}

	private <V extends IVariable, S extends ISample> void setSelectAllRow(ISamples<V, S> samples, boolean selection) {

		List<V> variables = samples.getVariables();
		for(int i = 0; i < variables.size(); i++) {
			variables.get(i).setSelected(selection);
		}
	}
}
