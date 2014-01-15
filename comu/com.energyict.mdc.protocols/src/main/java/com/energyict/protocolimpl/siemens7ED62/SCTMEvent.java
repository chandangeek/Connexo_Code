/*
 * SCTMEvent.java
 *
 * Created on 6 februari 2003, 15:31
 */

package com.energyict.protocolimpl.siemens7ED62;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class SCTMEvent {

    private int length;
    private int type;
    private SCTMTimeData from=null,to=null;
    String adat=null,edat=null;
    String subAddress=null;
    /** Creates a new instance of SCTMEvent */

    public SCTMEvent(byte[] data) throws IOException {
        this(data,-1);
    }

    public SCTMEvent(byte[] data,int meterType) throws IOException {
        length = (int)((data[0]-0x30)*10+(data[1]-0x30));
        if (length != 0) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append((char)data[2]);
            stringBuffer.append((char)data[3]);
            type = Integer.parseInt(stringBuffer.toString(),16);
        }
        else type = 0xFF;
        if (isFromTimeDate(type)) from = new SCTMTimeData(getTimeDataArray(data, 4));
        if (isToTimeDate(type)) to = new SCTMTimeData(getTimeDataArray(data, 14));

        // see SCTM doc...
        // length can vary e.g. 43 (2x12 for the aaaa and bbbb info)
        //                      51 (2x16 for the aaaa and bbbb info)
        if ((type==0xA1)||(type==0xA2)||(type==0xC1)||(type==0xC2)) {
            setSubAddress((new String(ProtocolUtils.getSubArray2(data, 14, 5))).trim());
            int sublength = ((length-19)/2);
            setAdat((new String(ProtocolUtils.getSubArray2(data, 19, sublength))).trim());
            setEdat((new String(ProtocolUtils.getSubArray2(data, 19+sublength, sublength))).trim());
        }

    }


    private boolean isFromTimeDate(int type) {
       if ((type==0xA1)||(type==0xA2)||(type==0xC1)||(type==0xC2)||
           (type==0xA3)||(type==0xD1)||(type==0xD2)||(type==0xD3)||(type==0xD4))
            return true;
       else return false;
    }


    private boolean isToTimeDate(int type) {
       if ((type==0xA1)||(type==0xA2)||(type==0xC1)||(type==0xC2)) return false;
       else if ((type==0xA3)||(type==0xD1)||(type==0xD2)||(type==0xD3)||(type==0xD4)) return true;
       else return false;
    }

    private byte[] getTimeDataArray(byte[] data, int offset) {
        byte[] td = new byte[10];
        for (int i=0;i<td.length;i++) td[i] = data[offset+i];
        return td;
    }

    public int getType() {
       return type;
    }
    public SCTMTimeData getFrom() {
       return from;
    }
    public SCTMTimeData getTo() {
       return to;
    }

    /**
     * Getter for property adat.
     * @return Value of property adat.
     */
    public java.lang.String getAdat() {
        return adat;
    }

    /**
     * Setter for property adat.
     * @param adat New value of property adat.
     */
    public void setAdat(java.lang.String adat) {
        this.adat = adat;
    }

    /**
     * Getter for property edat.
     * @return Value of property edat.
     */
    public java.lang.String getEdat() {
        return edat;
    }

    /**
     * Setter for property edat.
     * @param edat New value of property edat.
     */
    public void setEdat(java.lang.String edat) {
        this.edat = edat;
    }

    /**
     * Getter for property subAddress.
     * @return Value of property subAddress.
     */
    public java.lang.String getSubAddress() {
        return subAddress;
    }

    /**
     * Setter for property subAddress.
     * @param subAddress New value of property subAddress.
     */
    public void setSubAddress(java.lang.String subAddress) {
        this.subAddress = subAddress;
    }

} // public class SCTMEvent
