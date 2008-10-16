/*
 * MK6.java
 *
 * Created on 17 maart 2006, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.obis.*;
import com.energyict.protocolimpl.edmi.mk10.loadsurvey.*;
import com.energyict.protocolimpl.edmi.mk10.registermapping.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.hibernate.PropertyNotFoundException;
import org.hibernate.PropertyValueException;

import sun.security.action.GetLongAction;
        
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;

import com.energyict.protocolimpl.edmi.mk10.command.*;
import com.energyict.protocolimpl.edmi.mk10.core.*;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;

/**
 *
 * @author  Koen
 * @beginchanges
KV|17052006|Check for duplicates
KV|14112007|Fix to use the correct first record timestamp 
 * @endchanges
 */
public class MK10 extends AbstractProtocol {
    
    private static final int DEBUG=1;    
    private MK10Connection mk10Connection=null;
    private CommandFactory commandFactory=null;
    private ObisCodeFactory obisCodeFactory=null;
    MK10Profile mk10Profile=null;
    
    private String eventLogName;

    private int loadSurveyNumber;
    private int statusFlagChannel;
    
    /** Creates a new instance of MK10 */
    public MK10() {
       
    }
    
    protected void doConnect() throws IOException {
        sendDebug("doConnect()");
    	getCommandFactory().enterCommandLineMode();
        getCommandFactory().logon(getInfoTypeDeviceID(),getInfoTypePassword());
    }
    
