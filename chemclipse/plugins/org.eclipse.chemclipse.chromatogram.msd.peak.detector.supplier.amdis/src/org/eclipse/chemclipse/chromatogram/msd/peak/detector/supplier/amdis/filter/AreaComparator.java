/*******************************************************************************
 * Copyright (c) 2019 Christoph Läubrich.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.msd.peak.detector.supplier.amdis.filter;

import java.util.Comparator;

import org.eclipse.chemclipse.model.core.IPeak;
import org.eclipse.chemclipse.msd.model.core.IPeakMSD;
import org.eclipse.core.runtime.Adapters;

public class AreaComparator<T extends IPeak> implements Comparator<T> {

	@Override
	public int compare(T o1, T o2) {

		IPeakMSD m1 = Adapters.adapt(o1, IPeakMSD.class);
		IPeakMSD m2 = Adapters.adapt(o2, IPeakMSD.class);
		return Double.compare(m1.getIntegratedArea(), m2.getIntegratedArea());
	}
}
