/*
 * AlphaPlus.java
 *
 * Created on 5 juli 2005, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.DialerMarker;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.AlphaPlusProfile;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.ObisCodeMapper;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.BillingDataRegister;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.BillingDataRegisterFactoryImpl;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.Class31ModemBillingCallConfiguration;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.Class32ModemAlarmCallConfiguration;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.ClassFactory;
import com.energyict.protocolimpl.elster.alpha.core.Alpha;
import com.energyict.protocolimpl.elster.alpha.core.classes.BillingDataRegisterFactory;
import com.energyict.protocolimpl.elster.alpha.core.connection.AlphaConnection;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 *
 * @author  Koen
 * @beginchanges
 KV|12062007|Bugfix timeout large profiledata request
 KV|13072007|changes for Metersmart for A+ without quadrant info
 * @endchanges
 */
public class AlphaPlus extends AbstractProtocol implements Alpha, SerialNumberSupport {
    
    private static final int DEBUG=0;
    private AlphaConnection alphaConnection;
    private CommandFactory commandFactory;
    private ClassFactory classFactory;   
    private AlphaPlusProfile alphaPlusProfile;
    private int opticalHandshakeOverModemport;
    
    // lazy initializing
    private BillingDataRegisterFactoryImpl billingDataRegisterFactory=null;
    long whoAreYouTimeout;
    private int totalRegisterRate;
    
