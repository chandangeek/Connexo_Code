/*
 * BillingDataRegisterFactory.java
 *
 * Created on 30 september 2005, 11:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.classes;

import java.io.IOException;
import java.util.List;

/**
 * 
 * @author koen
 */
public interface BillingDataRegisterFactory {

	void buildAll() throws IOException;

	List getBillingDataRegisters(int set) throws IOException;

}
