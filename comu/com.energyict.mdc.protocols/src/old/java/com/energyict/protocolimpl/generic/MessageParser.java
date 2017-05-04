/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.generic;

import com.energyict.mdc.protocol.api.LoadProfileReader;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author gna
 * @BeginChanges: GNA |21042009| Changed the SingleActionScheduler Execution time, according to BlueBook9th the executionTime is an Array of Structures of 2 OctetStrings
 */
public abstract class MessageParser {

    protected abstract TimeZone getTimeZone();

    public void importMessage(String message, DefaultHandler handler) throws ParserConfigurationException, SAXException, IOException {
        byte[] bai = message.getBytes();
        InputStream i = new ByteArrayInputStream(bai);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(i, handler);
    }

    public String getMessageValue(String msgStr, String str) {
        try {
            return msgStr.substring(msgStr.indexOf(str + ">") + str.length()
                    + 1, msgStr.indexOf("</" + str));
        } catch (Exception e) {
            return "";
        }
    }

    public Array convertUnixToDateTimeArray(String strDate) throws IOException {
        try {
            Calendar cal = Calendar.getInstance(getTimeZone());
            cal.setTimeInMillis(Long.parseLong(strDate) * 1000);
            byte[] dateBytes = new byte[5];
            dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
            dateBytes[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
            dateBytes[2] = (byte) ((cal.get(Calendar.MONTH) & 0xFF) + 1);
            dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0xFF);
            dateBytes[4] = getDLMSDayOfWeek(cal);
            OctetString date = OctetString.fromByteArray(dateBytes);
            byte[] timeBytes = new byte[4];
            timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
            timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
            timeBytes[2] = (byte) 0x00;
            timeBytes[3] = (byte) 0x00;
            OctetString time = OctetString.fromByteArray(timeBytes);

            Array dateTimeArray = new Array();
            Structure strDateTime = new Structure();
            strDateTime.addDataType(time);
            strDateTime.addDataType(date);
            dateTimeArray.addDataType(strDateTime);
            return dateTimeArray;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Could not parse " + strDate + " to a long value");
        }
    }

    private byte getDLMSDayOfWeek(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (dow == 0) {
            dow = 7;
        }
        return (byte) dow;
    }

    /**
     * Convert a given epoch timestamp in SECONDS to an {@link com.energyict.dlms.axrdencoding.util.AXDRDateTime} object
     *
     * @param time - the time in seconds sinds 1th jan 1970 00:00:00
     * @return the AXDRDateTime of the given time
     * @throws java.io.IOException when the entered time could not be parsed to a long value
     */
    public AXDRDateTime convertUnixToGMTDateTime(String time) throws IOException {
        return convertUnixToDateTime(time, TimeZone.getTimeZone("GMT"));
    }

    public AXDRDateTime convertUnixToDateTime(String time, TimeZone timeZone) throws IOException {
        try {
            AXDRDateTime dateTime;
            Calendar cal = Calendar.getInstance(timeZone);
            cal.setTimeInMillis(Long.parseLong(time) * 1000);
            dateTime = new AXDRDateTime(cal.getTime(), timeZone);
            return dateTime;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Could not parse " + time + " to a long value");
        }
    }

    /**
     * Subtracts 5 seconds from the startReadingTime and adds 5 seconds to the endReadingTime
     *
     * @param loadProfileReader the reader
     * @return the reader with the adjested times
     */
    protected LoadProfileReader constructDateTimeCorrectdLoadProfileReader(Clock clock, LoadProfileReader loadProfileReader) {
        Instant from = loadProfileReader.getStartReadingTime().minus(Duration.ofSeconds(5));
        Instant to = loadProfileReader.getEndReadingTime().plus(Duration.ofSeconds(5));
        return new LoadProfileReader(
                clock,
                loadProfileReader.getProfileObisCode(),
                from,
                to,
                loadProfileReader.getLoadProfileId(),
                loadProfileReader.getDeviceIdentifier(),
                loadProfileReader.getChannelInfos(),
                loadProfileReader.getMeterSerialNumber(),
                loadProfileReader.getLoadProfileIdentifier());
    }

}