    protected void doDisConnect() throws IOException {
        sendDebug("doDisConnect()");
        getCommandFactory().exitCommandLineMode();
    }
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        sendDebug("doValidateProperties()");
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"1"));
        setEventLogName(properties.getProperty("EventLogName","Event Log"));
        validateLoadSurveyNumber(properties.getProperty("LoadSurveyNumber"));
        setLoadSurveyNumber(Integer.parseInt(properties.getProperty("LoadSurveyNumber").trim())-1);
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        setStatusFlagChannel(Integer.parseInt(properties.getProperty("StatusFlagChannel","0").trim()));
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException { 
        sendDebug("getProfileInterval()");
        return mk10Profile.getProfileInterval();
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        sendDebug("getNumberOfChannels()");
        return mk10Profile.getNumberOfChannels();
    }    
    
    protected List doGetOptionalKeys() {
        sendDebug("doGetOptionalKeys()");
        List result = new ArrayList();
        result.add("EventLogName");
        result.add("LoadSurveyNumber");
        result.add("StatusFlagChannel");
        return result;
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        sendDebug("doInit()");
        mk10Connection = new MK10Connection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber());
        commandFactory = new CommandFactory(this);
        mk10Profile = new MK10Profile(this);

        return getMk10Connection();
    }
    public Date getTime() throws IOException {
        sendDebug("getTime()");
        TimeInfo ti = new TimeInfo(this);
        return ti.getTime();
    }
    
    public void setTime() throws IOException {
        sendDebug("setTime()");
        TimeInfo ti = new TimeInfo(this);
        ti.setTime();
    }
    
    public String getProtocolVersion() {
        sendDebug("getProtocolVersion()");
        return "$Revision: 1.7 $";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        sendDebug("getFirmwareVersion()");
        return "Equipment model id:"+getCommandFactory().getReadCommand(0xF000).getRegister().getString()+"\n"+ // Equipment model id
               "Software version:"+getCommandFactory().getReadCommand(0xF003).getRegister().getString()+"\n"+ // Software version
               "Software revision:"+getCommandFactory().getReadCommand(0xF090).getRegister().getString()+"\n"+ // Software revision
               "Bootloader revision:"+getCommandFactory().getReadCommand(0xF091).getRegister().getString()+"\n"+ // Software revision
               "Serial number:"+getSerialNumber(); // serial number
    }
    
    public String getSerialNumber() throws IOException {
        sendDebug("getSerialNumber()");
        return getCommandFactory().getReadCommand(0xF002).getRegister().getString(); // Serial number
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        sendDebug("getProfileData()");
        return mk10Profile.getProfileData(from, to, includeEvents);
    }
    
    
    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }    
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        sendDebug("getRegistersInfo()");
        return getObicCodeFactory().getRegisterInfoDescription();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        MK10 mk10 = new MK10();
        Dialer dialer=null;
        try {
            
// direct rs232 connection
            dialer =DialerFactory.getDirectDialer().newDialer();
            dialer.init("COM1");
            dialer.connect("",60000); 
            
// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();

            properties.setProperty("SerialNumber","206332371");
            properties.setProperty(MeterProtocol.ADDRESS,"EDMI");
            properties.setProperty(MeterProtocol.PASSWORD,"IMDEIMDE");
            properties.setProperty("ProfileInterval", "1800");
            properties.setProperty("LoadSurveyNumber", "1");
            
            //properties.setProperty(MeterProtocol.NODEID,"1234");
//            properties.setProperty("HalfDuplex", "50");
            //properties.setProperty("Retries", "0");
            
// transfer the properties to the protocol
            mk10.setProperties(properties);    
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            //mk10.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("logger"));
            mk10.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"), null);
            
// if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)mk10).enableHHUSignOn(dialer.getSerialCommunicationChannel());
                        
// connect to the meter            
            mk10.connect();
            //mk10.sendDebug(mk10.getCommandFactory().getReadCommand(0xD800).toString());            
            //mk10.sendDebug(mk10.getCommandFactory().getReadCommand(0xE002).toString());
            //mk10.sendDebug("Number of channels: " + String.valueOf(mk10.getNumberOfChannels()));
            //mk10.sendDebug("Profile interval:   " + String.valueOf(mk10.getProfileInterval()));
            //System.out.println(mk10.mk10Profile.loadSurvey.toString());

            mk10.sendDebug("\n");
            mk10.sendDebug(mk10.commandFactory.getReadCommand(0xD8B0).getRegister().getDate().toString()); // The first time that was stored in the survey ever.                        
            //mk10.sendDebug(mk10.commandFactory.getFileAccessReadCommand('0', 0x00, 0x00000000, 0xFFFF).toString());
            mk10.sendDebug("\n");
            //mk10.sendDebug(mk10.getFirmwareVersion());
            mk10.disconnect();
            int i = 2;
            
            if (i==2) {
            	return;
            }
//            System.out.println(mk10.getCommandFactory().getInformationCommand(0xE397));
            // energy
//            System.out.println(mk10.getCommandFactory().getReadCommand(0xE097));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0xE093));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0xE397));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0xE497));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0xE393));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0xE493));
            
            // instantaneous
//            System.out.println(mk10.getCommandFactory().getReadCommand(0xE000));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0xE033));

            // tou
//            System.out.println("TOU channel types");
//            for (int i=0;i<=0xB;i++)
//                System.out.println(mk10.getCommandFactory().getReadCommand(0xF790+i));
            
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x0000));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x0100));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x0200));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x0300));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x0400));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x0500));
//            
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x1000));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x1100));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x1200));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x1300));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x1400));
//            System.out.println(mk10.getCommandFactory().getReadCommand(0x1500));
//
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk10.getCommandFactory().getReadCommand(0x8000).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk10.getCommandFactory().getReadCommand(0x8100).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk10.getCommandFactory().getReadCommand(0x8200).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk10.getCommandFactory().getReadCommand(0x8300).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk10.getCommandFactory().getReadCommand(0x8400).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk10.getCommandFactory().getReadCommand(0x8500).getRegister().getBigDecimal().intValue()));
//            
            

//            System.out.println(mk10.getSerialNumber());
//            System.out.println(mk10.getFirmwareVersion());
            
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.1.2.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.1.8.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.1.2.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.1.9.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.1.16.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.1.9.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.1.16.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.3.8.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.3.2.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.3.8.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.3.2.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.3.9.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.3.16.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.3.9.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.3.16.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.9.8.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.9.2.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.9.8.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.9.2.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.9.9.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.9.16.0.255")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.9.9.0.0")));
//            System.out.println(mk10.readRegister(ObisCode.fromString("1.1.9.16.0.0")));

            
//            System.out.println(mk10.getSerialNumber());
            
