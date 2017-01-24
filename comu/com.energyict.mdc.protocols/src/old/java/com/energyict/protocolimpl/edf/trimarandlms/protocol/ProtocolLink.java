/*
 * ProtocolLink.java
 *
 * Created on 15 februari 2007, 13:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.APSEPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.DLMSPDUFactory;

import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author Koen
 */
public interface ProtocolLink {

	Connection62056 getConnection62056();

	TimeZone getTimeZone();

	APSEPDUFactory getAPSEFactory();

	DLMSPDUFactory getDLMSPDUFactory();

	Logger getLogger();

}