    /** Creates a new instance of AlphaPlus */
    public AlphaPlus() {
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getAlphaPlusProfile().getProfileData(lastReading,includeEvents);
    }
    
    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters. 
    SerialCommunicationChannel commChannel;
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
        getAlphaConnection().setOptical(commChannel!=null);
    }
    
    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters. 
        if (commChannel!=null)
            commandFactory.opticalHandshake(commChannel, getInfoTypePassword(), getDtrBehaviour());
        else {
            if (opticalHandshakeOverModemport==1)
                commandFactory.opticalHandshakeOverModemport(getInfoTypePassword());
            else
                commandFactory.signOn(getInfoTypeNodeAddressNumber(),getInfoTypePassword());
        }
        
        // set packetsize so that all Multiple (lenh lenl) packets behave corect (lenh bit 7 last packet)
        getCommandFactory().getFunctionWithDataCommand().PacketSize(4);
    }
    protected void doDisConnect() throws IOException {
        try {
            if (commChannel==null) {
                 commandFactory.getFunctionWithoutDataCommand().sendBillingReadComplete();
                 commandFactory.getFunctionWithoutDataCommand().sendAlarmReadComplete(); // KV 27062007
            }
        }
        finally {
           commandFactory.getShortFormatCommand().terminateSession(); 
        }
    }
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        whoAreYouTimeout = Integer.parseInt(properties.getProperty("WhoAreYouTimeout","300").trim());
        totalRegisterRate = Integer.parseInt(properties.getProperty("TotalRegisterRate","1").trim());
        opticalHandshakeOverModemport =  Integer.parseInt(properties.getProperty("OpticalHandshakeOverModemport","0").trim());
    }
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("WhoAreYouTimeout");   
        result.add("TotalRegisterRate");   
        result.add("OpticalHandshakeOverModemport");
        return result;
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        int pi = getClassFactory().getClass14LoadProfileConfiguration().getLoadProfileInterval();
        return pi==0?getInfoTypeProfileInterval():pi;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getClassFactory().getClass14LoadProfileConfiguration().getNrOfChannels();
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeout,int maxRetries,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        alphaConnection = new AlphaConnection(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController, whoAreYouTimeout);
        commandFactory = new CommandFactory(alphaConnection);
        classFactory = new ClassFactory(this);
        alphaPlusProfile = new AlphaPlusProfile(this);
        return alphaConnection;
    }
    public Date getTime() throws IOException {
        return getClassFactory().getClass9Status1().getTD();
    }
    
    public void setTime() throws IOException {
        getCommandFactory().getFunctionWithDataCommand().syncTime(getInfoTypeRoundtripCorrection(), getTimeZone());
    }

    /**
     * The protocol version
     */
    public String getProtocolVersion() {
        return "$Date: 2017-02-13 16:43:02 +0100 (Mon, 13 Feb 2017)$";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        try {
           return getClassFactory().getClass8FirmwareConfiguration().getFirmwareVersion();
        }
        catch(IOException e) {
            return "ERROR, unable to get the firmware version of the meter, "+e.toString();
        }
    }
    
    public String getSerialNumber() {
        try {
            return Long.toString(getClassFactory().getClass7MeteringFunctionBlock().getXMTRSN());
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }
    
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        List passwords = discoverInfo.getPasswords();
        
        if (DEBUG>=1) System.out.println("alphaPlus, getSerialNumber, "+discoverInfo);
        
        if (passwords==null)
            passwords = new ArrayList();
        
        if (passwords.size()==0)
            passwords.add("00000000");
        
        int retries=0;
        for (int i=0;i<passwords.size();i++) {
            String password = (String)passwords.get(i);
//            while(true) {
                try {
                    Properties properties = new Properties();
                    properties.setProperty(MeterProtocol.PASSWORD,password);
                    setProperties(properties);
                    init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
                    connect();
                    //getCommandFactory().getFunctionWithDataCommand().whoAreYou(0);
                    String serialNumber =  Long.toString(getClassFactory().getSerialNumber()); //getSerialNumber();   
                   // disconnect(); // no disconnect because the meter will hangup the link... disconnect contains an EZ7 protocol command to the meter that hangup the link!
                    if (DEBUG>=1) System.out.println("alphaPlus, getSerialNumber, serialNumber="+serialNumber+" size="+serialNumber.length());
                    return serialNumber;
                }
                catch(IOException ex) {
                    
                    ex.printStackTrace();
    //                try {
    //                   Thread.sleep(500);
                       disconnect();
    //                }
    //                catch(Exception e) {
    //                    // absorb
    //                }

                    if (i==(passwords.size()-1))
                        throw ex;
                }
//            }
        }
        throw new IOException("AlphaPlus, getSerialNumber(), Error discovering serialnumber!");
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
        getBillingDataRegisterFactory().buildAll();
        StringBuffer strBuff = new StringBuffer();
        
        strBuff.append("************************ CLASSES READ ************************\n");
        strBuff.append(getClassFactory().getClass0ComputationalConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass2IdentificationAndDemandData()+"\n");
        strBuff.append(getClassFactory().getClass31ModemBillingCallConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass32ModemAlarmCallConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass33ModemConfigurationInfo()+"\n");
        strBuff.append(getClassFactory().getClass6MeteringFunctionBlock()+"\n");
        strBuff.append(getClassFactory().getClass7MeteringFunctionBlock()+"\n");
        strBuff.append(getClassFactory().getClass8FirmwareConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass14LoadProfileConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass9Status1()+"\n");
        strBuff.append(getClassFactory().getClass10Status2()+"\n");
        
        strBuff.append("************************ CLASS11 Current billing registers ************************\n");
        Iterator it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.CURRENT_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            strBuff.append(bdr.getRegisterValue().toString()+", "+description+"\n");
        }
        strBuff.append("************************ CLASS12 Previous month billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_MONTH_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            strBuff.append(bdr.getRegisterValue().toString()+", "+description+"\n");
        }
        strBuff.append("************************ CLASS13 Previous season billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_SEASON_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            strBuff.append(bdr.getRegisterValue().toString()+", "+description+"\n");
        }
        return strBuff.toString();
    }

    static public void main(String[] args) {
        AlphaPlus alphaPlus=null;
        Dialer dialer=null;
                         //  test 0             Hyatt Dallas 1    Plastic Enterprises Lowe 2   NNG 3               HEB GROCERY 4         ROSEWOOD Health center 5   Verizon 6              GPU solar 7          CP Cham Clemens Dome 8   TXI 9             Little Sisters 10   FOOD SOURCE 11    Wal-Mart 12       Texas Instruments 13   Toys R US 14                 CSU San Bernardino 15
        String[] phones={   "00019192505870",  "00012147127096", "00018168353147",            "00016202428765",   "00015123531047",     "00018165214844",          "00018059349769",      "00017077441332",    "00019797985439",        "00012145467856", "00018167638713",   "00012142265602", "00017143780113", "00014322384733",      "00015593227058,,,,,,,,,,,,,,,,,,11,+11,11", "00019098809343"};
        String[] passwords={"00000000",        "72633573",       "07041776",                  "07041776",         "00000000",           "00000000",                "17326630",            "00000000",          "11111111",              "37500110",       "00000000",         "28745652",       "73230000",       "82965564",            "00000000",                  "00000000"   };
        
        
        final int selection=1; 
        
        
        try {
// ********************************** DIALER ***********************************$            
// modem dialup connection
            dialer =DialerFactory.getDefault().newDialer();
            dialer.init("COM1"); //,"AT+MS=V22B"); //"2,0,2400,2400");
            dialer.getSerialCommunicationChannel().setParams(1200,
                                                             SerialCommunicationChannel.DATABITS_8,
                                                             SerialCommunicationChannel.PARITY_NONE,
                                                            SerialCommunicationChannel.STOPBITS_1);
            dialer.connect("102",60000);
            
// optical head connection
            //dialer =DialerFactory.getOpticalDialer().newDialer();
            //dialer.init("COM1");
            //dialer.connect("",60000); 
            
// direct rs232 connection
//            dialer =DialerFactory.getDirectDialer().newDialer();
//            dialer.init("COM4");
//            dialer.connect("",60000); 
            //00018173853675
            
//            dialer.connect(phones[selection],90000);  
            //dialer.connect("4",60000); 
// *********************************** PROTOCOL ******************************************$            
            alphaPlus = new AlphaPlus(); // instantiate the protocol
//            if (DialerMarker.hasOpticalMarker(dialer))
//                ((HHUEnabler)alphaPlus).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            List passwordlist = new ArrayList();
            //passwordlist.add("00000000");
            //passwordlist.add("11111111");
            passwordlist.add("22222222");
            passwordlist.add("07041776");
            System.out.println("Serial number = "+alphaPlus.getSerialNumber(new DiscoverInfo(dialer.getSerialCommunicationChannel(),null,-1, passwordlist)));
            if (true)
                return;
            
// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            //properties.setProperty("SecurityLevel","2");
            properties.setProperty(MeterProtocol.PASSWORD,passwords[selection]);
            properties.setProperty("ProfileInterval", "900");
            properties.setProperty(MeterProtocol.NODEID,"0");
//            properties.setProperty("HalfDuplex", "50");
            //properties.setProperty("Retries", "0");
            
// transfer the properties to the protocol
            alphaPlus.setProperties(properties); 
            
//            ez7.setHalfDuplexController(dialer.getHalfDuplexController());
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            alphaPlus.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("EST"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)alphaPlus).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            alphaPlus.connect();
            byte[] data=null;

            System.out.println(alphaPlus.getClassFactory().getClass0ComputationalConfiguration());
            System.out.println(alphaPlus.getClassFactory().getClass2IdentificationAndDemandData());
            System.out.println(alphaPlus.getClassFactory().getClass33ModemConfigurationInfo());
            System.out.println(alphaPlus.getClassFactory().getClass6MeteringFunctionBlock());
            System.out.println(alphaPlus.getClassFactory().getClass7MeteringFunctionBlock());
            System.out.println(alphaPlus.getClassFactory().getClass8FirmwareConfiguration());
            System.out.println(alphaPlus.getClassFactory().getClass11BillingData());
            System.out.println(alphaPlus.getClassFactory().getClass12PreviousMonthBillingData());
            System.out.println(alphaPlus.getClassFactory().getClass13PreviousSeasonBillingData());
            System.out.println(alphaPlus.getClassFactory().getClass14LoadProfileConfiguration());
            System.out.println(alphaPlus.getClassFactory().getClass31ModemBillingCallConfiguration());
            System.out.println(alphaPlus.getClassFactory().getClass32ModemAlarmCallConfiguration());
            System.out.println(alphaPlus.getClassFactory().getClass9Status1());
            System.out.println(alphaPlus.getClassFactory().getClass10Status2());
            System.out.println(alphaPlus.getRegistersInfo(0));
           
           //alphaPlus.getCommandFactory().getFunctionWithDataCommand().PacketSize(2);
//           System.out.println(alphaPlus.getClassFactory().getClass17LoadProfileData(6));
            
            
            System.out.println(alphaPlus.getClassFactory().getClass15EventLogConfiguration());
            
            
            System.out.println("*********************** Meter information ***********************");
            System.out.println(alphaPlus.getNumberOfChannels());
            System.out.println(alphaPlus.getProtocolVersion());
            System.out.println(alphaPlus.getProfileInterval());
        
// get the meter profile data            
//            System.out.println("*********************** getProfileData() ***********************");
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.DATE,-1);
//            System.out.println(alphaPlus.getProfileData(calendar.getTime(),true));
// get the metertime            
            System.out.println("*********************** getTime() ***********************");
            Date date = alphaPlus.getTime();  
            System.out.println(date);
