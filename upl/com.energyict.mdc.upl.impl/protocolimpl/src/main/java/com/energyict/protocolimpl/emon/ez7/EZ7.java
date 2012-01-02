/*
 * EZ7.java
 *
 * Created on 27 april 2005, 11:03
 */

package com.energyict.protocolimpl.emon.ez7;


import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.emon.ez7.core.*;
import com.energyict.protocolimpl.emon.ez7.core.command.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.meteridentification.DiscoverInfo;

/**
 *
 * @author  Koen
 */
public class EZ7 extends AbstractProtocol {
    
    // properties
    //int halfDuplex;
    //HalfDuplexController halfDuplexController=null;
    
    // core objects
    EZ7Connection ez7Connection=null;
    EZ7Profile ez7Profile=null;
    EZ7CommandFactory ez7CommandFactory=null;
    
    /** Creates a new instance of EZ7 */
    public EZ7() {
    }
    
    public String getProtocolVersion() {
        return "$Date$";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return ez7CommandFactory.getVersion().getVersion();
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return ez7Profile.getProfileData(from,to,includeEvents);
    }
    
    
    protected void doConnect() throws java.io.IOException {
        ez7Profile = new EZ7Profile(this);
        ez7CommandFactory = new EZ7CommandFactory(this);
        if ((getInfoTypePassword() != null) && ("".compareTo(getInfoTypePassword())!=0))
           ez7CommandFactory.getSetKey().logon(getInfoTypePassword());
    }
    
    protected void doDisConnect() throws IOException {
        if ((getInfoTypePassword() != null) && ("".compareTo(getInfoTypePassword())!=0))
           ez7CommandFactory.getSetKey().logoff();
        
        // This command must be initiated when the meter sets up the connection to indicate a successfull read!
        // See page 1-32 of the protocoldescription...
        // Do not wait for a response since the meter hangsup the connection!
        getEz7Connection().sendCommand("SRD","0",false);
    }
    
