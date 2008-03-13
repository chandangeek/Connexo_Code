/*
 * PPM.java
 *
 * Created on 16 juli 2004, 8:57
 */

package com.energyict.protocolimpl.iec1107.ppm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.core.SerialCommunicationChannel;
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
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.HHUEnabler;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.iec1107.ppm.opus.OpusConnection;
import com.energyict.protocolimpl.iec1107.ppm.register.LoadProfileDefinition;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocol.meteridentification.DiscoverInfo;

/**
 * @beginchanges
  FBL |06012005| In response to OBS150, problem with no password
  ||
  || fix: There MUST be a password, and it MUST be 8 characters long.
  ||
  || MUST BE PASSWORD reason:
  || I deduct this from the fact that the Encryption class throws a
  || NullPointerException when there is no password entered.
  ||
  || CHECK IF THE PASSWORD IS TOO SHORT reason:
  || I deduct this the from the fact that the Encryption class throws an
  || ArrayIndexOutOfBoundsException when the password is shorter then 8 characters.
  ||
  || CHECK IF THE PASSWORD IS TOO LONG reason:
  || The Powermaster Unit software allows maximum 8 characters.
  ||
  || I have added 3 checks in the init method:
  || - 1) check if there is a password
  || - 2) check if it is not too short
  || - 3) check if it is not too long
  ||
  KV |23032005| Changed header to be compatible with protocol version tool
  FBL|30032005| Changes to ObisCodeMapper.
  ||
  || The old way of mapping obis codes to Max Demand and Cum Max Demand was 
  || wrong: when the c-field was 2 (max demand) or 6 (cum max demand), the
  || e-field indicated the index of respectively the max demand register, the 
  || cum max demand register.
  ||
  || The reason for this is that the PPM allows for tarifs to be defined on 
  || max demand and cum max demand, but the protocol does not provide a way
  || to read these definitions.  So the protocol does not know wich of the 
  || max demand / cum max demand registers is holding the requested obis 
  || code.
  ||
  || To work around this an obiscode with e-field = 0 now returns the maximum 
  || of all registers that are available.  For instance, a meter is configured
  || with 
  || max demand register 1 = import W value= 10000
  || max demand register 2 = import W value= 15000
  || max demand register 3 = empty
  || max demand register 4 = empty
  || 
  || an obiscode 1.1.1.2.0.F would return max demand register 2, since that 
  || contains the highest value for the requested phenonemon: import W.  The
  || same is true for cum max demand.
  || 
  || Also, to allow for specific registers to be retrieved, manufacturer 
  || specific obis codes are used.  
  || for instance
  || 1.1.1.2.128.F 
  || would return the first import W max demand register
  ||
  || For a completely different matter: the sorting of historical data 
  || has changed.  The sorting is no longer done with dates but with the
  || billing counter.  For more info see HistoricalDataParser.
  FBL|28022005| Fix for data spikes
  || When retrieving profile data on or around an interval boundary, the meter 
  || can return invalid data.  These are intervals that are not yet correctly 
  || closed. (So in fact they should not appear in the profile data at all.)  
  || Such an invalid interval shows up in EIServer as a spike, because the value 
  || contains a lot of '9' characters (e.g. ‘99990.0’).
  || To make sure that only properly closed intervals are stored, intervals that 
  || are less then a minute old are ignored. The end time of an interval needs 
  || to be at least a minute before the current meter time.
  FBL|02032007| Fix for sporadic data corruption 
  || Added extra checking error checking in communication. 
  
 * @endchanges
 * @author fbo
 */
public class PPM implements MeterProtocol, HHUEnabler, SerialNumber, MeterExceptionInfo, RegisterProtocol {
    
    private final static int MAX_TIME_DIFF = 50000;
    /** The minimum period of time that must be elapsed in order 
     * for an interval to be valid/acceptable. (in millisecs) 
     * (see Fix for data spikes) */
    public final static int MINIMUM_INTERVAL_AGE = 60000; 
    
    
    private TimeZone timeZone = null;
    private Logger logger = null;
    
    /** Property keys specific for PPM protocol. */
    final static String PK_OPUS = "OPUS";
    final static String PK_TIMEOUT = "Timeout";
    final static String PK_RETRIES = "Retries";
    final static String PK_FORCE_DELAY = "ForcedDelay";
    
