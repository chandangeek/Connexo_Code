/*
 * WriteRequest.java
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
public class WriteRequest extends ConfirmedReqAPSE {

    private int variableName;

    private byte[] data;

    /** Creates a new instance of WriteRequest */
    public WriteRequest(DLMSPDUFactory dLMSPDUFactory) {
        super(dLMSPDUFactory.getProtocolLink().getAPSEFactory());
    }


    final int DLMSPDU_WRITE_REQUEST=6;

    protected byte[] preparebuildPDU() throws IOException {

        setConfirmedRespAPSE(new WriteResponse(getAPSEFactory().getProtocolLink().getDLMSPDUFactory()));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(DLMSPDU_WRITE_REQUEST);
        //see IEC 1334-4-41 page 221 VariableAccessSpecification
        baos.write(0x01); // nr of elements in the sequence following
        baos.write(0x02); // variable name
        baos.write((byte)(getVariableName()>>8));
        baos.write((byte)(getVariableName()));
        baos.write(getData());
        // encrypt the data
        Encryptor6205651 e = new Encryptor6205651();
        byte[] encryptedData = e.getEncryptedData(baos.toByteArray(),getAPSEFactory().getAPSEParameters().getEncryptionMask());
        return encryptedData;
    }

    public WriteResponse getWriteResponse() {
        return (WriteResponse)getConfirmedRespAPSE();
    }

    protected void parsePDU(byte[] data) throws IOException {
    }

    public int getVariableName() {
        return variableName;
    }

    public void setVariableName(int variableName) {
        this.variableName = variableName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
