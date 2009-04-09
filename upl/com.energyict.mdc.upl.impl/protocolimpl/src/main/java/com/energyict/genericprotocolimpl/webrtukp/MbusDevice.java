package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.genericprotocolimpl.common.RtuMessageConstant;
import com.energyict.genericprotocolimpl.webrtukp.messagehandling.MbusMessageExecutor;
import com.energyict.genericprotocolimpl.webrtukp.messagehandling.MessageExecutor;
import com.energyict.genericprotocolimpl.webrtukp.messagehandling.MessageHandler;
import com.energyict.genericprotocolimpl.webrtukp.profiles.MbusDailyMonthly;
import com.energyict.genericprotocolimpl.webrtukp.profiles.MbusProfile;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageElement;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

/**
 * @author gna
 * Changes:
 * GNA |27012009| Instead of using the nodeAddress as channelnumber we search for the channelnumber by looking at the mbusSerialNumbers
 * GNA |28012009| Added the connect/disconnect messages. There is an option to enter an activationDate but there is no Object description for the
 * 					Mbus disconnect controller yet ...
 * GNA |04022009| Mbus connect/disconnect can be applied with a scheduler. We use 0.x.24.6.0.255 as the ControlScheduler and 0.x.24.7.0.255 as ScriptTable
 * GNA |19022009| Added a message to change to connectMode of the disconnectorObject;
 * 					Changed all messageEntrys in date-form to a UnixTime entry; 
 */

public class MbusDevice implements GenericProtocol, Messaging{
	
	private long mbusAddress	= -1;		// this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
	private int physicalAddress = -1;		// this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
	private int medium = 15;				// value of an unknown medium
	private String customerID;
	private boolean valid;
	
	public Rtu	mbus;
	private WebRTUKP webRtu;
	private Logger logger;
	private ProtocolChannelMap channelMap = null;
	private Unit mbusUnit;
	private MbusObisCodeMapper mocm = null;
	
	public MbusDevice(){
		this.valid = false;
	}
	
