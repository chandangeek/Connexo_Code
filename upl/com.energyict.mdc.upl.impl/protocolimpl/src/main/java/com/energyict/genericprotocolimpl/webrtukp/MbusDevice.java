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
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.webrtukp.profiles.MbusProfile;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

public class MbusDevice implements GenericProtocol{
	
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
	
	public MbusDevice(long mbusAddress, int phyaddress, String serial, int medium, Rtu mbusRtu, Unit mbusUnit, Logger logger) throws SQLException, BusinessException, IOException{
		this.mbusAddress = mbusAddress;
		this.physicalAddress = phyaddress;
		this.medium = medium;
		this.customerID = serial;
		this.mbusUnit = mbusUnit;
		this.mbus = mbusRtu;
		this.logger = logger;
		this.valid = true;
		updatePhysicalAddressWithNodeAddress();
	}
	
	private void verifySerialNumber() throws IOException{
		//TODO complete code
		//TODO resolve NULLPOINTER
		String serial;
		String eiSerial = getMbus().getSerialNumber();
		try {
			 serial = webRtu.getCosemObjectFactory().getGenericRead(webRtu.getMeterConfig().getMbusSerialNumber(physicalAddress)).getString();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the serialnumber of meter " + eiSerial + e);
		}
		if(!eiSerial.equals(serial)){
			throw new IOException("Wrong serialnumber, EIServer settings: " + eiSerial + " - Meter settings: " + serial);
		}
	}

	private void updatePhysicalAddressWithNodeAddress() throws SQLException, BusinessException, IOException{
//		RtuShadow shadow = mbus.getShadow();
//		shadow.setNodeAddress(Integer.toString(this.physicalAddress));
//		try {
//			mbus.update(shadow);
//		} catch (SQLException e) {
//			e.printStackTrace();
//			throw new SQLException("Could not update NodeAddress of Mbus device.");
//		} catch (BusinessException e) {
//			e.printStackTrace();
//			throw new BusinessException("Could not update NodeAddress of Mbus device.");
//		}
		
		this.physicalAddress = Integer.parseInt(getMbus().getNodeAddress()) - 1;
		if(this.physicalAddress < 0 || this.physicalAddress > 3)
			throw new IOException("NodeAddress must be between 1 - 4, here value is " + (this.physicalAddress+1));
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
			mp.getProfile(Constant.mbusProfileObisCode);
		}
		
		// import daily/monthly
		if(commProfile.getReadMeterReadings()){
			getLogger().log(Level.INFO, "Getting registers from Mbus meter " + (getPhysicalAddress()+1));
			doReadRegisters();
		}
		
		// send rtuMessages
		if(commProfile.getSendRtuMessage()){
			
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
				rv = readRegister(adjustToMbusChannelObisCod(oc));
				rv.setRtuRegisterId(rr.getId());
				
				if(rr.getReadingAt(rv.getReadTime()) == null){
					getWebRTU().getStoreObject().add(rr, rv);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}
	
	private ObisCode adjustToMbusChannelObisCod(ObisCode oc) {
		return new ObisCode(oc.getA(), getPhysicalAddress()+1, oc.getC(), oc.getD(), oc.getE(), oc.getF());
	}

	private RegisterValue readRegister(ObisCode oc) throws IOException{
		if(this.mocm == null){
			this.mocm = new MbusObisCodeMapper(getWebRTU().getCosemObjectFactory());
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

}
