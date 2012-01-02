/*
 * AlphaBasic.java
 *
 * Created on 5 juli 2005, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic;
import com.energyict.protocolimpl.elster.alpha.core.connection.AlphaConnection;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;
import java.io.*;
import java.util.*;
import java.util.logging.*;
  
import com.energyict.protocol.HalfDuplexEnabler;    
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.elster.alpha.core.Alpha;
import com.energyict.protocolimpl.elster.alpha.core.classes.*;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.AlphaBasicProfile;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.ObisCodeMapper;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.BillingDataRegisterFactoryImpl;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.BillingDataRegister;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.ClassFactory;

/**
 *
 * @author  Koen
 * @beginchanges
 * @endchanges
 */
public class AlphaBasic extends AbstractProtocol implements Alpha {
    
    public static final int DEBUG=0;
    
    private AlphaConnection alphaConnection;
    private CommandFactory commandFactory;
    private ClassFactory classFactory;   
    private AlphaBasicProfile alphaBasicProfile;
    
    // lazy initializing
    private BillingDataRegisterFactoryImpl billingDataRegisterFactory=null;
    long whoAreYouTimeout;
    private int totalRegisterRate;
    
    /** Creates a new instance of AlphaBasic */
    public AlphaBasic() {
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getAlphaBasicProfile().getProfileData(lastReading,includeEvents);
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
            commandFactory.opticalHandshake(commChannel, getInfoTypePassword(),getDtrBehaviour());
        else 
            commandFactory.signOn(getInfoTypeNodeAddressNumber(),getInfoTypePassword());
        
        // set packetsize so that all Multiple (lenh lenl) packets behave corect (lenh bit 7 last packet)
        getCommandFactory().getFunctionWithDataCommand().PacketSize(4);
    }
    
    
    protected void doDisConnect() throws IOException {
        try {
            if (commChannel==null) {
                 commandFactory.getFunctionWithoutDataCommand().sendBillingReadComplete();
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
    }
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("WhoAreYouTimeout");   
        result.add("TotalRegisterRate");   
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
        alphaBasicProfile = new AlphaBasicProfile(this);
        return alphaConnection;
    }
    public Date getTime() throws IOException {
        return getClassFactory().getClass9Status1().getTD();
    }
    
    public void setTime() throws IOException {
        getCommandFactory().getFunctionWithDataCommand().syncTime(getInfoTypeRoundtripCorrection(), getTimeZone());
    }

    public String getProtocolVersion() {
        return "$Date$";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        try {
           return getClassFactory().getClass8FirmwareConfiguration().getFirmwareVersion()+" "+getClassFactory().getClass8FirmwareConfiguration().getMeterType();
        }
        catch(IOException e) {
            return "ERROR, unable to get the firmware version of the meter, "+e.toString();
        }
    }
    
    public String getSerialNumber() throws IOException {
        //return Long.toString(getClassFactory().getClass2IdentificationAndDemandData().getUMTRSN());
        return Long.toString(getClassFactory().getClass7MeteringFunctionBlock().getXMTRSN());
    }
    
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        List passwords = discoverInfo.getPasswords();
        
        if (DEBUG>=1) System.out.println("alphaPlus, getSerialNumber, "+discoverInfo);
        
        if (passwords==null)
            passwords = new ArrayList();
        
        if (passwords.size()==0)
            passwords.add("00000000");
        
        for (int i=0;i<passwords.size();i++) {
            String password = (String)passwords.get(i);
            try {
                Properties properties = new Properties();
                properties.setProperty(MeterProtocol.PASSWORD,password);
                setProperties(properties);
                init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
                connect();
                //getCommandFactory().getFunctionWithDataCommand().whoAreYou(0);
                String serialNumber =  Long.toString(getClassFactory().getSerialNumber()); //getSerialNumber();   
               // disconnect(); // no disconnect because the meter will hangup the link... disconnect contains an EZ7 protocol command to the meter that hangup the link!
                return serialNumber;
            }
            catch(IOException ex) {
                disconnect();
                if (i==(passwords.size()-1))
                    throw ex;
            }
        }
        throw new IOException("AlphaBasic, getSerialNumber(), Error discovering serialnumber!");
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
        strBuff.append(getClassFactory().getClass33ModemConfigurationInfo()+"\n");
        strBuff.append(getClassFactory().getClass6MeteringFunctionBlock()+"\n");
        strBuff.append(getClassFactory().getClass7MeteringFunctionBlock()+"\n");
        strBuff.append(getClassFactory().getClass8FirmwareConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass9Status1()+"\n");
        strBuff.append(getClassFactory().getClass10Status2()+"\n");
        strBuff.append(getClassFactory().getClass14LoadProfileConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass16LoadProfileHistory()+"\n");
        
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
    
    
   /*  
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {
         boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
        String sn = getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
        
    }    
    
    static public void main(String[] args) {
        AlphaBasic alphaBasic=null;
        Dialer dialer=null;
                         //  CITY OF LAGRANGE 
        String[] phones={   "00019799660593","00016202270906"};
        String[] passwords={"00000000","07041776"};
        
        
        final int selection=1; 
        
        
        try {
// ********************************** DIALER ***********************************$            
// modem dialup connection
//            dialer =DialerFactory.getDefault().newDialer();
//            dialer.init("COM1");//,"AT+MS=2,0,2400,2400");
//            dialer.getSerialCommunicationChannel().setParams(1200,
//                                                             SerialCommunicationChannel.DATABITS_8,
//                                                             SerialCommunicationChannel.PARITY_NONE,
//                                                             SerialCommunicationChannel.STOPBITS_1);
//            dialer.connect("phonenumber",60000);
            
// optical head connection
            dialer =DialerFactory.getOpticalDialer().newDialer();
            dialer.init("COM1");
            dialer.connect("",60000); 
            
// direct rs232 connection
//            dialer =DialerFactory.getDirectDialer().newDialer();
//            dialer.init("COM4");
//            dialer.connect("",60000); 
            //00018173853675
            
            //dialer.connect(phones[selection],90000);  
            //dialer.connect("4",60000); 
// *********************************** PROTOCOL ******************************************$            
            alphaBasic = new AlphaBasic(); // instantiate the protocol
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)alphaBasic).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            List passwordlist = new ArrayList();
            passwordlist.add("00000000");
            passwordlist.add("11111111");
            passwordlist.add("22222222");
            passwordlist.add("07041776");
            System.out.println("Serial number = "+alphaBasic.getSerialNumber(new DiscoverInfo(dialer.getSerialCommunicationChannel(),null,-1, passwordlist)));
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
            properties.setProperty("Timeout", "3000");
            
// transfer the properties to the protocol
            alphaBasic.setProperties(properties); 
            
//            ez7.setHalfDuplexController(dialer.getHalfDuplexController());
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(1200,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            alphaBasic.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("EST"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)alphaBasic).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            alphaBasic.connect();
            byte[] data=null;

//            System.out.println(alphaBasic.getClassFactory().getClass0ComputationalConfiguration());
//            System.out.println(alphaBasic.getClassFactory().getClass2IdentificationAndDemandData());
//            System.out.println(alphaBasic.getClassFactory().getClass33ModemConfigurationInfo());
//            System.out.println(alphaBasic.getClassFactory().getClass6MeteringFunctionBlock());
//            System.out.println(alphaBasic.getClassFactory().getClass7MeteringFunctionBlock());
//            System.out.println(alphaBasic.getClassFactory().getClass8FirmwareConfiguration());
//            System.out.println(alphaBasic.getClassFactory().getClass11BillingData());
//            System.out.println(alphaBasic.getClassFactory().getClass12PreviousMonthBillingData());
//            System.out.println(alphaBasic.getClassFactory().getClass13PreviousSeasonBillingData());
            System.out.println(alphaBasic.getClassFactory().getClass14LoadProfileConfiguration());
            System.out.println(alphaBasic.getClassFactory().getClass16LoadProfileHistory());
            System.out.println(alphaBasic.getClassFactory().getClass15TimeAdjustHistory());
//            System.out.println(alphaBasic.getClassFactory().getClass9Status1());
//            System.out.println(alphaBasic.getClassFactory().getClass10Status2());
//            System.out.println(alphaBasic.getRegistersInfo(0));
           
           //alphaBasic.getCommandFactory().getFunctionWithDataCommand().PacketSize(2);
//           System.out.println(alphaBasic.getClassFactory().getClass17LoadProfileData(6));
            
            
            //System.out.println(alphaBasic.getClassFactory().getClass15EventLogConfiguration());
            
            
            System.out.println("*********************** Meter information ***********************");
            //System.out.println(alphaBasic.getNumberOfChannels());
            //System.out.println(alphaBasic.getProtocolVersion());
            //System.out.println(alphaBasic.getProfileInterval());
        
// get the meter profile data            
//            System.out.println("*********************** getProfileData() ***********************");
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.DATE,-1);
//            System.out.println(alphaBasic.getProfileData(calendar.getTime(),true));
// get the metertime            
            System.out.println("*********************** getTime() ***********************");
            Date date = alphaBasic.getTime();  
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
                alphaBasic.disconnect();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }        
        
    }
  
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }  
    public AlphaConnection getAlphaConnection() {
        return alphaConnection;
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

    public AlphaBasicProfile getAlphaBasicProfile() {
        return alphaBasicProfile;
    }
    public void setDialinScheduleTime(Date date) throws IOException {
        getCommandFactory().getFunctionWithDataCommand().billingReadDialin(date,getTimeZone());        
    }    
    
    public int getTotalRegisterRate() {
        return totalRegisterRate;
    }

    private void setTotalRegisterRate(int totalRegisterRate) {
        this.totalRegisterRate = totalRegisterRate;
    }    
}
