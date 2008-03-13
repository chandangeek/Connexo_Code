/*
 * IndigoPlus.java
 *
 * Created on 5 juli 2004, 14:56
 */

package com.energyict.protocolimpl.sampleiec1107.indigo;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.energyict.protocolimpl.sampleiec1107.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;


// KV TO_DO
/*
 * Scaling of registers and profiledata using MeterDefinition and HistoricalData OK
 * Behaviour of whole current meters OK
 * Implementation and mapping of registers OK
 * Test with IMServ production meters OK
 * Set time!!! OK
 * Interpretation of profile daily flags and channel status flags OK
 * Test powerfail behaviour
 * Determine difference between gaps and zero-consumption
 * Password encryption level 2 and 3?
 */

/**
 *
 * @author  Koen
 */
public class IndigoPlus extends AbstractProtocol {
    
    private static final int DEBUG=0;
    
    LogicalAddressFactory logicalAddressFactory;
    IndigoProfile indigoProfile;
    FlagIEC1107Connection flagIEC1107Connection=null;
    
    /** Creates a new instance of IndigoPlus */
    public IndigoPlus() {
        super(false,new Encryption());
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendarFrom = ProtocolUtils.getCalendar(getTimeZone());
        calendarFrom.setTime(lastReading);
        Calendar calendarTo = ProtocolUtils.getCalendar(getTimeZone());
        calendarTo.setTime(new Date());
        return indigoProfile.getProfileData(calendarFrom.getTime(),calendarTo.getTime());
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
       return getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels();
    }
    
    protected void doConnect() throws java.io.IOException {
        logicalAddressFactory = new LogicalAddressFactory(this,(MeterExceptionInfo)this);
        indigoProfile = new IndigoProfile(this,(MeterExceptionInfo)this,logicalAddressFactory);
    }    
    
    protected void doDisConnect() throws IOException {
    }
    
