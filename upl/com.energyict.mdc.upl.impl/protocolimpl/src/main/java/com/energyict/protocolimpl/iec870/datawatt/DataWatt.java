/*
 * DataWatt.java
 *
 * Created on 18 juni 2003, 13:56
 */

package com.energyict.protocolimpl.iec870.datawatt;

import java.io.*;
import java.util.*;
import com.energyict.cbo.*;
import java.util.logging.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.iec870.*;
import com.energyict.dialer.core.*;

/**
 *
 * @author  Koen
 * @beginchanges
KV|23032005|Changed header to be compatible with protocol version tool
 * @endchanges
 */
public class DataWatt implements MeterProtocol,IEC870ProtocolLink { 
    
    static public final int MAX_COUNTER=100000000; // = max counter + 1
    
    private static final int DEBUG=0;
    
    // properties
    String strID;
    String strPassword;
    int iIEC870TimeoutProperty;
    int iProtocolRetriesProperty;
    int iRoundtripCorrection;
    int iProfileInterval;
    int iMeterType;
    ChannelMap channelMap=null;
    
    IEC870Connection iec870Connection=null;
    ApplicationFunction applicationFunction=null;
    
    TimeZone timeZone=null;
    Logger logger=null;
    DatawattRegistry datawattRegistry = null;
    DatawattProfile datawattProfile = null;
    
    /** Creates a new instance of DataWatt */
    public DataWatt() {
    }
    
