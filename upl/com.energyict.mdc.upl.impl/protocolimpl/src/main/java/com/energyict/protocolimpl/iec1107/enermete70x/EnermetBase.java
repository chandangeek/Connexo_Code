/*
 * EnermetE70X.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.cbo.TimeZoneManager;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.DialerMarker;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
// com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X
/**
 *
 * @author  Koen
 *
 * Remark:
 * KV 08112004 HHU's getSerialNumber() implementation uses securitylevel 1 and password 1. Should have a public read. Mail has been send to Asko!
 */
abstract public class EnermetBase extends AbstractProtocol {
    
    IEC1107Connection iec1107Connection=null;
    DataReadingCommandFactory dataReadingCommandFactory=null;
    EnermetLoadProfile enermetLoadProfile=null;
    protected boolean software7E1;
    protected boolean testE70xConnection = false;
    
    abstract protected RegisterConfig getRegs();
    
    /** Creates a new instance of EnermetE70X */
    public EnermetBase() {
        super(false); // true for datareadout;
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getEnermetLoadProfile().getProfileData(lastReading, includeEvents);
    }
    
    public void connect() throws IOException {
    	if(isTestE70xConnection()) {
    	int logonRetries = getInfoTypeRetries();
       
    	try {
            while(logonRetries >= 0) {
            	try {
            		setMeterType(getProtocolConnection().connectMAC(getStrID(),getStrPassword(),getSecurityLevel(),getNodeId()));
            		doConnect();
            		testConnection();
            		break;
            	} catch (Exception e) {
                    logonRetries--;
            		((IEC1107Connection)getProtocolConnection()).setBoolFlagIEC1107Connected(false);
            		e.printStackTrace();
                    if (logonRetries < 0) {
                        throw new ProtocolConnectionException("Unable to connect to meter after " + getInfoTypeRetries() + " retries, " + e.getMessage());
                    }
                }
            }
        }
        catch(ProtocolConnectionException e) {
            throw new IOException(e.getMessage());
        }
        
        try {
            validateSerialNumber();
        }
        catch(ProtocolConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        }
        
        try {
            validateDeviceId();
        }
        catch(ProtocolConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        }
        
        if (getExtendedLogging() >= 1) 
           getLogger().info(getRegistersInfo(getExtendedLogging()));
    	} else {
    		super.connect();
    	}
    }
    
    private void testConnection() throws Exception {
    	try {
    		getFirmwareVersion();
		} catch (Exception e) {
			throw new ProtocolConnectionException("Unable to enter programming mode. An Enermet70x meter switches to DataReadout mode after 1500ms timeout !!!");
		}
	}

	protected void doConnect() throws IOException {
        dataReadingCommandFactory = new DataReadingCommandFactory(this);
    }

