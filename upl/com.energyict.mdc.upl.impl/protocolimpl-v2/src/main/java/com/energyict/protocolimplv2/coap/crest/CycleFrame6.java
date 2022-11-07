/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.coap.crest;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CycleFrame6 {

    private final int primaryAddress;
    private final String secondaryAddress;
    private final ManufacturerID manufacturerID;
    private final int generation;
    private final Medium medium;
    private final int accessNumber;
    private final int status;
    private final String febricationNumber;
    private final int serialNumber;
    private final Date dateAndTime;
    private final int batteryLifeTime;
    private final int volume;
    private final int backFlowVolume;

    public CycleFrame6(byte[] bytes) {
        primaryAddress = ProtocolTools.getIntFromBytes(bytes, 5, 1);
        secondaryAddress = ProtocolTools.bytesToHex(ProtocolTools.reverseByteArray(ProtocolTools.getSubArray(bytes, 7, 11)));

        manufacturerID = ManufacturerID.forId(bytes[12]);
        generation = ProtocolTools.getIntFromBytes(bytes, 13, 1);
        medium = Medium.forId(bytes[14]);
        accessNumber = ProtocolTools.getIntFromBytes(bytes, 15, 1);
        status = ProtocolTools.getIntFromBytes(bytes, 16, 1);
        febricationNumber = ProtocolTools.bytesToHex(ProtocolTools.reverseByteArray(ProtocolTools.getSubArray(bytes, 21, 25)));
        serialNumber = ProtocolTools.getIntFromBytesLE(bytes, 37, 10);
        dateAndTime = buildDate(ProtocolTools.getIntFromBytesLE(bytes, 49, 4));
        batteryLifeTime = ProtocolTools.getIntFromBytesLE(bytes, 65, 2);
        volume = ProtocolTools.getIntFromBytesLE(bytes, 69, 4);
        backFlowVolume = ProtocolTools.getIntFromBytesLE(bytes, 76, 4);

        System.out.println("secondaryAddress : " + secondaryAddress);
        System.out.println("date : " + dateAndTime);
        System.out.println("fabricationumber : " + febricationNumber);
        System.out.println("volume : " + volume);
        System.out.println("backFlowVolume : " + backFlowVolume);
    }

    public String getFabricationNumber() {
        return febricationNumber;
    }

    public Date getDateAndTime() {
        return dateAndTime;
    }

    public BigDecimal getVolume() {
        return BigDecimal.valueOf(volume);
    }

    private Date buildDate(int dateTime) {
        int min = (dateTime & 0x3F);
        int hour = (dateTime >> 8 & 0x1F);
        int day = (dateTime >> 16) & 0x1F;
        int month = (dateTime >> 24) & 0x1F;
        int year = (((dateTime >> 16) & 0xE0) >> 5) + (((dateTime >> 24) & 0xF0) >> 1);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Amsterdam"));
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, 2000 + year);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

}
