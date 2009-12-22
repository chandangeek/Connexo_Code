/*
 * AbstractAPSEPDU.java
 *
 * Created on 13 februari 2007, 17:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractAPSEPDU {
    
    abstract byte[] preparebuild() throws IOException;
    abstract void parse(byte[] data) throws IOException;
    
    private APSEPDUFactory aPSEFactory;
    
    /** Creates a new instance of AbstractAPSEPDU */
    public AbstractAPSEPDU(APSEPDUFactory aPSEFactory) {
        this.aPSEFactory=aPSEFactory;
    }

    public void invoke() throws IOException {
        byte[] data = getAPSEFactory().getProtocolLink().getConnection62056().getLayerManager().send(preparebuild());
        if (data != null){
            parse(data);
        }
    }
    
    
    public APSEPDUFactory getAPSEFactory() {
        return aPSEFactory;
    }

    public void setAPSEFactory(APSEPDUFactory aPSEFactory) {
        this.aPSEFactory = aPSEFactory;
    }
    
    
}
