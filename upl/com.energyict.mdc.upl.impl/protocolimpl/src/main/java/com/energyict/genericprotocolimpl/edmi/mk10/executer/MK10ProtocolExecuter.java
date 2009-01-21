/**
 * MK10ProtocolExecuter.java
 * 
 * Created on 16-jan-2009, 09:16:11 by jme
 * 
 */
package com.energyict.genericprotocolimpl.edmi.mk10.executer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.edmi.mk10.MK10Push;
import com.energyict.genericprotocolimpl.edmi.mk10.streamfilters.MK10PushInputStream;
import com.energyict.genericprotocolimpl.edmi.mk10.streamfilters.MK10PushOutputStream;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.MeterUsageData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.edmi.mk10.MK10;

/**
 * @author jme
 *
 */
public class MK10ProtocolExecuter {

	private static final int DEBUG 					= 0;

	public static final byte STATUS_OK 				= 0;
	public static final byte STATUS_PROTOCOLERROR 	= 1;
	public static final byte STATUS_COMMERROR 		= 2;
	public static final String NO_ERROR 			= "no error";

	private Rtu meter				= null;
	private MK10Push mk10Push		= null;
	private MK10 mk10Protocol		= new MK10();
	private Properties properties	= new Properties();
    private List journal 			= new ArrayList();
    
    private int completionCode				= AmrJournalEntry.CC_OK;
    private String completionErrorString 	= "";

    private MeterReadingData meterReadingData	= null;
    private ProfileData meterProfileData		= null;
    private MeterUsageData meterUsageData		= null;

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
	
	private Link getLink() {
		return getMk10Push().getLink();
	}
	
	/*
	 * AMR and logging methods
	 */
	
	private Logger getLogger() {
		return getMk10Push().getLogger();
	}

	protected void log(Level level, String msg){
		getLogger().log(level, msg + "\n");
	}
    
    protected void setAmrJournalOutOfBoundary(long timeDiff) {
        journal(new AmrJournalEntry(AmrJournalEntry.TIME_OUT_OF_BOUNDARY, "Time difference " + timeDiff + " ms is out of boundary."));
    }
    
    public void setCompletionCodeConfiguration() {
        adjustCompletionCode(AmrJournalEntry.CC_CONFIGURATION);
    }
    
    public void adjustCompletionCode(int cc) {
    	if (DEBUG >= 1)	System.out.println(" ### adjustCompletionCode(), CC = " + cc);
    	if (this.completionCode == AmrJournalEntry.CC_OK)
            this.completionCode = cc;
    }
	
    public void setCompletionCodeTimeError(String msg) {
        if (msg != null) completionErrorString = msg;
        adjustCompletionCode(AmrJournalEntry.CC_TIME_ERROR);
    }

    
	private void readMeterProperties() {
		if (this.properties == null) this.properties = new Properties();
		
		CommunicationProtocol protocol = getMeter().getProtocol();
		if (protocol != null) {
			if (protocol.getProperties() != null) this.properties.putAll(protocol.getProperties());
		}
		if (getMeter().getProperties() != null) this.properties.putAll(getMeter().getProperties());
				
		if (getMeter().getSerialNumber() != null) this.properties.put(MeterProtocol.SERIALNUMBER, getMeter().getSerialNumber());
		if (getMeter().getNodeAddress() != null) this.properties.put(MeterProtocol.NODEID, getMeter().getNodeAddress());
		if (getMeter().getDeviceId() != null) this.properties.put(MeterProtocol.ADDRESS, getMeter().getDeviceId());
		if (getMeter().getPassword() != null) this.properties.put(MeterProtocol.PASSWORD, getMeter().getPassword());
		if (String.valueOf(getMeter().getIntervalInSeconds()) != null) this.properties.put(MeterProtocol.PROFILEINTERVAL, String.valueOf(getMeter().getIntervalInSeconds()));
		
		if (DEBUG >= 2)	properties.list(System.out);
		
	}

    private void doLogMeterDataCollection(MeterReadingData meterReadingData) {
    	if (meterReadingData == null) {
    		log(Level.INFO, "meterReadingData is null, probably meterreadings not supported by meter.");
    		return;
    	} 

    	List registerValues = meterReadingData.getRegisterValues();
    	Iterator it = registerValues.iterator();
    	
    	while (it.hasNext()) {
    		RegisterValue each = (RegisterValue) it.next();
    		log(Level.INFO, each.toString());
    	}
    	
    } // private void doLogMeterDataCollection(List readingsData)  throws ProtocolReaderException

