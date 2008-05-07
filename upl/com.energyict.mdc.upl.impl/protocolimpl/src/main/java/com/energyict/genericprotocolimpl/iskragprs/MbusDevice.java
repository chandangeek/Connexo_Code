/**
 * 
 */
package com.energyict.genericprotocolimpl.iskragprs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.genericprotocolimpl.common.AMRJournalManager;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
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
 * @author gna
 *
 */
public class MbusDevice implements Messaging, MeterProtocol{
	
	private long mbusAddress	= -1;
	
	private String customerID;
	private String rtuType;
	
	public Rtu	mbus;
	private Logger logger;
	
	
	private static String ONDEMAND = "ONDEMAND";

	/**
	 * 
	 */
	public MbusDevice() {
	}

	public MbusDevice(long address, String customerID, Rtu rtu, Logger logger) throws InvalidPropertyException, MissingPropertyException {
		this.mbusAddress = address;
		this.customerID	= customerID;
		this.mbus = rtu;
		this.logger = logger;
		setProperties(mbus.getProperties());
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
		return "$Revision: 1.2 $";
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

	public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
		rtuType = properties.getProperty("RtuType","mbus");
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
        
        MessageSpec msgSpec = addBasicMsg("ReadOnDemand", ONDEMAND, false);
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

	public Rtu getMbus() {
		return mbus;
	}

	public void sendMeterMessages(IskraMx37x iskraMx37x) throws IOException, BusinessException, SQLException {
		Iterator mi = mbus.getPendingMessages().iterator();
		
		while(mi.hasNext()){
            RtuMessage msg = (RtuMessage) mi.next();
            String contents = msg.getContents();
            contents = contents.substring(contents.indexOf("<")+1, contents.indexOf("/>"));
            
            boolean ondemand 	= contents.equalsIgnoreCase(ONDEMAND);
            
            if (ondemand){
            	String description = 
            		"Getting ondemand registers for MBus device with serailnumber: " + getMbus().getSerialNumber();
            	try {
	            	getLogger().log(Level.INFO, description);
	            	Iterator i = mbus.getRtuType().getRtuRegisterSpecs().iterator();
	                while (i.hasNext()) {
	                	
	                    RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
	                    ObisCode oc = spec.getObisCode();
	                    RtuRegister register = mbus.getRegister( oc );
	                    
	                    if (register != null){
	                    	
	                    	if (oc.getF() == 255){
	                        	RegisterValue rv = iskraMx37x.readRegister(oc);
	                        	rv.setRtuRegisterId(register.getId());
	                        	
	                        	MeterReadingData meterReadingData = new MeterReadingData();
	                        	meterReadingData.add(rv);
	                        	mbus.store(meterReadingData);
	                        	
	        					/*register.add(rv.getQuantity().getAmount(), rv
	        							.getEventTime(), rv.getFromTime(), rv.getToTime(),
	        							rv.getReadTime());*/
	                        }
	                    	
	                    }
	        			else {
	        				String obis = oc.toString();
	        				String msgError = "Register " + obis + " not defined on device";
	        				getLogger().info(msgError);
	        			}
	                }
	            	msg.confirm();
            	}
            	catch (Exception e) {
        			fail(e, msg, description);
            	}
            }
		}
	}
	
	protected void fail(Exception e, RtuMessage msg, String description) throws BusinessException, SQLException {
		msg.setFailed();
		/*Rtu rtu = getMbus();
		CommunicationScheduler scheduler = rtu.getC
		AMRJournalManager amrJournalManager = 
			new AMRJournalManager(rtu, scheduler);
		amrJournalManager.journal(
				new AmrJournalEntry(AmrJournalEntry.DETAIL, description + ": " + e.toString()));
		amrJournalManager.journal(new AmrJournalEntry(AmrJournalEntry.CC_UNEXPECTED_ERROR));
		amrJournalManager.updateRetrials();
		getLogger().severe(e.toString());*/
	}

	public Logger getLogger() {
		return logger;
	}

	public String getRtuType() {
		return rtuType;
	}

}
