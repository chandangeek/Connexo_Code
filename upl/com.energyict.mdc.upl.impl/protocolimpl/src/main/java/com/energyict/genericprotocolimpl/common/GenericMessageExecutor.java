package com.energyict.genericprotocolimpl.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdw.core.RtuMessage;

/**
 * 
 * @author gna
 * @BeginChanges:
 * 	GNA |21042009| Changed the SingleActionScheduler Execution time, according to BlueBook9th the executionTime is an Array of Structures of 2 OctetStrings
 */
public abstract class GenericMessageExecutor {
	
	abstract public void doMessage(RtuMessage rtuMessage)throws BusinessException, SQLException;
	
	abstract protected TimeZone getTimeZone();
	
	public void importMessage(String message, DefaultHandler handler) throws BusinessException{
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
			cal.setTimeInMillis(Long.parseLong(strDate)*1000);
			byte[] dateBytes = new byte[5];
			dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8)&0xFF);
			dateBytes[1] = (byte) (cal.get(Calendar.YEAR) &0xFF);
			dateBytes[2] = (byte) ((cal.get(Calendar.MONTH)&0xFF) +1 );	
			dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH)&0xFF);
			dateBytes[4] = getDLMSDayOfWeek(cal);
			OctetString date = new OctetString(dateBytes);
			byte[] timeBytes = new byte[4];
			timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
			timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
			timeBytes[2] = (byte) 0x00;
			timeBytes[3] = (byte) 0x00;
			OctetString time = new OctetString(timeBytes);
			
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
	
	private byte getDLMSDayOfWeek(Calendar cal){
		int dow = cal.get(Calendar.DAY_OF_WEEK) -1;
		if(dow == 0){
			dow = 7;
		}
		return (byte)dow;
	}
	
	public AXDRDateTime convertUnixToGMTDateTime(String time, TimeZone timeZone) throws IOException{
		try {
			AXDRDateTime dateTime = null;
//			Calendar cal = Calendar.getInstance(timeZone);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.setTimeInMillis(Long.parseLong(time)*1000);
			dateTime = new AXDRDateTime(cal);
			return dateTime;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IOException("Could not parse " + time + " to a long value");
		}
	}

}
