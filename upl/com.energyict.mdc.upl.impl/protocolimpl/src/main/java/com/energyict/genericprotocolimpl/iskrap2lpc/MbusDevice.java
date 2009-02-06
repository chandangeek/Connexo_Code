/**
 * 
 */
package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.UnsignedInt;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.genericprotocolimpl.common.RtuMessageConstant;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
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
import com.energyict.protocolimpl.base.ProtocolChannel;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

/**
 * @author gna 
 *
 */
public class MbusDevice implements Messaging, GenericProtocol{
	
	private long mbusAddress	= -1;		// this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
	private int physicalAddress = -1;		// this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
	private int medium = 15;				// value of an unknown medium
	
	private String customerID;
	
	public Rtu	mbus;
	private Logger logger;
	private ProtocolChannelMap channelMap = null;
	private Unit mbusUnit;
	private MeterReadTransaction mrt = null;
	private XmlHandler dataHandler;

	/**
	 * 
	 */
	public MbusDevice() {
	}

	public MbusDevice(long mbusAddress, int phyAddress, String serial, int mbusMedium, Rtu rtu, Unit unit, Logger logger) throws SQLException, BusinessException {
		this.mbusAddress = mbusAddress;
		this.physicalAddress = phyAddress;
		this.customerID = serial;
		this.medium = mbusMedium;
		this.mbus = rtu;
		this.mbusUnit = unit;
		this.logger = logger;
		updateNodeAddress();
	}

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		CommunicationProfile commProfile = scheduler.getCommunicationProfile();
		try {
			// import profile
			if(commProfile.getReadDemandValues()){
				dataHandler = initDatahandler();
				dataHandler.setChannelUnit(getMbusUnit());
				importProfiles();
			}
			
			// import Daily/Monthly registers
			if(commProfile.getReadMeterReadings()){
				dataHandler = initDatahandler();
				dataHandler.setDailyMonthlyProfile(true);
				dataHandler.setChannelUnit(getMbusUnit());
				importDailyMonthly();
				dataHandler.setDailyMonthlyProfile(false);
			}
			
			// send RtuMessages
			if(commProfile.getSendRtuMessage()){
				dataHandler = initDatahandler();
				if ( getRtu().getPendingMessages().size() != 0 ){
    				sendMeterMessages(mrt.getMeter());
    			}
			}
		} catch (ServiceException e) {
			e.printStackTrace();
			throw new BusinessException(e);
		}
	}
	
	private XmlHandler initDatahandler() throws InvalidPropertyException, BusinessException{
		return new XmlHandler(getLogger(), getChannelMap());
	}
	
	private void importProfiles() throws BusinessException, RemoteException, ServiceException, IOException{
		String profile = "";
		String xml = "";
		String from = "";
		String to = Constant.getInstance().format(new Date());
		String register = "";
		
        Channel chn;
        for( int i = 0; i < dataHandler.getChannelMap().getNrOfProtocolChannels(); i ++ ) {
        
            ProtocolChannel pc = dataHandler.getChannelMap().getProtocolChannel(i);
            xml = "";
            register = modifyObisCodeToChannelNumber(pc.getRegister());
            chn = mrt.getMeterChannelWithIndex(getRtu(), i+1);
            if(chn != null){
            	from = Constant.getInstance().format( mrt.getLastChannelReading(chn) );
            	if(!pc.containsDailyValues() && !pc.containsMonthlyValues()){
            		
            		if(mrt.useParameters()){
            			profile = mrt.getConcentrator().getLpMbus();
            		} else {
            			
            			if(chn.getIntervalInSeconds() == mrt.loadProfilePeriod1){
            				profile = "99.1.0";
            			} else if (chn.getIntervalInSeconds() == mrt.loadProfilePeriod2){
            				profile = "99.2.0";
            			} else {
            				getLogger().log(Level.SEVERE, "Interval didn't match for channel \"" + chn + "\" - ProfileInterval EIServer: " + chn.getIntervalInSeconds());
            				throw new BusinessException("Interval didn't match");
            			}
            			
            		}
            		
            		dataHandler.setProfileChannelIndex(i);
                	if(mrt.TESTING){
//                		FileReader inFile = new FileReader(Utils.class.getResource(getProfileTestName()[i]).getFile());
//                		xml = getConcentrator().readWithStringBuffer(inFile);
                	} else{
                		getLogger().log(Level.INFO, "Retrieving profiledata from " + from + " to " + to);
                		xml = mrt.getConnection().getMeterProfile(mrt.getMeter().getSerialNumber(), profile, register, from, to);
                	}
            	}
            }
            if(!xml.equalsIgnoreCase("")){
            	dataHandler.setChannelIndex( i );
            	mrt.getConcentrator().importData(xml, dataHandler);
            }
        }
        
        getLogger().log(Level.INFO, "Done reading PROFILE.");
        
        mrt.getStoreObjects().add(getRtu(), dataHandler.getProfileData());
	}
	
	private void importDailyMonthly() throws RemoteException, ServiceException, IOException, BusinessException{
		String daily = "";
		String monthly = "";
		String xml = "";
		String register = "";
		String from = "";
		String to = Constant.getInstance().format(new Date());
		
		if (mrt.useParameters()) {
			
			daily = mrt.getConcentrator().getLpDaily();
			monthly = mrt.getConcentrator().getLpMonthly();
			
		} else {
			
			if ( mrt.loadProfilePeriod2 == 86400 ){ 
				daily = "99.2.0";
			}else
				daily = null;
			
			if ( (mrt.billingReadTime.getDayOfMonth().intValue() == 1) && (mrt.billingReadTime.getHour().intValue() == 0) && (mrt.billingReadTime.getYear().intValue() == 65535) && (mrt.billingReadTime.getMonth().intValue() == 255) ){
				monthly = "98.1.0";
				if (daily == null) daily = "98.2.0";
			}else{
				monthly = "98.2.0";
				if (daily == null) daily = "98.1.0";
			}
			
		}
		
		Channel chn;
		ProtocolChannel pc;
		dataHandler.setChannelIndex(0);		// we will add channel per channel
		for(int i = 0; i < dataHandler.getChannelMap().getNrOfProtocolChannels(); i++){
			pc = dataHandler.getChannelMap().getProtocolChannel(i);
			xml = "";
			register = modifyObisCodeToChannelNumber(pc.getRegister());
			chn = mrt.getMeterChannelWithIndex(getRtu(), i+1);
			dataHandler.setProfileChannelIndex(i);
			if(chn != null){
				from = Constant.getInstance().format( mrt.getLastChannelReading(chn) );
				if(pc.containsDailyValues()){
					if(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS){
						getLogger().log(Level.INFO, "Reading Daily values with registername: " + pc.getRegister() + " from " + from + " to " + to);
						if(mrt.TESTING){
//		            		FileReader inFile = new FileReader(Utils.class.getResource(mrt.getBillingDaily()).getFile());
//							FileReader inFile = new FileReader(Utils.class.getResource("/offlineFiles/iskrap2lpc/nullpointerstuff.xml").getFile());
//		            		xml = getConcentrator().readWithStringBuffer(inFile);
						} else {
							xml = mrt.getConnection().getMeterProfile(mrt.getMeter().getSerialNumber(), daily, register, from, to);
						}
					}
					else
						throw new IOException("Channelconfiguration of channel \"" + chn + "\" is different from the channelMap");
				}
				else if(pc.containsMonthlyValues()){
					if(chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS){
						getLogger().log(Level.INFO, "Reading Monthly values with registername: " + pc.getRegister()  + " from " + from + " to " + to);
						if(mrt.TESTING){
//		            		FileReader inFile = new FileReader(Utils.class.getResource(mrt.getBillingMonthly()).getFile());
//		            		xml = getConcentrator().readWithStringBuffer(inFile);
						} else {
							xml = mrt.getConnection().getMeterProfile(mrt.getMeter().getSerialNumber(), monthly, register, from, to);
						}
					}
					else
						throw new IOException("Channelconfiguration of channel \"" + chn + "\" is different from the channelMap");
				}
			} else
				throw new IOException("Channel out of bound exception: no channel with profileIndex " + i+1 + " is configured on the meter.");

			if(!xml.equalsIgnoreCase("")){
				
				mrt.getConcentrator().importData(xml, dataHandler);
				ProfileData pd = dataHandler.getDailyMonthlyProfile();
				pd = mrt.sortOutProfileData(pd, pc);
				mrt.getStoreObjects().add(chn, pd);
				dataHandler.clearDailyMonthlyProfile();
			}
		}
	}
	
	private void sendMeterMessages(Rtu eMeter) throws BusinessException, SQLException{
		Iterator messageIt = getRtu().getPendingMessages().iterator();
		if(messageIt.hasNext())
			getLogger().log(Level.INFO, "Handling MESSAGES from meter with serialnumber " + getCustomerID());
		
		while(messageIt.hasNext()){
			RtuMessage msg = (RtuMessage) messageIt.next();
			String contents = msg.getContents();
			
			boolean doReadOnDemand = (contents.toLowerCase()).indexOf(RtuMessageConstant.READ_ON_DEMAND.toLowerCase()) != -1;
			boolean doDisconnect = contents.toLowerCase().indexOf(RtuMessageConstant.DISCONNECT_LOAD.toLowerCase()) != -1;
			boolean doConnect       = (contents.toLowerCase().indexOf(RtuMessageConstant.CONNECT_LOAD.toLowerCase()) != -1) && !doDisconnect;
			
			try {
				if(doReadOnDemand){
					dataHandler = initDatahandler();
					List registerList = new ArrayList();
					Iterator it = getRtu().getRtuType().getRtuRegisterSpecs().iterator();
					while(it.hasNext()){
						RtuRegisterSpec spec = (RtuRegisterSpec) it.next();
						ObisCode oc = ObisCode.fromString(modifyObisCodeToChannelNumber(spec.getRegisterMapping().getObisCode().toString()));
						if(oc.getF() == 255){
							if((oc.getA() == 0) && ((oc.getB() > 0) && (oc.getB() <= 4)) && (oc.getC() == 128) && (oc.getD() == 50) && (oc.getE() == 0)){
								registerList.add(oc.toString());
							} else {
								getLogger().log(Level.INFO, "Register with obisCode " + oc.toString() + " is not supported.");
							}
							
							dataHandler.checkOnDemands(true);
							dataHandler.setProfileDuration(-1);
						}
					}
					String registers[] = (String[])registerList.toArray(new String[0]);
					String r = mrt.getConnection().getMeterOnDemandResultsList(eMeter.toString(), registers);
					
					mrt.getConcentrator().importData(r, dataHandler);
					handleRegisters();
					
					msg.confirm();
					getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
					
				} else if(doDisconnect){
					
					// BASIC Concept: I guess ... 
					// Set the valveControl to a specific mbusAddress, and then set the valve state to open or closed.
					
					//TODO test this, physicalAddress can also be mbusAddress
					String[] times = mrt.prepareCosemGetRequest();
					ObisCode oc = Constant.valveControl;
					ObisCode instance = ObisCode.fromString(oc.getA()+"."+getPhysicalAddress()+"."+oc.getC()+
							"."+oc.getD()+"."+oc.getE()+"."+oc.getF());
					byte[] b = new byte[]{DLMSCOSEMGlobals.TYPEDESC_UNSIGNED, 0x1};
					mrt.getConnection().cosemSetRequest(eMeter.toString(), times[0], times[1], instance.toString(), new UnsignedInt(1), new UnsignedInt(2), b);
//					byte[] state = mrt.getConnection().cosemGetRequest(eMeter.toString(), times[0], times[1], instance.toString(), new UnsignedInt(1), new UnsignedInt(2));

					// TODO Testing
					times = mrt.prepareCosemGetRequest();
					oc = Constant.valveState;
					instance = ObisCode.fromString(oc.getA()+"."+getPhysicalAddress()+"."+oc.getC()+
							"."+oc.getD()+"."+oc.getE()+"."+oc.getF());
					b = new byte[]{DLMSCOSEMGlobals.TYPEDESC_UNSIGNED, 0x00};
					mrt.getConnection().cosemSetRequest(eMeter.toString(), times[0], times[1], instance.toString(), new UnsignedInt(1), new UnsignedInt(2), b);
					byte[] state = mrt.getConnection().cosemGetRequest(eMeter.toString(), times[0], times[1], instance.toString(), new UnsignedInt(1), new UnsignedInt(2));
					System.out.println(state[0] + " " + state[1]);
					
					msg.confirm();
					getLogger().log(Level.INFO, "Current message" + contents + " has finished.");
					
				} else if(doConnect){
					//TODO test this, physicalAddress can also be mbusAddress
					String[] times = mrt.prepareCosemGetRequest();
					ObisCode oc = Constant.valveState;
					ObisCode instance = ObisCode.fromString(oc.getA()+"."+getPhysicalAddress()+"."+oc.getC()+
							"."+oc.getD()+"."+oc.getE()+"."+oc.getF());
					byte[] b = new byte[]{DLMSCOSEMGlobals.TYPEDESC_BOOLEAN, 0x01};
					mrt.getConnection().cosemSetRequest(eMeter.toString(), times[0], times[1], instance.toString(), new UnsignedInt(1), new UnsignedInt(2), b);
					
					// TODO Testing
					times = mrt.prepareCosemGetRequest();
					oc = Constant.valveState;
					instance = ObisCode.fromString(oc.getA()+"."+getPhysicalAddress()+"."+oc.getC()+
							"."+oc.getD()+"."+oc.getE()+"."+oc.getF());
					byte[] state = mrt.getConnection().cosemGetRequest(eMeter.toString(), times[0], times[1], instance.toString(), new UnsignedInt(1), new UnsignedInt(2));
					System.out.println(state[0] + " " + state[1]);
					
					msg.confirm();
					getLogger().log(Level.INFO, "Current message" + contents + " has finished.");
				} else {
					msg.setFailed();
				}
			} catch (BusinessException e) {
				/** should go to the next message */
				getLogger().log(Level.INFO, "Message " + contents + " has failed.");
				e.printStackTrace();
				msg.setFailed();
			} catch (SQLException e) {
				/** should go to the next message */
				getLogger().log(Level.INFO, "Message " + contents + " has failed.");
				e.printStackTrace();
				msg.setFailed();
				
	        	/** Close the connection after an SQL exception, connection will startup again if requested */
	        	Environment.getDefault().closeConnection();
				
			} catch (InvalidPropertyException e) {
				/** should go to the next message */
				getLogger().log(Level.INFO, "Message " + contents + " has failed. Failure is caused by an invalid property.");
				e.printStackTrace();
				msg.setFailed();
			} catch (RemoteException e) {
				/** should go to the next message */
				getLogger().log(Level.INFO, "Message " + contents + " has failed.");
				e.printStackTrace();
				msg.setFailed();
			} catch (ServiceException e) {
				/** should go to the next message */
				getLogger().log(Level.INFO, "Message " + contents + " has failed.");
				e.printStackTrace();
				msg.setFailed();
			} catch (IOException e) {
				/** should go to the next message */
				getLogger().log(Level.INFO, "Message " + contents + " has failed.");
				e.printStackTrace();
				msg.setFailed();
			}
		}
	}
	
	private void handleRegisters(){
		Iterator it = dataHandler.getMeterReadingData().getRegisterValues().iterator();
		while(it.hasNext()){
			RegisterValue registerValue = (RegisterValue)it.next();
			RtuRegister register = getRtu().getRegister(revertModifyObisCodeToChannelNumber(registerValue.getObisCode()));
			
			if(register != null){
				if(register.getReadingAt(registerValue.getReadTime()) == null){
					mrt.getStoreObjects().add(register, registerValue);
				}
			}
            else {
                String obis = registerValue.getObisCode().toString();
                String msg = "Register " + obis + " not defined on device";
                getLogger().info( msg );
            }
		}
	}
	
	private String modifyObisCodeToChannelNumber(String register){
		String str = register.substring(0, 2)+(getPhysicalAddress()+1)+register.substring(3);
		return str;
	}
	
	private ObisCode revertModifyObisCodeToChannelNumber(ObisCode oc){
		String chObisCode = oc.toString();
		chObisCode = chObisCode.substring(0 ,2)+"1"+chObisCode.substring(3);
		return ObisCode.fromString(chObisCode);
	}
	
	public List getMessageCategories() {
        List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec("BasicMessages");
        
        MessageSpec msgSpec = addBasicMsg("ReadOnDemand", RtuMessageConstant.READ_ON_DEMAND, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Disconnect meter", RtuMessageConstant.DISCONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Connect meter", RtuMessageConstant.CONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        
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
	
	public int getMbusMedium(){
		return medium;
	}
	
	public int getPhysicalAddress(){
		return this.physicalAddress;
	}
	
	public long getMbusAddress(){
		return this.mbusAddress;
	}
	
	public String getCustomerID(){
		return this.customerID;
	}

	public void addProperties(Properties properties) {
		// TODO Auto-generated method stub
		
	}

	public String getVersion() {
		return "$Date$";
	}

	public List getOptionalKeys() {
		ArrayList result = new ArrayList();
		return result;
	}

	public List getRequiredKeys() {
		ArrayList result = new ArrayList();
		result.add(Constant.CHANNEL_MAP);
		return result;
	}

	public void setMeterReadTransaction(MeterReadTransaction meterReadTransaction) {
		this.mrt = meterReadTransaction;
	}
	
	private void updateNodeAddress() throws SQLException, BusinessException{
		RtuShadow shadow = mbus.getShadow();
		shadow.setNodeAddress(Integer.toString(this.physicalAddress));
		try {
			mbus.update(shadow);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("Could not update NodeAddress of Mbus device.");
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException("Could not update NodeAddress of Mbus device.");
		}
	}
}