    protected void doDisConnect() throws IOException {
    }
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return getRegs().getRegisterInfoForId()+
            "1.1.0.4.4.255 = Overal transformer ratio (text\n" +
            "1.1.0.2.0.255 = Configuration program number (text)";
    }
    
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
       return getEnermetLoadProfile().getNrOfChannels();
    }
    
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("Software7E1");
        return result;
    }
    
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        iec1107Connection=new IEC1107Connection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,ERROR_SIGNATURE, software7E1);
        iec1107Connection.setNoBreakRetry(isTestE70xConnection());
        enermetLoadProfile = new EnermetLoadProfile(this);
        return iec1107Connection;
    }
    
    protected void doValidateProperties(Properties properties) throws com.energyict.protocol.MissingPropertyException, com.energyict.protocol.InvalidPropertyException {
        this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return getDataReadingCommandFactory().getFirmwareVersion();
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
         boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
        String sn = getDataReadingCommandFactory().getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNiumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }
    
    
    
    /*******************************************************************************************
     * m a i n ( )  i m p l e m e n t a t i o n ,  u n i t  t e s t i n g
     *******************************************************************************************/
    public static void main(String[] args) {
        Dialer dialer=null;
        EnermetE70X enermetE70X=null;
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
            enermetE70X = new EnermetE70X(); // instantiate the protocol
            
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
            enermetE70X.setProperties(properties);
            
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
            enermetE70X.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZoneManager.getTimeZone("GMT"),Logger.getLogger("name"));
            
            // if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)enermetE70X).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("*********************** connect() ***********************");
            
            //for (int i=0;i<100;i++) {
                
                // connect to the meter
                enermetE70X.connect();
                //System.out.println(enermetE70X.getFirmwareVersion());
                //System.out.println(enermetE70X.getTime());
                
                System.out.println(enermetE70X.getDataReadingCommandFactory().getSerialNumber());
                
                
                //            System.out.println(enermetE70X.getDataReadingCommandFactory().getRegisterSet(0).toString());
                //            System.out.println(enermetE70X.getDataReadingCommandFactory().getRegisterSet(1).toString());
                //            System.out.println(enermetE70X.getDataReadingCommandFactory().getRegisterSet(2).toString());
                //            System.out.println(enermetE70X.getDataReadingCommandFactory().getRegisterSet(3).toString());
                
                byte[] ba;
                String strBa;
                //            enermetE70X.getIec1107Connection().sendRawCommandFrame(IEC1107Connection.READ1,"HSR(1,100,2004,10,27,00,00,00)".getBytes());
                //            ba = enermetE70X.getIec1107Connection().receiveRawData();
                //            strBa = new String(ba);
                //            System.out.println(strBa);
                //            enermetE70X.getIec1107Connection().sendRawCommandFrame(IEC1107Connection.READ1,"HSR(1,96,2004,10,28,00,00,00)".getBytes());
                //            ba = enermetE70X.getIec1107Connection().receiveRawData();
                //            strBa = new String(ba);
                //            System.out.println(strBa);
                //            enermetE70X.getIec1107Connection().sendRawCommandFrame(IEC1107Connection.READ1,"HSZ(1,96,2004,10,28,00,00,00)".getBytes());
                //            ba = enermetE70X.getIec1107Connection().receiveRawData();
                //            strBa = new String(ba);
                //            System.out.println(strBa);
                //    System.out.println(enermetE70X.getEnermetLoadProfile().getProfileData(new Date((new Date()).getTime()-(3600000L*24*3))));
                //System.out.println("nr of channels:"+enermetE70X.getEnermetLoadProfile().getNrOfChannels());
                //System.out.println(enermetE70X.readRegister(ObisCode.fromString("1.1.1.8.0.255")));
                //System.out.println(enermetE70X.readRegister(ObisCode.fromString("1.1.1.8.0.0")));
                //System.out.println(enermetE70X.readRegister(ObisCode.fromString("1.1.1.8.0.1")));
                //System.out.println(enermetE70X.readRegister(ObisCode.fromString("1.1.5.6.2.255")));
                
                //enermetE70X.setTime();
                System.out.println(enermetE70X.getTime());
                
                //enermetE70X.getDataReadingCommandFactory().getEventLog();
                //Calendar calendar = Calendar.getInstance(TimeZoneManager.getTimeZone("GMT"));
                //calendar.add(Calendar.HOUR_OF_DAY,-2);
                //calendar.add(Calendar.MINUTE, -3);
                
                //enermetE70X.getEnermetLoadProfile().retrieveEventLog(calendar.getTime());
                //System.out.println(enermetE70X.getProfileData(calendar.getTime(),false));
                
                // temporary...
             //   enermetE70X.disconnect();
                
            //} // for (int i=0;i<100;i++)
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                System.out.println("*********************** disconnect() ***********************");
                enermetE70X.disconnect();
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
    public com.energyict.protocolimpl.iec1107.enermete70x.DataReadingCommandFactory getDataReadingCommandFactory() {
        return dataReadingCommandFactory;
    }
    
    
    /*******************************************************************************************
     * R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getDataReadingCommandFactory(),getTimeZone(),getRegs());
        return ocm.getRegisterValue(obisCode);
    }
    
    /**
     * Getter for property enermetLoadProfile.
     * @return Value of property enermetLoadProfile.
     */
    public com.energyict.protocolimpl.iec1107.enermete70x.EnermetLoadProfile getEnermetLoadProfile() {
        return enermetLoadProfile;
    }
    
    public boolean isTestE70xConnection() {
		return testE70xConnection;
	}
    
    public void setTestE70xConnection(boolean testE70xConnection) {
		this.testE70xConnection = testE70xConnection;
	}
    
} // class EnermetE70X
