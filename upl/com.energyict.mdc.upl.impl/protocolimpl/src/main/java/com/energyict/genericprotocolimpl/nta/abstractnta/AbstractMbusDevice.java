package com.energyict.genericprotocolimpl.nta.abstractnta;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.nta.messagehandling.MbusMessageExecutor;
import com.energyict.genericprotocolimpl.nta.messagehandling.MbusMessages;
import com.energyict.genericprotocolimpl.nta.profiles.MbusDailyMonthlyProfile;
import com.energyict.genericprotocolimpl.nta.profiles.MbusEventProfile;
import com.energyict.genericprotocolimpl.nta.profiles.MbusProfile;
import com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers.MbusObisCodeMapper;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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

public abstract class AbstractMbusDevice extends MbusMessages implements GenericProtocol{

     /**
     * Extra protocol settings for a <b>subclassed NTA protocol</b>
     * Can be used to override a default custom property or add specific custom properties.
      * @param properties
      */
    protected abstract void doValidateProperties(Properties properties) throws InvalidPropertyException;

    /**
     * Add extra optional keys
     * @return a List<String> with optional key parameters, return null if no additionals are required
     */
    protected abstract List<String> doGetOptionalKeys();

    /**
     * Add extra required keys
     * @return a List<String> with required key parameters, return null if no additionals are required
     */
    protected abstract List<String> doGetRequiredKeys();    
	

    /**
     * Getter for the {@link MbusObisCodeProvider}
     * @return the {@link MbusObisCodeProvider}
     */
    public abstract MbusObisCodeProvider getObiscodeProvider() throws IOException;

	private long mbusAddress	= -1;		// this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
	private int physicalAddress = -1;		// this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
	private int medium = 15;				// value of an unknown medium
	private String customerID;
	private boolean valid;
	
	public Rtu	mbus;
	public CommunicationProfile commProfile;
	private AbstractNTAProtocol webRtu;
	private Logger logger;
	private ProtocolChannelMap channelMap = null;
	private Unit mbusUnit;
	private MbusObisCodeMapper mocm = null;
    private Properties properties;
	
	
	public AbstractMbusDevice(){
		this.valid = false;
	}
	
	public AbstractMbusDevice(String serial, Rtu mbusRtu, Logger logger){
		this(0, 0, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
	}
	
	public AbstractMbusDevice(String serial, int physicalAddress, Rtu mbusRtu, Logger logger){
		this(0, physicalAddress, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
	}
	
	public AbstractMbusDevice(long mbusAddress, int phyaddress, String serial, int medium, Rtu mbusRtu, Unit mbusUnit, Logger logger){
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
	
	protected DLMSMeterConfig getMeterConfig(){
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
		this.commProfile = scheduler.getCommunicationProfile();

        validateProperties();
		
//		try {
			// Before reading data, check the serialnumber
//			verifySerialNumber(); //TODO set back!
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new IOException(e.getMessage());
//		}
		
		// import profile
		if(commProfile.getReadDemandValues()){
			getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getMbus().getSerialNumber());
			MbusProfile mp = new MbusProfile(this);
			mp.getProfile(getWebRTU().getMeterConfig().getMbusProfile(getPhysicalAddress()).getObisCode());
		}
		
		if(commProfile.getReadMeterEvents()){
			getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + getMbus().getSerialNumber());
			MbusEventProfile mep = new MbusEventProfile(this);
			mep.getEvents();
		}
		
		// import daily/monthly
		if(commProfile.getReadMeterReadings()){
			MbusDailyMonthlyProfile mdm = new MbusDailyMonthlyProfile(this);
			
			if(getWebRTU().isReadDaily()){
				getLogger().log(Level.INFO, "Getting Daily values for meter with serialnumber: " + getMbus().getSerialNumber());
//				if(commProfile.getReadDemandValues()){
//					mdm.getDailyProfile((ProfileData)(getWebRTU().getStoreObject().getMap().get(getMbus())),
//							getWebRTU().getMeterConfig().getMbusProfile(getPhysicalAddress()).getObisCode());
//				} else {
//					mdm.getDailyProfile(getWebRTU().getMeterConfig().getMbusProfile(getPhysicalAddress()).getObisCode());
//				}
				mdm.getDailyProfile(getMeterConfig().getDailyProfileObject().getObisCode());
			}
			
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
	
	protected void doReadRegisters() throws IOException{
		Iterator<RtuRegister> it = getMbus().getRegisters().iterator();
		List groups = this.commProfile.getRtuRegisterGroups();
		ObisCode oc = null;
		RegisterValue rv;
		RtuRegister rr;
		while(it.hasNext()){
			try {
				rr = it.next();
				if (getWebRTU().isInRegisterGroup(groups, rr)) {
					oc = rr.getRtuRegisterSpec().getObisCode();
					try{
						rv = readRegister(adjustToMbusChannelObisCode(oc));
						if(rv != null){
							rv.setRtuRegisterId(rr.getId());
							
							if(rr.getReadingAt(rv.getReadTime()) == null){
								getWebRTU().getStoreObject().add(rr, rv);
							}
						} else {
							getLogger().log(Level.INFO, "Obiscode " + oc + " is not supported.");
						}
						
					} catch (NoSuchRegisterException e) {
						getLogger().log(Level.INFO, "ObisCode " + oc + " is not supported by the meter.");
					}
				}
			} catch (IOException e) {
				getLogger().log(Level.INFO, "Reading register with obisCode " + oc + " FAILED.");
			}
		}
	}
	
	protected void sendMeterMessages() throws BusinessException, SQLException {
		MbusMessageExecutor messageExecutor = new MbusMessageExecutor(this);

		Iterator<RtuMessage> it = getMbus().getPendingMessages().iterator();
		RtuMessage rm = null;
		while(it.hasNext()){
			rm = it.next();
			messageExecutor.doMessage(rm);
		}
	}

	protected ObisCode adjustToMbusChannelObisCode(ObisCode oc) {
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
        this.properties = properties;
	}

	public List getOptionalKeys() {
		List<String> result = new ArrayList(30);
   		List<String> protocolKeys = doGetOptionalKeys();
        if (protocolKeys != null) {
            result.addAll(protocolKeys);
        }
        return result;
	}

	public List getRequiredKeys() {
		List<String> result = new ArrayList(30);
   		List<String> protocolKeys = doGetRequiredKeys();
        if (protocolKeys != null) {
            result.addAll(protocolKeys);
        }
        return result;
	}

	public Logger getLogger() {
		return this.logger;
	}

	public void setWebRtu(AbstractNTAProtocol webRTU) {
		this.webRtu = webRTU;
	}
	
	public AbstractNTAProtocol getWebRTU(){
		return this.webRtu;
	}

    /**
     * {@inheritDoc}
     */
	public void validateProperties() throws MissingPropertyException, InvalidPropertyException {
		Iterator<String> iterator = getRequiredKeys().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (getProperties().getProperty(key) == null) {
				throw new MissingPropertyException(key + " key missing");
			}
		}
        doValidateProperties(getProperties());
	}

    /**
     * Getter for the properties. (fetch them ones from the device)
     * @return the properties
     */
    private Properties getProperties(){
        if(this.properties == null){
            this.properties = getMbus().getProperties();
        }
        return this.properties;
    }

	public long getTimeDifference() {
		return 0;
	}
}
