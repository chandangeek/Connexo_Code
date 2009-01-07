package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.webrtukp.profiles.MbusProfile;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
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
	
	public MbusDevice(){
		this.valid = false;
	}
	
	public MbusDevice(long mbusAddress, int phyaddress, String serial, int medium, Rtu mbusRtu, Unit mbusUnit, Logger logger) throws SQLException, BusinessException{
		this.mbusAddress = mbusAddress;
		this.physicalAddress = phyaddress;
		this.medium = medium;
		this.customerID = serial;
		this.mbusUnit = mbusUnit;
		this.mbus = mbusRtu;
		this.logger = logger;
		this.valid = true;
		updateNodeAddress();
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
		// import profile
		if(commProfile.getReadDemandValues()){
			getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getMbus().getSerialNumber());
			MbusProfile mp = new MbusProfile(this);
			mp.getProfile(Constant.mbusProfileObisCode);
		}
		
		// import daily/monthly
		if(commProfile.getReadMeterReadings()){
			
		}
		
		// send rtuMessages
		if(commProfile.getSendRtuMessage()){
			
		}
	}
	
	public Rtu getMbus(){
		return this.mbus;
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