    protected void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
        String sn = getEz7CommandFactory().getRGLInfo().getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }
    
    protected void validateDeviceId() throws IOException {
        boolean check = true;
        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID())==0)) return;
        String deviceId = getEz7CommandFactory().getRGLInfo().getDeviceId();
        if (deviceId.compareTo(getInfoTypeDeviceID()) == 0) return;
        throw new IOException("DeviceId mismatch! meter DeviceId="+deviceId+", configured deviceId="+getInfoTypeDeviceID());
    }
    
    
    protected java.util.List doGetOptionalKeys() {
        return null;
    }
    
    protected ProtocolConnection doInit(java.io.InputStream inputStream, java.io.OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws java.io.IOException {
        ez7Connection = new EZ7Connection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,halfDuplexController);
        return ez7Connection;	
    }
    
    protected void doValidateProperties(java.util.Properties properties) throws com.energyict.protocol.MissingPropertyException, com.energyict.protocol.InvalidPropertyException {
        //halfDuplex=Integer.parseInt(properties.getProperty("HalfDuplex","20").trim());
        if ((getInfoTypePassword() != null) && ("".compareTo(getInfoTypePassword())!=0) && (getInfoTypePassword().length() != 16)) {
            throw new InvalidPropertyException("EZ7, doValidateProperties, password length error! Password must have a length of 16 characters!");
        }
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"1A")); 
    }
    
    
    
    public java.util.Date getTime() throws java.io.IOException {
        
        // to verify device timezone against meter timezone...
        ez7CommandFactory.getImonInformation().getTimeZone();
        
        
        return ez7CommandFactory.getRTC().getDate();
    }
    
    /*
     * Override this method if the subclass wants to set the device time 
     */
    public void setTime() throws IOException {
        int accessLevel = getEz7CommandFactory().getSetKey().getAccessLevel();
        if (accessLevel < 2)
            throw new SecurityLevelException("EZ7, setTime(), accesslevel is "+SetKey.ACCESSLEVELS[accessLevel]);
        ez7CommandFactory.setRTC(); 
    }    
    

    public int getProtocolChannelValue(int channel) {
        if (getProtocolChannelMap()==null)
            return -1;
        else {
            ProtocolChannel pc = ez7CommandFactory.getEz7().getProtocolChannelMap().getProtocolChannel(channel);
            if (pc==null)
                return -1;
            else {
                return pc.getValue();
            }
        }
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        return ez7CommandFactory.getProfileStatus().getProfileInterval();
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return ez7CommandFactory.getHookUp().getNrOfChannels();
    }
    
    
    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getEz7CommandFactory());
        return ocm.getRegisterValue(obisCode);
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }    
    
    public String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("******************************************************************\n");
        strBuff.append("Manufacturer specific registers with code 0.B.96.99.E.F\n");
        strBuff.append("B=0, REG, Event General\n");
        strBuff.append("B=1, REL, Event Load\n");
        strBuff.append("B=2, RF, Flags status\n");
        strBuff.append("B=3, RGL, Read group & location & recorder id & serial#\n"); 
        strBuff.append("E=row (e.g. 0=LINE-1, 1=LINE-2,... \n"); 
        strBuff.append("F=col (0..7) 1 of the 8 values of the data LINE...\n"); 
        strBuff.append("******************************************************************\n");
        strBuff.append("Cumulative energy registers\n");
        for (int channel=1;channel<=8;channel++) {
            for (int tariff=1;tariff<=8;tariff++) {
                ObisCode obisCode = new ObisCode(1,channel,1,8,tariff,255);
                strBuff.append(obisCode+", "+obisCode.getDescription()+"\n");
            }
        }
        strBuff.append("******************************************************************\n");
        strBuff.append("Maximum demand registers\n");
        for (int channel=1;channel<=8;channel++) {
            for (int tariff=1;tariff<=8;tariff++) {
                ObisCode obisCode = new ObisCode(1,channel,1,6,tariff,255);
                strBuff.append(obisCode+", "+obisCode.getDescription()+"\n");
            }
        }
        strBuff.append("******************************************************************\n");
        strBuff.append("Sliding demand registers (12 x 5 minute sliding demand registers)\n");
        for (int channel=1;channel<=8;channel++) {
            for (int tariff=1;tariff<=12;tariff++) {
                ObisCode obisCode = new ObisCode(1,channel,1,5,tariff,255);
                strBuff.append(obisCode+", "+tariff+"-the 5 minute sliding demand register\n");
            }
        }
        strBuff.append("******************************************************************\n");
        strBuff.append("Power quality, instantaneous values \n");
        strBuff.append("1.0.1.7.0.255, "+ObisCode.fromString("1.0.1.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.21.7.0.255, "+ObisCode.fromString("1.0.21.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.41.7.0.255, "+ObisCode.fromString("1.0.41.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.61.7.0.255, "+ObisCode.fromString("1.0.61.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.11.7.0.255, "+ObisCode.fromString("1.0.11.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.31.7.0.255, "+ObisCode.fromString("1.0.31.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.51.7.0.255, "+ObisCode.fromString("1.0.51.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.71.7.0.255, "+ObisCode.fromString("1.0.71.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.12.7.0.255, "+ObisCode.fromString("1.0.12.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.32.7.0.255, "+ObisCode.fromString("1.0.32.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.52.7.0.255, "+ObisCode.fromString("1.0.52.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.72.7.0.255, "+ObisCode.fromString("1.0.72.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.13.7.0.255, "+ObisCode.fromString("1.0.13.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.33.7.0.255, "+ObisCode.fromString("1.0.33.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.53.7.0.255, "+ObisCode.fromString("1.0.53.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.73.7.0.255, "+ObisCode.fromString("1.0.73.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.14.7.0.255, "+ObisCode.fromString("1.0.14.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.34.7.0.255, "+ObisCode.fromString("1.0.34.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.54.7.0.255, "+ObisCode.fromString("1.0.54.7.0.255").getDescription()+"\n");
        strBuff.append("1.0.74.7.0.255, "+ObisCode.fromString("1.0.74.7.0.255").getDescription()+"\n");
        
        return strBuff.toString();
    }    
    
     /*******************************************************************************************
     m a i n ( )  i m p l e m e n t a t i o n ,  u n i t  t e s t i n g
     *******************************************************************************************/
    public static void main(String[] args) {
        Dialer dialer=null;
        EZ7 ez7=null;
        try {

// ********************************** DIALER ***********************************$            
// modem dialup connection
            dialer =DialerFactory.getDefault().newDialer();
            dialer.init("COM1");//,"AT+MS=2,0,2400,2400");
            //dialer.getSerialCommunicationChannel().setParams(2400,
            //                                                 SerialCommunicationChannel.DATABITS_7,
            //                                                 SerialCommunicationChannel.PARITY_EVEN,
             //                                                SerialCommunicationChannel.STOPBITS_1);
//            dialer.connect("phonenumber",60000);
            
// optical head connection
//            dialer =DialerFactory.getOpticalDialer().newDialer();
//            dialer.init("COM1");
//            dialer.connect("",60000); 
            
// direct rs232 connection
//            dialer =DialerFactory.getDirectDialer().newDialer();
//            dialer.init("COM4");
//            dialer.connect("",60000); 
            //00018173853675
            dialer.connect("00018173853675",90000); 
            //dialer.connect("4",60000); 
// *********************************** PROTOCOL ******************************************$            
            ez7 = new EZ7(); // instantiate the protocol
            
            System.out.println("Serial number = "+ez7.getSerialNumber(new DiscoverInfo(dialer.getSerialCommunicationChannel(),null)));
            if (true)
                return;
            
// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            //properties.setProperty("SecurityLevel","2");
            properties.setProperty(MeterProtocol.PASSWORD,"BBCCDDEE00000000"); //13579B"); //"123456");
            properties.setProperty("ProfileInterval", "900");
//            properties.setProperty("HalfDuplex", "50");
            //properties.setProperty("Retries", "0");
            
// transfer the properties to the protocol
            ez7.setProperties(properties); 
            
//            ez7.setHalfDuplexController(dialer.getHalfDuplexController());
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            ez7.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)ez7).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            ez7.connect();
            byte[] data=null;
//            data = ez7.getEz7Connection().sendCommand("RKA8");
//            System.out.println("received data "+data.length+" bytes, "+new String(data));
//            
//            data = ez7.getEz7Connection().sendCommand("RAA8");
//            System.out.println("received data "+data.length+" bytes, "+new String(data));
            
//            data = ez7.getEz7Connection().sendCommand("RDA8");
//            System.out.println("received data "+data.length+" bytes, "+new String(data));
//
//            data = ez7.getEz7Connection().sendCommand("RDD8");
//            System.out.println("received data "+data.length+" bytes, "+new String(data));
//            
//            data = ez7.getEz7Connection().sendCommand("RS*");
//            System.out.println("received data "+data.length+" bytes, "+new String(data));
//            
//            data = ez7.getEz7Connection().sendCommand("RAA8");
//            System.out.println("received data "+data.length+" bytes, "+new String(data));
            
//            data = ez7.getEz7Connection().sendCommand("RP2","03");
//            System.out.println("received data "+data.length+" bytes, "+new String(data));
            
            //data = ez7.getEz7Connection().sendCommand("RPH");
            //System.out.println("received data "+data.length+" bytes, "+new String(data));
            //System.out.println(ez7.getEz7CommandFactory().getAllMaximumDemand());
            
            //System.out.println(ez7.getEz7CommandFactory().getVerifyKey());
            //ez7.getEz7CommandFactory().getSetKey().logon("CCDDEEFF00000000");
            //System.out.println(ez7.getEz7CommandFactory().getSetKey().getAccessLevel());
            //ez7.getEz7CommandFactory().getSetKey().logoff();
            //System.out.println(ez7.getEz7CommandFactory().getSetKey().getAccessLevel());
            
            //System.out.println(ez7.getEz7CommandFactory().getRGLInfo());
            System.out.println(ez7.getEz7CommandFactory().getVerifyKey());
            
// get the meter profile data            
            System.out.println("*********************** getProfileData() ***********************");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,-1);
            //System.out.println(ez7.getEz7Profile().getProfileData(calendar.getTime(),new Date(),true));
// get the metertime            
            System.out.println("*********************** getTime() ***********************");
            Date date = ez7.getTime();
            System.out.println(date);
// set the metertime            
            System.out.println("*********************** setTime() ***********************");
//            ez7.setTime();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                System.out.println("*********************** disconnect() ***********************");
                ez7.disconnect();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Getter for property ez7Connection.
     * @return Value of property ez7Connection.
     */
    public com.energyict.protocolimpl.emon.ez7.core.EZ7Connection getEz7Connection() {
        return ez7Connection;
    }
    
    /**
     * Getter for property ez7Profile.
     * @return Value of property ez7Profile.
     */
    public com.energyict.protocolimpl.emon.ez7.core.EZ7Profile getEz7Profile() {
        return ez7Profile;
    }
    
    /**
     * Getter for property ez7CommandFactory.
     * @return Value of property ez7CommandFactory.
     */
    public com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory getEz7CommandFactory() {
        return ez7CommandFactory;
    }
    
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        Properties properties = new Properties();
        setProperties(properties);
        init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
        connect();
        String serialNumber =  getEz7CommandFactory().getRGLInfo().getSerialNumber();   
       // disconnect(); // no disconnect because the meter will hangup the link... disconnect contains an EZ7 protocol command to the meter that hangup the link!
        return serialNumber;
    }
    
}
