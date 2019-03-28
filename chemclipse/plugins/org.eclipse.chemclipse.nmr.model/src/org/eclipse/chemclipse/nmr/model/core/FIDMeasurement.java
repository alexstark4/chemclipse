/*******************************************************************************
 * Copyright (c) 2019 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.nmr.model.core;

import java.util.Collection;

import org.eclipse.chemclipse.model.core.IComplexSignalMeasurement;

public interface FIDMeasurement extends IComplexSignalMeasurement<FIDSignal> {

	enum DataDimension {
		ONE_DIMENSIONAL("1D"), TWO_DIMENSIONAL("2D"), THREE_DIMENSIONAL("3D"), FOUR_DIMENSIONAL("4D");

		private String name;

		DataDimension(String name) {
			this.name = name;
		}

		@Override
		public String toString() {

			return name;
		}
	}

	DataDimension getDataDimension();

	double getSweepWidth();

	double getIrradiationCarrierFrequency();

	double getAcquisitionTime();

	double getFirstDataPointOffset();

	/**
	 * 
	 * @return the signals that makes up this {@link FIDMeasurement}
	 */
	@Override
	Collection<? extends FIDSignal> getSignals();
}
