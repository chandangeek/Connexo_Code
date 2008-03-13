/*
 * MK6.java
 *
 * Created on 17 maart 2006, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6;

import com.energyict.obis.*;
import com.energyict.protocolimpl.edmi.mk6.loadsurvey.*;
import com.energyict.protocolimpl.edmi.mk6.loadsurvey.ExtensionFactory;
import com.energyict.protocolimpl.edmi.mk6.registermapping.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
        
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;

import com.energyict.protocolimpl.edmi.mk6.command.*;
import com.energyict.protocolimpl.edmi.mk6.core.*;

/**
 *
 * @author  Koen
 * @beginchanges
KV|17052006|Check for duplicates
KV|14112007|Fix to use the correct first record timestamp 
 * @endchanges
 */
public class MK6 extends AbstractProtocol {
    
    private static final int DEBUG=0;
    
    private MK6Connection mk6Connection=null;
    private CommandFactory commandFactory=null;
    private ObisCodeFactory obisCodeFactory=null;
    MK6Profile mk6Profile=null;
    
    private String eventLogName;

    private String loadSurveyName;
    
    private int statusFlagChannel;
    
    /** Creates a new instance of MK6 */
    public MK6() {
       
    }
    
    protected void doConnect() throws IOException {
        getCommandFactory().enterCommandLineMode();
        getCommandFactory().logon(getInfoTypeDeviceID(),getInfoTypePassword());
    }
    
    protected void doDisConnect() throws IOException {
        getCommandFactory().exitCommandLineMode();
    }
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"1"));
        setEventLogName(properties.getProperty("EventLogName","Event Log"));
        setLoadSurveyName(properties.getProperty("LoadSurveyName","Load_Survey"));
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        setStatusFlagChannel(Integer.parseInt(properties.getProperty("StatusFlagChannel","0").trim()));
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException { 
        return mk6Profile.getProfileInterval();
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return mk6Profile.getNumberOfChannels();
    }    
    
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("EventLogName");
        result.add("LoadSurveyName");
        result.add("StatusFlagChannel");
        return result;
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        mk6Connection = new MK6Connection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber());
        commandFactory = new CommandFactory(this);
        mk6Profile = new MK6Profile(this);
        return getMk6Connection();
    }
    public Date getTime() throws IOException {
        TimeInfo ti = new TimeInfo(this);
        return ti.getTime();
    }
    
    public void setTime() throws IOException {
        TimeInfo ti = new TimeInfo(this);
        ti.setTime();
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.7 $";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "Equipment model id:"+getCommandFactory().getReadCommand(0xF000).getRegister().getString()+"\n"+ // Equipment model id
               "Software revision:"+getCommandFactory().getReadCommand(0xF003).getRegister().getString()+"\n"+ // software version
               "Last version nr:"+getCommandFactory().getReadCommand(0xFC18).getRegister().getString()+"\n"+ // last version number
               "Last revision nr:"+getCommandFactory().getReadCommand(0xFC19).getRegister().getString()+"\n"+ // last revision number
               "Software revision nr:"+getCommandFactory().getReadCommand(0xF090).getRegister().getString()+"\n"+ // software revision number
               "Serial number:"+getSerialNumber(); // serial number
    }
    
    public String getSerialNumber() throws IOException {
        return getCommandFactory().getReadCommand(0xF002).getRegister().getString(); // Serial number
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return mk6Profile.getProfileData(from, to, includeEvents);
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
        return getObicCodeFactory().getRegisterInfoDescription();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        MK6 mk6 = new MK6();
        Dialer dialer=null;
        try {
            
// direct rs232 connection
            dialer =DialerFactory.getDirectDialer().newDialer();
            dialer.init("COM1");
            dialer.connect("",60000); 
            
// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            //properties.setProperty("SecurityLevel","2");
            properties.setProperty(MeterProtocol.PASSWORD,"22222222");
            properties.setProperty(MeterProtocol.ADDRESS,"RETAILR");
            
            properties.setProperty("ProfileInterval", "900");
            //properties.setProperty(MeterProtocol.NODEID,"1234");
//            properties.setProperty("SerialNumber","204006174"); // multidrop + serial number check...
//            properties.setProperty("HalfDuplex", "50");
            //properties.setProperty("Retries", "0");
            
// transfer the properties to the protocol
            mk6.setProperties(properties);    
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            mk6.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)mk6).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            mk6.connect();
            
