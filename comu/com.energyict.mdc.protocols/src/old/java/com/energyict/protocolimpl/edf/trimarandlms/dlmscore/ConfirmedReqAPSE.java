/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ConfirmedReqAPSE.java
 *
 * Created on 16 februari 2007, 14:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class ConfirmedReqAPSE extends AbstractAPSEPDU {

    abstract protected byte[] preparebuildPDU() throws IOException;
    abstract protected void parsePDU(byte[] data) throws IOException;

    private ConfirmedRespAPSE confirmedRespAPSE;

    /** Creates a new instance of ConfirmedReqAPSE */
    public ConfirmedReqAPSE(APSEPDUFactory aPSEPDUFactory) {
        super(aPSEPDUFactory);
    }

    final int CONFIRMED_REQ_APSE = 0;

    byte[] preparebuild() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(CONFIRMED_REQ_APSE);
        baos.write(preparebuildPDU().length);
        baos.write(preparebuildPDU());
        return baos.toByteArray();
    }

    void parse(byte[] data) throws IOException {

        getConfirmedRespAPSE().parse(data);
    }

    public ConfirmedRespAPSE getConfirmedRespAPSE() {
        return confirmedRespAPSE;
    }

    public void setConfirmedRespAPSE(ConfirmedRespAPSE confirmedRespAPSE) {
        this.confirmedRespAPSE = confirmedRespAPSE;
    }
}
