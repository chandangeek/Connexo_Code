/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IEC870Frame.java
 *
 * Created on 18 juni 2003, 13:57
 */

package com.energyict.protocolimpl.mbus.core.connection.iec870;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.mbus.core.ApplicationData;

import java.io.IOException;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class IEC870Frame {

    // from primary station
    static public final int CONTROL_SEND_CONFIRM_RESET_REMOTE_LINK = 0;
    static public final int CONTROL_SEND_CONFIRM_RESET_USER_PROCESS = 1;
    static public final int CONTROL_SEND_CONFIRM_BALANCED_TX = 2;
    static public final int CONTROL_SEND_CONFIRM_USER_DATA = 3;
    static public final int CONTROL_SEND_NOREPLY_USER_DATA = 4;
    static public final int CONTROL_REQUEST_ACCESS_DEMAND = 8;
    static public final int CONTROL_REQUEST_RESPOND_STATUS_LINK = 9;
    static public final int CONTROL_REQUEST_RESPOND_CLASS1 = 10;
    static public final int CONTROL_REQUEST_RESPOND_CLASS2 = 11;
    final int[] CLIENT_CONTROL_FCV={0,0,0,1,0,0,0,0,0,0,1,1,0,0,0,0};

    // from secondary station
    static public final int CONTROL_CONFIRM_ACK=0;
    static public final int CONTROL_CONFIRM_NACK=1;
    static public final int CONTROL_RESPOND_USER_DATA=8;
    static public final int CONTROL_RESPOND_NACK=9;
    static public final int CONTROL_RESPOND_STATUS_LINK=11;


    private static final int PRM_PRIMARY=0x40;
    private static final int FCV_PRIMARY=0x10;
    private static final int FCB_PRIMARY=0x20;

    //final int[] CLIENT_CONTROL_FILTER={1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1};
    //final int[] SERVER_CONTROL_FILTER={1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1};
    private static final int[] CLIENT_CONTROL_FILTER={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
    private static final int[] SERVER_CONTROL_FILTER={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

    final String[] FROM_CLIENT = {"SEND/CONFIRM expected, Reset of remote link",
    "SEND/CONFIRM expected, Reset of user process",
    "SEND/CONFIRM expected, Reserved for balanced transmission procedure",
    "SEND/CONFIRM expected, User data",
    "SEND/NO REPLY expected, User data",
    "Reserved",
    "Reserved",
    "Reserved",
    "REQUEST for access demand, Expected respons specifies access demand",
    "REQUEST/RESPONSE, Request status of link",
    "REQUEST/RESPONSE, Request user data class 1",
    "REQUEST/RESPONSE, Request user data class 2",
    "Reserved",
    "Reserved",
    "Reserved",
    "Reserved"};

    final String[] FROM_SERVER = {"CONFIRM, ACK: positive acknowledge",
    "CONFIRM, NACK: message not accepted, link busy",
    "Reserved",
    "Reserved",
    "Reserved",
    "Reserved",
    "Reserved",
    "Reserved",
    "RESPOND, User data",
    "RESPOND, NACK: requested data not available",
    "Reserved",
    "RESPOND, Status of link or access demand",
    "Reserved",
    "Reserved for special use by agreement",
    "Link service not functioning",
    "Link service not implemented"};


    static public final int FRAME_VARIABLE_LENGTH=0x68;
    static public final int FRAME_FIXED_LENGTH=0x10;
    static public final int FRAME_SINGLE_CHAR_E5=0xE5;
    static public final int FRAME_SINGLE_CHAR_A2=0xA2;
    static public final int FRAME_END=0x16;

    static public final int VARIABLE_FRAME_HEADER_LENGTH=4;
    static public final int VARIABLE_FRAME_TAIL_LENGTH=2;
    static public final int FIXED_FRAME_HEADER_LENGTH=1;
    static public final int FIXED_FRAME_TAIL_LENGTH=2;

    int type=-1;
    int control=-1;
    int address=-1;
    int length=-1;
    ApplicationData asdu=null;
    byte[] data=null;
    //int checksum;

    int nrOfDevices;

    /** Creates a new instance of IEC870Frame */
    public IEC870Frame(byte[] data) throws IEC870ConnectionException,IOException {
    	this(data,1);
    } // public IEC870Frame(byte[] data)

    public IEC870Frame(byte[] data,int nrOfDevices) throws IEC870ConnectionException,IOException {
    	this.nrOfDevices=nrOfDevices;
        this.data=data;
        asdu=null;
        control=-1;
        length=-1;
        address=-1;
        type=ProtocolUtils.getByte2Int(data,0);
        switch(type) {
            case IEC870Frame.FRAME_VARIABLE_LENGTH:
                buildVariableLengthFrame(data);
                break; // FRAME_VARIABLE_LENGTH

            case IEC870Frame.FRAME_FIXED_LENGTH:
                buildFixedLengthFrame(data);
                break; // FRAME_FIXED_LENGTH

            case IEC870Frame.FRAME_SINGLE_CHAR_E5:
            case IEC870Frame.FRAME_SINGLE_CHAR_A2:
                break; // FRAME_SINGLE_CHAR_A2

            default: throw new IEC870ConnectionException("Invalid frame format (type) (start character) "+Integer.toHexString(type));
        } // switch(type)
    }

    /** Creates a new instance of IEC870Frame */
    public IEC870Frame(int function,int address,ApplicationData asdu) throws IEC870ConnectionException {
        this(FRAME_VARIABLE_LENGTH,function,address,asdu.getData().length+3,asdu);
    }
    /** Creates a new instance of IEC870Frame */
    public IEC870Frame(int function,int address) throws IEC870ConnectionException {
        this(FRAME_FIXED_LENGTH,function,address,2,null);
    }
    /** Creates a new instance of IEC870Frame */
    public IEC870Frame(int type,int function,int address,int length, ApplicationData asdu) throws IEC870ConnectionException {
    	this.nrOfDevices=1;
        this.type=type;
        this.control=function|PRM_PRIMARY;
        if (CLIENT_CONTROL_FCV[function] == 1)
            this.control |= FCV_PRIMARY;
        this.address=address;
        if (asdu != null)
            this.length=asdu.getData().length+3; // + control, address and CI field
        else
            this.length=length;
        this.asdu=asdu;
    }


    public boolean toggleFCB(boolean fcb) {
        if ((fcb) || (!isFCV())) {
            resetFCB();
            return false;
        }
        else {
            setFCB();
            return true;
        }
    }
    public void setFCB() {
        control |= FCB_PRIMARY;
    }
    public void resetFCB() {
        control &= (FCB_PRIMARY^0xFF);
    }

    public byte[] getData() throws IEC870ConnectionException {
        buildData();
        return data;
    }

    public boolean isSingleCharAck() {
        return (type == FRAME_SINGLE_CHAR_E5);
    }
    public int getType() {
        return type;
    }
    public int getControl() {
        return control;
    }
    public int getAddress() {
        return address;
    }
    public int getLength() {
        return length;
    }
    public ApplicationData getASDU() {
        return asdu;
    }

    public boolean isClient() {
        return (control & 0x40) != 0;
    }
    public boolean isServer() {
        return !((control & 0x40) != 0);
    }
    public int getFunction() {
        return control & 0x0F;
    }
    public boolean isFunction(int function) {
        return (getFunction() == function);
    }
    public boolean isFCB() {
        return (control & 0x20) != 0;
    }
    public boolean isFCV() {
        return (control & 0x10) != 0;
    }
    public boolean isACD() {
        return (control & 0x20) != 0;
    }
    public boolean isDFC() {
        return (control & 0x10) != 0;
    }

//    public String getFrameInfo(int i) {
//        String strControl=null;
//        String strasdu=null;
//        if ((type == IEC870Frame.FRAME_SINGLE_CHAR_E5) || (type == IEC870Frame.FRAME_SINGLE_CHAR_A2)) {
//            strControl=i+"<-----------SERVER (0x"+Integer.toHexString(type)+")<-----------";
//        }
//        else {
//            if (asdu != null)
//                strasdu = ", TID="+asdu.getCIFieldDescription()+", CAUSE="+asdu.getCauseOfTransmissionCauseDescription();
//            else strasdu="";
//            if (isClient()) {
//                strControl=i+"----------->CLIENT: (0x"+Integer.toHexString(type)+") "+FROM_CLIENT[getFunction()]+" FCV="+isFCV()+" FCB="+isFCB()+strasdu+"----------->";
//            }
//            else if (isServer()) {
//                strControl=i+"<-----------SERVER: (0x"+Integer.toHexString(type)+") "+FROM_SERVER[getFunction()]+" ACD="+isACD()+" DFC="+isDFC()+strasdu+"<-----------";
//            }
//        }
//        return strControl;
//    }

    public String toString() {
        return toString(TimeZone.getDefault());
    }

    public String toString(TimeZone timeZone) {
        return toString(-1,timeZone);
    }

    public String toString(int info, TimeZone timeZone) {
        StringBuffer strbuff = new StringBuffer();
        String strControl=null;
        if (((isClient()) && (CLIENT_CONTROL_FILTER[getFunction()] == 1)) ||
        ((isServer()) && (SERVER_CONTROL_FILTER[getFunction()] == 1))) {

            if (info != -1)
                strbuff.append("****************************** IEC870 FRAME "+info+" ********************************\r\n");
            else
                strbuff.append("****************************** IEC870 FRAME ********************************\r\n");

            if (isClient()) {
                strControl="------->CLIENT: "+FROM_CLIENT[getFunction()]+" FCV="+isFCV()+" FCB="+isFCB();
            }
            else if (isServer()) {
                strControl="<-------SERVER: "+FROM_SERVER[getFunction()]+" ACD="+isACD()+" DFC="+isDFC();
            }

            if ((type == IEC870Frame.FRAME_SINGLE_CHAR_E5) || (type == IEC870Frame.FRAME_SINGLE_CHAR_A2))
                strbuff.append("type=0x"+Integer.toHexString(type)+" (Single character)"+"\r\n");
            else {
                if (length != -1) {
                    strbuff.append("type=0x"+Integer.toHexString(type)+", control=0x"+Integer.toHexString(control)+" ("+strControl+") , address=0x"+Integer.toHexString(address)+", length=0x"+Integer.toHexString(length)+"\r\n");
                }
                else {
                    strbuff.append("type=0x"+Integer.toHexString(type)+", control=0x"+Integer.toHexString(control)+" ("+strControl+") , address=0x"+Integer.toHexString(address)+"\r\n");
                }
            }

            if (asdu != null) strbuff.append(asdu.toString());
            strbuff.append("\r\n");
        }
        return strbuff.toString();
    }

    private void buildVariableLengthFrame(byte[] data) throws IEC870ConnectionException,IOException {
        length = ProtocolUtils.getByte2Int(data,1);
        control = ProtocolUtils.getByte2Int(data,4);
        address = ProtocolUtils.getInt(data,5,1);
        asdu = new ApplicationData(ProtocolUtils.getSubArray(data,6,data.length-3));
        //checksum = ProtocolUtils.getByte2Int(data,data.length-VARIABLE_FRAME_TAIL_LENGTH);

    } // private void buildVariableLengthFrame(byte[] data)

    private void buildFixedLengthFrame(byte[] data) throws IEC870ConnectionException,IOException {
        length = 2;
        control = ProtocolUtils.getByte2Int(data,1);
        address = ProtocolUtils.getInt(data,2,1);
        byte[] subarray = ProtocolUtils.getSubArray(data,3,data.length-3);
        if (subarray.length>0)
            asdu = new ApplicationData(ProtocolUtils.getSubArray(data,4,data.length-3));
        //checksum = ProtocolUtils.getByte2Int(data,data.length-FIXED_FRAME_TAIL_LENGTH);
    } // private void buildFixedLengthFrame(byte[] data)

    private void buildData() throws IEC870ConnectionException {
        int i,checksum=0;
        switch(type) {
            case IEC870Frame.FRAME_VARIABLE_LENGTH:
                data = new byte[length+VARIABLE_FRAME_HEADER_LENGTH+VARIABLE_FRAME_TAIL_LENGTH];
                data[0] = (byte)type;
                data[1] = (byte)length;
                data[2] = (byte)length;
                data[3] = (byte)type;
                data[4] = (byte)control;
                data[5] = (byte)(address&0xFF);
                data[6] = (byte)asdu.getCIField();
                checksum += (int)data[4]&0xFF;
                checksum += (int)data[5]&0xFF;
                checksum += (int)data[6]&0xFF;
                for (i=0;i<(length-3);i++) {
                    data[7+i] = asdu.getData()[i];
                    checksum += (int)asdu.getData()[i] & 0xFF;
                }
                break; // FRAME_VARIABLE_LENGTH

            case IEC870Frame.FRAME_FIXED_LENGTH:
                data = new byte[length+FIXED_FRAME_HEADER_LENGTH+FIXED_FRAME_TAIL_LENGTH];
                data[0] = (byte)type;
                data[1] = (byte)control;
                data[2] = (byte)(address);
                checksum += (int)data[1]&0xFF;
                checksum += (int)data[2]&0xFF;
                break; // FRAME_FIXED_LENGTH

            default: throw new IEC870ConnectionException("IEC870Frame, buildData, Invalid frame format (type) (start character) "+Integer.toHexString(type));
        } // switch(type)

        data[data.length-2] = (byte)checksum;
        data[data.length-1] = FRAME_END;
    } // buildData()

	public int getNrOfDevices() {
		return nrOfDevices;
	}


}
