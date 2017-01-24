/*
 * WriteResponse.java
 *
 * Created on 16 februari 2007, 15:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.ConfirmedRespAPSE;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.StatusIdentify;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class StatusResponse extends ConfirmedRespAPSE {

    final int DEBUG=0;

    private int vDEType;
    private String SerialNumber;
    private int status;
    private int[] vAAList;
    private StatusIdentify[] statusIdentifies;

    /** Creates a new instance of WriteResponse */
    public StatusResponse(DLMSPDUFactory dLMSPDUFactory) {
        super(dLMSPDUFactory.getProtocolLink().getAPSEFactory());
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("StatusResponse:\n");
        for (int i=0;i<getVAAList().length;i++) {
            strBuff.append("       VAAList["+i+"]="+getVAAList()[i]+"\n");
        }
        strBuff.append("   VDEType="+getVDEType()+"\n");
        strBuff.append("   serialNumber="+getSerialNumber()+"\n");
        strBuff.append("   status="+getStatus()+"\n");
        for (int i=0;i<getStatusIdentifies().length;i++) {
            strBuff.append("       statusIdentifies["+i+"]="+getStatusIdentifies()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected byte[] preparebuildPDU() throws IOException {
        return null;
    }

    final int DLMSPDU_STATUS_RESPONSE=0x09;

    protected void parsePDU(byte[] data) throws IOException {

//    	System.out.println("GN_DEBUG> write to file");
//    	File file = new File("c://TEST_FILES/089807000857StatusResponce.bin");
//    	FileOutputStream fos = new FileOutputStream(file);
//    	fos.write(data);
//    	fos.close();


        int offset=0;
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> "+ProtocolUtils.outputHexString(data));
		}

        int length = ProtocolUtils.getInt(data,offset++,1);
        if ((length & 0x80) == 0x80){
            offset++;
        }

        int tag = ProtocolUtils.getInt(data,offset++,1);
        if (tag != DLMSPDU_STATUS_RESPONSE) {
			throw new IOException("StatusResponse, parse, invalid tag 0x"+Integer.toHexString(tag)+" received");
		}

        setVDEType(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        length = ProtocolUtils.getInt(data,offset++, 1);
        setSerialNumber(new String(ProtocolUtils.getSubArray2(data, offset, length)));
        offset+=length;
        setStatus(ProtocolUtils.getInt(data,offset++, 1));
        setVAAList(new int[ProtocolUtils.getInt(data,offset++, 1)]);
        for (int i=0;i<getVAAList().length;i++) {
            getVAAList()[i] = ProtocolUtils.getInt(data,offset, 2);
            offset+=2;
        }
        setStatusIdentifies(new StatusIdentify[ProtocolUtils.getInt(data,offset++, 1)]);
        for (int i=0;i<getStatusIdentifies().length;i++) {
            getStatusIdentifies()[i] = new StatusIdentify(data,offset);
            offset += getStatusIdentifies()[i].getSize();
        }
    }

    public int getVDEType() {
        return vDEType;
    }

    public void setVDEType(int vDEType) {
        this.vDEType = vDEType;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String SerialNumber) {
        this.SerialNumber = SerialNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int[] getVAAList() {
        return vAAList;
    }

    public void setVAAList(int[] vAAList) {
        this.vAAList = vAAList;
    }

    public StatusIdentify[] getStatusIdentifies() {
        return statusIdentifies;
    }

    public void setStatusIdentifies(StatusIdentify[] statusIdentifies) {
        this.statusIdentifies = statusIdentifies;
    }

}
