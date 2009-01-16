/**
 * MK10ProtocolExecuter.java
 * 
 * Created on 16-jan-2009, 09:16:11 by jme
 * 
 */
package com.energyict.genericprotocolimpl.edmi.mk10;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.edmi.mk10.streamfilters.MK10PushInputStream;
import com.energyict.genericprotocolimpl.edmi.mk10.streamfilters.MK10PushOutputStream;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
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
	
	/*
	 * Constructors
	 */

	public MK10ProtocolExecuter(MK10Push mk10Push) {
		this.mk10Push = mk10Push;
	}
	
	/*
	 * Private getters, setters and methods
	 */
	
    private MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }

	private MK10Push getMk10Push() {
		return mk10Push;
	}
	
	private Logger getLogger() {
		return getMk10Push().getLogger();
	}
	
	private Link getLink() {
		return getMk10Push().getLink();
	}
	
	private void log(Level level, String msg){
		getLogger().log(level, msg + "\n");
	}

	private CommunicationProfile getCommunicationProfile() throws BusinessException {
		List schedulerList = getMeter().getCommunicationSchedulers();
		if (schedulerList.size() != 1) {
			throw new BusinessException(
					"Rtu MUST have one and only one CommunicationScheduler when using push protocol. " +
					"CommunicationSchedulers found for Rtu: " + schedulerList.size()
					);
		}
		
		CommunicationScheduler cs = (CommunicationScheduler) schedulerList.get(0);
		CommunicationProfile cp =  mw().getCommunicationProfileFactory().find(cs.getCommunicationProfileId());
		if (cp == null) throw new BusinessException("No CommunicationProfile found for Rtu.");
		
		return cp;
	}
	
	private void readMeterProperties() {
		this.properties.putAll(getMeter().getProtocol().getProperties());
		this.properties.putAll(getMeter().getProperties());
				
		this.properties.put(MeterProtocol.SERIALNUMBER, getMeter().getSerialNumber());
		this.properties.put(MeterProtocol.NODEID, getMeter().getNodeAddress());
		this.properties.put(MeterProtocol.ADDRESS, getMeter().getDeviceId());
		this.properties.put(MeterProtocol.PASSWORD, getMeter().getPassword());
		
	}
		
	private void verifyAndWriteClock() throws IOException, BusinessException{
		try {
			Date meterTime = getMk10Protocol().getTime();
			Date now = Calendar.getInstance(getMeter().getTimeZone()).getTime();
			
			long diff = Math.abs(now.getTime()-meterTime.getTime());
			
			log(Level.INFO, "Difference between metertime(" + meterTime + ") and systemtime(" + now + ") is " + diff + "ms.");
			if(getCommunicationProfile().getWriteClock()){
				if( (diff < getCommunicationProfile().getMaximumClockDifference()*1000) && (diff > getCommunicationProfile().getMinimumClockDifference()*1000) ){
					log(Level.INFO, "Metertime will be set to systemtime: " + now);
					getMk10Protocol().setTime();
				}
			} else {
				log(Level.INFO, "WriteClock is disabled, metertime will not be set.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
	}

	/*
	 * Public methods
	 */

	public void doMeterProtocol() throws IOException, BusinessException {

		// Let the MK10 protocol know that it's been used as generic Push protocol
		getMk10Protocol().setPushProtocol(true);
		
		// Read the properties from the Rtu and the Protocol in EIServer and apply them to the MK10Protocol.
		readMeterProperties();
		getMk10Protocol().setProperties(this.properties);
		if (DEBUG >= 2) properties.list(System.out);

		// Create new streams and pass them to the MK10 protocol
		getMk10Protocol().init(
				new MK10PushInputStream(getLink().getInputStream()), 
				new MK10PushOutputStream(getLink().getOutputStream()),	 
				getMeter().getTimeZone(), 
				getLogger()
		);
		
		// Try to connect to the meter ...
		getMk10Protocol().connect();
		
		// Try to read or set the clock in the meter
		verifyAndWriteClock();
		
		
		// Disconnect the meter ...
		getMk10Protocol().disconnect();
		
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

	public List getOptionalKeys() {
		ArrayList list = new ArrayList();
		list.addAll(getMk10Protocol().getOptionalKeys());
		return list;
	}

	public List getRequiredKeys() {
		ArrayList list = new ArrayList(0);
		list.addAll(getMk10Protocol().getRequiredKeys());
		return list;
	}

}
