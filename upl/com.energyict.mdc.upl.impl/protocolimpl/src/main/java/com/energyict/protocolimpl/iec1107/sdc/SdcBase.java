/*
 * Sdc.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.sdc;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import com.energyict.cbo.TimeZoneManager;
import com.energyict.dialer.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;
// com.energyict.protocolimpl.iec1107.sdc.Sdc
/**
 *
 * @author gna
 * <B>@beginchanges</B><BR>
 * GN|29012008| Adjusted the readRegisters according to the spec
 *@endchanges
 *
 */
abstract public class SdcBase extends AbstractProtocol {
    
    IEC1107Connection iec1107Connection=null;
    DataReadingCommandFactory dataReadingCommandFactory=null;
    SdcLoadProfile sdcLoadProfile=null;
    ObisCodeMapper ocm = null;
	private int iSecurityLevelProperty;
	private int extendedLogging;
    
    
    abstract protected RegisterConfig getRegs();
    
    /** Creates a new instance of Sdc */
    public SdcBase() {
        super(false); // true for datareadout;
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getSdcLoadProfile().getProfileData(lastReading, includeEvents);
    }
    
    protected void doConnect() throws IOException {
        dataReadingCommandFactory = new DataReadingCommandFactory(this);
//        if (extendedLogging >= 1) 
//            getRegistersInfo();
    }
    
    private void getRegistersInfo() throws IOException{
//        StringBuffer strBuff = new StringBuffer();
////        if (getDataReadoutRequest()==1) {
////            strBuff.append("******************* ExtendedLogging *******************\n");
////            strBuff.append(new String(getDataReadout()));
////        }
////        else {
//            strBuff.append("******************* ExtendedLogging *******************\n");
//            strBuff.append("1.0.1.8.128.255: Active Energy tariff HV" + "\n");
//            strBuff.append("1.0.1.8.129.255: Active Energy tariff HP" + "\n");
//            strBuff.append("1.0.1.8.130.255: Active Energy tariff HC" + "\n");
//            strBuff.append("1.0.1.8.131.255: Active Energy tariff HSV" + "\n");
//            strBuff.append("1.0.3.8.128.255: Reactive Energy inductive tariff HV" + "\n");
//            strBuff.append("1.0.3.8.132.255: Reactive Energy inductive tariff HFV" + "\n");
//            strBuff.append("1.0.4.8.128.255: Reactive Energy capacitive tariff HV" + "\n");
//            strBuff.append("1.0.4.8.132.255: Reactive Energy capacitive tariff HFV" + "\n");
//            strBuff.append("1.0.1.6.128.255: Active Energy maximum demand tariff HV" + "\n");
//            strBuff.append("1.0.1.6.132.255: Active Energy maximum demand tariff HFV" + "\n");
//            strBuff.append("1.0.3.6.128.255: Reactive Energy maximum demand inductive tariff HV" + "\n");
//            strBuff.append("1.0.3.6.132.255: Reactive Energy maximum demand inductive tariff HFV" + "\n");
//            strBuff.append("1.0.4.6.128.255: Reactive Energy maximum demand capacitive tariff HV" + "\n");
//            strBuff.append("1.0.4.6.132.255: Reactive Energy maximum demand capacitive tariff HFV" + "\n");
//            strBuff.append("1.0.1.8.0.255: Active Energy total (all phases)" + "\n");
//            strBuff.append("1.0.3.8.0.255: Reactive Energy inductive total (all phases)" + "\n");
//            strBuff.append("1.0.4.8.0.255: Reactive Energy capacitive total (all phases)" + "\n");
//            strBuff.append("*******************************************************\n");
////        }
//        getLogger().info(strBuff.toString());
    }

