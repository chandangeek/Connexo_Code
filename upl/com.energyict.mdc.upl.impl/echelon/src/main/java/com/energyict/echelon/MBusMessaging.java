package com.energyict.echelon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;

/**
 * Messaging class for Echelon M-Bus devices. 
 * On an M-Bus device, one can only execute the "ReadRegistersOnDemand" command.
 * 
 * @author Steven W
 * 
 */
public class MBusMessaging implements MeterProtocol, Messaging {

	private final static boolean ADVANCED = true;

	public final static String READ_REGISTERS_ON_DEMAND = "readRegistersOnDemand";

	public List getMessageCategories() {
		List theCategories = new ArrayList();

		MessageCategorySpec cat = new MessageCategorySpec("Actions");
		MessageSpec msgSpec = null;

		msgSpec = addBasicMsg("Read registers on demand", READ_REGISTERS_ON_DEMAND, !ADVANCED);
		cat.addMessageSpec(msgSpec);

		theCategories.add(cat);
		return theCategories;
	}

	public String writeMessage(Message msg) {
		return msg.write(this);
	}

	public String writeTag(MessageTag tag) {
		StringBuffer buf = new StringBuffer();

		// a. Opening tag
		buf.append("<");
		buf.append(tag.getName());

		// b. Attributes
		for (Iterator it = tag.getAttributes().iterator(); it.hasNext();) {
			MessageAttribute att = (MessageAttribute) it.next();
			if (att.getValue() == null || att.getValue().length() == 0)
				continue;
			buf.append(" ").append(att.getSpec().getName());
			buf.append("=").append('"').append(att.getValue()).append('"');
		}
		if (tag.getSubElements().isEmpty()) {
			buf.append("/>");
			return buf.toString();
		}
		buf.append(">");
		// c. sub elements
		for (Iterator it = tag.getSubElements().iterator(); it.hasNext();) {
			MessageElement elt = (MessageElement) it.next();
			if (elt.isTag())
				buf.append(writeTag((MessageTag) elt));
			else if (elt.isValue()) {
				String value = writeValue((MessageValue) elt);
				if (value == null || value.length() == 0)
					return "";
				buf.append(value);
			}
		}

		// d. Closing tag
		buf.append("</");
		buf.append(tag.getName());
		buf.append(">");

		return buf.toString();
	}

	public String writeValue(MessageValue value) {
		return null;
	}

	private MessageSpec addBasicMsg(String keyId, String tagName,
			boolean advanced) {
		MessageSpec msgSpec = new MessageSpec(keyId, advanced);
		MessageTagSpec tagSpec = new MessageTagSpec(tagName);
		msgSpec.add(tagSpec);
		return msgSpec;
	}

	public void connect() throws IOException {
	}

	public void disconnect() throws IOException {
	}

	public Object fetchCache(int rtuid) throws SQLException, BusinessException {
		return null;
	}

	public Object getCache() {
		return null;
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		return null;
	}

	public Quantity getMeterReading(int channelId) throws UnsupportedException,
			IOException {
		return null;
	}

	public Quantity getMeterReading(String name) throws UnsupportedException,
			IOException {
		return null;
	}

	public int getNumberOfChannels() throws UnsupportedException, IOException {
		return 0;
	}

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		return null;
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents)
			throws IOException {
		return null;
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
			throws IOException, UnsupportedException {
		return null;
	}

	public int getProfileInterval() throws UnsupportedException, IOException {
		return 0;
	}

	public String getProtocolVersion() {
        return "$Revision: 1.1 $";
	}

	public String getRegister(String name) throws IOException,
			UnsupportedException, NoSuchRegisterException {
		return null;
	}

	public Date getTime() throws IOException {
		return null;
	}

	public void init(InputStream inputStream, OutputStream outputStream,
			TimeZone timeZone, Logger logger) throws IOException {
	}

	public void initializeDevice() throws IOException, UnsupportedException {
	}

	public void release() throws IOException {
	}

	public void setCache(Object cacheObject) {
	}

	public void setProperties(Properties properties)
			throws InvalidPropertyException, MissingPropertyException {
	}

	public void setRegister(String name, String value) throws IOException,
			NoSuchRegisterException, UnsupportedException {
	}

	public void setTime() throws IOException {
	}

	public void updateCache(int rtuid, Object cacheObject) throws SQLException,
			BusinessException {
	}

	public List getOptionalKeys() {
		return new ArrayList(0);
	}

	public List getRequiredKeys() {
		return new ArrayList(0);
	}

}