    final static String PK_DELAY_AFTER_FAIL = "DelayAfterFail";
    final static String PK_SECURITY_LEVEL = "SecurityLevel";
    final static String PK_EXTENDED_LOGGING = "ExtendedLogging";
    
    /** Property Default values */
    final static String PD_NODE_ID = "";
    final static int PD_TIMEOUT = 10000;
    final static int PD_RETRIES = 5;
    final static int PD_ROUNDTRIP_CORRECTION = 0;
    final static long PD_FORCE_DELAY = 350;
    
    final static long PD_DELAY_AFTER_FAIL = 500;
    final static int PD_SECURITY_LEVEL = 2;
    final static String PD_OPUS = "1";
    final static String PD_EXTENDED_LOGGING = "0";
    
    
    /** Property values
     * Required properties will have NO default value
     * Optional properties make use of default value */
    String pAddress = null;
    String pNodeId = PD_NODE_ID;
    String pSerialNumber = null;
    String pPassword = null;
    
    /* Protocol timeout fail in msec */
    int pTimeout = PD_TIMEOUT;
    /* Max nr of consecutive protocol errors before end of communication */
    int pRetries = PD_RETRIES;
    /* Offset in ms to the get/set time */
    int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
    /* Delay in msec between protocol Message Sequences */
    long pForceDelay = PD_FORCE_DELAY;
    /* Delay in msec after a protocol error */
    long pDelayAfterFail = PD_DELAY_AFTER_FAIL;
    int pSecurityLevel = PD_SECURITY_LEVEL;
    //String pProfileInterval = null;
    /* 1 if opus protocol is used, 0 if not */
    String pOpus = PD_OPUS;
    String pExtendedLogging = PD_EXTENDED_LOGGING;
    
    int pCorrectTime;
    
    FlagIEC1107Connection flagIEC1107Connection = null;
    OpusConnection opusConnection = null;
    
    MeterType meterType = null;
    RegisterFactory rFactory = null;
    Profile profile = null;
    ObisCodeMapper obisCodeMapper = null;
    
    private final String[] REGISTERCONFIG =
    { "TotalImportKwh","TotalExportKwh","TotalImportKvarh",
      "TotalExportKvarh","TotalKvah"
    };

    
    /** Creates a new instance of PPM */
    public PPM() {
    }
    
    /* ___ Implement interface MeterProtocol ___ */
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#
     *      setProperties(java.util.Properties)
     */
    public void setProperties(Properties p) throws InvalidPropertyException,
    MissingPropertyException {
        
        if (p.getProperty(MeterProtocol.ADDRESS) != null)
            pAddress = p.getProperty(MeterProtocol.ADDRESS);
        
        if (p.getProperty(MeterProtocol.NODEID) != null)
            pNodeId = p.getProperty(MeterProtocol.NODEID);
        
        if (p.getProperty(MeterProtocol.SERIALNUMBER) != null)
            pSerialNumber = p.getProperty(MeterProtocol.SERIALNUMBER);
        
        if (p.getProperty(MeterProtocol.PASSWORD) != null)
            pPassword = p.getProperty(MeterProtocol.PASSWORD);
        
        if (p.getProperty(PK_OPUS) != null)
            pOpus = p.getProperty(PK_OPUS);
        
        if (p.getProperty(PK_TIMEOUT) != null)
            pTimeout = Integer.parseInt(p.getProperty(PK_TIMEOUT));
        
        if (p.getProperty(PK_RETRIES) != null)
            pRetries = Integer.parseInt(p.getProperty(PK_RETRIES));
        
        if (p.getProperty(MeterProtocol.ROUNDTRIPCORR) != null)
            pRountTripCorrection = Integer.parseInt(p.getProperty(MeterProtocol.ROUNDTRIPCORR));
        
        if (p.getProperty(PK_DELAY_AFTER_FAIL) != null)
            pDelayAfterFail = Integer.parseInt(p.getProperty(PK_DELAY_AFTER_FAIL));
        
        if (p.getProperty(PK_SECURITY_LEVEL) != null)
            pRetries = Integer.parseInt(p.getProperty(PK_SECURITY_LEVEL));
        
        if ( p.getProperty(MeterProtocol.CORRECTTIME) != null )
            pCorrectTime = Integer.parseInt(p.getProperty(MeterProtocol.CORRECTTIME) );
        
        if (p.getProperty(PK_EXTENDED_LOGGING) != null)
            pExtendedLogging = p.getProperty(PK_EXTENDED_LOGGING);
        
        if (p.getProperty(PK_FORCE_DELAY) != null)
            pForceDelay = Integer.parseInt( p.getProperty(PK_FORCE_DELAY) );
        
        validateProperties();
        
    }
    