// set the metertime            
//            System.out.println("*********************** setTime() ***********************");
//            ez7.setTime();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                System.out.println("*********************** disconnect() ***********************");
                alphaPlus.disconnect();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }        
        
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }
    
    public ClassFactory getClassFactory() {
        return classFactory;
    }
    
    public BillingDataRegisterFactory getBillingDataRegisterFactory() throws IOException {
        if (billingDataRegisterFactory==null) {
           billingDataRegisterFactory = new BillingDataRegisterFactoryImpl(getClassFactory());
        }
        return billingDataRegisterFactory;
        
    }

    public AlphaPlusProfile getAlphaPlusProfile() {
        return alphaPlusProfile;
    }
    public AlphaConnection getAlphaConnection() {
        return alphaConnection;
    }    
    
    public void setDialinScheduleTime(Date date) throws IOException {

        // Programm class31
        Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
        cal.setTime(date);
        Class31ModemBillingCallConfiguration o = getClassFactory().getClass31ModemBillingCallConfiguration();
        if (o.getTimingWindowFrom() != cal.get(Calendar.HOUR_OF_DAY)) {
            o.setTimingWindowFrom(cal.get(Calendar.HOUR_OF_DAY));
            o.setTimingWindowTo(cal.get(Calendar.HOUR_OF_DAY));
            o.write();
        }
        
        getCommandFactory().getFunctionWithDataCommand().billingReadDialin(date,getTimeZone());        
    }
    
    public void setPhoneNr(String phoneNr) throws IOException {
        
        Class31ModemBillingCallConfiguration o = getClassFactory().getClass31ModemBillingCallConfiguration();
        if (changePhoneNr(o.getDialString().trim(), phoneNr.trim()))  {
            o.setDialString(phoneNr);
            o.write();
        }
        
        Class32ModemAlarmCallConfiguration o2 = getClassFactory().getClass32ModemAlarmCallConfiguration();
        if (changePhoneNr(o2.getDialString().trim(), phoneNr.trim()))  {
            o2.setDialString(phoneNr);
            o2.write();
        }
    }
    
    private boolean changePhoneNr(String programmedPhoneNr, String newPhoneNr) {
        return ((newPhoneNr != null) && // if the phone nr != null
                (newPhoneNr.compareTo("") != 0) && // if the phone nr != ""
                (programmedPhoneNr.compareTo(newPhoneNr.trim()) != 0) && // phone nr != programmed phone nr 
                (!((programmedPhoneNr.compareTo("") == 0) && (newPhoneNr.compareTo("remove")==0)))); // NOT phone nr == remove AND programmed phone nr empty
    }
    
    
    public int getTotalRegisterRate() {
        return totalRegisterRate;
    }

    private void setTotalRegisterRate(int totalRegisterRate) {
        this.totalRegisterRate = totalRegisterRate;
    }
}
