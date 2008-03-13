/*
 * ProtocolLink.java
 *
 * Created on 15 februari 2007, 13:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.protocol;

import com.energyict.protocolimpl.edf.trimaranplus.*;
import com.energyict.protocolimpl.edf.trimaranplus.dlmscore.*;
import com.energyict.protocolimpl.edf.trimaranplus.dlmscore.dlmspdu.*; 
import java.util.*;
import com.energyict.protocolimpl.edf.trimaranplus.dlmscore.APSEPDUFactory;
import com.energyict.protocolimpl.edf.trimaranplus.dlmscore.dlmspdu.DLMSPDUFactory;
import java.util.logging.*;

/**
 *
 * @author Koen
 */
public interface ProtocolLink {
    public Connection62056 getConnection62056();
    public TimeZone getTimeZone();
    public APSEPDUFactory getAPSEFactory();
    public DLMSPDUFactory getDLMSPDUFactory();
    public Logger getLogger();
}
