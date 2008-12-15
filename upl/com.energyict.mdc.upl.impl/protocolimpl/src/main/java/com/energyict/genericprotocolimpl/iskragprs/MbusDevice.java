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

import javax.xml.rpc.ServiceException;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.common.AMRJournalManager;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
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
public class MbusDevice implements Messaging, GenericProtocol{
	
	private int mbusAddress	= -1;
	private int physicalAddress = -1;
	private int medium = 15;
	
	private String customerID;
	private String rtuType;
	private Unit mbusUnit;
	
	public Rtu	mbus;
	private Logger logger;
	
	private IskraMx37x iskra;
	
	private static String ONDEMAND = "ONDEMAND";

	/**
	 * 
	 */
	public MbusDevice() {
	}

	public MbusDevice(int address, String customerID, Rtu rtu, Logger logger) throws InvalidPropertyException, MissingPropertyException {
		this.mbusAddress = address;
		this.customerID	= customerID;
		this.mbus = rtu;
		this.logger = logger;
		setProperties(mbus.getProperties());
	}

	public MbusDevice(int mbusAddress, int phyAddress, String serial, int mbusMedium, Rtu rtu, Unit unit, Logger logger) {
		this.mbusAddress = mbusAddress;
		this.physicalAddress = phyAddress;
		this.customerID = serial;
		this.medium = mbusMedium;
		this.mbus = rtu;
		this.mbusUnit = unit;
		this.logger = logger;
	}
	

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		CommunicationProfile commProfile = scheduler.getCommunicationProfile();
		// import profile
		if(commProfile.getReadDemandValues()){
			MbusProfile mp = new MbusProfile(this);
			mp.getProfile(iskra.getMbusLoadProfile());
		}
		
		// import Daily/Monthly registers
		if(commProfile.getReadMeterReadings()){
			MbusDailyMonthly mdm = new MbusDailyMonthly(this);
			mdm.getDailyValues(iskra.getDailyLoadProfile());
			mdm.getMonthlyValues(iskra.getMonthlyLoadProfile());
		}
		
		// send RtuMessages
		if(commProfile.getSendRtuMessage()){
			sendMeterMessages();
		}
	}

	public void addProperties(Properties properties) {
		try {
			setProperties(properties);
		} catch (InvalidPropertyException e) {
			e.printStackTrace();
		} catch (MissingPropertyException e) {
			e.printStackTrace();
		}
	}

	public String getVersion() {
		return getProtocolVersion();
	}

	public String getProtocolVersion() {
		return "$Date$";
	}

	public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
		rtuType = properties.getProperty("RtuType","mbus");
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

	public void sendMeterMessages() throws IOException, BusinessException, SQLException {
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
	                        	RegisterValue rv = iskra.readRegister(oc);
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
		Rtu concentrator = getMbus().getGateway();
		if (concentrator != null) {
			List schedulers = concentrator.getCommunicationSchedulers();
			if (schedulers.size() > 0) {
				CommunicationScheduler scheduler = (CommunicationScheduler) schedulers.get(0);
				if (scheduler != null) {
					AMRJournalManager amrJournalManager = 
						new AMRJournalManager(concentrator, scheduler);
					amrJournalManager.journal(
							new AmrJournalEntry(AmrJournalEntry.DETAIL, description + ": " + e.toString()));
					amrJournalManager.journal(new AmrJournalEntry(AmrJournalEntry.CC_UNEXPECTED_ERROR));
					amrJournalManager.updateRetrials();
				}
			}
		}
		getLogger().severe(e.toString());
	}
	
	public boolean isIskraMbusObisCode(ObisCode oc){
		if ((oc.getA() == 0) && (oc.getC() == 128) && (oc.getD() == 50)){
			if((oc.getB() >= 1) && (oc.getB() <= 4)){
				if(((oc.getE() >= 0) && (oc.getE() <= 3)) ||
						((oc.getE() >= 20) && (oc.getE() <= 25)) ||
						((oc.getE() >= 30) && (oc.getE() <= 33))){
					return true;
				}
			}
		}
		
		return false;
	}

	public Logger getLogger() {
		return logger;
	}

	public String getRtuType() {
		return rtuType;
	}

	public int getMbusAddress() {
		return mbusAddress;
	}

	public void setMbusAddress(int mbusAddress) {
		this.mbusAddress = mbusAddress;
	}

	public int getPhysicalAddress() {
		return physicalAddress;
	}

	public void setPhysicalAddress(int physicalAddress) {
		this.physicalAddress = physicalAddress;
	}

	public int getMedium() {
		return medium;
	}

	public void setMedium(int medium) {
		this.medium = medium;
	}

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public Unit getMbusUnit() {
		return mbusUnit;
	}

	public void setMbusUnit(Unit mbusUnit) {
		this.mbusUnit = mbusUnit;
	}

	public void setMbus(Rtu mbus) {
		this.mbus = mbus;
	}

	public void setIskraDevice(IskraMx37x iskraMx37x) {
		this.iskra = iskraMx37x;
	}
	
	public IskraMx37x getIskraDevice(){
		return this.iskra;
	}
}
