/*
 * Alpha.java
 *
 * Created on 27 september 2005, 13:20
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core;


import java.util.*;
import java.io.*;
import java.util.logging.*;
import com.energyict.protocolimpl.elster.alpha.core.connection.*;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.elster.alpha.core.connection.AlphaConnection;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;
import com.energyict.protocolimpl.elster.alpha.core.classes.BillingDataRegisterFactory;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.ClassFactory;

/**
 *
 * @author koen
 */
public interface Alpha {
    public TimeZone getTimeZone();
    public AlphaConnection getAlphaConnection();
    public CommandFactory getCommandFactory();
    //public ClassFactory getClassFactory();
    public BillingDataRegisterFactory getBillingDataRegisterFactory() throws IOException;
    public int getNumberOfChannels() throws UnsupportedException, IOException;
    public ProtocolChannelMap getProtocolChannelMap();
    public int getProfileInterval() throws UnsupportedException, IOException;
    public Logger getLogger();
    public int getTotalRegisterRate();
}
