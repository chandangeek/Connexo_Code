/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractTrimaranObject.java
 *
 * Created on 16 februari 2007, 15:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.ReadRequest;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractTrimaranObject {

    private TrimaranObjectFactory trimaranObjectFactory;

    abstract protected byte[] prepareBuild() throws IOException;
    abstract protected void parse(byte[] data) throws IOException;
    abstract protected int getVariableName();


    /** Creates a new instance of AbstractTrimaranObject */
    public AbstractTrimaranObject(TrimaranObjectFactory trimaranObjectFactory) {
        this.trimaranObjectFactory=trimaranObjectFactory;
    }


    public void write() throws IOException {
        getTrimaranObjectFactory().getTrimaran().getDLMSPDUFactory().getWriteRequest(getVariableName(), prepareBuild());
    }

    public void read() throws IOException {
        ReadRequest rr = getTrimaranObjectFactory().getTrimaran().getDLMSPDUFactory().getReadRequest(getVariableName());
        parse(rr.getReadResponse().getReadResponseData());
    }


    public TrimaranObjectFactory getTrimaranObjectFactory() {
        return trimaranObjectFactory;
    }

    public void setTrimaranObjectFactory(TrimaranObjectFactory trimaranObjectFactory) {
        this.trimaranObjectFactory = trimaranObjectFactory;
    }

}
