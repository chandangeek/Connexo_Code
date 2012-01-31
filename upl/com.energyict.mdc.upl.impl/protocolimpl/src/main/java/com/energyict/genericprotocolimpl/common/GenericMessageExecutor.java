package com.energyict.genericprotocolimpl.common;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.protocol.LoadProfileReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * @author gna
 * @BeginChanges: GNA |21042009| Changed the SingleActionScheduler Execution time, according to BlueBook9th the executionTime is an Array of Structures of 2 OctetStrings
 */
public abstract class GenericMessageExecutor {

    abstract public void doMessage(RtuMessage rtuMessage) throws BusinessException, SQLException, IOException;

    abstract protected TimeZone getTimeZone();

    public void importMessage(String message, DefaultHandler handler) throws BusinessException {
        try {

            byte[] bai = message.getBytes();
            InputStream i = (InputStream) new ByteArrayInputStream(bai);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(i, handler);

        } catch (ParserConfigurationException thrown) {
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        } catch (SAXException thrown) {
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        } catch (IOException thrown) {
            thrown.printStackTrace();
            throw new BusinessException(thrown);
        }
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
     * Convert a given epoch timestamp in SECONDS to an {@link AXDRDateTime} object
     *
     * @param time - the time in seconds sinds 1th jan 1970 00:00:00
     * @return the AXDRDateTime of the given time
     * @throws IOException when the entered time could not be parsed to a long value
     */
    public AXDRDateTime convertUnixToGMTDateTime(String time) throws IOException {
        try {
            AXDRDateTime dateTime = null;
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.setTimeInMillis(Long.parseLong(time) * 1000);
            dateTime = new AXDRDateTime(cal);
            return dateTime;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Could not parse " + time + " to a long value");
        }
    }

    /**
     * Substracts 5 seconds from the startReadingTime and adds 5 seconds to the endReadingTime
     *
     * @param loadProfileReader the reader
     * @return the reader with the adjested times
     */
    protected LoadProfileReader constructDateTimeCorrectdLoadProfileReader(final LoadProfileReader loadProfileReader) {
        Date from = new Date(loadProfileReader.getStartReadingTime().getTime() - 5000);
        Date to = new Date(loadProfileReader.getEndReadingTime().getTime() + 5000);
        return new LoadProfileReader(loadProfileReader.getProfileObisCode(), from, to, loadProfileReader.getLoadProfileId(), loadProfileReader.getMeterSerialNumber(), loadProfileReader.getChannelInfos());
    }
}
