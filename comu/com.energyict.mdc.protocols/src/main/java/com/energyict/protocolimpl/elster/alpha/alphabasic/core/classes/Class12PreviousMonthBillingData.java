/*
 * Class12PreviousBillingData.java
 *
 * Created on 13 juli 2005, 15:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;



/**
 *
 * @author Koen
 */
public class Class12PreviousMonthBillingData extends ClassBillingData {

	private ClassIdentification classIdentification = new ClassIdentification(12,496,false);

	/** Creates a new instance of Class11BillingData */
	public Class12PreviousMonthBillingData(ClassFactory classFactory) {
		super(classFactory);
	}

	@Override
	protected ClassIdentification getClassIdentification() {
		return this.classIdentification;
	}
}