package com.energyict.genericprotocolimpl.iskrap2lpc;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ProtocolChannel;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Some example xml documents:
 * 
 * <Device DeviceID="40349033" P2LPCIdent="43355084">
 *   <Results>
 *     <Profile Ident="99.1.0" Period="3600">
 *     
 *       <Register Ident="1.8.0" Value="0000000.000" 
 *          DateTime="2007-03-29T07:00:00Z" Status="192" />
 *     
 *     </Profile>
 *     
 *    <Register Ident="1.8.1" Value="0000039.795" DateTime="2002-01-13T22:00:00Z" Status="0" />
 *     
 *   </Results>
 * </Device>
 * 
 * <MeterEvents>
 *   <Device DeviceID="40349033" P2LPCIdent="37463587">
 *   
 *     <Event DateTime="2007-06-07T05:38:43Z" Status="32786" />
 *   
 *   </Device>
 * </MeterEvents>
 * 
 * <MeterPowerEvents>
 *   <Device DeviceID="40349033" P2LPCIdent="37463587">
 *   
 *     <PowerEvent DateTimeLog="2007-06-20T15:45:58Z"
 *         DateTimeFail="2007-06-15T10:46:22Z" Duration="449976" />
 *   
 *   </Device>
 * </MeterPowerEvents>
 * 
 * @author fbo
 */

class XmlHandler extends DefaultHandler {
	
    private static final String PROFILE = "Profile";
    private static final String REGISTER = "Register";
    private static final String EVENT = "Event";
    private static final String ERROR = "Error";
    private static final String POWER_EVENT = "PowerEvent";
    private static final String METER_RESULTS = "MeterResults";
    private static final String METER_STATUS = "MeterStatus";
    private static final String ACTIVITY_CALENDAR = "ActivityCalendar";
    
    private static final String VALUE = "Value";
    private static final String DATE_TIME = "DateTime";
    private static final String STATUS = "Status";
    
    private static final String POWER_UP_MSG = "Power up.";
    private static final String POWER_DOWN_MSG = "Power down.";
    
    private static final int DAILY			= 0x00;
    private static String dailyStr = null;
    private static final int MONTHLY		= 0x01;
    private static String monthlyStr = null;
    
    private int profileDuration = -1;
    private boolean dailyMonthlyProfile = false;
    
    private SimpleDateFormat dateFormat;
    private boolean inProfile;
    private Logger logger;
    
    private ProtocolChannelMap channelMap;
    
    private String 	activeCalendarName = "";
    private boolean	activeCalendarBool = false;
    private Date	activeCalendarDate = null;
    
    /* index of the channel to store interval data into (if encountered) */
    private int currentChannelIndex = 0;
    private int profileChannelIndex = 0;
    private List profileIndex = new ArrayList();
    
    private MeterReadingData meterReadingData = new MeterReadingData();
    private Profile profile = new Profile();
    private List eventList = new ArrayList();
	private boolean checkOndemands;
	private Unit channelUnit = null;
	private boolean profileNotComplete = true;
	private Date lastAddedDate;
    
    public XmlHandler(Logger logger, ProtocolChannelMap channelMap) {
        this.logger = logger;
        this.channelMap = channelMap;
        dateFormat = Constant.getInstance().getDateFormatFixed();
    }
    
    public void setChannelIndex(int channelIndex) {
        currentChannelIndex = channelIndex;
    }
    
    public void startElement(String uri, String lName, String qName, Attributes attrbs) throws SAXException {
        if( PROFILE.equals(qName) ) {
			handleStartProfile(attrbs);
		}
        if( REGISTER.equals(qName) ) {
			handleStartRegister(attrbs);
		}
        if( EVENT.equals(qName) ) {
			handleStartEvent(attrbs);
		}
        if( POWER_EVENT.equals(qName) ) {
			handleStartPowerEvent(attrbs);
		}
        if (METER_RESULTS.equals(qName)){
        	activeCalendarBool = false;
        	if(dailyMonthlyProfile) {
				inProfile = true;
			}
        }
        if(METER_STATUS.equals(qName)){
        	activeCalendarBool = true;
        }
        if(ACTIVITY_CALENDAR.equals(qName)){
        	handleActivityCalendar(attrbs);
        }
        if(ERROR.equals(qName)){
        	profileNotComplete = false;
        }
    }
    