//            System.out.println(mk6.getCommandFactory().getInformationCommand(0xE397));
            // energy
//            System.out.println(mk6.getCommandFactory().getReadCommand(0xE097));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0xE093));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0xE397));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0xE497));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0xE393));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0xE493));
            
            // instantaneous
//            System.out.println(mk6.getCommandFactory().getReadCommand(0xE000));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0xE033));

            // tou
//            System.out.println("TOU channel types");
//            for (int i=0;i<=0xB;i++)
//                System.out.println(mk6.getCommandFactory().getReadCommand(0xF790+i));
            
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x0000));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x0100));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x0200));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x0300));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x0400));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x0500));
//            
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x1000));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x1100));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x1200));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x1300));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x1400));
//            System.out.println(mk6.getCommandFactory().getReadCommand(0x1500));
//
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk6.getCommandFactory().getReadCommand(0x8000).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk6.getCommandFactory().getReadCommand(0x8100).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk6.getCommandFactory().getReadCommand(0x8200).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk6.getCommandFactory().getReadCommand(0x8300).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk6.getCommandFactory().getReadCommand(0x8400).getRegister().getBigDecimal().intValue()));
//            System.out.println(DateTimeBuilder.getDateFromSecondsSince1996(TimeZone.getTimeZone("ECT"),mk6.getCommandFactory().getReadCommand(0x8500).getRegister().getBigDecimal().intValue()));
//            
            

//            System.out.println(mk6.getSerialNumber());
//            System.out.println(mk6.getFirmwareVersion());
            
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.1.2.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.1.8.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.1.2.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.1.9.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.1.16.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.1.9.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.1.16.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.3.8.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.3.2.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.3.8.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.3.2.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.3.9.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.3.16.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.3.9.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.3.16.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.9.8.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.9.2.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.9.8.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.9.2.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.9.9.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.9.16.0.255")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.9.9.0.0")));
//            System.out.println(mk6.readRegister(ObisCode.fromString("1.1.9.16.0.0")));

            
//            System.out.println(mk6.getSerialNumber());
            
//            System.out.println(mk6.getFirmwareVersion());
//            System.out.println("Meter:  "+mk6.getTime());
//            System.out.println("System: "+new Date());
            
            //mk6.setTime();
            
//System.out.println(mk6.getCommandFactory().getReadCommand(0x2F000));  
//{
//    for (int i=0;i<10;i++) {
//    System.out.println("Extension "+i+" name: "+mk6.getCommandFactory().getReadCommand(0x20000+i));            
//    System.out.println("Extension "+i+" registerid: "+mk6.getCommandFactory().getReadCommand(0x21000+i));               
//    System.out.println("Extension "+i+" size: "+mk6.getCommandFactory().getReadCommand(0x22000+i));          
//    System.out.println("Extension "+i+" usage: "+mk6.getCommandFactory().getReadCommand(0x23000+i));             
//    }
//}

// load profile ID 3
int regId=0x00A00000; //0x03000000;   //0x03200000;          

            
//System.out.println("Load profile:");            
//System.out.println("Nr of channels: "+mk6.getCommandFactory().getReadCommand(0x5F012+regId));            
//System.out.println("Nr of entries: "+mk6.getCommandFactory().getReadCommand(0x5F013+regId));               
//System.out.println("Interval: "+mk6.getCommandFactory().getReadCommand(0x5F014+regId));             
//System.out.println("Widest channel: "+mk6.getCommandFactory().getReadCommand(0x5F019+regId));              
//System.out.println("Registers stored in the channels:");
//int nrOfChannels = mk6.getCommandFactory().getReadCommand(0x5F012+regId).getRegister().getBigDecimal().intValue();
//for (int channel=0;channel<nrOfChannels;channel++) {
//   System.out.println("channel="+channel+" register: "+mk6.getCommandFactory().getReadCommand(0x5E000+channel+regId));              
//   System.out.println("channel="+channel+" size: "+mk6.getCommandFactory().getReadCommand(0x5E100+channel+regId));                
//   System.out.println("channel="+channel+" type: "+mk6.getCommandFactory().getReadCommand(0x5E200+channel+regId));               
//   System.out.println("channel="+channel+" unit: "+mk6.getCommandFactory().getReadCommand(0x5E300+channel+regId));               
//   System.out.println("channel="+channel+" name: "+mk6.getCommandFactory().getReadCommand(0x5E400+channel+regId));             
//   System.out.println("channel="+channel+" record offset: "+mk6.getCommandFactory().getReadCommand(0x5E500+channel+regId));             
//}
//
//
//System.out.println("Start of load survey: "+mk6.getCommandFactory().getReadCommand(0x5F020+regId));    
//System.out.println("Current Entry nr in load profile: "+mk6.getCommandFactory().getReadCommand(0x5F021+regId));    

