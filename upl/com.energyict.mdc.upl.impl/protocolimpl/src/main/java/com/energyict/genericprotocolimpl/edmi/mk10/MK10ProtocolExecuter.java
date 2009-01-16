/**
 * MK10ProtocolExecuter.java
 * 
 * Created on 16-jan-2009, 09:16:11 by jme
 * 
 */
package com.energyict.genericprotocolimpl.edmi.mk10;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.energyict.dialer.core.Link;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.edmi.mk10.MK10;

/**
 * @author jme
 *
 */
public class MK10ProtocolExecuter {

	private static final int DEBUG 	= 0;

	private Rtu meter				= null;
	private MK10Push mk10Push		= null;
	private MK10 mk10Protocol		= new MK10();
	private Properties properties	= new Properties();

	private int securityLevel;	// 0: No Authentication - 1: Low Level - 2: High Level
	private int timeout;
	private int forceDelay;
	private int retries;
	private int extendedLogging;
	private String password;
	private String serialNumber;

	
	/*
	 * Constructors
	 */

	public MK10ProtocolExecuter(MK10Push mk10Push) {
		this.mk10Push = mk10Push;
	}
	
	/*
	 * Private getters, setters and methods
	 */

	private MK10Push getMk10Push() {
		return mk10Push;
	}
	
	private Logger getLogger() {
		return getMk10Push().getLogger();
	}
	
	private Link getLink() {
		return getMk10Push().getLink();
	}
	
	private void readMeterProperties() {
		this.properties.putAll(getMeter().getProperties());
	}
	
	public List getOptionalKeys() {
		ArrayList list = new ArrayList();
		list.addAll(getMk10Protocol().getOptionalKeys());
		return list;
	}

	public List getRequiredKeys() {
		ArrayList list = new ArrayList();
		list.addAll(getMk10Protocol().getRequiredKeys());
		return list;
	}
		
	/*
	 * Public methods
	 */

	public void doMeterProtocol() throws InvalidPropertyException, MissingPropertyException {
		// TODO Auto-generated method stub
		readMeterProperties();
		getMk10Protocol().setProperties(this.properties);
		
		properties.list(System.out);
		
	}
	
	/*
	 * Public getters and setters
	 */

	public Rtu getMeter() {
		return meter;
	}
	
	public void setMeter(Rtu meter) {
		this.meter = meter;
	}
	
	public MK10 getMk10Protocol() {
		return mk10Protocol;
	}
	
	public void addProperties(Properties properties) {
		this.properties.putAll(properties);
	}

}
