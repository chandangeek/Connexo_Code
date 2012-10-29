/**
 * MK10DemandValuesExecuter.java
 * 
 * Created on 16-jan-2009, 15:44:57 by jme
 * 
 */
package test.com.energyict.protocolimplV2.edmi.mk10.executer;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * @author jme
 *
 */
// ToDo: should be integrated in new DeviceProtocol
public class MK10DemandValuesExecuter {

	private static final int DEBUG 			= 0;
	
	private MK10ProtocolExecuter executer 	= null;
    private Date validatedLastReading		= null;
    private Date now						= null;

	/*
	 * Constructors
	 */

	public MK10DemandValuesExecuter(MK10ProtocolExecuter mk10ProtocolExecuter) {
		this.executer = mk10ProtocolExecuter;
	}
	
	/*
	 * Private getters, setters and methods
	 */

	private MK10ProtocolExecuter getExecuter() {
		return executer;
	}

    private int getConfigNumberOfChannels() {
        return getExecuter().getMeter().getChannels().size();
    }
	
    private int getExtraIntervals() {
    	return Integer.parseInt(getExecuter().getProperties().getProperty("ExtraIntervals", "0").trim());
	}

	/*
	 * Public methods
	 */

	public void validateChannels() throws UnsupportedException, IOException, BusinessException {
        if (getExecuter().getCommunicationProfile().getCheckChannelConfig()) {
            // check if configuration nr of channels == protocol nr of channels... else throw exception
            if (getConfigNumberOfChannels() != getExecuter().getMk10Protocol().getNumberOfChannels()) {
            	getExecuter().adjustCompletionCode(AmrJournalEntry.CC_CONFIGURATION);
                throw new IOException(
                		"nr of channels configuration (" + getConfigNumberOfChannels() + 
                		") != nr of channels protocol (" + getExecuter().getMk10Protocol().getNumberOfChannels() + ")"
                );
            }
        }
	}

    public Date validateLastReading(Date lastReading, TimeZone timeZone) {
        Date testdate;
        if (lastReading == null) lastReading = new Date(0L);
        if (timeZone.inDaylightTime(lastReading)) {
            testdate = new Date(lastReading.getTime() + 3600000);
            if (timeZone.inDaylightTime(testdate))
                return lastReading;
            else
                return new Date(lastReading.getTime() - 3600000);
        } else
            testdate = new Date(lastReading.getTime() - 3600000);

        if (timeZone.inDaylightTime(testdate))
            return new Date(testdate.getTime() - 3600000);
        else
            return lastReading;
    }
    
    public ProfileData validateProfileData(ProfileData profileData, Date date) {
        Iterator it = profileData.getIntervalDatas().iterator();
        while (it.hasNext()) {
            IntervalData ivdt = (IntervalData) it.next();
            if (ivdt.getEndTime().after(date)) {
                //System.out.println("KV_DEBUG> remove "+ivdt);
                it.remove();
            }
        }
        return profileData;
    }
    
	public ProfileData getReadDemandValues() throws IOException, BusinessException {

	    if (getExtraIntervals() > 0) {
	    	this.validatedLastReading = validateLastReading(new Date(getExecuter().getMeter().getLastReading().getTime() - getExtraIntervals() * getExecuter().getMk10Protocol().getInfoTypeProfileInterval() * 1000), getExecuter().getMeter().getTimeZone());
	    } else {
	    	this.validatedLastReading = validateLastReading(getExecuter().getMeter().getLastReading(), getExecuter().getMeter().getTimeZone());
	    }
	    this.now = new Date(); //Calendar.getInstance().getTime();
		
		// Partial data read
    	getExecuter().log(Level.INFO,	"retrieve interval data from " + (new Date(validatedLastReading.getTime())) +	" to " + now);
        if (validatedLastReading.getTime() > Calendar.getInstance().getTime().getTime()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            getExecuter().log(Level.SEVERE, "error lastreading > current time, use " + calendar.getTime() + " as last reading");
            return getExecuter().getMk10Protocol().getProfileData(calendar.getTime(), getExecuter().getCommunicationProfile().getReadMeterEvents());
        } else {
            return getExecuter().getMk10Protocol().getProfileData(new Date(validatedLastReading.getTime()), getExecuter().getCommunicationProfile().getReadMeterEvents());
        }
	}
	
	public ProfileData getReadAllDemandValues() throws IOException, BusinessException {
	    if (getExtraIntervals() > 0) {
	    	this.validatedLastReading = validateLastReading(new Date(getExecuter().getMeter().getLastReading().getTime() - getExtraIntervals() * getExecuter().getMk10Protocol().getInfoTypeProfileInterval() * 1000), getExecuter().getMeter().getTimeZone());
	    } else {
	    	this.validatedLastReading = validateLastReading(getExecuter().getMeter().getLastReading(), getExecuter().getMeter().getTimeZone());
	    }
	    this.now = new Date(); //Calendar.getInstance().getTime();
	    return getExecuter().getMk10Protocol().getProfileData(getExecuter().getCommunicationProfile().getReadMeterEvents());
	}
	
	/*
	 * Public getters and setters
	 */

	public Date getNow() {
		return now;
	}
	
	public Date getValidatedLastReading() {
		return validatedLastReading;
	}
	
}