    public void connect() throws IOException {
        try {
            iec870Connection.connectLink();
            if (strID.compareTo(String.valueOf(iec870Connection.getRTUAddress())) != 0)
                throw new IOException("DataWatt, connect, invalid meter address, config="+strID+", meter="+String.valueOf(iec870Connection.getRTUAddress()));
        }
        catch(IEC870ConnectionException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public void disconnect() {
        try {
            iec870Connection.disconnectLink();
        }
        catch(IEC870ConnectionException e) {
            logger.severe("DataWatt, disconnect() error, "+e.getMessage());
        }
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException("DataWatt, getFirmwareVersion");
    }
    
    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        return new Quantity(getDatawattRegistry().getRegister(Channel.parseChannel(name)), Unit.get(""));
    }
    
    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        if (channelId >= getChannelMap().getNrOfChannels())
           throw new IOException("DataWatt, getMeterReading, invalid channelId, "+channelId);
        return new Quantity(getDatawattRegistry().getRegister(getChannelMap().getChannel(channelId)), Unit.get(""));
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return channelMap.getNrOfChannels();
    }
    
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.clear();
        return datawattProfile.getProfileData(fromCalendar,includeEvents);
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar fromCalendar = ProtocolUtils.getCleanCalendar(timeZone);
        fromCalendar.setTime(lastReading);
        return datawattProfile.getProfileData(fromCalendar,includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,UnsupportedException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }
    
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        return iProfileInterval;
    }
    
    public String getProtocolVersion() {
        return "$Date$";
    }
    
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        return getDatawattRegistry().getRegister(Channel.parseChannel(name)).toString();
    }
    
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("MeterType");
        return result;
    }
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        result.add("ChannelMap");
        return result;
    }
    
    public Date getTime() throws IOException {
        return getApplicationFunction().dsapGetClockASDU().getTime();
    }
    
    public DatawattRegistry getDatawattRegistry() {
        return datawattRegistry;
    }
    public DatawattProfile getDatawattProfile() {
        return datawattProfile;
    }
    public IEC870Connection getIEC870Connection() {
        return iec870Connection;
    }
    public ApplicationFunction getApplicationFunction() {
        return applicationFunction;
    }
    public ChannelMap getChannelMap() {
        return channelMap;
    }
    public int getMeterType() {
        return iMeterType;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, java.util.logging.Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        try {
            iec870Connection=new IEC870Connection(inputStream,outputStream,iIEC870TimeoutProperty,iProtocolRetriesProperty,(long)300,0,getTimeZone());
            applicationFunction = new ApplicationFunction(timeZone, iec870Connection, logger);
            datawattRegistry = new DatawattRegistry(this);
            datawattProfile = new DatawattProfile(this);
        }
        catch(IEC870ConnectionException e) {
            logger.severe("DataWatt: init(...), "+e.getMessage());
        }
    }
    
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException("DataWatt, initializeDevice");
    }
    
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        validateProperties(properties);
    }
    /** <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException(key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            iIEC870TimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","25000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","3").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iProfileInterval=Integer.parseInt(properties.getProperty("ProfileInterval","3600").trim());
            channelMap = new ChannelMap(properties.getProperty("ChannelMap",""));
            iMeterType=Integer.parseInt(properties.getProperty("MeterType","0").trim());
            
        }
        catch (NumberFormatException e) {
            throw new InvalidPropertyException("DataWatt, validateProperties, NumberFormatException, "+e.getMessage());
        }
        catch (IOException e) {
            throw new InvalidPropertyException("DataWatt, validateProperties, IOException, "+e.getMessage());
        }
    }
    
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        throw new UnsupportedException("DataWatt, setRegister");
    }
    
    public void setTime() throws ProtocolException {
        try {
            Calendar calendar=null;
            calendar = ProtocolUtils.getCalendar(timeZone);
            calendar.add(Calendar.MILLISECOND,iRoundtripCorrection);
            getApplicationFunction().clockSynchronizationASDU(calendar);
        }
        catch(IOException e) {
            throw new ProtocolException("DataWatt, setTime, "+e.getMessage());
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Dialer dialer = null;
        DataWatt dataWatt = null;
        Calendar calendar = null;
        try {
            dataWatt=new DataWatt();
            dialer = DialerFactory.getDefault().newDialer();
            dialer.init("COM1","AT+CBST=71,0,1");
            dialer.connect("0,0031162481265",60000);
            InputStream is = dialer.getInputStream();
            OutputStream os = dialer.getOutputStream();
            dataWatt=new DataWatt();
            Properties properties = new Properties();
            //properties.setProperty(MeterProtocol.ADDRESS,"4");
            properties.setProperty(MeterProtocol.ADDRESS,"61");
            properties.setProperty(MeterProtocol.PASSWORD,"");
            properties.setProperty("Retries","5");
            properties.setProperty("ProfileInterval","300");
            //properties.setProperty("ChannelMap","1.5:1.7:1.1:2.5:2.6:2.7");
            //properties.setProperty("ChannelMap","1.5:1.7:1.1:2.5:2.6");
            properties.setProperty("ChannelMap","1.2"); //:1.5");
            properties.setProperty("MeterType", "0");
            dataWatt.setProperties(properties);
            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                   SerialCommunicationChannel.DATABITS_8,
                                                   SerialCommunicationChannel.PARITY_NONE,
                                                   SerialCommunicationChannel.STOPBITS_1);
            
            // initialize
            dataWatt.init(is,os,TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            
            System.out.println("Start session");
            dataWatt.connect();
            
            //            System.out.println("******************************************** getApplicationFunction().testASDU() ********************************************");
            //            dataWatt.getApplicationFunction().testASDU();
            
            
            System.out.println("******************************************** getProfileData() ********************************************");
            calendar = Calendar.getInstance(dataWatt.getTimeZone());
            calendar.add(Calendar.HOUR_OF_DAY,-2);
            dataWatt.getDatawattProfile().doLogMeterDataCollection(dataWatt.getProfileData(calendar.getTime(),true));


            System.out.println("******************************************** getMeterReading() ********************************************");
            System.out.println("Meterreading: "+dataWatt.getMeterReading("1.1").toString());
            
//            System.out.println("******************************************** setTime() ********************************************");
//            System.out.println("setTime()");
//            dataWatt.setTime();

            System.out.println("******************************************** getTime() ********************************************");
            System.out.println("System: "+new Date());
            System.out.println("Meter: "+dataWatt.getTime());
            
            
            
            dataWatt.disconnect();
            System.out.println("End session");
        }
        catch(LinkException e) {
            System.out.println("DataWatt, DialerException, "+e.getMessage());
            dataWatt.disconnect();
        }
        catch(IOException e) {
            System.out.println("DataWatt, IOException, "+e.getMessage());
            dataWatt.disconnect();
        }
        
    }
    
    // implementation of IEC870ProtocolLink
    public String getPassword() {
        return strPassword;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    public Object getCache() {
        return null;
    }
    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        return null;
    }
    
    public void setCache(Object cacheObject) {
    }
    
    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
    }

    public void release() throws IOException {
    }
    
} // public class DataWatt implements MeterProtocol
