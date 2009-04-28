/*
 * Unigas300.java
 *
 * Created on 8 mei 2003, 17:56
 */

package com.energyict.protocolimpl.iec1107.kamstrup.unigas300;

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

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * @author   jme
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the Kamstrup Unigas 300 meter protocol.
 * <BR>
 * @beginchanges
 *	JME	|28042009|	Initial protocol version
 *
 */
public class Unigas300 implements MeterProtocol, ProtocolLink, RegisterProtocol { //,CommunicationParameters {
    
    private static final byte DEBUG=0;
    
    private static final int KAMSTRUP_NR_OF_CHANNELS=11;
    private static final String[] KAMSTRUP_METERREADINGS_979D1 = {"23.2.0","13.1.0","1:13.0.0","0:41.0.0","0:42.0.0","97.97.0"};
    private static final String[] KAMSTRUP_METERREADINGS_979E1 = {"23.2.0","1:12.0.0","1:13.0.0","0:41.0.0","0:42.0.0","97.97.0"};
    private static final String[] KAMSTRUP_METERREADINGS_979A1 = {"23.2.0","13.1.0","1:13.0.0","0:41.0.0","0:42.0.0","97.97.0"};
    private static final String[] KAMSTRUP_METERREADINGS_DEFAULT = {"23.2.0","13.1.0","1:13.0.0","0:41.0.0","0:42.0.0","97.97.0"};

    private String strID;
    private String strPassword;
    private int iIEC1107TimeoutProperty; 
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int iProfileInterval;
    private boolean software7E1;
    private TimeZone timeZone;
    private Logger logger;

    private String deviceSerialNumber = null;
    private String serialNumber = null;

    FlagIEC1107Connection flagIEC1107Connection=null;
    Unigas300Registry unigas300Registry=null;
    Unigas300Profile unigas300Profile=null;
    int extendedLogging;
            
    byte[] dataReadout=null;
    
    /** Creates a new instance of Unigas300, empty constructor*/
    public Unigas300() {
    } // public Unigas300()

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR,-10);
          return doGetProfileData(calendar.getTime(),includeEvents);
    }

    public ProfileData getProfileData(Date lastReading,boolean includeEvents) throws IOException
    {
        return doGetProfileData(lastReading,includeEvents);
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,UnsupportedException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(from);
        Calendar toCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        toCalendar.setTime(to);
        return getUnigas300Profile().getProfileData(fromCalendar,
                                                   toCalendar,
                                                   getNumberOfChannels(),
                                                   1);
    }
    
    private ProfileData doGetProfileData(Date lastReading,boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return getUnigas300Profile().getProfileData(fromCalendar,
                                                   ProtocolUtils.getCalendar(timeZone),
                                                   getNumberOfChannels(),
                                                   1);
    }
    
    // Only for debugging
    public ProfileData getProfileData(Calendar from,Calendar to) throws IOException {
        return getUnigas300Profile().getProfileData(from,
                                                   to,
                                                   getNumberOfChannels(),
                                                   1);
    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        try {
           return (Quantity)getUnigas300Registry().getRegister(name);
        }
        catch(ClassCastException e) {
           throw new IOException("Unigas300, getMeterReading, register "+name+" is not type Quantity");
        }
    }
    
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        String[] KAMSTRUP_METERREADINGS=null;
        try {
            String revision = (String)getUnigas300Registry().getRegister("UNIGAS software revision number");
            if ("979D1".compareTo(revision) == 0)
                KAMSTRUP_METERREADINGS = KAMSTRUP_METERREADINGS_979D1;
            else if ("979E1".compareTo(revision) == 0)
                KAMSTRUP_METERREADINGS = KAMSTRUP_METERREADINGS_979E1;
            else if ("979A1".compareTo(revision) == 0)
                KAMSTRUP_METERREADINGS = KAMSTRUP_METERREADINGS_979A1;
            else
                KAMSTRUP_METERREADINGS = KAMSTRUP_METERREADINGS_DEFAULT;
            
            if (channelId >= getNumberOfChannels())
                throw new IOException("Unigas300, getMeterReading, invalid channelId, "+channelId);
            return (Quantity)getUnigas300Registry().getRegister(KAMSTRUP_METERREADINGS[channelId]);
        }
        catch(ClassCastException e) {
           throw new IOException("Unigas300, getMeterReading, register "+KAMSTRUP_METERREADINGS[channelId]+" ("+channelId+") is not type Quantity");
        }
    } 
    
