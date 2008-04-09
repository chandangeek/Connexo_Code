package com.energyict.protocolimpl.iec1107.zmd;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.iec1107.*;

import java.io.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version 1.0
 * @author Koenraad Vanderschaeve
 * @author FBL
 * @beginchanges
   FBL |14062007| 
   || Bugfix: 
   || RegisterValues for registers from last billing point did not have a toTime.
   || This is now filled with register: 0.1.0*F.
   GN |march 2008| Added serialnumber support and message is thrown when meter doesn't support this; then use property 'ignoreSerialNumberCheck'
 * @endchanges
 */

public class Zmd 
    implements  MeterProtocol, HHUEnabler, ProtocolLink, MeterExceptionInfo, 
                RegisterProtocol {
    
    private String strID;
    private String strPassword;
    private String serialNumber;
    private String mSerialNumber = null;
    private int iIEC1107TimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private String nodeId;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int profileInterval;
    private ProtocolChannelMap protocolChannelMap;
    
    private TimeZone timeZone;
    private Logger logger;
    private int extendedLogging;
    
    private FlagIEC1107Connection flagIEC1107Connection;
    private Registry registry;
    private Profile profile;
    
    private byte[] dataReadout;
    private int billingCount=-1;
    private LinkedHashMap obisMap = new LinkedHashMap();
    
    private Date lastBillingTime = null;
    private int lastBilling = -1;
    
    private static SimpleDateFormat registerFormat;
    private DataDumpParser dataDumpParser;
    
    ObisCode serialNumbObisCode = ObisCode.fromString("1.0.9.0.0.255");
    
    public Zmd() { } 
    
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.YEAR, -10);
        return getProfileData(calendar.getTime(), includeEvents);
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return profile.getProfileData(lastReading, includeEvents);
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,
            UnsupportedException {
        return profile.getProfileData(from, to, includeEvents);
    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }
    
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        throw new UnsupportedException();
    }
    
    public void setTime() throws IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
        Date date = calendar.getTime();
        registry.setRegister("Time", date);
        registry.setRegister("Date", date);
    } 
    
    public Date getTime() throws IOException {
        Date date = (Date) registry.getRegister("TimeDate");
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
            
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            nodeId = properties.getProperty(MeterProtocol.NODEID, "");
            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            iIEC1107Compatible = Integer.parseInt(properties.getProperty("IEC1107Compatible", "1").trim());
            profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
            protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0,0,0,0"));
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
            serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
            
        } catch (NumberFormatException e) {
            String msg = "validateProperties, NumberFormatException, " + e.getMessage();
            throw new InvalidPropertyException( msg );
        }
        
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
        registry.setRegister(name, value);
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
        return new ArrayList(0);
    }
    
    /**
     * this implementation returns an empty list
     * 
     * @return a list of strings
     */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("IEC1107Compatible");
        result.add("ChannelMap");
        result.add("ExtendedLogging");
        result.add("IgnoreSerialNumberCheck");
        return result;
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.6 $";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return ("Unknown");
    } 
    
    /**
     * initializes the receiver
     * 
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        
        try {
            
            flagIEC1107Connection = 
                new FlagIEC1107Connection(  inputStream, outputStream, 
                    iIEC1107TimeoutProperty, iProtocolRetriesProperty, 0, 
                    iEchoCancelling, iIEC1107Compatible);
            
            flagIEC1107Connection.setErrorSignature( "ER" );
            
            registry = new Registry(this, this);
            profile = new Profile(this, this, registry);
            
            registerFormat = new SimpleDateFormat( "yy-MM-dd HH:mm" );
            registerFormat.setTimeZone(getTimeZone());
            
        } catch (ConnectionException e) {
            logger.severe("init(...), " + e.getMessage());
            throw new NestedIOException(e);
        }
        
    }
    
    /**
     * @throws IOException
     */
    public void connect() throws IOException {
        try {
            
            if (getFlagIEC1107Connection().getHhuSignOn() == null) {
                dataReadout = flagIEC1107Connection.dataReadout(strID, nodeId);
                flagIEC1107Connection.disconnectMAC();
            }
            
            flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);
            
    		if (!verifyMeterSerialNR()) 
    			throw new IOException("L&G ZMD, connect, Wrong SerialNR!, EISerialNumber="+serialNumber+", MeterSerialNumber="+getSerialNumber());
     
            if (getFlagIEC1107Connection().getHhuSignOn() != null)
                dataReadout = getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
            
        } catch (FlagIEC1107ConnectionException e) {
            throw new IOException(e.getMessage());
        }
        
        initObis();
        
        if (extendedLogging >= 1) getRegistersInfo();
        
    }
    
    private String getSerialNumber() throws IOException {
    	if ( mSerialNumber == null ){
			mSerialNumber = getDataDumpParser().getRegisterFFStrValue("0.0.0");
			mSerialNumber = mSerialNumber.substring(mSerialNumber.indexOf("(") + 1, mSerialNumber.indexOf(")"));
    	}

		return mSerialNumber; 
	}

	private boolean verifyMeterSerialNR() throws IOException {
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0) || (serialNumber.compareTo(getSerialNumber()) == 0))
            return true;
        else 
            return false;
	}

	public void disconnect() throws IOException {
        try {
            flagIEC1107Connection.disconnectMAC();
        } catch (FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, " + e.getMessage());
        }
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
            return getProtocolChannelMap().getNrOfProtocolChannels();
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
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
        return (iIEC1107Compatible == 1);
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
    
    /** should not be called since deprecated */ 
    public ChannelMap getChannelMap() {
        return null;
    }
    
    public void release() throws IOException {}
    
    public Logger getLogger() {
        return logger;
    }
    
    public String getExceptionInfo(String id) {
        String strippedId = ProtocolUtils.stripBrackets(id);
        if( "ER0001".equals(strippedId) ) return "command not recognised";
        if( "ER0002".equals(strippedId) ) return "faulty parameters";
        return null;
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
        return false;
    }
    
    public ProtocolChannelMap getProtocolChannelMap() {
        return protocolChannelMap;
    }
    
    /* Translate the obis codes to edis codes, and read */ 
    public RegisterValue readRegister(ObisCode obis) throws IOException {
        
        try {
            
            return toRegisterValue(obis);
            
        } catch (NoSuchRegisterException e) {
            throw createNoSuchRegisterException(obis);
        } catch (FlagIEC1107ConnectionException e) {
            throw createNoSuchRegisterException(obis);
        } catch (IOException e) {
            throw createNoSuchRegisterException(obis);
        } catch (ParseException e) {
            String m = "obisCode " + obis.toString();
            throw new NestedIOException(e, m);
        } catch (NumberFormatException e) {
            throw createNoSuchRegisterException(obis);
        }
        
    }
    
    
    private RegisterValue toRegisterValue(ObisCode obis) throws IOException, ParseException {
        
        if( isTimeCode(obis) && (obis.getF()==255) ) 
            return new RegisterValue(obis, toQuantity(getTime()));
        
        if( isTimeCode(obis) && (obis.getF()==0) ) {
            return new RegisterValue(obis, toQuantity(getLastBillTime()));
        }
        
        if ((obis.getC()==97) && (obis.getD()==97) &&(obis.getE()==0)) {
              String str = getDataDumpParser().getRegisterFFStrValue("F.F");
              return new RegisterValue(obis, str);
        }
        
        if (obis.equals(serialNumbObisCode)){
        	byte[] data = getDataDumpParser().getRegisterStrValue(toEdis(obis)).getBytes();
        	String text = parseText(data);
        	return new RegisterValue(obis,null,null,null,null,null,0,text);
        }
        
        Quantity quantity = getDataDumpParser().getRegister(toEdis(obis));
        Date eventTime = getDataDumpParser().getRegisterDateTime(toEdis(obis), getTimeZone());
        Date toTime = null;
        if(obis.getF()!=255)
            toTime = getDataDumpParser().getRegisterDateTime("0.1.0" + getEdisBillingNotation(obis), getTimeZone());
        return new RegisterValue(obis, quantity, eventTime, toTime);
    }
    
    private String parseText(byte[] data) throws IOException {
        DataParser dp = new DataParser(getTimeZone());
        String text = dp.parseBetweenBrackets(data,0,0);
        return text;
	}
    
    /* Convert Obis code to Edis code. */
    private String toEdis(ObisCode obis) throws IOException{
        return obis.getC() + "." + obis.getD() + "." + obis.getE() + getEdisBillingNotation(obis);
    }

    private String getEdisBillingNotation(ObisCode obis) throws IOException {
        if( obis.getF() != 255 ) {
            
            int bc = getBillingCount();
            if( bc == 0 ) throw createNoSuchRegisterException(obis);
            return "*" + ProtocolUtils.buildStringDecimal(bc - Math.abs(obis.getF()), 2);
        }
        else
            return "";
    }
    
    /* Is o a code represeting a timestamp? */
    private boolean isTimeCode(ObisCode o){
        return  o.getA() == 1 &&
                o.getB() == 1 &&
                o.getC() == 0 && 
                o.getD() == 1 &&
                o.getE() == 2;
    }
    
    private NoSuchRegisterException createNoSuchRegisterException(ObisCode o){
        String msg = "ObisCode " + o.toString() + " is not supported.";
        return new NoSuchRegisterException(msg);
    }
    
    private DataDumpParser getDataDumpParser() throws IOException {
        if( dataDumpParser==null )
            dataDumpParser = new DataDumpParser(getDataReadout());
        return dataDumpParser;
    }

    private Quantity toQuantity(Date date) {
        Long seconds = new Long(date.getTime() / 1000);
        return new Quantity( seconds, Unit.get(BaseUnit.SECOND) );
    }
    
    // billingcount = last billing period...
    private int getBillingCount() throws IOException {
        if( billingCount == -1 ){
            Quantity quantity = getDataDumpParser().getRegister("0.1.0");
            billingCount = quantity.intValue();
        }
        return billingCount;
    }    
    
    /* Read 0.1.0*F: toTime of last billing period */
    private Date getLastBillTime( ) throws ParseException, IOException{
        if( lastBillingTime==null ){
            lastBillingTime = getDataDumpParser().getRegisterDateTime("0.1.0*" + ProtocolUtils.buildStringDecimal(getBillingCount(),2), getTimeZone());
        }
        return lastBillingTime;
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo( (String)obisMap.get(obisCode.toString()) );
    }

    void initObis( ) throws IOException { 
        
        obisMap.put( "1.1.0.1.2.255", "Date and time (0.9.1 0.9.2)" );
        obisMap.put( "1.1.0.1.2.VZ",  "Date and time last billing point" );
        obisMap.put( "1.1.0.1.0.255", "Billing counter" );
        
        
        obisMap.put( "1.1.1.8.1.255", "Energy +A rate 1 (1.8.1)" );
        String obis = "1.1.1.8.1.VZ";
        String dscr = "Energy +A rate 1 (1.8.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);
        
        
        obisMap.put( "1.1.1.8.2.255", "Energy +A rate 2 (1.8.2)" );
        obis = "1.1.1.8.2.VZ";
        dscr = "Energy +A rate 2 (1.8.2*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);
        
        
        obisMap.put( "1.1.2.8.1.255", "Energy -A rate 1 (2.8.1)" );
        obis = "1.1.2.8.1.VZ";
        dscr = "Energy -A rate 1 (2.8.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);
        
        obisMap.put( "1.1.2.8.2.255", "Energy -A rate 2 (2.8.2)" );
        obis = "1.1.2.8.2.VZ";
        dscr = "Energy +R rate 2 (2.8.2*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);

        
        obisMap.put( "1.1.3.8.1.255", "Energy +R rate 1 (3.8.1)" );
        obis = "1.1.3.8.1.VZ";
        dscr = "Energy +R rate 1 (3.8.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);
        
        obisMap.put( "1.1.3.8.2.255", "Energy +R rate 2 (3.8.2)" );
        obis = "1.1.3.8.2.VZ";
        dscr = "Energy +R rate 2 (3.8.2*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);
        
        
        obisMap.put( "1.1.4.8.1.255", "Energy -R rate 1 (4.8.1)" );
        obis = "1.1.4.8.1.VZ";
        dscr = "Energy -R rate 1 (4.8.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);
        
        obisMap.put( "1.1.4.8.2.255", "Energy -R rate 2 (4.8.2)" );
        obis = "1.1.4.8.2.VZ";
        dscr = "Energy -R rate 2 (4.8.2*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);

        
        obisMap.put( "1.1.1.6.1.255", "Maximum Demand +A rate 1 (1.6.1)" );
        obis = "1.1.1.6.1.VZ";
        dscr = "Maximum Demand +A rate 3 (1.6.1*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);
        
        
        obisMap.put( "1.1.1.6.2.255", "Maximum Demand +A rate 2 (1.6.2)" );
        obis = "1.1.1.6.2.VZ";
        dscr = "Maximum Demand +A rate 3 (1.6.2*" + getBillingCount() + ")"; 
        obisMap.put(obis, dscr);
        
        
        obisMap.put( "1.1.1.6.3.255", "Maximum Demand +A rate 3 (1.6.3)" );
        obis = "1.1.1.6.3.VZ";
        dscr = "Maximum Demand +A rate 3 (1.6.3*" + getBillingCount() + ")";
        obisMap.put(obis, dscr);
        
        obisMap.put( "1.1.1.2.1.255", "Cumulative Maximum Demand +A rate 1 (1.2.1)" );
        obisMap.put( "1.1.1.2.2.255", "Cumulative Maximum Demand +A rate 2 (1.2.2)" );
        obisMap.put( "1.1.1.2.3.255", "Cumulative Maximum Demand +A rate 3 (1.2.3)" );
        
      }
    
    private void getRegistersInfo() throws IOException {
        
        StringBuffer rslt = new StringBuffer();
        
        Iterator i = obisMap.keySet().iterator();
        while(i.hasNext()){

            String obis = (String)i.next();
            ObisCode oc = ObisCode.fromString(obis);
            
            rslt.append( obis )
                .append( " " )
                .append( translateRegister(oc).toString() + "\n" );
             
            if( extendedLogging == 2){
                try {
                    RegisterValue value = readRegister(oc);
                    rslt.append( value.toString() + "\n" );
                } catch(NoSuchRegisterException ex){
                    // ignore
                }
            } 
            
        }
        
        logger.info(rslt.toString());
        
    }
    
    // ********************************************************************************************************
    // implementation of the HHUEnabler interface
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, true);
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
    
} 