    public boolean isProfileComplete(){
    	return profileNotComplete;
    }
    
    public void setProfileComplete(boolean state){
    	this.profileNotComplete = state;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if( PROFILE.equals(qName) ) {
			handleEndProfile( );
		}
    }
    
    private void handleActivityCalendar(Attributes attrbs){
    	// TODO partially handled the activityCalendar, only needed the activityCalendarName for now ...
    	try {
			if(attrbs.getValue("CalendarNameActive") != null){
				this.activeCalendarName = attrbs.getValue("CalendarNameActive");
				String dateTime = attrbs.getValue("DateTime");
				this.activeCalendarDate = (dateTime!=null) ? dateFormat.parse(dateTime) : new Date();
			}
		} catch (ParseException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            throw new ApplicationException(e);
		}
    }

    private void handleStartProfile(Attributes attrbs) {
    	String ident = attrbs.getValue("Ident");
    	if ( ident.equals(getDailyStr()) ) {
			setProfileDuration(DAILY);
		} else if ( ident.equals(getMonthlyStr()) ) {
			setProfileDuration(MONTHLY);
		} else {
			inProfile = true;
		}
    }
    
    private void handleStartRegister(Attributes att) {
    	if(!activeCalendarBool){
    		try {
    			
    			if( ! inProfile ) {
    				
    				/* register data */
    				String ident    = att.getValue("Ident");
    				String dateTime = att.getValue("DateTime");
    				String value    = att.getValue("Value");
    				String error    = att.getValue("Error");
    				
    				if(ident.equalsIgnoreCase("0.0.128.101.18")) {
						System.out.println("");
					}
    				
    				ObisCode oc = null;
    				
    				if( error == null ){
    					if (profileDuration == DAILY){
    						
    						if( ident.split("\\.").length == 3 ) {
								oc = ObisCode.fromString( "1.0." + ident + ".VZ");
							}
    						
    						if( ident.split("\\.").length == 5 ) {
								oc = ObisCode.fromString( ident + ".VZ");
							}
    						
    						if( ident.split("\\.").length ==  6 ) {
								oc = ObisCode.fromString( ident );
							}
    					}
    					else if ( profileDuration == MONTHLY ){
    						if( ident.split("\\.").length == 3 ) {
								oc = ObisCode.fromString( "1.0." + ident + ".VZ-1");
							}
    						
    						if( ident.split("\\.").length == 5 ) {
								oc = ObisCode.fromString( ident + ".VZ-1");
							}
    						
    						if( ident.split("\\.").length ==  6 ) {
								oc = ObisCode.fromString( ident );
							}
    					}
    					
    					if (checkOndemands){
    						
    						String end = ".255";
    						
    						if (end != null){
    							if( ident.split("\\.").length == 3 ) {
									oc = ObisCode.fromString( "1.0." + ident + end);
								}
    							
    							if( ident.split("\\.").length == 5 ) {
									oc = ObisCode.fromString( ident + end);
								}
    							
    							if( ident.split("\\.").length ==  6 ) {
									oc = ObisCode.fromString( ident );
								}
    						}
    					}
    				}
    				
    				if( oc != null ) {
    					
    					Date d = (dateTime!=null) ? dateFormat.parse(dateTime) : new Date();
    					RegisterValue rv;
    					rv = toRegisterValue(oc, value, d);
    					
    					if( rv!=null ) {
							meterReadingData.add(rv);
						}
    					
    				} else {
    					String msg = "Code: " + ident + " not supported ";
    					if( error != null ) {
							msg += "msg: [" + error + "]";
						}
    					logger.log(Level.INFO, msg);
    				}
    			} else {
    				/* profile data */
    				String valueString      = att.getValue(VALUE);
    				String dateTimeString   = att.getValue(DATE_TIME);
    				String statusString     = att.getValue(STATUS);
    				
    				BigDecimal value = new BigDecimal(valueString);
    				
    				Date time = dateFormat.parse(dateTimeString);
    				int intervalStatus = toIntervalState(statusString);
    				
    				int pStatus = Integer.parseInt(statusString);
    				
    				profile.add(time, value, intervalStatus, pStatus);
    				lastAddedDate = time;
    			}
    		} catch (ParseException e) {
    			logger.log(Level.SEVERE, e.getMessage(), e);
    			e.printStackTrace();
    			throw new ApplicationException(e);
    		} 
    	}
    }
    