    private void doLogMeterDataCollection(ProfileData profileData) {
        if (profileData == null) {
            log(Level.INFO, "ProfileData is null");
            return;
        }
        int i, iNROfChannels = profileData.getNumberOfChannels();
        int t, iNROfIntervals = profileData.getNumberOfIntervals();
        int z, iNROfEvents = profileData.getNumberOfEvents();
        log(Level.INFO, "Channels: " + iNROfChannels);
        log(Level.INFO, "Intervals per channel: " + iNROfIntervals);
        for (t = 0; t < iNROfIntervals; t++) {
            log(Level.FINER, " Interval " + t + "\tendtime = " + profileData.getIntervalData(t).getEndTime());
            log(Level.FINER, "Channel\tvalue\tstatus\tunit\trtu channel");
            for (i = 0; i < iNROfChannels; i++) {
                log(Level.FINER, i + "\t" + profileData.getIntervalData(t).get(i) + "\t" + profileData.getIntervalData(t).getEiStatusTranslation(i) + "\t" + profileData.getChannel(i).getUnit() + "\t" + profileData.getChannel(i).getChannelId());
            }
        }

        log(Level.INFO, "Events in profiledata: " + iNROfEvents);
        log(Level.FINER, "Event\tEICode\t\tProtocolCode\t\ttime");
        log(Level.FINER, "\t(descr)\t(hex)\t(dec)\t(hex)\ttime");
        for (z = 0; z < iNROfEvents; z++) {
            log(Level.FINER, z + "\t" + profileData.getEvent(z).toString() + "\t0x" + Integer.toHexString(profileData.getEvent(z).getEiCode()) + "\t" + profileData.getEvent(z).getProtocolCode() + "\t0x" + Integer.toHexString(profileData.getEvent(z).getProtocolCode()) + "\t" + profileData.getEvent(z).getTime());
        }

    } // private void doLogMeterDataCollection(ProfileData profileData)  throws ProtocolReaderException
    
    protected void verifyMeterProfileInterval() throws IOException {
        // Verify profileinterval against the configuration
        int iProfileInterval = getMk10Protocol().getInfoTypeProfileInterval();
        int meterProfileInterval = getMk10Protocol().getProfileInterval();
        
        try {
            if (meterProfileInterval != iProfileInterval) {
                setCompletionCodeConfiguration();
                throw new IOException("profile interval setting in eiserver configuration (" + iProfileInterval + "sec) is different then requested from the meter (" + meterProfileInterval + "sec)");
            }
        }
        catch (UnsupportedException e) {
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
		
        // Before continuing with reading the meter's data, check some basic
        // things to be sure that we can continue...
        if (getCommunicationProfile().getReadDemandValues()) verifyMeterProfileInterval();
        
        MK10ClockExecuter clockExec = new MK10ClockExecuter(this);
        boolean markAsBadTime = clockExec.verifyMeterMaxTimeDiff(getCommunicationProfile().getCollectOutsideBoundary());

        if (getCommunicationProfile().getReadDemandValues()) {
        	MK10DemandValuesExecuter demandValuesExec = new MK10DemandValuesExecuter(this);
        	demandValuesExec.validateChannels();
        }

        if (getCommunicationProfile().getReadMeterReadings()) {
        	MK10MeterReadingExecuter meterReadingExec = new MK10MeterReadingExecuter(this);
        	meterReadingData = meterReadingExec.getMeterReadings();
        }

        // Get the version of the meterfirmware
        try {
            String strVersion = getMk10Protocol().getFirmwareVersion();
            log(Level.FINE, "meter firmware: " + strVersion);
        } catch (UnsupportedException e) {}

        // Get the version of the MK10 protocol
        String strVersion = getMk10Protocol().getProtocolVersion();
        log(Level.FINE, "MK10: protocol version: " + strVersion);

        
        // Get the meter profile
    	MK10DemandValuesExecuter demandValuesExec = new MK10DemandValuesExecuter(this);
        if (getCommunicationProfile().getReadDemandValues()) {
        	meterProfileData = demandValuesExec.getReadDemandValues();
        } else if (getCommunicationProfile().getReadAllDemandValues()) {
        	meterProfileData = demandValuesExec.getReadAllDemandValues();
        }

        // Sort the received profile
        if (meterProfileData != null) {
            meterProfileData = demandValuesExec.validateProfileData(meterProfileData, demandValuesExec.getNow());
            meterProfileData.sort();
            if (markAsBadTime)
                meterProfileData.markIntervalsAsBadTime();
        }
        
        // Try to read or set the clock in the meter
		long timeDiff = clockExec.verifyAndSetMeterTime(getMeter().getTimeZone());
        journal(new AmrJournalEntry(AmrJournalEntry.TIMEDIFF, timeDiff));

		
        
		// Disconnect the meter ...
		getMk10Protocol().disconnect();

        meterUsageData = new MeterUsageData(meterReadingData, meterProfileData);
		
		// Show data from meter in loggings
		doLogMeterDataCollection(meterProfileData);
		doLogMeterDataCollection(meterReadingData);
		
	}
	
	/*
	 * Public getters and setters
	 */

	public CommunicationProfile getCommunicationProfile() throws BusinessException {
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

    public List getJournal() {
        return journal;
    }

    protected void setJournal(List journal) {
        this.journal = journal;
    }

    public void journal(AmrJournalEntry entry) {
        journal.add(entry);
    }

    public Properties getProperties() {
		return properties;
	}
    
    public ProfileData getMeterProfileData() {
		return meterProfileData;
	}
    
    public MeterReadingData getMeterReadingData() {
		return meterReadingData;
	}
    
    public int getCompletionCode() {
		return completionCode;
	}
    
    public String getCompletionErrorString() {
		return completionErrorString;
	}
    
    public MeterUsageData getMeterUsageData() {
		return meterUsageData;
	}
    
}
