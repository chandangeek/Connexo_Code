package com.energyict.genericprotocolimpl.iskrap2lpc.handlers;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.energyict.cbo.ApplicationException;
import com.energyict.genericprotocolimpl.iskrap2lpc.Constant;
import com.energyict.genericprotocolimpl.iskrap2lpc.MeterReadTransaction;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProfileData;

public class ProfileHandler extends DefaultHandler{
	
    private final static String PROFILE = "Profile";
    private final static String REGISTER = "Register";
    
    private final static String VALUE = "Value";
    private final static String DATE_TIME = "DateTime";
    private final static String STATUS = "Status";
    
    private boolean inProfile = false;
    private SimpleDateFormat dateFormat;
	private MeterReadTransaction mrt;
	private ProfileData profile = new ProfileData();
	
	public ProfileHandler(){
	}
	
	public ProfileHandler(MeterReadTransaction mrt){
		this.mrt = mrt;
		this.dateFormat = Constant.getInstance().getDateFormatFixed();
	}
	
	public void startElement(String uri, String lName, String qName, Attributes attrbs) throws SAXException {
        if( PROFILE.equals(qName) )
            handleStartProfile(attrbs);
        if( REGISTER.equals(qName) )
            handleStartProfileParsing(attrbs);
	}
	 
	public void endElement(String uri, String localName, String qName) throws SAXException {
		 
	}
	
	private MeterReadTransaction getMeterReadTransaction(){
		return this.mrt;
	}
	
	private void handleStartProfile(Attributes attrbs){
		inProfile = true;
	}

	private void handleStartProfileParsing(Attributes attrbs){
		try {
			String valueString      = attrbs.getValue(VALUE);
			String dateTimeString   = attrbs.getValue(DATE_TIME);
			String statusString     = attrbs.getValue(STATUS);
			
			BigDecimal value = new BigDecimal(valueString);
			Date time = dateFormat.parse(dateTimeString);
			int intervalStatus = toIntervalState(statusString);
			int pStatus = Integer.parseInt(statusString);
			IntervalData id = new IntervalData(time, intervalStatus, pStatus);
//			id.addValue(v)
			//TODO keep an intervalMap in memory and store all the values in there.
			//TODO if there are multiple values for one date, just put them in a different channel
			//TODO at the end of the parsing, just add all intervals to the profileData object
//			profile.add(time, value, intervalStatus, pStatus);
		} catch (NumberFormatException e) {
			mrt.getLogger().log(Level.INFO, e.getMessage(), e);
			e.printStackTrace();
			throw new ApplicationException(e);
		} catch (ParseException e) {
			mrt.getLogger().log(Level.INFO, e.getMessage(), e);
			e.printStackTrace();
			throw new ApplicationException(e);
		}
		
	}
	
    private int toIntervalState(String status){
        int flag = Integer.parseInt(status);
        int eiStatus = IntervalStateBits.OK;
        
        if( (flag & Constant.PROFILE_STATUS_DEVICE_DISTURBANCE) > 0 )
            eiStatus |= IntervalStateBits.DEVICE_ERROR;
        
        if( (flag & Constant.PROFILE_STATUS_RESET_CUMULATION) > 0 )
            eiStatus |= IntervalStateBits.OTHER;
        
        if( (flag & Constant.PROFILE_STATUS_DEVICE_CLOCK_CHANGED) > 0 )
            eiStatus |= IntervalStateBits.SHORTLONG;        

        if( (flag & Constant.PROFILE_STATUS_POWER_RETURNED) > 0 )
            eiStatus |= IntervalStateBits.POWERUP;        

        if( (flag & Constant.PROFILE_STATUS_POWER_FAILURE) > 0 )
            eiStatus |= IntervalStateBits.POWERDOWN;        

        return eiStatus;
    }
}