/**
 * This method sets the time/date in the remote meter equal to the system time/date of the machine where this object resides.
 * @exception IOException 
 */
    public void setTime() throws IOException {
       Calendar calendar=null;
       calendar = ProtocolUtils.getCalendar(timeZone);
       calendar.add(Calendar.MILLISECOND,iRoundtripCorrection); 
       Date date = calendar.getTime();
       getUnigas300Registry().setRegister("0.9.1",date);
       getUnigas300Registry().setRegister("0.9.2",date);
    } // public void setTime() throws IOException
    
    public Date getTime() throws IOException {
        if (DEBUG >= 1) System.out.println("DEBUG: Entering Unigas300, getTime()");
    	Date date =  (Date)getUnigas300Registry().getRegister("TimeDate");
        return new Date(date.getTime()-iRoundtripCorrection);
    }
    
    public byte getLastProtocolState(){
        return -1;
    }
    
    /************************************** MeterProtocol implementation ***************************************/
    
    /** this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     * @see AbstractMeterProtocol#validateProperties
     */    
    public void setProperties(Properties properties) throws MissingPropertyException , InvalidPropertyException {
        validateProperties(properties);
    }
    
    /** <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */    
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException
    {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext())
            { 
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException (key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            iIEC1107TimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","20000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","5").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iSecurityLevel=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());
            nodeId=properties.getProperty(MeterProtocol.NODEID,"");
            iEchoCancelling=Integer.parseInt(properties.getProperty("EchoCancelling","0").trim());
            iIEC1107Compatible=Integer.parseInt(properties.getProperty("IEC1107Compatible","1").trim());
            iProfileInterval=Integer.parseInt(properties.getProperty("ProfileInterval","3600").trim());
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0").trim());
            this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
            this.serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
        }
        catch (NumberFormatException e) {
           throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, "+e.getMessage());    
        }
        
    }
    
    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @return the register value
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     * @throws NoSuchRegisterException <br>
     */    
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        return ProtocolUtils.obj2String(getUnigas300Registry().getRegister(name));
    }
    
    /** this implementation throws UnsupportedException. Subclasses may override
     * @param name <br>
     * @param value <br>
     * @throws IOException <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException <br>
     */    
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        getUnigas300Registry().setRegister(name,value);
    }    

    /** this implementation throws UnsupportedException. Subclasses may override
     * @throws IOException <br>
     * @throws UnsupportedException <br>
     */    
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }
    
    /** the implementation returns both the address and password key
     * @return a list of strings
     */    
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }
     
    /** this implementation returns an empty list
     * @return a list of strings
     */    
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("IEC1107Compatible");
        result.add("ExtendedLogging");
        result.add("Software7E1");
        return result;
    }

    public String getProtocolVersion() {
        return "$Date$";
    }
    
    public String getFirmwareVersion() throws IOException,UnsupportedException {
       try {
         return((String)getUnigas300Registry().getRegister("UNIGAS software revision number"));        
//         return((String)getUnigas300Registry().getRegister("CI software revision number")+" "+(String)getUnigas300Registry().getRegister("UNIGAS software revision number"));        
       }
       catch(IOException e) {
           throw new IOException("Unigas300, getFirmwareVersion, "+e.getMessage()); 
       }
    } // public String getFirmwareVersion()
    
    /** initializes the receiver
     * @param inputStream <br>
     * @param outputStream <br>
     * @param timeZone <br>
     * @param logger <br>
     */    
    public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger)
    {
        this.timeZone = timeZone;
        this.logger = logger;     
        
        try {
           
//        	if (isSoftware7E1()) {
//        		Software7E1InputStream softIn = new Software7E1InputStream(inputStream);
//        		Software7E1OutputStream softOut = new Software7E1OutputStream(outputStream);
//        		flagIEC1107Connection=new FlagIEC1107Connection(softIn,softOut,iIEC1107TimeoutProperty,iProtocolRetriesProperty,0,iEchoCancelling,iIEC1107Compatible);
//        	} else {
//            	flagIEC1107Connection=new FlagIEC1107Connection(inputStream,outputStream,iIEC1107TimeoutProperty,iProtocolRetriesProperty,0,iEchoCancelling,iIEC1107Compatible);
//        	}
        	flagIEC1107Connection=new FlagIEC1107Connection(inputStream,outputStream,iIEC1107TimeoutProperty,iProtocolRetriesProperty,0,iEchoCancelling,iIEC1107Compatible,software7E1);
        	unigas300Registry = new Unigas300Registry(this);
           unigas300Profile = new Unigas300Profile(this,unigas300Registry);
        }
        catch(ConnectionException e) {
           logger.severe ("ABBA1500: init(...), "+e.getMessage());
        }
        
    } // public void init(InputStream inputStream,OutputStream outputStream,TimeZone timeZone,Logger logger)

	/**
     * @throws IOException  */    
    public void connect() throws IOException {
       try {
          
    	  sendWakeUp();
    	  dataReadout = flagIEC1107Connection.dataReadout(strID,nodeId);
          flagIEC1107Connection.disconnectMAC();
       /*   try {
              Thread.sleep(2000);
          }
          catch(InterruptedException e) {
              throw new NestedIOException(e); 
          }*/
          flagIEC1107Connection.connectMAC(strID,strPassword,iSecurityLevel,nodeId);
          
          validateSerialNumber();
          getLogger().info("Connected to device with serial number: " + getDeviceSerialNumber());
          registerInfo();
       }
       catch(FlagIEC1107ConnectionException e) {
          throw new IOException(e.getMessage());
       }
    }
    
    private void sendWakeUp() throws ConnectionException {
 	   byte[] wakeUp = new byte[20];
       for (int i = 0; i < wakeUp.length; i++) wakeUp[i] = (byte) 0x00;
 	  	getFlagIEC1107Connection().sendOut(wakeUp);
	}

	public void disconnect() throws IOException {
       try {
          flagIEC1107Connection.disconnectMAC();
       }
       catch(FlagIEC1107ConnectionException e) {
          logger.severe("disconnect() error, "+e.getMessage());
       }
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return KAMSTRUP_NR_OF_CHANNELS;
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        return iProfileInterval;
    }
    
    public Unigas300Registry getUnigas300Registry() {
        return unigas300Registry;   
    }
    private Unigas300Profile getUnigas300Profile() {
        return unigas300Profile;   
    }

    // Implementation of interface ProtocolLink
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }
    
    public String getPassword() {
       return strPassword;
    }

    public byte[] getDataReadout() {
       return dataReadout;    
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }    

    public boolean isRequestHeader() {
        return false;
    }

    private void registerInfo() {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        if (extendedLogging >= 1) {
            logger.info(ocm.getRegisterInfo());
        }
    }
    
    private void validateSerialNumber() throws IOException {
        if ((getSerialNumber() == null) || ("".compareTo(getSerialNumber())==0)) return;
        String sn = getDeviceSerialNumber();
        if (sn.compareTo(serialNumber) != 0) 
        	throw new IOException("SerialNiumber mismatch! meter sn="+sn+", configured sn="+serialNumber);
    }
    
    public String getDeviceSerialNumber() throws IOException {
    	if (deviceSerialNumber == null) {
    		deviceSerialNumber = (String)getUnigas300Registry().getRegister("DeviceSerialNumber");
    	}
    	return deviceSerialNumber;
	}
    
    public String getSerialNumber() {
		return serialNumber;
	}
    
    // RegisterProtocol Interface implementation
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
			ObisCodeMapper ocm = new ObisCodeMapper(this);
			return ocm.getRegisterValue(obisCode);
		} catch (Exception e) {
			if ((e instanceof IOException) && (e.getMessage().indexOf("not initialized") != -1)) {
				return new RegisterValue(obisCode, "No value available");
			}
			throw new NoSuchRegisterException("Problems while reading register " + obisCode + ": " + e.getMessage());
		}
    }
    
    //unused methods
    public Object getCache() {return null;}
    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {return null;}
    public void setCache(Object cacheObject) {}
    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {}
    public ChannelMap getChannelMap() {return null;}
    public ProtocolChannelMap getProtocolChannelMap() {return null;}
    public void release() throws IOException {}

    
} // public class Unigas300 implements MeterProtocol {
