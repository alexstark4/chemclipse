/*******************************************************************************
 * Copyright (c) 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.internal.methods;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import org.eclipse.chemclipse.model.methods.ProcessEntry;
import org.eclipse.chemclipse.model.methods.ProcessMethod;
import org.eclipse.chemclipse.model.types.DataType;
import org.eclipse.chemclipse.processing.DataCategory;
import org.eclipse.chemclipse.processing.methods.IProcessMethod;
import org.eclipse.chemclipse.xxd.converter.supplier.chemclipse.internal.support.IFormat;

public class MethodReader_1001 extends AbstractMethodReader {

	public MethodReader_1001() {
		super(IFormat.METHOD_VERSION_0002);
	}

	@Override
	protected IProcessMethod deserialize(DataInputStream dataInputStream, File file) throws IOException {

		ProcessMethod processMethod = new ProcessMethod(ProcessMethod.CHROMATOGRAPHY);
		processMethod.setSourceFile(file);
		processMethod.setUUID(readString(dataInputStream));
		processMethod.setName(readString(dataInputStream));
		processMethod.setOperator(readString(dataInputStream));
		processMethod.setDescription(readString(dataInputStream));
		processMethod.setCategory(readString(dataInputStream));
		int processEntries = dataInputStream.readInt();
		for(int i = 0; i < processEntries; i++) {
			ProcessEntry processEntry = new ProcessEntry(processMethod);
			processEntry.setProcessorId(readString(dataInputStream));
			processEntry.setName(readString(dataInputStream));
			processEntry.setDescription(readString(dataInputStream));
			processEntry.setSettings(readString(dataInputStream));
			//
			int dataTypes = dataInputStream.readInt();
			for(int j = 0; j < dataTypes; j++) {
				String dataTypeString = readString(dataInputStream);
				try {
					DataCategory category = DataType.valueOf(dataTypeString).toDataCategory();
					if(category == DataCategory.AUTO_DETECT) {
						continue;
					}
					processEntry.addDataCategory(category);
				} catch(NullPointerException | IllegalArgumentException e) {
					continue;
				}
			}
			processMethod.getEntries().add(processEntry);
		}
		return processMethod;
	}
}
