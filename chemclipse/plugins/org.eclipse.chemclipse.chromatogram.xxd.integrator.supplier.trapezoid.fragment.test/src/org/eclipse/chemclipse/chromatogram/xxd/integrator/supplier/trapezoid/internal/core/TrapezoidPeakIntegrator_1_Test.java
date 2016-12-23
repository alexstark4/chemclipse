/*******************************************************************************
 * Copyright (c) 2008, 2016 Dr. Philip Wenig.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.integrator.supplier.trapezoid.internal.core;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.chemclipse.msd.model.core.IPeakMSD;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.core.settings.peaks.IPeakIntegrationSettings;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.exceptions.ValueMustNotBeNullException;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.supplier.trapezoid.internal.core.IPeakIntegrator;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.supplier.trapezoid.internal.core.PeakIntegrator;
import org.eclipse.chemclipse.chromatogram.xxd.integrator.supplier.trapezoid.settings.TrapezoidPeakIntegrationSettings;

import junit.framework.TestCase;

public class TrapezoidPeakIntegrator_1_Test extends TestCase {

	private IPeakIntegrator integrator;
	private IPeakIntegrationSettings integrationSettings;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		integrator = new PeakIntegrator();
		integrationSettings = new TrapezoidPeakIntegrationSettings();
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public void testIntegrate_1() {

		IPeakMSD peak = null;
		try {
			integrator.integrate(peak, integrationSettings, new NullProgressMonitor());
		} catch(ValueMustNotBeNullException e) {
			assertTrue("ValueMustNotBeNullException", true);
		}
	}

	public void testIntegrate_2() {

		List<IPeakMSD> peaks = null;
		try {
			integrator.integrate(peaks, integrationSettings, new NullProgressMonitor());
		} catch(ValueMustNotBeNullException e) {
			assertTrue("ValueMustNotBeNullException", true);
		}
	}
}