//            System.out.println(mk10.getFirmwareVersion());
//            System.out.println("Meter:  "+mk10.getTime());
//            System.out.println("System: "+new Date());
            
            //mk10.setTime();
            
//System.out.println(mk10.getCommandFactory().getReadCommand(0x2F000));  
//{
//    for (int i=0;i<10;i++) {
//    System.out.println("Extension "+i+" name: "+mk10.getCommandFactory().getReadCommand(0x20000+i));            
//    System.out.println("Extension "+i+" registerid: "+mk10.getCommandFactory().getReadCommand(0x21000+i));               
//    System.out.println("Extension "+i+" size: "+mk10.getCommandFactory().getReadCommand(0x22000+i));          
//    System.out.println("Extension "+i+" usage: "+mk10.getCommandFactory().getReadCommand(0x23000+i));             
//    }
//}

// load profile ID 3
int regId=0x00A00000; //0x03000000;   //0x03200000;          

            
//System.out.println("Load profile:");            
//System.out.println("Nr of channels: "+mk10.getCommandFactory().getReadCommand(0x5F012+regId));            
//System.out.println("Nr of entries: "+mk10.getCommandFactory().getReadCommand(0x5F013+regId));               
//System.out.println("Interval: "+mk10.getCommandFactory().getReadCommand(0x5F014+regId));             
//System.out.println("Widest channel: "+mk10.getCommandFactory().getReadCommand(0x5F019+regId));              
//System.out.println("Registers stored in the channels:");
//int nrOfChannels = mk10.getCommandFactory().getReadCommand(0x5F012+regId).getRegister().getBigDecimal().intValue();
//for (int channel=0;channel<nrOfChannels;channel++) {
//   System.out.println("channel="+channel+" register: "+mk10.getCommandFactory().getReadCommand(0x5E000+channel+regId));              
//   System.out.println("channel="+channel+" size: "+mk10.getCommandFactory().getReadCommand(0x5E100+channel+regId));                
//   System.out.println("channel="+channel+" type: "+mk10.getCommandFactory().getReadCommand(0x5E200+channel+regId));               
//   System.out.println("channel="+channel+" unit: "+mk10.getCommandFactory().getReadCommand(0x5E300+channel+regId));               
//   System.out.println("channel="+channel+" name: "+mk10.getCommandFactory().getReadCommand(0x5E400+channel+regId));             
//   System.out.println("channel="+channel+" record offset: "+mk10.getCommandFactory().getReadCommand(0x5E500+channel+regId));             
//}
//
//
//System.out.println("Start of load survey: "+mk10.getCommandFactory().getReadCommand(0x5F020+regId));    
//System.out.println("Current Entry nr in load profile: "+mk10.getCommandFactory().getReadCommand(0x5F021+regId));    

