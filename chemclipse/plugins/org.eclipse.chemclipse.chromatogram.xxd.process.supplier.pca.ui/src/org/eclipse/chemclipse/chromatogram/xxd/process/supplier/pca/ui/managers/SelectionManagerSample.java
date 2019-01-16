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
package org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.ui.managers;

import org.eclipse.chemclipse.model.statistics.ISample;
import org.eclipse.chemclipse.ux.fx.ui.SelectionManagerProto;

public class SelectionManagerSample extends SelectionManagerProto<ISample> {

	private static SelectionManagerSample instance;

	public static SelectionManagerSample getInstance() {

		synchronized(SelectionManagerSample.class) {
			if(instance == null) {
				instance = new SelectionManagerSample();
			}
		}
		return instance;
	}

	public SelectionManagerSample() {
		super();
	}
}
