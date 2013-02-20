/**
 * MK10ClockExecuter.java
 * 
 * Created on 16-jan-2009, 14:14:47 by jme
 * 
 */
package com.energyict.genericprotocolimpl.edmi.mk10.executer;

import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocolimpl.edmi.mk10.MK10;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * @author jme
 *
 */
public class MK10ClockExecuter {

	private static final int DEBUG 			= 0;

    private DateFormat format 				= DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	private MK10ProtocolExecuter executer 	= null;

	/*
	 * Constructors
	 */

    public MK10ClockExecuter(MK10ProtocolExecuter mk10ProtocolExecuter) {
		this.executer = mk10ProtocolExecuter;
	}
	
	/*
	 * Private getters, setters and methods
	 */

    private boolean isCrossBoundary(java.util.Date systemDate, java.util.Date meterDate, MeterProtocol meterProtocol, int iProfileInterval) {
        final int safetyMargin = 10;
        if (((meterDate.getTime() / 1000) / iProfileInterval) ==
                ((systemDate.getTime() / 1000) / iProfileInterval)) {
            if ((((meterDate.getTime() / 1000) % iProfileInterval) > safetyMargin) &&
                    (((meterDate.getTime() / 1000) % iProfileInterval) < (iProfileInterval - safetyMargin)) &&
                    (((systemDate.getTime() / 1000) % iProfileInterval) > safetyMargin) &&
                    (((systemDate.getTime() / 1000) % iProfileInterval) < (iProfileInterval - safetyMargin)))
                return false;
        }

        return true;
    }
    
    private MK10ProtocolExecuter getExecuter() {
		return executer;
	}
    
    private long getDiff(java.util.Date systemDate, java.util.Date date) {
        return Math.abs(date.getTime() - systemDate.getTime());
    }

	private boolean inDSTGreyZone(Date date, TimeZone timeZone) {
        Date testdate;
        if (timeZone.inDaylightTime(date)) {
            testdate = new Date(date.getTime() + 3600000);
            if (timeZone.inDaylightTime(testdate))
                return false;
            else
                return true;
        } else
            testdate = new Date(date.getTime() - 3600000);

        if (timeZone.inDaylightTime(testdate))
            return true;
        else
            return false;
    }

	private void setAmrJournalOutOfBoundary(long signedDiff) {
		getExecuter().setAmrJournalOutOfBoundary(signedDiff);
	}

	private void log(Level level, String msg) {
		getExecuter().log(level, msg);		
	}

	private MK10 getMk10Protocol() {
		return getExecuter().getMk10Protocol();
	}
//
//	private CommunicationProfile getCommunicationProfile() throws BusinessException {
//		return getExecuter().getCommunicationProfile();
//	}

    private boolean isProtocolCorrectTime() {
        return (Integer.parseInt(getExecuter().getProperties().getProperty("CorrectTime", "1").trim()) == 1); // allow time correction
    }

	/*
	 * Public methods
	 */

    public long verifyAndSetMeterTime(TimeZone timeZone) throws IOException, BusinessException {
        
        java.util.Date systemDate = Calendar.getInstance().getTime();	// get the system time
        java.util.Date meterDate = getExecuter().getMk10Protocol().getTime();			// get the meter time (NOT roundtrip corrected !!!)

        log(Level.INFO, "metertime = " + format.format(meterDate) + " (" + meterDate.getTime() + "), systemtime = " + format.format(systemDate) + " (" + systemDate.getTime() + ")");
        
        long diff = getDiff(systemDate, meterDate);
        long signedDiff = meterDate.getTime() - systemDate.getTime();
        log(Level.INFO, "Difference between metertime and systemtime is " + signedDiff + " ms");

//        if (getCommunicationProfile().getAdHoc() && getCommunicationProfile().getForceClock()) {
//        	log(Level.WARNING ,"Force time clock ad-hoc action!");
//            getMk10Protocol().setTime();
//        }
//        else {
//	        if ((diff < (getCommunicationProfile().getMaximumClockDifference() * 1000)) && (diff > (getCommunicationProfile().getMinimumClockDifference() * 1000))) { // minimum difference to set time?
//	            if (isCrossBoundary(systemDate, meterDate, getMk10Protocol(), getMk10Protocol().getInfoTypeProfileInterval())) {
//	                setAmrJournalOutOfBoundary(signedDiff);
//	                log(Level.SEVERE , "time difference too close to (within 10 sec) or crosses the intervalboundary, will try again next communication session ");
//	            } else {
//	            	if (isProtocolCorrectTime()) {
//	            		if (!inDSTGreyZone(systemDate, timeZone)) {
//	            			getMk10Protocol().setTime();
//	            			log(Level.SEVERE , "Adjust meter time to system time");
//	            		} else {log(Level.SEVERE , "System time in DST switch 'grey' zone, time will NOT be set");}
//	            	} else {log(Level.WARNING , "used profile has writeclock disabled, time will NOT be set");}
//	            }
//	        }
//        }
        return signedDiff;
    }
    
    public boolean verifyMeterMaxTimeDiff(boolean collectOutsiteMaxTimeDiff) throws IOException, BusinessException {
        // verify the meter time difference against the max difference in the configuration
        // get the system time
        java.util.Date systemDate = Calendar.getInstance().getTime();
        // get the meter time (roundtrip corrected)
        java.util.Date meterDate = getMk10Protocol().getTime();
        long diff = getDiff(systemDate, meterDate) / 1000;
//        if (diff > getCommunicationProfile().getMaximumClockDifference()) {
//            String msg = "Time difference exceeds configured maximum: (" + diff + " s >" + getCommunicationProfile().getMaximumClockDifference() + " s )";
//            log(Level.SEVERE, msg);
//
//            if (!collectOutsiteMaxTimeDiff) {
//                getExecuter().setCompletionCodeTimeError(null);
//                throw new IOException(msg);
//            } else {
//            	getExecuter().setCompletionCodeTimeError(msg);
//                return true;
//            }
//        }
        return false;
    }

}