Calendar cal = ProtocolUtils.getCalendar(mk10.getTimeZone());
//cal.set(Calendar.DAY_OF_MONTH,31);
//cal.set(Calendar.HOUR_OF_DAY,14);
//cal.set(Calendar.MINUTE,40);
//cal.set(Calendar.SECOND,0);
//System.out.println(cal.getTime());
//System.out.println(mk10.getCommandFactory().getFileAccessInfoCommand(0x5F008+regId));    
//System.out.println("FW: "+mk10.getCommandFactory().getFileAccessSearchForwardCommand(0x5F008+regId, cal.getTime()));
////System.out.println("BW: "+mk10.getCommandFactory().getFileAccessSearchBackwardCommand(0x5F008+regId, 0, cal.getTime()));
//System.out.println("FW: "+mk10.getCommandFactory().getFileAccessReadCommand(0x5F008+regId, 0, 2, 26, 10));
//
//
//
//System.out.println("LOAD PROFILE");
//regId=0x03000000;
//cal = ProtocolUtils.getCalendar(mk10.getTimeZone());
//cal.set(Calendar.DAY_OF_MONTH,31);
//cal.set(Calendar.HOUR_OF_DAY,8);
//cal.set(Calendar.MINUTE,5);
//cal.set(Calendar.SECOND,0);
//System.out.println(cal.getTime());
//System.out.println(mk10.getCommandFactory().getFileAccessInfoCommand(0x5F008+regId));    
//System.out.println("FW: "+mk10.getCommandFactory().getFileAccessSearchForwardCommand(0x5F008+regId, cal.getTime()));
//System.out.println("BW: "+mk10.getCommandFactory().getFileAccessSearchBackwardCommand(0x5F008+regId, 0, cal.getTime()));
//System.out.println("FW: "+mk10.getCommandFactory().getFileAccessReadCommand(0x5F008+regId, 513, 1, 0, 22));
//

//System.out.println(mk10.getCommandFactory().getReadCommand(0x0325F012));

//ExtensionFactory ef = ExtensionFactory.getExtensionFactory(mk10.getCommandFactory());
//LoadSurvey ls = ef.findLoadSurvey("Event Log"); //"Load_Survey1");
//System.out.println(ls);
//Calendar from = Calendar.getInstance();
////from.add(Calendar.DAY_OF_MONTH,-3);
//from.add(Calendar.HOUR_OF_DAY,-8);
//LoadSurveyData lsd = ls.readFile(from.getTime());
//System.out.println(lsd);


Calendar from = Calendar.getInstance();
from.add(Calendar.DAY_OF_MONTH,-3);
System.out.println(mk10.getProfileData(from.getTime(),null,true));

            mk10.disconnect();
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        
    }

    public TimeZone getTimeZone() {
        return ProtocolUtils.getWinterTimeZone(super.getTimeZone());
    }    
    
    public MK10Connection getMk10Connection() {
        return mk10Connection;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public ObisCodeFactory getObicCodeFactory() throws IOException {
        if (obisCodeFactory==null)
            obisCodeFactory = new ObisCodeFactory(this);
        return obisCodeFactory;
    }

    public String getEventLogName() {
        return eventLogName;
    }

    private void setEventLogName(String eventLogName) {
    	this.eventLogName = eventLogName;
        sendDebug("setEventLogName(): " + this.eventLogName);
    }

    public int getLoadSurveyNumber() {
        return loadSurveyNumber;
    }

    private void setLoadSurveyNumber(int loadSurveyNr) {
        this.loadSurveyNumber = loadSurveyNr;
        sendDebug("setLoadSurveyNumber(): " + String.valueOf(this.loadSurveyNumber));
    }

    private void validateLoadSurveyNumber(String value) throws MissingPropertyException, InvalidPropertyException {
    	if (value == null)
    		throw new MissingPropertyException("No LoadSurveyNumber property found! Must be 1 or 2 for the EDMI MK10 meter.");
    	if (!value.trim().equalsIgnoreCase("1") && !value.trim().equalsIgnoreCase("2")) 
    		throw new InvalidPropertyException("Wrong LoadSurveyNumber value: " + value + "! Must be 1 or 2 for the EDMI MK10 meter.");
    }
    
    public boolean isStatusFlagChannel() {
        return statusFlagChannel==1;
    }

    public void setStatusFlagChannel(int statusFlagChannel) {
        this.statusFlagChannel = statusFlagChannel;
        sendDebug("setStatusFlagChannel(): " + String.valueOf(statusFlagChannel));
    }

    public void sendDebug(String str){
        if (DEBUG == 1) {
        	str = " **** DEBUG > " + str;
        	Logger log = getLogger();
        	if (log != null) {
            	getLogger().info(str);
        	} 
        	else {
            	System.out.println(str);
        	}
        }
    }

}
