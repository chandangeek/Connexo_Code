package com.energyict.protocolimpl.iec1107.abba1350;

import java.io.*;
import java.math.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.iec1107.*;
import com.energyict.protocolimpl.iec1107.vdew.*;
import com.energyict.protocol.messaging.*;
import com.energyict.obis.ObisCode;

/**
 * @version 1.0
 * @author Koenraad Vanderschaeve
 * @author fbl
 * @author jme
 * @beginchanges 
 * 18-11-2008 jme > Implemented MessageProtocol to support messages. Messages for new Switch Point Clock data from alphaSET 3.0
 * 20-11-2008 jme > Added check for serial number match
 * 24-11-2008 jme > Added firmware version and hardware key readout
 * 24-11-2008 jme > Added support for power Quality readout (P.02)
 * @endchanges
 */
public class ABBA1350 
    implements  MeterProtocol, HHUEnabler, ProtocolLink, MeterExceptionInfo, 
                RegisterProtocol, MessageProtocol {
    
    private final static int DEBUG = 1;

	private static final int MIN_LOADPROFILE = 1;
	private static final int MAX_LOADPROFILE = 2;
        
    private String strID;
    private String strPassword;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
	private String serialNumber;
    private int iEchoCancelling;
    private int profileInterval;
    private ChannelMap channelMap;
    private int requestHeader;
    private ProtocolChannelMap protocolChannelMap = null;
    private int scaler;
    private int dataReadoutRequest;
	private int loadProfileNumber;
    
    private TimeZone timeZone;
    private Logger logger;
    private int extendedLogging;
    private int vdewCompatible;
    
    private FlagIEC1107Connection flagIEC1107Connection = null;
    private ABBA1350Registry abba1350Registry = null;
    private ABBA1350Profile abba1350Profile = null;
    private ABBA1350Messages abba1350Messages = new ABBA1350Messages(this);
    private ABBA1350ObisCodeMapper abba1350ObisCodeMapper = null;
    
    private byte[] dataReadout = null;
    private int [] billingCount;
    
    /** Creates a new instance of ABBA1350, empty constructor */
    public ABBA1350() {
    } 
    
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getAbba1350Profile().getProfileData(lastReading, includeEvents, loadProfileNumber);
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,
            UnsupportedException {
        return getAbba1350Profile().getProfileData(from, to, includeEvents, loadProfileNumber);
    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }
    
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }
    
    /**
     * This method sets the time/date in the remote meter equal to the system 
     * time/date of the machine where this object resides.
     * 
     * @exception IOException
     */
    
    public void setTime() throws IOException {
        if (vdewCompatible == 1)
            setTimeVDEWCompatible();
        else
            setTimeAlternativeMethod();
    }
    
    private void setTimeAlternativeMethod() throws IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getAbba1350Registry().setRegister("TimeDate2", date);
    } // public void setTime() throws IOException
    
    private void setTimeVDEWCompatible() throws IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        getAbba1350Registry().setRegister("Time", date);
        getAbba1350Registry().setRegister("Date", date);
    } // public void setTime() throws IOException
    
    public Date getTime() throws IOException {
    	Date date = (Date) getAbba1350Registry().getRegister("TimeDate");
        return new Date(date.getTime() - iRoundtripCorrection);
    }

    
    /** ************************************ MeterProtocol implementation ************************************** */
    
    /**
     * This implementation calls <code> validateProperties </code> and assigns 
     * the argument to the properties field
     */
    public void setProperties(Properties properties) 
        throws MissingPropertyException, InvalidPropertyException {
        
        validateProperties(properties);
        
    }
    
    /**
     * Validates the properties.  The default implementation checks that all 
     * required parameters are present.
     */
    private void validateProperties(Properties properties) 
        throws MissingPropertyException, InvalidPropertyException {
        
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException(key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS, "");
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
            channelMap = new ChannelMap(properties.getProperty("ChannelMap", "0"));
            requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "1").trim());
            protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0,0,0,0"));
            scaler = Integer.parseInt(properties.getProperty("Scaler", "0").trim());
            dataReadoutRequest = Integer.parseInt(properties.getProperty("DataReadout", "0").trim());
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            vdewCompatible = Integer.parseInt(properties.getProperty("VDEWCompatible", "0").trim());
            loadProfileNumber = Integer.parseInt(properties.getProperty("LoadProfileNumber", "1"));
                        
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, "
                    + e.getMessage());
        }
        
        if ((loadProfileNumber < MIN_LOADPROFILE) || (loadProfileNumber > MAX_LOADPROFILE)) 
        	throw new InvalidPropertyException("Invalid loadProfileNumber (" + loadProfileNumber + "). Minimum value: " + MIN_LOADPROFILE + " Maximum value: " + MAX_LOADPROFILE); 
        
    }
    
    private boolean isDataReadout() {
        return (dataReadoutRequest == 1);
    }
    
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(name.getBytes());
        flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
        byte[] data = flagIEC1107Connection.receiveRawData();
        return new String(data);
    }
    
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException,
            UnsupportedException {
        getAbba1350Registry().setRegister(name, value);
    }
    
    /**
     * this implementation throws UnsupportedException. Subclasses may override
     * 
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }
    
    /**
     * the implementation returns both the address and password key
     * 
     * @return a list of strings
     */
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }
    
    /**
     * this implementation returns an empty list
     * 
     * @return a list of strings
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("LoadProfileNumber");
        result.add("Timeout");
        result.add("Retries");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("ChannelMap");
        result.add("RequestHeader");
        result.add("Scaler");
        result.add("DataReadout");
        result.add("ExtendedLogging");
        result.add("VDEWCompatible");
        return result;
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.7 $";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
    	String fw = "";
    	String hw = "";
    	String dev = "";
    	String fwdev = "";

    	if (iSecurityLevel < 1) return "Unknown (SecurityLevel to low)";
    	
    	fwdev = (String)getAbba1350Registry().getRegister("Firmware");
    	hw = (String)getAbba1350Registry().getRegister("Hardware");
    	if ((fwdev != null) && (fwdev.length() >= 30)) {
        	fw = fwdev.substring(0, 10);
        	dev = fwdev.substring(10, 30);
        	fw = new String(ProtocolUtils.convert2ascii(fw.getBytes())).trim();
        	dev = new String(ProtocolUtils.convert2ascii(dev.getBytes())).trim();
    	} else {
    		fw = "Unknown";
    		dev = "Unknown";
    	}
    	if (hw != null) {
        	hw = new String(ProtocolUtils.convert2ascii(hw.getBytes())).trim();
    	} else {
    		hw = "Unknown"; 
    	}

    	return " FirmwareNumber: " + fw + " DeviceType: " + dev + " HardwareKey: " + hw;
    } // public String getFirmwareVersion()
    
    /**
     * initializes the receiver
     * 
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.timeZone = timeZone;
        this.logger = logger;
       
        try {
            flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty,
                    iProtocolRetriesProperty, 0, iEchoCancelling, 1);
            abba1350Registry = new ABBA1350Registry(this, this);
            abba1350Profile = new ABBA1350Profile(this, this, abba1350Registry);
            
        } catch (ConnectionException e) {
            logger.severe("ABBA1350: init(...), " + e.getMessage());
        }
        
    }
    
    /**
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (isDataReadout())) {
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                flagIEC1107Connection.disconnectMAC();
            }
            
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);
            validateSerialNumber();

            if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (isDataReadout())) {
            	dataReadout = getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
            }

        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }
        
        abba1350ObisCodeMapper = new ABBA1350ObisCodeMapper(this);
        abba1350ObisCodeMapper.initObis();
        
        if (extendedLogging >= 2) getMeterInfo();
        if (extendedLogging >= 1) getRegistersInfo();
        
    }
    
    public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        if (requestHeader == 1)
            return getAbba1350Profile().getProfileHeader(loadProfileNumber).getNrOfChannels();
        else
            return getProtocolChannelMap().getNrOfProtocolChannels();
    }
    
    public int getISecurityLevel() {
		return iSecurityLevel;
	}

	public int getProfileInterval() throws UnsupportedException, IOException {
        if (requestHeader == 1)
            return getAbba1350Profile().getProfileHeader(loadProfileNumber).getProfileInterval();
        else
            return profileInterval;
    }
    
    // Implementation of interface ProtocolLink
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return flagIEC1107Connection;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    public boolean isIEC1107Compatible() {
        return true;
    }
    
    public String getPassword() {
        return strPassword;
    }
    
    public byte[] getDataReadout() {
        return dataReadout;
    }
    
    public Object getCache() {
        return null;
    }
    
    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        return null;
    }
    
    public void setCache(Object cacheObject) {
    }
    
    public void updateCache(int rtuid, Object cacheObject) 
        throws SQLException, BusinessException { }
    
    public ChannelMap getChannelMap() {
        return channelMap;
    }
    
    public void release() throws IOException {}
    
    public Logger getLogger() {
        return logger;
    }
    
    static Map exceptionInfoMap = new HashMap();
    static {
        exceptionInfoMap.put("ERROR", "Request could not execute!");
        exceptionInfoMap.put("ERROR01", "A1350 ERROR 01, invalid command!");
        exceptionInfoMap.put("ERROR06", "A1350 ERROR 06, invalid command!");
    }
    
    public String getExceptionInfo(String id) {
        String exceptionInfo = (String) exceptionInfoMap.get(ProtocolUtils.stripBrackets(id));
        if (exceptionInfo != null)
            return id + ", " + exceptionInfo;
        else
            return "No meter specific exception info for " + id;
    }
    
    public int getNrOfRetries() {
        return iProtocolRetriesProperty;
    }
    
    /**
     * Getter for property requestHeader.
     * 
     * @return Value of property requestHeader.
     */
    public boolean isRequestHeader() {
        return requestHeader == 1;
    }
    
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }
    
    
    /* Translate the obis codes to edis codes, and read */ 
    public RegisterValue readRegister(ObisCode obis) throws IOException {
        
        try {
            
            /* it is not possible to translate the time edis code in this way
             * => it is an exception */
            if( "1.1.0.1.2.255".equals( obis.toString() ) )
                return new RegisterValue(obis, readTime());
            
            String fs = "";
            if( obis.getF() != 255 ) {
                int f = getBillingCount() - Math.abs(obis.getF());
                fs = "*" + ProtocolUtils.buildStringDecimal(f, 2);
            }
            
            String edis = 
                obis.getC() + "." + obis.getD() + "." + obis.getE() + fs;
            
            byte[] data = read(edis);
            
            DataParser dp = new DataParser(getTimeZone());
            BigDecimal bd = new BigDecimal(dp.parseBetweenBrackets(data, 0, 0));
            Date date = null;
            
            try {
            
                String dString = dp.parseBetweenBrackets(data, 0, 1);
                
                if( "0000000000".equals(dString) ) 
                    throw new NoSuchRegisterException();
                
                VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
                vts.parse(dString);
                date = vts.getCalendar().getTime();
            
            } catch (DataParseException e) {
                // absorb
            }
            
            Quantity q = new Quantity(bd, obis.getUnitElectricity(scaler));
            return new RegisterValue(obis, q, date, null);
            
        } catch (NoSuchRegisterException e) {
            String m = "ObisCode " + obis.toString() + " is not supported!";
            throw new NoSuchRegisterException(m);
        } catch (FlagIEC1107ConnectionException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            throw new IOException(m);
        } catch (IOException e) {
            String m = "getMeterReading() error, " + e.getMessage();
            throw new IOException(m);
        } catch (NumberFormatException e) {
            String m = "ObisCode " + obis.toString() + " is not supported!";
            throw new NoSuchRegisterException(m);
        }
        
    }
    
    private byte[] read(String edisNotation) throws IOException {
        byte[] data;
        if (!isDataReadout()) {
            String name = edisNotation + "(;)";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(name.getBytes());
            flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream
                    .toByteArray());
            data = flagIEC1107Connection.receiveRawData();
        } else {
            DataDumpParser ddp = new DataDumpParser(getDataReadout());
            data = ddp.getRegisterStrValue(edisNotation).getBytes();
        }
        return data;
    }

    Quantity readTime( ) throws IOException {
        Long seconds = new Long(getTime().getTime() / 1000);
        return new Quantity( seconds, Unit.get(BaseUnit.SECOND) );
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("" + abba1350ObisCodeMapper.getObisMap().get(obisCode.toString()));
    }

    
    
    private void getRegistersInfo() throws IOException {
        StringBuffer rslt = new StringBuffer();
        
        Iterator i = abba1350ObisCodeMapper.getObisMap().keySet().iterator();
        while(i.hasNext()){
            String obis = (String)i.next();
            ObisCode oc = ObisCode.fromString(obis);
            
            if(DEBUG >= 1) {
                try {
                    rslt.append( translateRegister(oc) + "\n" );
                    rslt.append( readRegister(oc) + "\n" );
                } catch( NoSuchRegisterException nsre ) {
                    // ignore and continue
                }
            } else {
                rslt.append( obis + " " + translateRegister(oc) + "\n");
            }
            
        }
        
        logger.info(rslt.toString());
    }
    
    private void getMeterInfo() throws IOException {
    	String returnString = "";
    	if (iSecurityLevel < 1) {
    		returnString = "Set the SecurityLevel > 0 to show more information about the meter.\n";
    	} else {
    		returnString += " Meter ID1: " + new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister("ID1")).getBytes())) + "\n";
    		returnString += " Meter ID2: " + new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister("ID2")).getBytes())) + "\n";
    		returnString += " Meter ID3: " + new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister("ID3")).getBytes())) + "\n";
    		returnString += " Meter ID4: " + new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister("ID4")).getBytes())) + "\n";
    		returnString += " Meter ID5: " + new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister("ID5")).getBytes())) + "\n";
    		returnString += " Meter ID6: " + new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister("ID6")).getBytes())) + "\n";

    		returnString += " Meter IEC1107 ID:" + new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister("IEC1107_ID")).getBytes())) + "\n";
    		returnString += " Meter IECII07 address (optical):    " + new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister("IEC1107_ADDRESS_OP")).getBytes())) + "\n";
    		returnString += " Meter IECII07 address (electrical): " + new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister("IEC1107_ADDRESS_EL")).getBytes())) + "\n";

    	}
        logger.info(returnString);
    }

    // ********************************************************************************************************
    // implementation of the HHUEnabler interface
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, isDataReadout());
    }
    
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty,
                iProtocolRetriesProperty, 300, iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }
    
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }
    
    public ABBA1350Registry getAbba1350Registry() {
        return abba1350Registry;
    }
    
    public ABBA1350Profile getAbba1350Profile() {
        return abba1350Profile;
    }
    
    int getBillingCount() throws IOException{
        if( billingCount == null ){
        
            String data;
			try {
				data = new String( read("0.1.0") );
			} catch (NoSuchRegisterException e) {
				if (!isDataReadout()) throw e;
				data = "()";
			}
            
			int start = data.indexOf('(') + 1;
            int stop = data.indexOf(')');
            String v = data.substring( start, stop );
            
            try {
				billingCount = new int [] { Integer.parseInt(v) };
			} catch (NumberFormatException e) {
				billingCount = new int [] {0};
			}
			
        }
        return billingCount[0];
    }

    protected void validateSerialNumber() throws IOException {
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0)) return;
        String sn = (String)getAbba1350Registry().getRegister("Serial");
        if (sn.compareTo(serialNumber) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+serialNumber);
    }
    
    /**
     * Implementation of methods in MessageProtocol
     */
    
	public void applyMessages(List messageEntries) throws IOException {
        abba1350Messages.applyMessages(messageEntries);
	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		return abba1350Messages.queryMessage(messageEntry);
	}
	
	public List getMessageCategories() {
		return abba1350Messages.getMessageCategories();
	}

	public String writeMessage(Message msg) {
		return abba1350Messages.writeMessage(msg);
	}

	public String writeTag(MessageTag tag) {
        return abba1350Messages.writeTag(tag);    
	}

	public String writeValue(MessageValue value) {
		return abba1350Messages.writeValue(value);
	}
	  
    public void sendDebug(String str){
        if (DEBUG >= 1) {
        	str = "######## DEBUG > " + str + "\n";
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