    public Date getLastAddedDate(){
    	return this.lastAddedDate;
    }
    
    private void handleStartEvent(Attributes attrbs) {
        
        try {
            
            String dateTime = attrbs.getValue("DateTime");
            String status = attrbs.getValue("Status");
            String ident = attrbs.getValue("Ident");
            String value = attrbs.getValue("Value");
            int eventId = Integer.parseInt(status);
            Date time = dateFormat.parse(dateTime);
            
            if(ident != null){
            	if( mask(ident, Constant.COM_GSMModemError)) {
					addMeterEvent(time, MeterEvent.OTHER, "Communication failure: GSM modem error.");
				}
            	if( mask(ident, Constant.COM_OpenPortError)) {
					addMeterEvent(time, MeterEvent.OTHER, "Communication failure: Port open.");
				}
            	if( mask(ident, Constant.COM_PhyLayerError)) {
					addMeterEvent(time, MeterEvent.OTHER, "Communication failure: Physical layer error.");
				}
            	if( mask(ident, Constant.COM_PPPConnect)) {
					addMeterEvent(time, MeterEvent.OTHER, "Communication event: PPP connected.");
				}
            	if( mask(ident, Constant.COM_PPPDisconnect)) {
					addMeterEvent(time, MeterEvent.OTHER, "Communication event: PPP disconnected.");
				}
            	if( mask(ident, Constant.COM_RASServerError)) {
					addMeterEvent(time, MeterEvent.OTHER, "Communication failure: RAS Server failure.");
				}
            	if( mask(ident, Constant.NON_Unknown)) {
					addMeterEvent(time, MeterEvent.OTHER, "Event UNKNOWN.");
				}
            	if( mask(ident, Constant.SYS_Startup)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: System startup.");
				}
            	if( mask(ident, Constant.SYS_Exit)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: System shut down.");
				}
            	if( mask(ident, Constant.SYS_Restart)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: System restart.");
				}
            	if( mask(ident, Constant.SYS_DeviceId)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: System device ID.");
				}
            	if( mask(ident, Constant.SYS_ParamsOK)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: System parameters OK.");
				}
            	if( mask(ident, Constant.SYS_ConfigOK)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: System configuration OK.");
				}
            	if( mask(ident, Constant.SYS_ParamsError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: System parameters ERROR.");
				}
            	if( mask(ident, Constant.SYS_ConfigError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: System configuration ERROR.");
				}
            	if( mask(ident, Constant.SYS_ReadingError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: System reading ERROR.");
				}
            	if( mask(ident, Constant.SYS_ReadingSessionError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: Session reading ERROR.");
				}
            	if( mask(ident, Constant.SYS_ReadingTransError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: ERROR during transfor readings.");
				}
            	if( mask(ident, Constant.SYS_DemandReadingError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: System demand reading ERROR.");
				}
            	if( mask(ident, Constant.SYS_DemandReadingSessionError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: Session demand reading ERROR.");
				}
            	if( mask(ident, Constant.SYS_DemandReadingTransError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: ERROR during transfor demand readings.");
				}
            	if( mask(ident, Constant.SYS_DemandReadingXMLOK)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: XML demand reading OK.");
				}
            	if( mask(ident, Constant.SYS_DemandReadingXMLError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: XML demand reading ERROR.");
				}
            	if( mask(ident, Constant.SYS_TariffXMLOK)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: Tariff xml file OK.");
				}
            	if( mask(ident, Constant.SYS_TariffXMLError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: Tariff xml file ERROR.");
				}
            	if( mask(ident, Constant.SYS_DLCMetersXMLError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: DLC meters XML file ERROR.");
				}
            	if( mask(ident, Constant.SYS_ThreadStartError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: ERROR starting thread.");
				}
            	if( mask(ident, Constant.SYS_HDLCError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: ERROR in HDLC packets.");
				}
            	if( mask(ident, Constant.SYS_MemoryError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: Memory ERROR.");
				}
            	if( mask(ident, Constant.SYS_SerialMetersXMLError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: Serial meters XML ERROR.");
				}
            	if( mask(ident, Constant.SYS_SaveThreadError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: ERROR during saving thread.");
				}
            	if( mask(ident, Constant.SYS_TimeSync)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: Time sync has occured.");
				}
            	if( mask(ident, Constant.SYS_CodeRed)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: Code red.");
				}
            	if( mask(ident, Constant.SYS_UpgradeStart)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: Upgrade started.");
				}
            	if( mask(ident, Constant.SYS_UpgradeStartSection)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: Started a section of the upgrade.");
				}
            	if( mask(ident, Constant.SYS_UpgradeFileError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: ERROR during file upgrade.");
				}
            	if( mask(ident, Constant.SYS_UpgradeStartMissing)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: A part of the upgrade is missing for meter : " + value);
				}
            	if( mask(ident, Constant.SYS_UpgradeCompleteOK)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: Upgrade complete for meter : " + value);
				}
            	if( mask(ident, Constant.SYS_UpgradeFinish)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: Upgrade is finished.");
				}
            	if( mask(ident, Constant.SYS_UpgradeFinishSection)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: Finished a section of the upgrade.");
				}
            	if( mask(ident, Constant.SYS_KeysFileOK)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: File containing the keys is OK.");
				}
            	if( mask(ident, Constant.SYS_KeysFileError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: ERROR in file containing the keys.");
				}
            	if( mask(ident, Constant.SYS_ResultsFileError)) {
					addMeterEvent(time, MeterEvent.OTHER, "System error: ERROR in file containing the results.");
				}
            	if( mask(ident, Constant.SYS_UpgradeStartActivate)) {
					addMeterEvent(time, MeterEvent.OTHER, "System event: Activated upgrade for meter : " + value);
				}
            	if( mask(ident, Constant.DLC_AddSubstation)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC event: DLC meter added a substation.");
				}
            	if( mask(ident, Constant.DLC_Deinstall)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC event: Deinstall DLC meter.");
				}
            	if( mask(ident, Constant.DLC_DoubleAddress)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC error: DLC meter with double address.");
				}
            	if( mask(ident, Constant.DLC_GlobalDeinstall)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC event: Global DLC meter deinstallation.");
				}
            	if( mask(ident, Constant.DLC_Install)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC event: DLC meter installed.");
				}
            	if( mask(ident, Constant.DLC_NetworkError)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC error: DLC network error.");
				}
            	if( mask(ident, Constant.DLC_NewAddress)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC event: DLC meter with new address.");
				}
            	if( mask(ident, Constant.DLC_SlaveDelete)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC event: DLC slave deleted.");
				}
            	if( mask(ident, Constant.DLC_SlaveExists)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC event: DLC slave exists.");
				}
            	if( mask(ident, Constant.DLC_SlaveLost)) {
					addMeterEvent(time, MeterEvent.OTHER, "DLC error: DLC slave is lost.");
				}
            	if( mask(ident, Constant.SUB_SetEncryptionKeyError)) {
					addMeterEvent(time, MeterEvent.OTHER, "SUB error: ERROR during the set of the encryption keys.");
				}
            	if( mask(ident, Constant.SUB_SetEncryptionKeyOK)) {
					addMeterEvent(time, MeterEvent.OTHER, "SUB event: Encryption keys are set OK.");
				}
            	if( mask(ident, Constant.SUB_TariffActivateError)) {
					addMeterEvent(time, MeterEvent.OTHER, "SUB error: Error during activation of new tariff.");
				}
            	if( mask(ident, Constant.SUB_TariffActivateOK)) {
					addMeterEvent(time, MeterEvent.OTHER, "SUB event: Tariff activation OK for meter : " + value);
				}
            	if( mask(ident, Constant.SUB_TariffWriteError)) {
					addMeterEvent(time, MeterEvent.OTHER, "SUB error: ERROR during the writing of the new tariff.");
				}
            	if( mask(ident, Constant.SUB_TariffWriteOK)) {
					addMeterEvent(time, MeterEvent.OTHER, "SUB event: Writing of new tariff was OK.");
				}
            }
            else{
            	if ( mask(eventId, Constant.EVENT_FATAL_ERROR) ){
            		final String msg = "Fatal error.";
            		addMeterEvent(time,MeterEvent.FATAL_ERROR, msg);
            	}
            	if ( mask(eventId, Constant.EVENT_DEVICE_CLOCK_RESERVE) ){
            		final String msg = "Event status device clock reserve.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_VALUE_CORRUPT) ){
            		final String msg = "Event status value corrupt.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_DAYLIGHT_CHANGE) ){
            		final String msg = "Event status daylight change.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_BILLING_RESET) ){
            		final String msg = "Billing.";
            		addMeterEvent(time,MeterEvent.BILLING_ACTION, msg);
            	}
            	if ( mask(eventId, Constant.EVENT_DEVICE_CLOCK_CHANGED) ){
            		final String msg = "Set Clock.";
            		addMeterEvent(time,MeterEvent.SETCLOCK, msg);
            	}
            	if ( mask(eventId, Constant.EVENT_POWER_RETURNED) ){
            		final String msg = POWER_UP_MSG;
            		addMeterEvent(time,MeterEvent.POWERUP, msg);
            	}
            	if ( mask(eventId, Constant.EVENT_POWER_FAILURE) ){
            		final String msg = POWER_DOWN_MSG;
            		addMeterEvent(time,MeterEvent.POWERDOWN, msg);
            	}
            	if ( mask(eventId, Constant.EVENT_VARIABLE_SET) ){
            		final String msg = "Event status variable set.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_UNRELIABLE_OPERATING_CONDITIONS) ){
            		final String msg = "Event status unreliable operating conditions.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_END_OF_UNRELIABLE_OPERATING_CONDITIONS) ){
            		final String msg = "Event status end of unreliable operating conditions.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_UNRELIABLE_EXTERNAL_CONTROL) ){
            		final String msg = "Event status unreliable external control.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_END_OF_UNRELIABLE_EXTERNAL_CONTROL) ){
            		final String msg = "Event status end of unreliable external control.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_EVENTLOG_CLEARED) ){
            		final String msg = "Event status event log cleared.";
            		addMeterEvent(time,MeterEvent.CLEAR_DATA,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_LOADPROFILE_CLEARED) ){
            		final String msg = "Event status load profile cleared.";
//            		addMeterEvent(time,MeterEvent.CLEAR_DATA,msg);
            		/** Current event only supported from EIServer8.3.13, otherwise be sure to use the event above */
            		addMeterEvent(time, MeterEvent.LOADPROFILE_CLEARED, msg);
            	}
            	if ( mask(eventId, Constant.EVENT_L1_POWER_FAILURE) ) {
            		final String msg = "Event status L1 phase failure.";
            		addMeterEvent(time,MeterEvent.PHASE_FAILURE,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_L2_POWER_FAILURE) ) {
            		final String msg = "Event status L2 phase failure.";
            		addMeterEvent(time,MeterEvent.PHASE_FAILURE,msg); 
            	}
            	if ( mask(eventId, Constant.EVENT_L3_POWER_FAILURE) ){
            		final String msg = "Event status L3 phase failure.";
            		addMeterEvent(time,MeterEvent.PHASE_FAILURE,msg);
            	}
            	if (mask(eventId, Constant.EVENT_L1_POWER_RETURNED) ){
            		final String msg = "Event status end of L1 phase failure.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_L2_POWER_RETURNED) ){
            		final String msg = "Event status end of L2 phase failure.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_L3_POWER_RETURNED) ){
            		final String msg = "Event status end of L3 phase failure.";
            		addMeterEvent(time,MeterEvent.OTHER,msg);
            	}
            	if ( mask(eventId, Constant.EVENT_METER_COVER_OPENED) ){
            		final String msg = "Event status meter cover opened.";
//            		addMeterEvent(time, MeterEvent.OTHER, msg);
            		addMeterEvent(time, MeterEvent.COVER_OPENED, msg);	// Only in later version of EISERVER
            	}
            	if ( mask(eventId, Constant.EVENT_TERMINAL_COVER_OPENED) ){
            		final String msg = "Event status terminal cover opened.";
//            		addMeterEvent(time, MeterEvent.OTHER, msg);
            		addMeterEvent(time,MeterEvent.TERMINAL_OPENED,msg);	// Only in later version of EISERVER
            	}
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ApplicationException(e);
        }
    }
    
    private boolean mask(String string1, String string2) {
		return string1.equalsIgnoreCase(string2);
	}

	/** check if event IS mask */
    private boolean mask(int event, int mask){
        return event == mask;
    }

    private RegisterValue toRegisterValue(ObisCode obis, String value, Date time) {
    
        try {
        
            if( obis.getC()==1 && obis.getD()==0 && obis.getE()==0 ) {
				return new RegisterValue(obis, dateFormat.parse(value));
			}
            
            Unit unit = null;
            if( obis.getC()==1 && obis.getD()==8 ) {
				unit = Unit.get(BaseUnit.WATTHOUR, 3);
			}

            if( obis.getC()==1 && obis.getD()==6 ) {
				unit = Unit.get(BaseUnit.WATT, 3);
			}
            
            if(unit==null){
                unit = Unit.getUndefined();
            }
             
            BigDecimal amount = new BigDecimal(value);
            Quantity q = new Quantity(amount, unit);
            return new RegisterValue(obis, q, null, null, time, new Date(System.currentTimeMillis()));
            
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ApplicationException(e);
        } 
        
    }
    
    private int toIntervalState(String status){
        int flag = Integer.parseInt(status);
        int eiStatus = IntervalStateBits.OK;
        
        if( (flag & Constant.PROFILE_STATUS_DEVICE_DISTURBANCE) > 0 ) {
			eiStatus |= IntervalStateBits.DEVICE_ERROR;
		}
        
        if( (flag & Constant.PROFILE_STATUS_RESET_CUMULATION) > 0 ) {
			eiStatus |= IntervalStateBits.OTHER;
		}
        
        if( (flag & Constant.PROFILE_STATUS_DEVICE_CLOCK_CHANGED) > 0 ) {
			eiStatus |= IntervalStateBits.SHORTLONG;
		}        

        if( (flag & Constant.PROFILE_STATUS_POWER_RETURNED) > 0 ) {
			eiStatus |= IntervalStateBits.POWERUP;
		}        

        if( (flag & Constant.PROFILE_STATUS_POWER_FAILURE) > 0 ) {
			eiStatus |= IntervalStateBits.POWERDOWN;
		}        

        return eiStatus;
    }
    
    private void addMeterEvent(Date time, int event, String description){
        eventList.add( new MeterEvent(time,event,description) );
    }

    private void handleStartPowerEvent(Attributes attrbs) {
        
        try {
            Date log  = dateFormat.parse( attrbs.getValue( "DateTimeLog" ) );
            Date fail = dateFormat.parse( attrbs.getValue( "DateTimeFail" ) );
            
            addMeterEvent(fail, MeterEvent.POWERDOWN, POWER_DOWN_MSG);
            addMeterEvent(log, MeterEvent.POWERUP, POWER_UP_MSG);
            
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ApplicationException(e);
        }            
        
    }

    private void handleEndProfile() {
        inProfile = false;
    }

    private List getDailyMonthlyChannelInfos(){
    	List result = new ArrayList();
    	for(int i = 0; i < getChannelMap().getNrOfProtocolChannels(); i++){
    		ProtocolChannel channel = getChannelMap().getProtocolChannel(i);
    		if((channel.containsDailyValues() || channel.containsMonthlyValues())&&(i == profileChannelIndex)){
    			if(getChannelUnit() == null) {
					result.add(channel.toChannelInfo(currentChannelIndex, profileChannelIndex));
				} else {
					result.add(channel.toChannelInfo(currentChannelIndex, profileChannelIndex, getChannelUnit()));
				}
    		}
    	}
    	return result;
    }
    
    private List getProfileChannelInfos(){
    	List result = new ArrayList();
    	for(int i = 0; i < getChannelMap().getNrOfProtocolChannels(); i++){
    		ProtocolChannel channel = getChannelMap().getProtocolChannel(i);
    		if(!channel.containsDailyValues() && !channel.containsMonthlyValues()){
    			if(getChannelUnit() == null) {
					result.add(channel.toChannelInfo(i, (Integer) getProfileIndexes().get(i)));
				} else {
					result.add(channel.toChannelInfo(i, (Integer) getProfileIndexes().get(i), getChannelUnit()));
				}
    			
    		}
    	}
    	return result;
    }
    
    public void setProfileChannelIndex(int index){
    	this.profileChannelIndex = index;
    	addProfileIndex(profileChannelIndex);
    }
    
    MeterReadingData getMeterReadingData( ) {
        return meterReadingData;
    }
    
    ProfileData getProfileData( ) {
        return profile.toProfileData();
    }
    
    ProfileData getDailyMonthlyProfile(){
    	return profile.toDailyMonthlyProfile();
    }

    private class Profile {
        
        private Map intervalMap = new HashMap();
        
        public void add( Date time, BigDecimal bd, int eiStatus, int protocolStatus ) {
            
            if( intervalMap.get(time) == null ) {
				intervalMap.put(time, new Interval(time));
			}
            
            Interval interval = (Interval)intervalMap.get(time);
            
            interval.values.set(currentChannelIndex, bd);
            interval.eiStatus |= eiStatus;
            interval.protocolStatus |= protocolStatus;
        }
        
        public ProfileData toDailyMonthlyProfile(){
        	ProfileData profileData = new ProfileData();
        	profileData.setChannelInfos(getDailyMonthlyChannelInfos());
        	Interval interval;
        	Iterator it = intervalMap.values().iterator();
        	while(it.hasNext()){
        		interval = (Interval)it.next();
        		profileData.addInterval(interval.toIntervalData());
        	}
        	
        	profileData.sort();
        	
        	return profileData;
        }
        
        public ProfileData toProfileData( ) {
        	ProfileData profileData = new ProfileData();
        	profileData.setChannelInfos(getProfileChannelInfos());
        	Interval interval;
        	Iterator it = intervalMap.values().iterator();
        	while(it.hasNext()){
        		interval = (Interval)it.next();
        		IntervalData id = interval.toIntervalData();
        		Iterator valit = id.getValuesIterator();
        		boolean corrupt = false;
        		while(valit.hasNext()){
        			IntervalValue val = (IntervalValue) valit.next();
        			if(val.getNumber() == null) {
						corrupt = true;
					}
        		}
        		if(!corrupt) {
					profileData.addInterval(interval.toIntervalData());
				}
        	}
        	
        	it = eventList.iterator();
            while( it.hasNext() ){
                MeterEvent event = (MeterEvent)it.next();
                profileData.addEvent( event );
            }
        	
        	profileData.sort();
        	return profileData;
        }

		private Interval[] copyIntervalSettings(Interval[] interval, int index) {
			interval[index].eiStatus = interval[2].eiStatus;
			interval[index].endTime = interval[2].endTime;
			interval[index].protocolStatus = interval[2].protocolStatus;
			return interval;
		}
    }
    
    private class Interval {
        
        private Date endTime;
        private int eiStatus;
        private int protocolStatus;
        private List values;
        
        Interval(Date endTime) {
            this.endTime = endTime;
            
            int profileChannels = getProfileChannels();
            values = new ArrayList( profileChannels );
            for( int i = 0; i < profileChannels; i ++ ) {
            	values.add( i, null );
            }
        }
        
        IntervalData toIntervalData( ){
            IntervalData id = new IntervalData( endTime, eiStatus, protocolStatus );
            id.addValues(values);
            return id;
        }
    }
    
    public int getProfileChannels(){
    	int count = 0;
    	if(dailyMonthlyProfile){
    		return 1; // we try to add channel by channel
    		
    	} else {
    		for(int i = 0; i < getChannelMap().getNrOfProtocolChannels(); i++){
    			if(!getChannelMap().getProtocolChannel(i).containsDailyValues() && !getChannelMap().getProtocolChannel(i).containsMonthlyValues()) {
					count++;
				}
    		}
    	}
    	return count;
    }

	public ProtocolChannelMap getChannelMap() {
		return channelMap;
	}

	public void checkOnDemands(boolean b) {
		checkOndemands = b;
		
	}

	public int getProfileDuration() {
		return profileDuration;
	}

	public void setProfileDuration(int profileDuration) {
		this.profileDuration = profileDuration;
	}

	public String getDailyStr() {
		return dailyStr;
	}

	public void setDailyStr(String dailyStr) {
		XmlHandler.dailyStr = dailyStr;
	}

	public String getMonthlyStr() {
		return monthlyStr;
	}

	public void setMonthlyStr(String monthlyStr) {
		XmlHandler.monthlyStr = monthlyStr;
	}

	public ProfileData addEvents() {
		ProfileData profileData = new ProfileData();
        Iterator i = eventList.iterator();
        Date now = new Date();
        while( i.hasNext() ){
            MeterEvent event = (MeterEvent)i.next();
            if (event.getTime().after(now)) {
                logger.warning("Received event from the future! Skipping this event. [" + event.toString() + " - " + event.getTime() + "]");
            } else {
                profileData.addEvent( event );
            }
        }
		return profileData;
	}

	public void setDailyMonthlyProfile(boolean b) {
		dailyMonthlyProfile = b;
	}
	public void clearDailyMonthlyProfile(){
		profile.intervalMap = new HashMap();
	}
	
	private List getProfileIndexes(){
		return profileIndex;
	}
	
	public void addProfileIndex(int index){
		getProfileIndexes().add((int)index);
	}
	
	public void clearChannelUnit(){
		this.channelUnit = null;
	}
	
	public void setChannelUnit(Unit unit){
		this.channelUnit = unit;
	}
	
	private Unit getChannelUnit(){
		return this.channelUnit;
	}
	
	public String getActiveCalendar(){
		return this.activeCalendarName;
	}
	public Date getActiveCalendarDate(){
		return this.activeCalendarDate;
	}
	
	public static void main(String[] args){
		try{
//			System.out.println("MeterEvent cover opened: " + MeterEvent.COVER_OPENED);
		} catch (Exception e){
			System.out.println(e);
		}
	}

}