	public MbusDevice(String serial, Rtu mbusRtu, Logger logger) throws SQLException, BusinessException, IOException{
		this(0, 0, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
	}
	
	public MbusDevice(String serial, int physicalAddress, Rtu mbusRtu, Logger logger) throws SQLException, BusinessException, IOException{
		this(0, physicalAddress, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
	}
	
	public MbusDevice(long mbusAddress, int phyaddress, String serial, int medium, Rtu mbusRtu, Unit mbusUnit, Logger logger) throws SQLException, BusinessException, IOException{
		this.mbusAddress = mbusAddress;
		this.physicalAddress = phyaddress;
		this.medium = medium;
		this.customerID = serial;
		this.mbusUnit = mbusUnit;
		this.mbus = mbusRtu;
		this.logger = logger;
		this.valid = true;
	}
	
	private void verifySerialNumber() throws IOException{
		String serial;
		String eiSerial = getMbus().getSerialNumber();
		try {
			 serial = getCosemObjectFactory().getGenericRead(getMeterConfig().getMbusSerialNumber(physicalAddress)).getString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the serialnumber of meter " + eiSerial + e);
		}
		if(!eiSerial.equals(serial)){
			throw new IOException("Wrong serialnumber, EIServer settings: " + eiSerial + " - Meter settings: " + serial);
		}
	}
	
	private CosemObjectFactory getCosemObjectFactory(){
		return getWebRTU().getCosemObjectFactory();
	}
	
	private DLMSMeterConfig getMeterConfig(){
		return getWebRTU().getMeterConfig();
	}
	
	public boolean isValid() {
		return valid;
	}

	public String getCustomerID() {
		return this.customerID;
	}
	
	public String getVersion() {
		return "$Date$";
	}

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		CommunicationProfile commProfile = scheduler.getCommunicationProfile();
		
		try {
			// Before reading data, check the serialnumber
			verifySerialNumber();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		
		// import profile
		if(commProfile.getReadDemandValues()){
			getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getMbus().getSerialNumber());
			MbusProfile mp = new MbusProfile(this);
			mp.getProfile(getWebRTU().getMeterConfig().getMbusProfile(getPhysicalAddress()).getObisCode(), commProfile.getReadMeterEvents());
		}
		
		// import daily/monthly
		if(commProfile.getReadMeterReadings()){
			MbusDailyMonthly mdm = new MbusDailyMonthly(this);
			if(getWebRTU().isReadMonthly()){
				getLogger().log(Level.INFO, "Getting Monthly values for meter with serialnumber: " + getMbus().getSerialNumber());
				mdm.getMonthlyProfile(getMeterConfig().getMonthlyProfileObject().getObisCode());
			}
			getLogger().log(Level.INFO, "Getting registers from Mbus meter " + (getPhysicalAddress()+1));
			doReadRegisters();
		}
		
		// send rtuMessages
		if(commProfile.getSendRtuMessage()){
			sendMeterMessages();
		}
	}
	
	private void doReadRegisters() throws IOException{
		Iterator<RtuRegister> it = getMbus().getRegisters().iterator();
		ObisCode oc = null;
		RegisterValue rv;
		RtuRegister rr;
		try {
			while(it.hasNext()){
				rr = it.next();
				oc = rr.getRtuRegisterSpec().getObisCode();
				rv = readRegister(adjustToMbusChannelObisCode(oc));
				if(rv != null){
					rv.setRtuRegisterId(rr.getId());
					
					if(rr.getReadingAt(rv.getReadTime()) == null){
						getWebRTU().getStoreObject().add(rr, rv);
					}
				} else {
					getLogger().log(Level.INFO, "Obiscode " + oc + " is not supported.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
	
	private void sendMeterMessages() throws BusinessException, SQLException {
		MbusMessageExecutor messageExecutor = new MbusMessageExecutor(this);

		Iterator<RtuMessage> it = getMbus().getPendingMessages().iterator();
		RtuMessage rm = null;
		while(it.hasNext()){
			rm = it.next();
			messageExecutor.doMessage(rm);
		}
	}

	private ObisCode adjustToMbusChannelObisCode(ObisCode oc) {
		return new ObisCode(oc.getA(), getPhysicalAddress()+1, oc.getC(), oc.getD(), oc.getE(), oc.getF());
	}

	private RegisterValue readRegister(ObisCode oc) throws IOException{
		if(this.mocm == null){
			this.mocm = new MbusObisCodeMapper(getCosemObjectFactory());
		}
		return mocm.getRegisterValue(oc);
	}
	
	public Rtu getMbus(){
		return this.mbus;
	}
	
	public int getPhysicalAddress(){
		return this.physicalAddress;
	}

	public void addProperties(Properties properties) {
	}

	public List getOptionalKeys() {
		return new ArrayList(0);
	}

	public List getRequiredKeys() {
		return new ArrayList(0);
	}

	public Logger getLogger() {
		return this.logger;
	}

	public void setWebRtu(WebRTUKP webRTUKP) {
		this.webRtu = webRTUKP;
	}
	
	public WebRTUKP getWebRTU(){
		return this.webRtu;
	}

	public List getMessageCategories() {
		List categories = new ArrayList();
		MessageCategorySpec catDisconnect = new MessageCategorySpec("Disconnect Control");
		MessageCategorySpec catMbusSetup = new MessageCategorySpec("Mbus setup");
		
		// Disconnect control related messages
		MessageSpec msgSpec = addConnectControl("Disconnect", RtuMessageConstant.DISCONNECT_LOAD, false);
		catDisconnect.addMessageSpec(msgSpec);
		msgSpec = addConnectControl("Connect", RtuMessageConstant.CONNECT_LOAD, false);
		catDisconnect.addMessageSpec(msgSpec);
		msgSpec = addConnectControlMode("ConnectControl mode", RtuMessageConstant.CONNECT_CONTROL_MODE, false);
		catDisconnect.addMessageSpec(msgSpec);
		
		// Mbus setup related messages
		msgSpec = addNoValueMsg("Decommission", RtuMessageConstant.MBUS_DECOMMISSION, false);
		catMbusSetup.addMessageSpec(msgSpec);
		msgSpec = addEncryptionkeys("Set Encryption keys", RtuMessageConstant.MBUS_ENCRYPTION_KEYS, false);
		catMbusSetup.addMessageSpec(msgSpec);
		
		categories.add(catDisconnect);
		categories.add(catMbusSetup);
		return categories;
	}

	private MessageSpec addConnectControl(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, false);
        tagSpec.add(msgVal);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	
	private MessageSpec addConnectControlMode(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.CONNECT_MODE, true);
        tagSpec.add(msgVal);
        tagSpec.add(msgAttrSpec);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	
	private MessageSpec addNoValueMsg(String keyId, String tagName, boolean advanced){
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
	}
	
	private MessageSpec addEncryptionkeys(String keyId, String tagName, boolean advanced) {
    	MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.MBUS_OPEN_KEY, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.MBUS_TRANSFER_KEY, false);
        tagSpec.add(msgAttrSpec);
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
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

	public static void main(String args[]){
		MbusDevice md = new MbusDevice();
		String string = "123456781AFF";
//		try {
////			System.out.println(md.convertStringToByte(string));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public long getTimeDifference() {
		// TODO Auto-generated method stub
		return 0;
	}
}
