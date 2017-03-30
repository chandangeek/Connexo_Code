/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * StatusRequest.java
 *
 * Created on 16 februari 2007, 15:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu;

import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.ConfirmedReqAPSE;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.Encryptor6205651;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
public class StatusRequest extends ConfirmedReqAPSE {

    private boolean identify;

    /** Creates a new instance of StatusRequest */
    public StatusRequest(DLMSPDUFactory dLMSPDUFactory) {
        super(dLMSPDUFactory.getProtocolLink().getAPSEFactory());
    }


    final int DLMSPDU_STATUS_REQUEST=2;

    protected byte[] preparebuildPDU() throws IOException {

        setConfirmedRespAPSE(new StatusResponse(getAPSEFactory().getProtocolLink().getDLMSPDUFactory()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(DLMSPDU_STATUS_REQUEST);
        //see IEC 1334-4-41 page 209 GetStatusRequest
        baos.write(isIdentify()?0xFF:0x00); // identify

        // encrypt the data
        Encryptor6205651 e = new Encryptor6205651();
        byte[] encryptedData = e.getEncryptedData(baos.toByteArray(),getAPSEFactory().getAPSEParameters().getEncryptionMask());
        return encryptedData; //baos.toByteArray();
    }

    public StatusResponse getStatusResponse() {
        return (StatusResponse)getConfirmedRespAPSE();
    }

    public String toString() {
        return getStatusResponse().toString();
    }

    protected void parsePDU(byte[] data) throws IOException {
    }

    public boolean isIdentify() {
        return identify;
    }

    public void setIdentify(boolean identify) {
        this.identify = identify;
    }



}
