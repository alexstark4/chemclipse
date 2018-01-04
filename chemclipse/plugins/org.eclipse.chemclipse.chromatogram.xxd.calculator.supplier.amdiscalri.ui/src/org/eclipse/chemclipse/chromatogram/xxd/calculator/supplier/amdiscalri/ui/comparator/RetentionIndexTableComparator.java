/*******************************************************************************
 * Copyright (c) 2016, 2018 Lablicate GmbH.
 * 
 * All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.calculator.supplier.amdiscalri.ui.comparator;

import org.eclipse.chemclipse.chromatogram.xxd.calculator.supplier.amdiscalri.model.IRetentionIndexEntry;
import org.eclipse.chemclipse.support.ui.swt.AbstractRecordTableComparator;
import org.eclipse.chemclipse.support.ui.swt.IRecordTableComparator;
import org.eclipse.jface.viewers.Viewer;

public class RetentionIndexTableComparator extends AbstractRecordTableComparator implements IRecordTableComparator {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {

		int sortOrder = 0;
		if(e1 instanceof IRetentionIndexEntry && e2 instanceof IRetentionIndexEntry) {
			IRetentionIndexEntry retentionIndexEntry1 = (IRetentionIndexEntry)e1;
			IRetentionIndexEntry retentionIndexEntry2 = (IRetentionIndexEntry)e2;
			switch(getPropertyIndex()) {
				case 0:
					sortOrder = Integer.compare(retentionIndexEntry2.getRetentionTime(), retentionIndexEntry1.getRetentionTime());
					break;
				case 1:
					sortOrder = Float.compare(retentionIndexEntry2.getRetentionIndex(), retentionIndexEntry1.getRetentionIndex());
					break;
				case 2:
					sortOrder = retentionIndexEntry2.getName().compareTo(retentionIndexEntry1.getName());
					break;
				default:
					sortOrder = 0;
			}
		}
		if(getDirection() == ASCENDING) {
			sortOrder = -sortOrder;
		}
		return sortOrder;
	}
}