    private void validateProperties( )
    throws MissingPropertyException, InvalidPropertyException {
        
        if( pPassword == null ) {
            String msg = "";
            msg += "There was no password entered, ";
            msg += "stopping communication. ";
            throw new InvalidPropertyException( msg );
        }
        
        if( pPassword.length() < 8 ){
            String msg = "";
            msg += "Password is too short, the length must be 8 characters, ";
            msg += "stopping communication. ";
            throw new InvalidPropertyException( msg );
        }
        
        if( pPassword.length() > 8 ){
            String msg = "";
            msg += "Password is too long, the length must be 8 characters, ";
            msg += "stopping communication. ";
            throw new InvalidPropertyException( msg );
        }
        
    }
    
        /*
         * (non-Javadoc)
         *
         * @see com.energyict.protocol.MeterProtocol#init( java.io.InputStream,
         *      java.io.OutputStream, java.util.TimeZone, java.util.logging.Logger)
         */
    public void init(InputStream inputStream, OutputStream outputStream,
    TimeZone timeZone, Logger logger) throws IOException {
        
        this.timeZone = timeZone;
        this.logger = logger;
        
        if (logger.isLoggable(Level.INFO)) {
            String infoMsg = "PPM protocol init \n"
            + "- Address          = " + pAddress + "\n"
            + "- Node Id          = " + pNodeId + "\n"
            + "- SerialNumber     = " + pSerialNumber + "\n"
            + "- Password         = " + pPassword + "\n"
            + "- Opus             = " + pOpus + "\n"
            + "- Timeout          = " + pTimeout + "\n"
            + "- Retries          = " + pRetries + "\n"
            + "- Extended Logging = " + pExtendedLogging + "\n"
            + "- RoundTripCorr    = " + pRountTripCorrection + "\n"
            + "- Correct Time     = " + pCorrectTime + "\n"
            + "- TimeZone         = " + timeZone + "\n"
            + "- Force Delay      = " + pForceDelay;
            
            logger.info(infoMsg);
        }
        
        if( isOpus() ){
            opusConnection = new OpusConnection( inputStream, outputStream,
            this );
        } else {
            try {
                flagIEC1107Connection = new FlagIEC1107Connection(inputStream,
                outputStream, pTimeout, pRetries, pForceDelay,
                0, 0,
                new com.energyict.protocolimpl.iec1107.ppm.Encryption());
            } catch (ConnectionException e) {
                logger.severe("PPM: init(...), " + e.getMessage());
            }
        }
        
    }

        /*
         * (non-Javadoc)
         *
         * @see com.energyict.protocol.MeterProtocol#connect()
         */
    public void connect() throws IOException {
        try {
            if( !isOpus() ){
                meterType =
                flagIEC1107Connection.connectMAC(pAddress, pPassword,
                pSecurityLevel, pNodeId);
                
                String ri = meterType.getReceivedIdent().substring(10, 13);
                int version = Integer.parseInt(ri);
                
                logger.log(Level.INFO, "Meter " + meterType.getReceivedIdent() );
                logger.log(Level.INFO, "MeterType version = " + ri + " - "
                + version);
            }
            
            rFactory = new RegisterFactory(this, this, PPMMeterType.ISSUE2);
            profile = new Profile(this, rFactory);
            
            validateSerialNumber();
            doExtendedLogging();
            
        } catch (FlagIEC1107ConnectionException e) {
            disconnect();
            throw new IOException(e.getMessage());
        } catch (NumberFormatException nex) {
            throw new IOException(nex.getMessage());
        }
        
    }
    
    /*  change on: 26/01/2005.  The method should basically ignore the leading
     *  dashes (-) in the comparison.
     */
    private void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((pSerialNumber == null) || ("".equals(pSerialNumber)))
            return;
        // at this point pSerialNumber can not be null any more
        
        String sn = (String) rFactory.getRegister("SerialNumber");
        if( sn!= null ) {
            
            String snNoDash = sn.replaceAll( "-+", "" );
            
            String pSerialNumberNoDash = pSerialNumber.replaceAll( "-+", "" );
            
            if( pSerialNumberNoDash.equals( snNoDash ) )
                return;
        }
        