Calendar cal = ProtocolUtils.getCalendar(mk6.getTimeZone());
//cal.set(Calendar.DAY_OF_MONTH,31);
//cal.set(Calendar.HOUR_OF_DAY,14);
//cal.set(Calendar.MINUTE,40);
//cal.set(Calendar.SECOND,0);
//System.out.println(cal.getTime());
//System.out.println(mk6.getCommandFactory().getFileAccessInfoCommand(0x5F008+regId));    
//System.out.println("FW: "+mk6.getCommandFactory().getFileAccessSearchForwardCommand(0x5F008+regId, cal.getTime()));
////System.out.println("BW: "+mk6.getCommandFactory().getFileAccessSearchBackwardCommand(0x5F008+regId, 0, cal.getTime()));
//System.out.println("FW: "+mk6.getCommandFactory().getFileAccessReadCommand(0x5F008+regId, 0, 2, 26, 10));
//
//
//
//System.out.println("LOAD PROFILE");
//regId=0x03000000;
//cal = ProtocolUtils.getCalendar(mk6.getTimeZone());
//cal.set(Calendar.DAY_OF_MONTH,31);
//cal.set(Calendar.HOUR_OF_DAY,8);
//cal.set(Calendar.MINUTE,5);
//cal.set(Calendar.SECOND,0);
//System.out.println(cal.getTime());
//System.out.println(mk6.getCommandFactory().getFileAccessInfoCommand(0x5F008+regId));    
//System.out.println("FW: "+mk6.getCommandFactory().getFileAccessSearchForwardCommand(0x5F008+regId, cal.getTime()));
//System.out.println("BW: "+mk6.getCommandFactory().getFileAccessSearchBackwardCommand(0x5F008+regId, 0, cal.getTime()));
//System.out.println("FW: "+mk6.getCommandFactory().getFileAccessReadCommand(0x5F008+regId, 513, 1, 0, 22));
//

//System.out.println(mk6.getCommandFactory().getReadCommand(0x0325F012));

//ExtensionFactory ef = ExtensionFactory.getExtensionFactory(mk6.getCommandFactory());
//LoadSurvey ls = ef.findLoadSurvey("Event Log"); //"Load_Survey1");
//System.out.println(ls);
//Calendar from = Calendar.getInstance();
////from.add(Calendar.DAY_OF_MONTH,-3);
//from.add(Calendar.HOUR_OF_DAY,-8);
//LoadSurveyData lsd = ls.readFile(from.getTime());
//System.out.println(lsd);


Calendar from = Calendar.getInstance();
from.add(Calendar.DAY_OF_MONTH,-3);
System.out.println(mk6.getProfileData(from.getTime(),null,true));

            mk6.disconnect();
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        
    }

    public TimeZone getTimeZone() {
        return ProtocolUtils.getWinterTimeZone(super.getTimeZone());
    }    
    
    public MK6Connection getMk6Connection() {
        return mk6Connection;
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
    }

    public String getLoadSurveyName() {
        return loadSurveyName;
    }

    private void setLoadSurveyName(String loadSurveyName) {
        this.loadSurveyName = loadSurveyName;
    }

    public boolean isStatusFlagChannel() {
        return statusFlagChannel==1;
    }

    public void setStatusFlagChannel(int statusFlagChannel) {
        this.statusFlagChannel = statusFlagChannel;
    }



}
