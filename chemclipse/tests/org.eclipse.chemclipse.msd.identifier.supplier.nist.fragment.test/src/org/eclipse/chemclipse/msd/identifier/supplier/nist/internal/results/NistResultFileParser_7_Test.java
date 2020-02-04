/*******************************************************************************
 * Copyright (c) 2018, 2020 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.identifier.supplier.nist.internal.results;

import java.io.File;

import org.eclipse.chemclipse.msd.identifier.supplier.nist.TestPathHelper;

import junit.framework.TestCase;

public class NistResultFileParser_7_Test extends TestCase {

	private NistResultFileParser nistResultFileParser;
	private File results;
	private Compounds compounds;
	private Compound compound;
	private Hit hit;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		nistResultFileParser = new NistResultFileParser();
		results = new File(TestPathHelper.getAbsolutePath(TestPathHelper.TESTFILE_NIST_SRCRESLT_5));
		compounds = nistResultFileParser.getCompounds(results);
		compound = compounds.getCompound(2);
		hit = compound.getHit(2);
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public void testGetIdentifier_1() {

		assertEquals("ID-2", compound.getIdentfier());
	}

	public void testGetCompoundInLibraryFactor_1() {

		assertEquals("-117", compound.getCompoundInLibraryFactor());
	}

	public void testGetName_1() {

		assertEquals("á-D-Allopyranose, 5TMS derivative", hit.getName());
	}

	public void testGetFormula_1() {

		assertEquals("C21H52O6Si5", hit.getFormula());
	}

	public void testGetMF_1() {

		assertEquals(92.1f, hit.getMatchFactor());
	}

	public void testGetRMF_1() {

		assertEquals(92.2f, hit.getReverseMatchFactor());
	}

	public void testGetProb_1() {

		assertEquals(8.85f, hit.getProbability());
	}

	public void testGetCAS_1() {

		assertEquals("0-00-0", hit.getCAS());
	}

	public void testGetMw_1() {

		assertEquals(540, hit.getMolecularWeight());
	}

	public void testGetLib_1() {

		assertEquals("mainlib", hit.getLib());
	}

	public void testGetId_1() {

		assertEquals(205524, hit.getId());
	}

	public void testGetRI_1() {

		assertEquals(1829, hit.getRetentionIndex());
	}
}