        throw new IOException("SerialNumber mismatch! meter sn=" + sn
        + ", configured sn=" + pSerialNumber);
    }
    
        /*
         * (non-Javadoc)
         *
         * @see com.energyict.protocol.MeterProtocol#disconnect()
         */
    public void disconnect() throws IOException {
        if( !isOpus() ) {
            try {
                flagIEC1107Connection.disconnectMAC();
            } catch (FlagIEC1107ConnectionException e) {
                logger.severe("disconnect() error, " + e.getMessage());
            }
        }
    }
    
        /*
         * (non-Javadoc)
         *
         * @see com.energyict.protocol.MeterProtocol#getRequiredKeys()
         */
    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }
    
        /*
         * (non-Javadoc)
         *
         * @see com.energyict.protocol.MeterProtocol#getOptionalKeys()
         */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add( PK_OPUS );
        result.add( PK_TIMEOUT );
        result.add( PK_RETRIES );
        result.add( PK_EXTENDED_LOGGING );
        result.add(PK_FORCE_DELAY);
        return result;
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.48 $";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return pAddress;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
     */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        ProfileData profileData = profile.getProfileData(new Date(),
        new Date(), true);
        
        logger.log(Level.INFO, profileData.toString());
        return profileData;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date,
     *      boolean)
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
    throws IOException {
        ProfileData profileData = profile.getProfileData(lastReading,
        new Date(), includeEvents);
        logger.log(Level.INFO, profileData.toString());
        return profileData;
        
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date,
     *      java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
    throws IOException, UnsupportedException {
        ProfileData profileData = profile.getProfileData(from, to,
        includeEvents);
        logger.log(Level.INFO, profileData.toString());
        return profileData;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getMeterReading(int)
     */
    public Quantity getMeterReading(int channelId) throws UnsupportedException,
    IOException {
        
        LoadProfileDefinition lpd = rFactory.getLoadProfileDefinition();
        List l = lpd.toChannelInfoList();
        ChannelInfo ci = (ChannelInfo)l.get( channelId );
        if( ci == null ) {
            logger.log(Level.INFO, "REGISTERCONFIG[0] " + channelId  );
            return null;
        } else {
            logger.log(Level.INFO, "REGISTERCONFIG[0] " + channelId  + " "
            + rFactory.getRegister(REGISTERCONFIG[ci.getChannelId() - 1 ]) );
            return (Quantity) rFactory.getRegister(REGISTERCONFIG[ci.getChannelId() - 1 ]);
        }
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getMeterReading(java.lang.String)
     */
    public Quantity getMeterReading(String name) throws UnsupportedException,
    IOException {
        return (Quantity) rFactory.getRegister(name);
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getNumberOfChannels()
     */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return rFactory.getLoadProfileDefinition().getNrOfChannels();
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getProfileInterval()
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        return rFactory.getSubIntervalPeriod().intValue() * 60;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getTime()
     */
    public Date getTime() throws IOException {
        return rFactory.getTimeDate();
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getRegister(java.lang.String)
     */
    public String getRegister(String name) throws IOException,
    UnsupportedException, NoSuchRegisterException {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#setTime()
     */
    public void setTime() throws IOException {
        
        logger.log( Level.INFO, "Setting time" );

        Date meterTime = getTime();
        
        Calendar sysCalendar = null;
        sysCalendar = ProtocolUtils.getCalendar(timeZone);
        sysCalendar.add(Calendar.MILLISECOND, pRountTripCorrection);
        
        long diff = meterTime.getTime() - sysCalendar.getTimeInMillis();
        
        if( Math.abs( diff ) > MAX_TIME_DIFF ) {
            
            String msg = "Time difference exceeds maximum difference allowed.";
            msg += " ( difference=" + Math.abs( diff ) + " ms ).";
            msg += "The time will only be corrected with ";
            msg += MAX_TIME_DIFF  + " ms.";
            logger.severe( msg );
            
            sysCalendar.setTime( meterTime );
            if( diff < 0 )
                sysCalendar.add(Calendar.MILLISECOND, MAX_TIME_DIFF );
            else
                sysCalendar.add(Calendar.MILLISECOND, -MAX_TIME_DIFF );
            
        }
        
        if( isOpus() ) {
            logger.log( Level.WARNING, "setting clock" );
            try {
                rFactory.setRegister(
                RegisterFactory.R_TIME_ADJUSTMENT_RS232, sysCalendar.getTime());
            } catch( IOException ex ){
                String msg = "Could not do a timeset, probably wrong password.";
                logger.severe( msg );
                
                throw new NestedIOException( ex );
                
            }

        }
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#setRegister(java.lang.String,
     *      java.lang.String)
     */
    public void setRegister(String name, String value) throws IOException,
    NoSuchRegisterException, UnsupportedException {
        // TODO Auto-generated method stub
        
    }
    
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getCache()
     */
    public Object getCache() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#fetchCache(int)
     */
    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#getFirmwareVersion()
     */
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#setCache(java.lang.Object)
     */
    public void setCache(Object cacheObject) {
        // TODO Auto-generated method stub
        
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#updateCache(int,
     *      java.lang.Object)
     */
    public void updateCache(int rtuid, Object cacheObject) throws SQLException,
    BusinessException {
        // TODO Auto-generated method stub
        
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.MeterProtocol#release()
     */
    public void release() throws IOException {
        // TODO Auto-generated method stub
        
    }
    
    /* ___ Implement interface ProtocolLink ___ */
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getChannelMap()
     */
    public ChannelMap getChannelMap() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getDataReadout()
     */
    public byte[] getDataReadout() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107.
     *      ProtocolLink#getFlagIEC1107Connection()
     */
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return this.flagIEC1107Connection;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107. ProtocolLink#getLogger()
     */
    public Logger getLogger() {
        return this.logger;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getPassword()
     */
    public String getPassword() {
        return pPassword;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.iec1107.ProtocolLink#getTimeZone()
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    /* ___ Implement interface HHUEnabler ___ */
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.base.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel,
     *      boolean)
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,
    boolean enableDataReadout) throws ConnectionException {
        HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(commChannel,
        pTimeout, pRetries, pForceDelay, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.base.HHUEnabler#enableHHUSignOn(com.energyict.dialer.core.SerialCommunicationChannel)
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel)
    throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.base.HHUEnabler#getHHUDataReadout()
     */
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.base.SerialNumber#getSerialNumber(com.energyict.dialer.core.SerialCommunicationChannel,
     *      java.lang.String)
     */
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        // KV 22072005 unused code
        //SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        //String nodeId = discoverInfo.getNodeId();
        
        return rFactory.getSerialNumber();
    }
    
    static Map exception = new HashMap();
    static {
        exception
        .put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");
        
        exception
        .put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist"
        + " in the meter");
        exception
        .put("ERR3", "Invalid Packet Number");
        
        exception
        .put("ERR5", "Data Identity is locked - password timeout");
        
        exception
        .put("ERR6", "General Comms error");
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocolimpl.base.MeterExceptionInfo#getExceptionInfo(java.lang.String)
     */
    public String getExceptionInfo(String id) {
        String exceptionInfo = (String) exception.get(id);
        if (exceptionInfo != null)
            return id + ", " + exceptionInfo;
        else
            return "No meter specific exception info for " + id;
    }
    
    /* ___ Implement interface RegisterProtocol ___ */
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.RegisterProtocol#readRegister(com.energyict.obis.ObisCode)
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if( obisCodeMapper == null )
            obisCodeMapper = new ObisCodeMapper( rFactory );
        return obisCodeMapper.getRegisterValue(obisCode);
    }
    
    /*
     * (non-Javadoc)
     *
     * @see com.energyict.protocol.RegisterProtocol#translateRegister(com.energyict.obis.ObisCode)
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    
    /* ___ ___ */
    
    public RegisterFactory getRegisterFactory() {
        return rFactory;
    }
    
    public OpusConnection getOpusConnection( ){
        return opusConnection;
    }
    
    public boolean isOpus( ){
        return "1".equals( pOpus );
    }
    
    public String getNodeId() {
        return pNodeId;
    }
    
    public int getMaxRetry() {
        return pRetries;
    }
    
    public long getForceDelay() {
        return pForceDelay;
    }
    
    public long getDelayAfterFail(){
        return pDelayAfterFail;
    }
    
    public long getTimeout() {
        return pTimeout;
    }
    
    public void doExtendedLogging( ) throws IOException {
        
        if( "1".equals(pExtendedLogging ) )
            this.logger.info( rFactory.getRegisterInformation().getExtendedLogging() + " \n" );
        
    }
    
}