    protected void doDisConnect() throws IOException {
    }
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
    	StringBuffer strBuff = new StringBuffer();
    	strBuff.append("******************* ExtendedLogging *******************\n");
    	strBuff.append("1.0.1.8.128.255: Active Energy tariff HV" + "\n");
    	strBuff.append("1.0.1.8.129.255: Active Energy tariff HP" + "\n");
    	strBuff.append("1.0.1.8.130.255: Active Energy tariff HC" + "\n");
    	strBuff.append("1.0.1.8.131.255: Active Energy tariff HSV" + "\n");
    	strBuff.append("1.0.3.8.128.255: Reactive Energy inductive tariff HV" + "\n");
    	strBuff.append("1.0.3.8.132.255: Reactive Energy inductive tariff HFV" + "\n");
    	strBuff.append("1.0.4.8.128.255: Reactive Energy capacitive tariff HV" + "\n");
    	strBuff.append("1.0.4.8.132.255: Reactive Energy capacitive tariff HFV" + "\n");
    	strBuff.append("1.0.1.6.128.255: Active Energy maximum demand tariff HV" + "\n");
    	strBuff.append("1.0.1.6.132.255: Active Energy maximum demand tariff HFV" + "\n");
    	strBuff.append("1.0.3.6.128.255: Reactive Energy maximum demand inductive tariff HV" + "\n");
    	strBuff.append("1.0.3.6.132.255: Reactive Energy maximum demand inductive tariff HFV" + "\n");
    	strBuff.append("1.0.4.6.128.255: Reactive Energy maximum demand capacitive tariff HV" + "\n");
    	strBuff.append("1.0.4.6.132.255: Reactive Energy maximum demand capacitive tariff HFV" + "\n");
    	strBuff.append("1.0.1.8.0.255: Active Energy total (all phases)" + "\n");
    	strBuff.append("1.0.3.8.0.255: Reactive Energy inductive total (all phases)" + "\n");
    	strBuff.append("1.0.4.8.0.255: Reactive Energy capacitive total (all phases)" + "\n");
    	strBuff.append("*******************************************************\n");
    	return strBuff.toString();
    }
    
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
       return getSdcLoadProfile().getNrOfChannels();
    }
    
    protected List doGetOptionalKeys() {
        return null;
    }
    
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {

        iec1107Connection=new IEC1107Connection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,ERROR_SIGNATURE);
        sdcLoadProfile = new SdcLoadProfile(this);
        iec1107Connection.setChecksumMethod(1);
        
        
