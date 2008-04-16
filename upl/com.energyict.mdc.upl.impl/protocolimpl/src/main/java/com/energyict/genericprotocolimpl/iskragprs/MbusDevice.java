/**
 * 
 */
package com.energyict.genericprotocolimpl.iskragprs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;

/**
 * @author gna
 *
 */
public class MbusDevice implements Messaging, MeterProtocol{

	/**
	 * 
	 */
	public MbusDevice() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void connect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void disconnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public Object fetchCache(int rtuid) throws SQLException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getCache() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		throw new UnsupportedException();
	}

	public Quantity getMeterReading(int channelId) throws UnsupportedException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Quantity getMeterReading(String name) throws UnsupportedException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfChannels() throws UnsupportedException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
			throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getProfileInterval() throws UnsupportedException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getProtocolVersion() {
		return "$Revision: 1.1 $";
	}

	public String getRegister(String name) throws IOException,
			UnsupportedException, NoSuchRegisterException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getTime() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(InputStream inputStream, OutputStream outputStream,
			TimeZone timeZone, Logger logger) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void initializeDevice() throws IOException, UnsupportedException {
		// TODO Auto-generated method stub
		
	}

	public void release() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void setCache(Object cacheObject) {
		// TODO Auto-generated method stub
		
	}

	public void setProperties(Properties properties)
			throws InvalidPropertyException, MissingPropertyException {
		// TODO Auto-generated method stub
		
	}

	public void setRegister(String name, String value) throws IOException,
			NoSuchRegisterException, UnsupportedException {
		// TODO Auto-generated method stub
		
	}

	public void setTime() throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void updateCache(int rtuid, Object cacheObject) throws SQLException,
			BusinessException {
		// TODO Auto-generated method stub
		
	}

	public List getOptionalKeys() {
		return new ArrayList(0);
	}

	public List getRequiredKeys() {
		return new ArrayList(0);
	}

	public List getMessageCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	public String writeMessage(Message msg) {
		// TODO Auto-generated method stub
		return null;
	}

	public String writeTag(MessageTag tag) {
		// TODO Auto-generated method stub
		return null;
	}

	public String writeValue(MessageValue value) {
		// TODO Auto-generated method stub
		return null;
	}

}
