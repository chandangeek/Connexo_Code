/*
 * AbstractBasePageFactory.java
 *
 * Created on 22 september 2006, 13:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol;

import com.energyict.protocolimpl.base.AbstractProtocol;

import java.util.logging.Logger;

/**
 *
 * @author Koen
 */
abstract public class AbstractBasePageFactory {

    private Logger logger;

    abstract public ProtocolLink getProtocolLink();
    
    private int memStartAddress=0;
    
    /** Creates a new instance of AbstractBasePageFactory */
    public AbstractBasePageFactory() {
    }

    public int getMemStartAddress() {
        return memStartAddress;
    }

    public void setMemStartAddress(int memStartAddress) {
        this.memStartAddress = memStartAddress;
    }

    public Logger getLogger() {
        return ((AbstractProtocol)getProtocolLink()).getLogger();
    }
}
