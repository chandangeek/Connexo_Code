package com.energyict.genericprotocolimpl.webrtukp;

import java.util.logging.Logger;

import com.energyict.cbo.Unit;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

public class MbusDevice {
	
	private long mbusAddress	= -1;		// this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
	private int physicalAddress = -1;		// this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
	private int medium = 15;				// value of an unknown medium
	private String customerID;
	private boolean valid;
	
	public Rtu	mbus;
	private Logger logger;
	private ProtocolChannelMap channelMap = null;
	private Unit mbusUnit;
	
	public MbusDevice(){
		this.valid = false;
	}
	
	public MbusDevice(long mbusAddress, int phyaddress, String serial, int medium, Rtu mbusRtu, Unit mbusUnit, Logger logger){
		this.mbusAddress = mbusAddress;
		this.physicalAddress = phyaddress;
		this.medium = medium;
		this.customerID = serial;
		this.mbusUnit = mbusUnit;
		this.mbus = mbusRtu;
		this.logger = logger;
		this.valid = true;
	}

	public boolean isValid() {
		return valid;
	}

	public String getCustomerID() {
		return this.customerID;
	}

}