//        getSdcLoadProfile().setNrOfChannels(3);
        getSdcLoadProfile().setNrOfChannels(1);

        return iec1107Connection;
    }
    
    protected void doValidateProperties(Properties properties) throws com.energyict.protocol.MissingPropertyException, com.energyict.protocol.InvalidPropertyException {
//    	properties.setProperty("SecurityLevel","0");    	
    	extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0").trim());
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "Unknown";
    }
    
    public Date getTime() throws IOException {
        // KV_DEBUG
//        TimeZone tz = getDataReadingCommandFactory().getTimeZoneRead();
//        System.out.println(tz.getRawOffset());
//        System.out.println(tz.getDisplayName());
//        System.out.println(tz);
        return getDataReadingCommandFactory().getDateTimeGmt();
    }
    
    public void setTime() throws IOException {
        //Calendar calendar = ProtocolUtils.getCalendar(TimeZoneManager.getTimeZone("GMT"));
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND,getInfoTypeRoundtripCorrection());
        getDataReadingCommandFactory().setDateTimeGmt(calendar.getTime());
    }
    
    
    /*******************************************************************************************
     * g e t t e r s  a n d  s e t t e r s
     *******************************************************************************************/
    
    /**
     * Getter for property iec1107Connection.
     * @return Value of property iec1107Connection.
     */
    public com.energyict.protocolimpl.iec1107.IEC1107Connection getIec1107Connection() {
        return iec1107Connection;
    }
    
    
    //    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
    //        enableHHUSignOn(commChannel,true);
    //    }
    
    /*******************************************************************************************
     * M e t e r E x c e p t i o n I n f o  i n t e r f a c e
     *******************************************************************************************/
    /*
     *  This method must be overridden by the subclass to implement meter specific error
     *  messages. Us sample code of a static map with error codes below as a sample and
     *  use code in method as a sample of how to retrieve the error code.
     *  This code has been taken from a real protocol implementation.
     */
    
    static public final String COMMAND_CANNOT_BE_EXECUTED="([4])";
    static public final String ERROR_SIGNATURE="([";
    
    static Map exceptionInfoMap = new HashMap();
    static {
        exceptionInfoMap.put("([1])","General error, insufficient access rights");
        exceptionInfoMap.put("([2])","The nr of command parameters is not correct");
        exceptionInfoMap.put("([3])","The value of a command parameters is not valid");
        exceptionInfoMap.put(COMMAND_CANNOT_BE_EXECUTED,"The command is formally correct, but it cannot be executed in this context");
        exceptionInfoMap.put("([6])","EEPROM write error");
        exceptionInfoMap.put("([7])","Core communication error");
    }
    
    public String getExceptionInfo(String id) {
        
        String exceptionInfo = (String)exceptionInfoMap.get(id);
        if (exceptionInfo != null)
            return id+", "+exceptionInfo;
        else
            return "No meter specific exception info for "+id;
    }
    
    /*  
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {

    	ObisCode oc = new ObisCode(1,0,0,0,0,255);
    	String str = readRegister(oc).getText();
    	str = str.substring(str.indexOf(",") + 2 );
    	if(!getInfoTypeNodeAddress().equalsIgnoreCase("")){
	    	if ( str.compareTo(getInfoTypeNodeAddress().substring(getInfoTypeNodeAddress().indexOf(str.charAt(0)))) == -1 )
	    		throw new IOException("Incorrect node Address!");
    	}
    	else 
    		throw new IOException("Incorrect node Address!");
	    
    	return;
    }
    
    
    
    /*******************************************************************************************
     * m a i n ( )  i m p l e m e n t a t i o n ,  u n i t  t e s t i n g
     *******************************************************************************************/
    private static void main(String[] args) {
        Dialer dialer=null;
        Sdc sdc=null;
        try {
            
            // ********************************** DIALER ***********************************$
            // modem dialup connection
            //            dialer =DialerFactory.getDefault().newDialer();
            //            dialer.init("COM1","AT+MS=2,0,1200,1200");
            //            dialer.getSerialCommunicationChannel().setParams(1200,
            //                                                             SerialCommunicationChannel.DATABITS_7,
            //                                                             SerialCommunicationChannel.PARITY_EVEN,
            //                                                             SerialCommunicationChannel.STOPBITS_1);
            //            dialer.connect("000351249559970",60000);
            //
            // optical head connection
            dialer =DialerFactory.getOpticalDialer().newDialer();
            dialer.init("COM1");
            dialer.connect("",60000);
            
            // direct rs232 connection
            //            dialer =DialerFactory.getDirectDialer().newDialer();
            //            dialer.init("COM1");
            //            dialer.connect("",60000);
            
            // *********************************** PROTOCOL ******************************************$
            sdc = new Sdc(); // instantiate the protocol
            
            // setup the properties (see AbstractProtocol for default properties)
            // protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            properties.setProperty("SecurityLevel","1");
            properties.setProperty(MeterProtocol.PASSWORD,"2");
            properties.setProperty("ProfileInterval", "900");
            
            // depending on the dialer, set the NodeAddress property
            if (DialerMarker.hasModemMarker(dialer))
                properties.setProperty(MeterProtocol.NODEID,"");
            else
                properties.setProperty(MeterProtocol.NODEID,"");
            
            // transfer the properties to the protocol
            sdc.setProperties(properties);
            
            // depending on the dialer, set the initial (pre-connect) communication parameters
            if (DialerMarker.hasModemMarker(dialer))
                dialer.getSerialCommunicationChannel().setParamsAndFlush(1200,
                SerialCommunicationChannel.DATABITS_7,
                SerialCommunicationChannel.PARITY_EVEN,
                SerialCommunicationChannel.STOPBITS_1);
            else
                dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                SerialCommunicationChannel.DATABITS_7,
                SerialCommunicationChannel.PARITY_EVEN,
                SerialCommunicationChannel.STOPBITS_1);
            
            // initialize the protocol
            sdc.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZoneManager.getTimeZone("GMT"),Logger.getLogger("name"));
            
            // if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)sdc).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("*********************** connect() ***********************");
            
            //for (int i=0;i<100;i++) {
                
                // connect to the meter
                sdc.connect();
                //System.out.println(Sdc.getFirmwareVersion());
                //System.out.println(Sdc.getTime());
                
