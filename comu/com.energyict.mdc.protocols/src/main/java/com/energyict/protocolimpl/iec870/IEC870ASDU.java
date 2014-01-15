/*
 * IEC870ASDU.java
 *
 * Created on 18 juni 2003, 14:02
 */

package com.energyict.protocolimpl.iec870;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.protocols.util.Calculate;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class IEC870ASDU {

    private static final int DUI_LENGTH=6;

    int typeIdentification;
    int varStructQualifier;
    int causeOfTransmission;
    int originatorAddress;
    int commonAddressOfASDU;
    List informationObjects=null;
    byte[] data=null;
    int length=-1;


    /** Creates a new instance of IEC870ASDU */
    public IEC870ASDU(int typeIdentification,int varStructQualifier,int causeOfTransmission,int originatorAddress,int commonAddressOfASDU,List informationObjects) {
        this.typeIdentification=typeIdentification; //1
        this.varStructQualifier=varStructQualifier; //1
        this.causeOfTransmission=causeOfTransmission; //1
        this.originatorAddress=originatorAddress; //1
        this.commonAddressOfASDU=commonAddressOfASDU; //2
        this.informationObjects=informationObjects;
    }

    /** Creates a new instance of IEC870ASDU */
    public IEC870ASDU(int typeIdentification,int varStructQualifier,int causeOfTransmission,int originatorAddress,int commonAddressOfASDU,IEC870InformationObject io) {
        this(typeIdentification,varStructQualifier,causeOfTransmission,originatorAddress,commonAddressOfASDU,new ArrayList());
        informationObjects.add(io);
    }

    public IEC870ASDU(byte[] data) throws IEC870ConnectionException {
        typeIdentification = ProtocolUtils.getByte2Int(data, 0);
        varStructQualifier = ProtocolUtils.getByte2Int(data, 1);
        causeOfTransmission = ProtocolUtils.getByte2Int(data, 2);
        originatorAddress = ProtocolUtils.getByte2Int(data, 3);
        commonAddressOfASDU = ProtocolUtils.getShortLE(data, 4);
        buildInformationObjects(ProtocolUtils.getSubArray(data,6));
    }

    public int setOriginatorAddress(int originatorAddress) {
        this.originatorAddress=originatorAddress+1;
        return this.originatorAddress;
    }
    public byte[] getInformationObjectObjectData(int index) throws IOException {
        // KV_DEBUG KV 16072003 to avoid indexoutofboundsexception... when receiving DSAP messages ??
        if (index >= getInformationObjects().size()) {
            throw new IOException("IEC870ASDU, getInformationObjectObjectData, wrong nr of informationobjects in ASDU, size=" + getInformationObjects().size() + " index=" + index);
        }
        return ((IEC870InformationObject)getInformationObjects().get(index)).getObjData();
    }

    public List getInformationObjects() {
        return informationObjects;
    }

    public IEC870InformationObject getInformationObject() {
        return (IEC870InformationObject)informationObjects.get(0);
    }

    public byte[] getData() throws IEC870ConnectionException {
        buildData();
        return data;
    }
    public int getTypeIdentification() {
        return typeIdentification;
    }
    public String getTypeIdentificationDescription() {
        return IEC870TypeIdentification.getTypeIdentification(getTypeIdentification()).getDescription();
    }

    public int getVarStructQualifierNumber() {
        return varStructQualifier & 0x7F;
    }
    public boolean isVarStructQualifierSequence() {
        return ((varStructQualifier & 0x80) != 0);
    }
    public int getCauseOfTransmissionCause() {
        return  (causeOfTransmission&0x3F);
    }
    public String getCauseOfTransmissionCauseDescription() {
        return  IEC870TransmissionCause.getTransmissionCause(getCauseOfTransmissionCause()).getDescription();
    }

    public boolean isCauseOfTransmissionConfirm() {
        return  ((causeOfTransmission&0x40)!=0);
    }
    public boolean isCauseOfTransmissionTest() {
        return  ((causeOfTransmission&0x80)!=0);
    }

    public String toString(TimeZone timeZone) {
        StringBuilder strbuff = new StringBuilder();
        String tidName,causeName;

        try {
            tidName = IEC870TypeIdentification.getTypeIdentification(typeIdentification).getDescription()+" "+IEC870TypeIdentification.getTypeIdentification(typeIdentification).getShortdescr();
        }
        catch(NotFoundException e) {
            tidName = e.getMessage();
        }
        try {
            causeName = IEC870TransmissionCause.getTransmissionCause(getCauseOfTransmissionCause()).getDescription();
        }
        catch(NotFoundException e) {
            causeName = e.getMessage();
        }
        strbuff.append("********************** ASDU ************************\r\n");

        strbuff.append("typeIdentification=0x")
                .append(Integer.toHexString(typeIdentification))
                .append(" (")
                .append(tidName)
                .append(")\r\n")
                .append("causeOfTransmission=0x")
                .append(Integer.toHexString(causeOfTransmission))
                .append(" (")
                .append(causeName)
                .append(")\r\n")
                .append("varStructQualifier=0x")
                .append(Integer.toHexString(varStructQualifier))
                .append("\r\n")
                .append("getVarStructQualifierNumber()=0x")
                .append(Integer.toHexString(getVarStructQualifierNumber()))
                .append("\r\n")
                .append("originatorAddress=0x")
                .append(Integer.toHexString(originatorAddress))
                .append("\r\n")
                .append("commonAddressOfASDU=0x")
                .append(Integer.toHexString(commonAddressOfASDU))
                .append("\r\n");

        if (informationObjects != null) {
            for (int i=0;i<informationObjects.size();i++) {
                strbuff.append("************** Information object ").append(i).append(" ****************\r\n");
                IEC870InformationObject io = (IEC870InformationObject)informationObjects.get(i);
                strbuff.append(io.toString());
                if ((typeIdentification == 1) || (typeIdentification == 2)) { // M_SP_NA_1 || M_SP_TA_1
                    try {
                        strbuff.append("flags(7.2.6.1)=0x").append(Integer.toHexString(ProtocolUtils.getIntLE(io.getObjData(), 0, 1))).append("\r\n");
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if ((typeIdentification == 15) || (typeIdentification == 16)) { // M_IT_NA_1 || M_IT_TA_1
                    try {
                        strbuff.append("val=").append(ProtocolUtils.getIntLE(io.getObjData())).append(" flags(7.2.6.9)=0x");
                        strbuff.append(Integer.toHexString(ProtocolUtils.getIntLE(io.getObjData(), 4, 1))).append("\r\n");
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (typeIdentification == 16) { // M_IT_TA_1
                    try {
                        CP24Time2a cp24 = new CP24Time2a(timeZone,io.getObjData(),5);
                        strbuff.append("CP24Time2a=").append(cp24.toString()).append(" IV=").append(cp24.isInValid()).append("\r\n");
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
                if (typeIdentification == 2) { // M_SP_TA_1
                    try {
                        CP24Time2a cp24 = new CP24Time2a(timeZone,io.getObjData(),1);
                        strbuff.append("CP24Time2a=").append(cp24.toString()).append(" IV=").append(cp24.isInValid()).append("\r\n");
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
                if (typeIdentification == 142) { // C_IH_NA_P
                    try {
                        CP56Time2a cp56 = new CP56Time2a(timeZone,io.getObjData(),0);
                        strbuff.append("CP56Time2a=").append(cp56.toString()).append(" QOI(7.2.6.22)=0x");
                        strbuff.append(Integer.toHexString(ProtocolUtils.getIntLE(io.getObjData(), 7, 1))).append(" IV=").append(cp56.isInValid()).append("\r\n");
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
                if ((typeIdentification == 9) || (typeIdentification == 10)){ // M_ME_NA_1 || M_ME_TA_1
                    try {
                        strbuff.append("QDS(7.2.6.3)=0x").append(Integer.toHexString(ProtocolUtils.getIntLE(io.getObjData(), 2, 1))).append(" val=");
                        strbuff.append(Calculate.convertNormSignedFP2NumberLE(io.getObjData(), 0))
                                .append(" *** ")
                                .append((ProtocolUtils.getIntLE(io.getObjData(), 0, 2) & 0x7FF0) >> 4)
                                .append(" ***\r\n");
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
                if (typeIdentification == 10) { // M_ME_TA_1
                    try {
                        CP24Time2a cp24 = new CP24Time2a(timeZone,io.getObjData(),3);
                        strbuff.append("CP24Time2a=").append(cp24.toString()).append(" IV=").append(cp24.isInValid()).append("\r\n");
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return strbuff.toString();
    }

    private void buildInformationObjects(byte[] data) throws IEC870ConnectionException {
        informationObjects = null;
        IEC870InformationObject io;
        if (getVarStructQualifierNumber() != 0) {
            informationObjects = new ArrayList();
            if (isVarStructQualifierSequence()) {
                int ioLength = (data.length-2) / getVarStructQualifierNumber();
                io = new IEC870InformationObject(ProtocolUtils.getShortLE(data,0));
                io.addData(ProtocolUtils.getSubArray(data,2,2));
                informationObjects.add(io);
                for (int i=1;i<getVarStructQualifierNumber();i++) {
                    io = new IEC870InformationObject(-1);
                    io.addData(ProtocolUtils.getSubArray(data,2+ioLength*i,2+(ioLength+ioLength*i)-1));
                    informationObjects.add(io);
                }
            }
            else {
                int ioLength = data.length / getVarStructQualifierNumber();
                for (int i=0;i<getVarStructQualifierNumber();i++) {
                    io = new IEC870InformationObject(ProtocolUtils.getShortLE(data,i*ioLength));
                    io.addData(ProtocolUtils.getSubArray(data,(ioLength*i+2),ioLength*(i+1)-1));
                    informationObjects.add(io);
                }
            }
        }
    }

    private void buildData() throws IEC870ConnectionException {

        Iterator it = informationObjects.iterator();
        length=0;
        while(it.hasNext()) {
            IEC870InformationObject iobj = (IEC870InformationObject)it.next();
            length += iobj.getData().length;
        }
        data = new byte[DUI_LENGTH+length];
        data[0] = (byte)typeIdentification;
        data[1] = (byte)varStructQualifier;
        data[2] = (byte)causeOfTransmission;
        data[3] = (byte)originatorAddress;
        data[4] = (byte)(commonAddressOfASDU&0xFF);
        data[5] = (byte)((commonAddressOfASDU>>8)&0xFF);

        int offset=DUI_LENGTH;
        it = informationObjects.iterator();
        while(it.hasNext()) {
            IEC870InformationObject iobj = (IEC870InformationObject)it.next();
            try {
                offset = ProtocolUtils.arrayCopy(iobj.getData(),data, offset);
            }
            catch(IOException e) {
                throw new IEC870ConnectionException("IEC870ASDU, buildData, "+e.getMessage());
            }
        }
    }

}