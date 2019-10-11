/*******************************************************************************
 * Copyright (c) 2019 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 * Christoph Läubrich - enhance method definition, add readonly support
 *******************************************************************************/
package org.eclipse.chemclipse.processing.methods;

public interface IProcessMethod extends ProcessEntryContainer {

	/**
	 * 
	 * @return returns the methods UUID to identify the method across file-systems
	 */
	String getUUID();

	/**
	 * The name is used to display a label to the user
	 * 
	 * @return the human readable label/name
	 */
	String getName();

	/**
	 * The category is used to group similar methods
	 * 
	 * @return the category
	 */
	String getCategory();

	/**
	 * The operator is the person who has created / currently manages this method
	 * 
	 * @return the operator of the method
	 */
	String getOperator();

	/**
	 * 
	 * @return the human readable description of this method
	 */
	String getDescription();

	/**
	 * a method marked as final is one that is approved or otherwise locked for further modifications and will stay constant over time
	 * 
	 * @return <code>true</code> if this is a final method or <code>false</code> otherwise
	 */
	boolean isFinal();

	/**
	 * Compares that this process methods content equals the other process method, the default implementation compares {@link #getName()}, {@link #getCategory()}, {@link #getDescription()}, {@link #getOperator()} and all contained {@link IProcessEntry}s, {@link #isFinal()}
	 * this method is different to {@link #equals(Object)} that it does compares for user visible properties to be equal in contrast to objects identity and it allows to compare different instance type, this also means that it is not required that
	 * Object1.contentEquals(Object2} == Object2.contentEquals(Object1}
	 * 
	 * @param other
	 * @return
	 */
	default boolean contentEquals(IProcessMethod other) {

		if(other == null) {
			return false;
		}
		if(other == this) {
			return true;
		}
		if(isFinal() != other.isFinal()) {
			return false;
		}
		if(!getName().equals(other.getName())) {
			return false;
		}
		if(!getCategory().equals(other.getCategory())) {
			return false;
		}
		if(!getDescription().equals(other.getDescription())) {
			return false;
		}
		if(!getOperator().equals(other.getOperator())) {
			return false;
		}
		if(getNumberOfEntries() != other.getNumberOfEntries()) {
			return false;
		}
		return ProcessEntryContainer.super.contentEquals(other);
	}
}