//                System.out.println(sdc.getDataReadingCommandFactory().getSerialNumber());
                
                
                //            System.out.println(Sdc.getDataReadingCommandFactory().getRegisterSet(0).toString());
                //            System.out.println(Sdc.getDataReadingCommandFactory().getRegisterSet(1).toString());
                //            System.out.println(Sdc.getDataReadingCommandFactory().getRegisterSet(2).toString());
                //            System.out.println(Sdc.getDataReadingCommandFactory().getRegisterSet(3).toString());
                
                byte[] ba;
                String strBa;
                //            Sdc.getIec1107Connection().sendRawCommandFrame(IEC1107Connection.READ1,"HSR(1,100,2004,10,27,00,00,00)".getBytes());
                //            ba = Sdc.getIec1107Connection().receiveRawData();
                //            strBa = new String(ba);
                //            System.out.println(strBa);
                //            Sdc.getIec1107Connection().sendRawCommandFrame(IEC1107Connection.READ1,"HSR(1,96,2004,10,28,00,00,00)".getBytes());
                //            ba = Sdc.getIec1107Connection().receiveRawData();
                //            strBa = new String(ba);
                //            System.out.println(strBa);
                //            Sdc.getIec1107Connection().sendRawCommandFrame(IEC1107Connection.READ1,"HSZ(1,96,2004,10,28,00,00,00)".getBytes());
                //            ba = Sdc.getIec1107Connection().receiveRawData();
                //            strBa = new String(ba);
                //            System.out.println(strBa);
                //    System.out.println(Sdc.getEnermetLoadProfile().getProfileData(new Date((new Date()).getTime()-(3600000L*24*3))));
                //System.out.println("nr of channels:"+Sdc.getEnermetLoadProfile().getNrOfChannels());
                //System.out.println(Sdc.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
                //System.out.println(Sdc.readRegister(ObisCode.fromString("1.1.1.8.0.0")));
                //System.out.println(Sdc.readRegister(ObisCode.fromString("1.1.1.8.0.1")));
                //System.out.println(Sdc.readRegister(ObisCode.fromString("1.1.5.6.2.255")));
                
                //Sdc.setTime();
                System.out.println(sdc.getTime());
                
                //Sdc.getDataReadingCommandFactory().getEventLog();
                //Calendar calendar = Calendar.getInstance(TimeZoneManager.getTimeZone("GMT"));
                //calendar.add(Calendar.HOUR_OF_DAY,-2);
                //calendar.add(Calendar.MINUTE, -3);
                
                //Sdc.getEnermetLoadProfile().retrieveEventLog(calendar.getTime());
                //System.out.println(Sdc.getProfileData(calendar.getTime(),false));
                
                // temporary...
             //   Sdc.disconnect();
                
            //} // for (int i=0;i<100;i++)
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                System.out.println("*********************** disconnect() ***********************");
                sdc.disconnect();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Getter for property dataReadingCommandFactory.
     * @return Value of property dataReadingCommandFactory.
     */
    public com.energyict.protocolimpl.iec1107.sdc.DataReadingCommandFactory getDataReadingCommandFactory() {
        return dataReadingCommandFactory;
    }
    
    
    /*******************************************************************************************
     * R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	
    	if (ocm == null)
    		ocm = new ObisCodeMapper(getDataReadingCommandFactory(),getTimeZone(),getRegs());
        
        return ocm.getRegisterValue(obisCode);
    }
    
    /**
     * Getter for property sdcLoadProfile.
     * @return Value of property sdcLoadProfile.
     */
    public com.energyict.protocolimpl.iec1107.sdc.SdcLoadProfile getSdcLoadProfile() {
        return sdcLoadProfile;
    }
    
    
    
} // class Sdc