    /*
     *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("************************* Extended Logging *************************\n");
        strBuff.append(getLogicalAddressFactory().getMeterIdentity().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getMeterStatus().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDefaultStatus().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDateTimeGMT().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDateTimeLocal().toString()+"\n");
        
        strBuff.append(getLogicalAddressFactory().getTotalRegisters(extendedLogging-1).toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getRateRegisters(extendedLogging-1).toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDemandRegisters(extendedLogging-1).toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDefaultRegisters(extendedLogging-1).toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getHistoricalData(extendedLogging-1).toString()+"\n");
        
        strBuff.append(getLogicalAddressFactory().getMeteringDefinition().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getClockDefinition().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getBillingPeriodDefinition().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getGeneralMeterData().toString()+"\n");
        
        
        for (int i=-1;i<16;i++) {
            int billingPoint;
            if (i==-1) billingPoint = 255;
            else billingPoint = i;
            strBuff.append("Cumulative registers (total & tariff):\n");
            for(int obisC=1;obisC<=8;obisC++) {
                String code = "1.1."+obisC+".8.0."+billingPoint;
                strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
            }
            for(int obisC=1;obisC<=2;obisC++) {
                for(int obisE=1;obisE<=8;obisE++) {
                    String code = "1.1."+obisC+".8."+obisE+"."+billingPoint;
                    strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
                }
            }
            
            strBuff.append("Cumulative maximum demand registers:\n");
            strBuff.append(buildDemandReg(0,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(1,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(2,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(3,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            
            strBuff.append("Current average registers:\n");
            strBuff.append(buildDemandReg(0,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(1,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(2,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(3,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            
            strBuff.append("Maximum demand registers:\n");
            strBuff.append(buildDemandReg(0,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(1,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(2,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(3,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            
            strBuff.append("General purpose registers:\n");
            if( billingPoint != 255) {
                String code = "1.1.0.1.2."+billingPoint;
                strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
            }
            if( billingPoint == 255) {
               String code = "1.1.0.1.0.255";
               strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
            }
        }
        return strBuff.toString();
    }
    
    private String buildDemandReg(int index, int obisD, int billingPoint) throws IOException {
       int obisC = getLogicalAddressFactory().getHistoricalData(billingPoint==255?0:billingPoint).getObisC(index);
       if (obisC != 255) {
          String code = "1.1."+obisC+"."+obisD+".0."+billingPoint;
          return code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n";
       }
       return "";
    }
    
    public Date getTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        calendar.setTime(getLogicalAddressFactory().getDateTimeGMT().getDate());
        return calendar.getTime();
    }
    
    public void setTime() throws IOException {
        Calendar calendar=null;
        calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND,getInfoTypeRoundtripCorrection());
        setTime(calendar.getTime()); 
    }
    
    private void setTime(Date date) throws IOException {
        getLogicalAddressFactory().setDateTimeGMT(date); 
    }
    
    protected java.util.List doGetOptionalKeys() {
        return null;
    }
    
    protected void doValidateProperties(java.util.Properties properties) throws com.energyict.protocol.MissingPropertyException, com.energyict.protocol.InvalidPropertyException {
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.9 $";
    }
    
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return getLogicalAddressFactory().getMeterIdentity().getSoftwareVersionNumber();
    }

    /*
     *  doInit() is called from the MeterProtocol init() method. 
     *  Here we must instantiate our low level communication implementation class. 
     *  @return ProtocolConnection interface
     */ 
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        flagIEC1107Connection=new FlagIEC1107Connection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor);
        return flagIEC1107Connection;	
    }
    
    
    /**
     * Getter for property flagIEC1107Connection, the low level communication implementation class.
     * @return Value of property flagIEC1107Connection.
     */
    public com.energyict.protocolimpl.sampleiec1107.FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }
    
    /**
     * Getter for property logicalAddressFactory.
     * @return Value of property logicalAddressFactory.
     */
    public com.energyict.protocolimpl.sampleiec1107.indigo.LogicalAddressFactory getLogicalAddressFactory() {
        return logicalAddressFactory;
    }    
    
    /*******************************************************************************************
     M e t e r E x c e p t i o n I n f o  i n t e r f a c e 
     *******************************************************************************************/
    /*
     *  This method must be overridden by the subclass to implement meter specific error
     *  messages. Us sample code of a static map with error codes below as a sample and 
     *  use code in method as a sample of how to retrieve the error code.  
     *  This code has been taken from a real protocol implementation.
     */

    static Map exceptionInfoMap = new HashMap();
    static {
           exceptionInfoMap.put("ERRDAT","Error setting the time");
           exceptionInfoMap.put("ERRADD","Protocol error");
    }
 
    public String getExceptionInfo(String id) {
        
        String exceptionInfo = (String)exceptionInfoMap.get(id);
        if (exceptionInfo != null)
           return id+", "+exceptionInfo;
        else
           return "No meter specific exception info for "+id; 
    }        
    
    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getLogicalAddressFactory());
        return ocm.getRegisterValue(obisCode);
    }
    

    /*******************************************************************************************
     m a i n ( )  i m p l e m e n t a t i o n ,  u n i t  t e s t i n g
     *******************************************************************************************/
    public static void main(String[] args) {
        Dialer dialer=null;
        IndigoPlus indigoPlus=null;
        try {

// ********************************** DIALER ***********************************$            
// modem dialup connection
//            dialer =DialerFactory.getDefault().newDialer();
//            dialer.init("COM1","AT+MS=2,0,2400,2400");
//            dialer.getSerialCommunicationChannel().setParams(2400,
//                                                             SerialCommunicationChannel.DATABITS_7,
//                                                             SerialCommunicationChannel.PARITY_EVEN,
//                                                             SerialCommunicationChannel.STOPBITS_1);
//            dialer.connect("phonenumber",60000);
            
// optical head connection
//            dialer =DialerFactory.getOpticalDialer().newDialer();
//            dialer.init("COM1");
//            dialer.connect("",60000); 
            
// direct rs232 connection
            dialer =DialerFactory.getDirectDialer().newDialer();
            dialer.init("COM1");
            dialer.connect("",60000); 
            
// *********************************** PROTOCOL ******************************************$            
            indigoPlus = new IndigoPlus(); // instantiate the protocol

// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            properties.setProperty("SecurityLevel","2");
            properties.setProperty(MeterProtocol.PASSWORD,"ABCDEF"); //13579B"); //"123456");
            properties.setProperty("ProfileInterval", "1800");
            
// depending on the dialer, set the NodeAddress property         
            if (DialerMarker.hasModemMarker(dialer))
              properties.setProperty(MeterProtocol.NODEID,"002");
            else
              properties.setProperty(MeterProtocol.NODEID,"");
            
// transfer the properties to the protocol
            indigoPlus.setProperties(properties); 
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            if (DialerMarker.hasModemMarker(dialer))
                dialer.getSerialCommunicationChannel().setParamsAndFlush(2400,
                                                                         SerialCommunicationChannel.DATABITS_7,
                                                                         SerialCommunicationChannel.PARITY_EVEN,
                                                                         SerialCommunicationChannel.STOPBITS_1);
            else
                dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                                         SerialCommunicationChannel.DATABITS_7,
                                                                         SerialCommunicationChannel.PARITY_EVEN,
                                                                         SerialCommunicationChannel.STOPBITS_1);
                
// initialize the protocol
            indigoPlus.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("GMT"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)indigoPlus).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            indigoPlus.connect();
            
// read raw meterregisterdata (only for debugging)
            System.out.println("*********************** DEBUG ***********************");            
            System.out.println(indigoPlus.getLogicalAddressFactory().getMeterIdentity().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getMeterStatus().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDefaultStatus().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDateTimeGMT().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDateTimeLocal().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getTotalRegisters().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getRateRegisters().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDemandRegisters().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDefaultRegisters().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getHistoricalData().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getMeteringDefinition().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getClockDefinition().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getBillingPeriodDefinition().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getGeneralMeterData().toString());
            
// get obis register
            System.out.println("*********************** readRegister() ***********************");            
            System.out.println("1.1.1.8.0.255 = "+indigoPlus.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
            
// get the meter profile data            
            System.out.println("*********************** getProfileData() ***********************");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,-1);
            System.out.println(indigoPlus.getProfileData(calendar.getTime(),true).toString());
// get the metertime            
            System.out.println("*********************** getTime() ***********************");
            Date date = indigoPlus.getTime();
            System.out.println(date);
// set the metertime            
            System.out.println("*********************** setTime() ***********************");
            indigoPlus.setTime();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                System.out.println("*********************** disconnect() ***********************");
                indigoPlus.disconnect();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    } // main
 }
