/**
 * 
 */
package com.energyict.genericprotocolimpl.iskrap2lpc;

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

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdw.core.Rtu;
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
import com.energyict.protocolimpl.base.ProtocolChannelMap;

/**
 * @author gna
 *
 */
public class MbusDevice implements Messaging, MeterProtocol{
	
	private long mbusAddress	= -1;
	
	private String customerID;
	
	public Rtu	mbus;
	private Logger logger;
	private ProtocolChannelMap channelMap = null;
	private Unit mbusUnit;
	
	
	private static String ONDEMAND = "ONDEMAND";

	/**
	 * 
	 */
	public MbusDevice() {
	}

	public MbusDevice(long address, String customerID, Rtu rtu, Logger logger) {
		this.mbusAddress = address;
		this.customerID	= customerID;
		this.mbus = rtu;
		this.logger = logger;
		// TODO hardcoded unit
		this.mbusUnit = Unit.get(BaseUnit.CUBICMETER);
	}

	public MbusDevice(Rtu rtu) {
		this.mbusAddress = -1;
		this.customerID = null;
		this.mbus = rtu;
		this.logger = null;
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
		return "$Date$";
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
        List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");
        
        MessageSpec msgSpec = addBasicMsg("ReadOnDemand", Constant.ON_DEMAND, false);
        cat.addMessageSpec(msgSpec);
        
//        MessageSpec msgSpec = addBasicMsg("Disconnect meter", DISCONNECT, false);
//        cat.addMessageSpec(msgSpec);
//        msgSpec = addBasicMsg("Connect meter", CONNECT, false);
//        cat.addMessageSpec(msgSpec);
        
        theCategories.add(cat);
        
		return theCategories;
	}

	public String writeMessage(Message msg) {
		return msg.write(this);
	}

	public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();
        
        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());
        
        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0)
                continue;
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
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
        buf.append(msgTag.getName());
        buf.append(">");
        
        return buf.toString();
	}

	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}
	
    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

	public Rtu getRtu() {
		return mbus;
	}

	public Logger getLogger() {
		return logger;
	}

	public ProtocolChannelMap getChannelMap() throws InvalidPropertyException, BusinessException {
    	if (channelMap == null){
    		String sChannelMap = getRtu().getProperties().getProperty( Constant.CHANNEL_MAP );
    		if(sChannelMap != null)
    			channelMap = new ProtocolChannelMap( sChannelMap );
    		else
    			throw new BusinessException("No channelmap configured on the meter, meter will not be handled.");
    	}
        return channelMap;
	}

	public Unit getMbusUnit(){
		return mbusUnit;
	}
}
