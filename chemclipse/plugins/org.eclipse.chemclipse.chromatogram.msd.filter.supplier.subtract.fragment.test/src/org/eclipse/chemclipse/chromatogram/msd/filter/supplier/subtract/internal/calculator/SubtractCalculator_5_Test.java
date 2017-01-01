/*******************************************************************************
 * Copyright (c) 2014, 2017 Lablicate GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.msd.filter.supplier.subtract.internal.calculator;

import java.util.Map;

import org.eclipse.chemclipse.model.exceptions.AbundanceLimitExceededException;
import org.eclipse.chemclipse.msd.model.core.IScanMSD;
import org.eclipse.chemclipse.msd.model.exceptions.IonLimitExceededException;
import org.eclipse.chemclipse.msd.model.implementation.CombinedMassSpectrum;
import org.eclipse.chemclipse.msd.model.implementation.Ion;

import junit.framework.TestCase;

public class SubtractCalculator_5_Test extends TestCase {

	private SubtractCalculator subtractCalculator;
	private IScanMSD subtractMassSpectrum;
	private IScanMSD targetMassSpectrum;
	private Map<Double, Float> subtractMassSpectrumMap;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		//
		boolean useNominalMasses = false;
		boolean useNormalize = false;
		//
		subtractCalculator = new SubtractCalculator();
		//
		subtractMassSpectrum = new CombinedMassSpectrum();
		subtractMassSpectrum.addIon(new Ion(18.2, 200));
		subtractMassSpectrum.addIon(new Ion(28.1, 1000));
		subtractMassSpectrum.addIon(new Ion(32.3, 500));
		//
		targetMassSpectrum = new CombinedMassSpectrum();
		targetMassSpectrum.addIon(new Ion(16.1, 2893.3f));
		targetMassSpectrum.addIon(new Ion(18.1, 8484.3f));
		targetMassSpectrum.addIon(new Ion(20.0, 3894.4f));
		targetMassSpectrum.addIon(new Ion(28.1, 57693.0f));
		targetMassSpectrum.addIon(new Ion(32.3, 3894.6f));
		targetMassSpectrum.addIon(new Ion(43.0, 3793.5f));
		//
		subtractMassSpectrumMap = subtractCalculator.getMassSpectrumMap(subtractMassSpectrum, useNominalMasses, useNormalize);
		subtractCalculator.adjustIntensityValues(targetMassSpectrum, subtractMassSpectrumMap, useNominalMasses, useNormalize);
	}

	@Override
	protected void tearDown() throws Exception {

		subtractCalculator = null;
		super.tearDown();
	}

	public void testMassSpectrumMap_1() {

		assertEquals(3, subtractMassSpectrumMap.size());
	}

	public void testMassSpectrumMap_2() {

		assertEquals(200.0f, subtractMassSpectrumMap.get(18.2));
	}

	public void testMassSpectrumMap_3() {

		assertEquals(1000.0f, subtractMassSpectrumMap.get(28.1));
	}

	public void testMassSpectrumMap_4() {

		assertEquals(500.0f, subtractMassSpectrumMap.get(32.3));
	}

	public void testSubtractedMassSpectrum_1() {

		assertEquals(6, targetMassSpectrum.getNumberOfIons());
	}

	public void testSubtractedMassSpectrum_2() {

		try {
			assertEquals(2893.3f, targetMassSpectrum.getIon(16.1).getAbundance());
		} catch(AbundanceLimitExceededException e) {
			assertTrue(false);
		} catch(IonLimitExceededException e) {
			assertTrue(false);
		}
	}

	public void testSubtractedMassSpectrum_3() {

		try {
			assertEquals(8484.3f, targetMassSpectrum.getIon(18.1).getAbundance());
		} catch(AbundanceLimitExceededException e) {
			assertTrue(false);
		} catch(IonLimitExceededException e) {
			assertTrue(false);
		}
	}

	public void testSubtractedMassSpectrum_4() {

		try {
			assertEquals(3894.4f, targetMassSpectrum.getIon(20.0).getAbundance());
		} catch(AbundanceLimitExceededException e) {
			assertTrue(false);
		} catch(IonLimitExceededException e) {
			assertTrue(false);
		}
	}

	public void testSubtractedMassSpectrum_5() {

		try {
			assertEquals(3394.6f, targetMassSpectrum.getIon(32.3).getAbundance());
		} catch(AbundanceLimitExceededException e) {
			assertTrue(false);
		} catch(IonLimitExceededException e) {
			assertTrue(false);
		}
	}

	public void testSubtractedMassSpectrum_6() {

		try {
			assertEquals(3793.5f, targetMassSpectrum.getIon(43.0).getAbundance());
		} catch(AbundanceLimitExceededException e) {
			assertTrue(false);
		} catch(IonLimitExceededException e) {
			assertTrue(false);
		}
	}

	public void testSubtractedMassSpectrum_7() {

		try {
			assertEquals(56693.0f, targetMassSpectrum.getIon(28.1).getAbundance());
		} catch(AbundanceLimitExceededException e) {
			assertTrue(false);
		} catch(IonLimitExceededException e) {
			assertTrue(false);
		}
	}
}
