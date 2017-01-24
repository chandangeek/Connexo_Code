/*
 * ReadRequest.java
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
public class ReadRequest extends ConfirmedReqAPSE {

    private int variableName;


    /** Creates a new instance of ReadRequest */
    public ReadRequest(DLMSPDUFactory dLMSPDUFactory) {
        super(dLMSPDUFactory.getProtocolLink().getAPSEFactory());
    }


    final int DLMSPDU_READ_REQUEST=5;

    protected byte[] preparebuildPDU() throws IOException {

        setConfirmedRespAPSE(new ReadResponse(getAPSEFactory().getProtocolLink().getDLMSPDUFactory()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(DLMSPDU_READ_REQUEST);
        //see IEC 1334-4-41 page 221 VariableAccessSpecification
        baos.write(0x01); // nr of elements in the sequence following
        baos.write(0x02); // variable name
        baos.write((byte)(getVariableName()>>8));
        baos.write((byte)(getVariableName()));
        // encrypt the data
        Encryptor6205651 e = new Encryptor6205651();
        byte[] encryptedData = e.getEncryptedData(baos.toByteArray(),getAPSEFactory().getAPSEParameters().getEncryptionMask());
        return encryptedData;
    }

    public ReadResponse getReadResponse() {
        return (ReadResponse)getConfirmedRespAPSE();
    }

    protected void parsePDU(byte[] data) throws IOException {
    }

    public int getVariableName() {
        return variableName;
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }

}
