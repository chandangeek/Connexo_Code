/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * VDEWLogbook.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
/**
 *
 * @author  Koen
 * changes:
 * KV 17022004 extended with MeterExceptionInfo
 */
public class VDEWLogbook {

    private static final int DEBUG=0;
    ProtocolLink protocolLink=null;
    private MeterExceptionInfo meterExceptionInfo=null; // KV 17022004

    /** Creates a new instance of VDEWLogbook */
    public VDEWLogbook(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink) {
        this.protocolLink = protocolLink;
        this.meterExceptionInfo=meterExceptionInfo;
    }

    protected ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public byte[] readRawLogbookData(Calendar fromCalendar, Calendar toCalendar, int profileId) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(getProtocolLink().getTimeZone().inDaylightTime(fromCalendar.getTime()) ? '1' : '0');
        byteArrayOutputStream.write(((fromCalendar.get(fromCalendar.YEAR)%100)/10)+0x30);
        byteArrayOutputStream.write(((fromCalendar.get(fromCalendar.YEAR)%100)%10)+0x30);
        byteArrayOutputStream.write(((fromCalendar.get(fromCalendar.MONTH)+1)/10)+0x30);
        byteArrayOutputStream.write(((fromCalendar.get(fromCalendar.MONTH)+1)%10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(fromCalendar.DAY_OF_MONTH)/10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(fromCalendar.DAY_OF_MONTH)%10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(fromCalendar.HOUR_OF_DAY)/10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(fromCalendar.HOUR_OF_DAY)%10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(fromCalendar.MINUTE)/10)+0x30);
        byteArrayOutputStream.write((fromCalendar.get(fromCalendar.MINUTE)%10)+0x30);
        byteArrayOutputStream.write((int)';');
        byteArrayOutputStream.write(getProtocolLink().getTimeZone().inDaylightTime(toCalendar.getTime()) ? '1' : '0');
        byteArrayOutputStream.write(((toCalendar.get(toCalendar.YEAR)%100)/10)+0x30);
        byteArrayOutputStream.write(((toCalendar.get(toCalendar.YEAR)%100)%10)+0x30);
        byteArrayOutputStream.write(((toCalendar.get(toCalendar.MONTH)+1)/10)+0x30);
        byteArrayOutputStream.write(((toCalendar.get(toCalendar.MONTH)+1)%10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(toCalendar.DAY_OF_MONTH)/10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(toCalendar.DAY_OF_MONTH)%10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(toCalendar.HOUR_OF_DAY)/10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(toCalendar.HOUR_OF_DAY)%10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(toCalendar.MINUTE)/10)+0x30);
        byteArrayOutputStream.write((toCalendar.get(toCalendar.MINUTE)%10)+0x30);
        return doReadRawLogbookData(new String(byteArrayOutputStream.toByteArray()), profileId);
    } // protected byte[] readRawLogbookData(Calendar fromCalendar, Calendar toCalendar, profileId)

    private byte[] doReadRawLogbookData(String data,int profileid) throws IOException {
        try {
            String cmd = "P."+String.valueOf(profileid)+"("+data+";20)";
            protocolLink.getFlagIEC1107Connection().sendRawCommandFrame(FlagIEC1107Connection.READ6,cmd.getBytes());
            byte[] rawprofile = protocolLink.getFlagIEC1107Connection().receiveRawData();
            String str = new String(rawprofile);
            if (DEBUG>=1) System.out.println(str);
            if (str.indexOf("ER") != -1) {
               if (getMeterExceptionInfo() != null) {
                  String exceptionId = "ER" + str.substring(str.indexOf("ER") + 3,str.indexOf("ER")+5);
                  throw new FlagIEC1107ConnectionException("VDEWLogbook, readRawLogbookData, error received ("+str+") = "+getMeterExceptionInfo().getExceptionInfo(exceptionId));
               }
               else throw new FlagIEC1107ConnectionException("VDEWLogbook, readRawLogbookData, error received ("+str+")");
            }

            return rawprofile;
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException("VDEWLogbook, readRawLogbookData, FlagIEC1107ConnectionException, "+e.getMessage());
        }
    }

    /** Getter for property meterExceptionInfo.
     * @return Value of property meterExceptionInfo.
     *
     */
    public MeterExceptionInfo getMeterExceptionInfo() {
        return meterExceptionInfo;
    }

}
