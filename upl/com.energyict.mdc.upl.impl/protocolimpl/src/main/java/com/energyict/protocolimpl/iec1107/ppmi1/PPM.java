/*
 * PPM.java
 *
 * Created on 16 juli 2004, 8:57
 */

package com.energyict.protocolimpl.iec1107.ppmi1;

import java.io.FileOutputStream;
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
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SerialNumber;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ppmi1.opus.OpusConnection;
import com.energyict.protocolimpl.iec1107.ppmi1.register.LoadProfileDefinition;

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

public class PPM implements
MeterProtocol, HHUEnabler, SerialNumber,
MeterExceptionInfo, RegisterProtocol {

	private final static int MAX_TIME_DIFF = 50000;

	/** The minimum period of time that must be elapsed in order
	 * for an interval to be valid/acceptable. (in millisecs)
	 * (see Fix for data spikes) */
	public final static int MINIMUM_INTERVAL_AGE = 60000;

	private TimeZone timeZone = null;
	private Logger logger = null;

	/** Property keys specific for PPM protocol. */
	public final static String PK_OPUS = "OPUS";
	public final static String PK_TIMEOUT = "Timeout";
	public final static String PK_RETRIES = "Retries";
	public final static String PK_FORCE_DELAY = "ForcedDelay";
	public final static String PK_DELAY_AFTER_FAIL = "DelayAfterFail";
	public final static String PK_OFFLINE = "pkoffline";
	public final static String PK_EXTENDED_LOGGING = "ExtendedLogging";

	/** Property Default values */
	//final static String PD_ADDRESS = null;
	private final static String PD_NODE_ID = "";
	private final static int PD_TIMEOUT = 10000;
	private final static int PD_RETRIES = 5;
	private final static int PD_ROUNDTRIP_CORRECTION = 0;
	private final static long PD_FORCE_DELAY = 350;
	private final static long PD_DELAY_AFTER_FAIL = 500;
	private final static int PD_IEC1107_COMPAT = 0;
	private final static String PD_OPUS = "1";
	private final static String PD_PASSWORD = "--------";
	private final static String PD_EXTENDED_LOGGING = "0";
	private static final int PD_SECURITY_LEVEL = 0;

	/** Property values */
	/** Required properties will have NO default value */
	private String pProfileInterval = null;
	private String pSerialNumber = null;
	private String pAddress = null;

	/** Optional properties make use of default value */
	private String pNodeId = PD_NODE_ID;
	private String pPassword = PD_PASSWORD;
	/* Protocol timeout fail in msec */
	private int pTimeout = PD_TIMEOUT;
	/* Max nr of consecutive protocol errors before end of communication */
	private int pRetries = PD_RETRIES;
	/* Offset in ms to the get/set time */
	private int pRountTripCorrection = PD_ROUNDTRIP_CORRECTION;
	/* Delay in msec between protocol Message Sequences */
	private long pForceDelay = PD_FORCE_DELAY;
	/* Delay in msec after a protocol error */
	private long pDelayAfterFail = PD_DELAY_AFTER_FAIL;
	/* 1 if opus protocol is used, 0 if not */
	private String pOpus = PD_OPUS;
	private String pExtendedLogging = PD_EXTENDED_LOGGING;
	private int pSecurityLevel = PD_SECURITY_LEVEL;

	// KV22072005 flagIEC1107Connection is never created???
	FlagIEC1107Connection flagIEC1107Connection = null;

	OpusConnection opusConnection = null;

	RegisterFactory rFactory = null;
	Profile profile = null;
	ObisCodeMapper obisCodeMapper = null;

	private final String[] REGISTERCONFIG = {
			"TotalImportKwh",
			"TotalExportKwh",
			"TotalImportKvarh",
			"TotalExportKvarh",
			"TotalKvah"
	};

	private boolean software7E1 = false;

	private MeterType meterType = null;

	/** The historical data register contains data for 4 days */
	public final static int NR_HISTORICAL_DATA = 4;
	public final static int NR_TOU_REGISTERS = 8; // KV 22072005 made final
	public final static int NR_MD_TOU_REGISTERS = 4; // KV 22072005 made final

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
	public void setProperties(Properties p) throws InvalidPropertyException, MissingPropertyException {

		if (p.getProperty(MeterProtocol.ADDRESS) != null) {
			this.pAddress = p.getProperty(MeterProtocol.ADDRESS);
		}

		if (p.getProperty(MeterProtocol.NODEID) != null) {
			pNodeId = p.getProperty(MeterProtocol.NODEID);
		}

		if (p.getProperty(MeterProtocol.SERIALNUMBER) != null) {
			pSerialNumber = p.getProperty(MeterProtocol.SERIALNUMBER);
		}

		if (p.getProperty(MeterProtocol.PROFILEINTERVAL) != null) {
			pProfileInterval = p.getProperty(MeterProtocol.PROFILEINTERVAL);
		}

		if (p.getProperty(MeterProtocol.PASSWORD) != null) {
			pPassword = p.getProperty(MeterProtocol.PASSWORD);
		}

		if (p.getProperty(PK_OPUS) != null) {
			pOpus = p.getProperty(PK_OPUS);
		}

		if (p.getProperty(PK_TIMEOUT) != null) {
			pTimeout = Integer.parseInt(p.getProperty(PK_TIMEOUT));
		}

		if (p.getProperty(PK_RETRIES) != null) {
			pRetries = Integer.parseInt(p.getProperty(PK_RETRIES));
		}

		if (p.getProperty(PK_EXTENDED_LOGGING) != null) {
			pExtendedLogging = p.getProperty(PK_EXTENDED_LOGGING);
		}

		if (p.getProperty(PK_FORCE_DELAY) != null) {
			pForceDelay = Integer.parseInt( p.getProperty(PK_FORCE_DELAY) );
		}

		this.software7E1 = !p.getProperty("Software7E1", "0").equalsIgnoreCase("0");

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
			String infoMsg =
				"PPM protocol init \n" +
				"- Password         = " + pPassword + "\n" +
				"- ProfileInterval  = " + pProfileInterval + "\n" +
				"- SerialNumber     = " + pSerialNumber + "\n" +
				"- Node Id          = " + pNodeId + "\n" +
				"- Timeout          = " + pTimeout + "\n" +
				"- Retries          = " + pRetries + "\n" +
				"- Extended Logging = " + pExtendedLogging + "\n" +
				"- RoundTripCorr    = " + pRountTripCorrection + "\n" +
				"- TimeZone         = " + timeZone + "\n" +
				"- Force Delay      = " + pForceDelay;

			logger.info(infoMsg);
		}

		if (isOpus()) {
			opusConnection = new OpusConnection(inputStream, outputStream, this);
		} else {
			try {
				this.flagIEC1107Connection = new FlagIEC1107Connection(
						inputStream,
						outputStream,
						this.pTimeout,
						this.pRetries,
						this.pForceDelay,
						0,
						0,
						new com.energyict.protocolimpl.iec1107.ppm.Encryption(),
						this.software7E1
				);
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
				this.meterType  = this.flagIEC1107Connection.connectMAC(this.pAddress, this.pPassword, this.pSecurityLevel, this.pNodeId);
				String ri = this.meterType.getReceivedIdent().substring(10, 13);
				int version = Integer.parseInt(ri);
				this.logger.log(Level.INFO, "Meter " + this.meterType.getReceivedIdent());
				this.logger.log(Level.INFO, "MeterType version = " + ri + " - " + version);
				rFactory = new OpticalRegisterFactory(this, this );
			} else {
				rFactory = new OpusRegisterFactory(this, this );
			}

			obisCodeMapper = new ObisCodeMapper(rFactory);
			profile = new Profile(this, rFactory);

			validateSerialNumber();

		} catch (FlagIEC1107ConnectionException e) {
			disconnect();
			throw new IOException(e.getMessage());
		} catch (NumberFormatException nex) {
			throw new IOException(nex.getMessage());
		}

		doExtendedLogging();

	}

	/*  change on: 26/01/2005.  The method should basically ignore the leading
	 *  dashes (-) in the comparison.
	 */
	private void validateSerialNumber() throws IOException {

		if ((pSerialNumber == null) || ("".equals(pSerialNumber))) {
			return;
		}

		// at this point pSerialNumber can not be null any more

		String sn = (String) rFactory.getRegister("SerialNumber");

		if( sn != null ) {

			String snNoDash = sn.replaceAll( "-+", "" );

			String pSerialNumberNoDash = pSerialNumber.replaceAll( "-+", "" );

			if( pSerialNumberNoDash.equals( snNoDash ) ) {
				return;
			}
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
		if (!isOpus()) {
			try {
				this.flagIEC1107Connection.disconnectMAC();
			} catch (FlagIEC1107ConnectionException e) {
				this.logger.severe("disconnect() error, " + e.getMessage());
			}
		} else {
			logger.info("errorCount="+opusConnection.getErrorCount());
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
		result.add(PK_OPUS);
		result.add("Timeout");
		result.add("Retries");
		result.add(PK_EXTENDED_LOGGING);
		result.add(PK_FORCE_DELAY);
		result.add("Software7E1");
		return result;
	}

	public String getProtocolVersion() {
		return "$Revision: 1.30 $";
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		String str = "unknown";
		return str;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energyict.protocol.MeterProtocol#getProfileData(boolean)
	 */
	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		ProfileData profileData = profile.getProfileData(new Date(), new Date(), true);
		logger.log(Level.INFO, profileData.toString());
		return profileData;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date,
	 *      boolean)
	 */
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		ProfileData profileData = profile.getProfileData(lastReading, new Date(), includeEvents);
		logger.log(Level.INFO, profileData.toString());
		return profileData;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energyict.protocol.MeterProtocol#getProfileData(java.util.Date,
	 *      java.util.Date, boolean)
	 */
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
		ProfileData profileData = profile.getProfileData(from, to, includeEvents);
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
		ChannelInfo ci = (ChannelInfo) l.get(channelId);
		if (ci == null) {
			logger.log(Level.INFO, "REGISTERCONFIG[0] " + channelId);
			return null;
		} else {
			logger.log(Level.INFO, "REGISTERCONFIG[0] "
					+ channelId
					+ " "
					+ rFactory.getRegister(REGISTERCONFIG[ci
					                                      .getChannelId() - 1]));
			return (Quantity) rFactory.getRegister(REGISTERCONFIG[ci
			                                                      .getChannelId() - 1]);
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
		LoadProfileDefinition lpd = rFactory.getLoadProfileDefinition();
		System.out.println(lpd.toString());
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
	public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {


		return "";
	}

	/*
	 * Important: A timeset can only be done if the difference is less than
	 * 50 seconds.
	 *
	 * @see com.energyict.protocol.MeterProtocol#setTime()
	 */
	public void setTime() throws IOException {

		logger.log(Level.INFO, "Setting time");

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
			if( diff < 0 ) {
				sysCalendar.add(Calendar.MILLISECOND, MAX_TIME_DIFF );
			} else {
				sysCalendar.add(Calendar.MILLISECOND, -MAX_TIME_DIFF );
			}

		}

		if (isOpus()) {
			logger.log(Level.WARNING, "setting clock" );
			try {
				rFactory.setRegister(OpusRegisterFactory.R_TIME_ADJUSTMENT_RS232, sysCalendar
						.getTime());
			} catch( IOException ex ){
				String msg = "Could not do a timeset, probably wrong password";
				msg += " (PPM Isue 1 only checks the complete password during ";
				msg += "timesets).";
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
	public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
		if (name.equals("dumpLoadProfile") && value.equals("1")) {
			String fileName = System.currentTimeMillis() + "_ppm1";
			byte[] data = rFactory.getRegisterRawData(OpticalRegisterFactory.R_LOAD_PROFILE, 128*192);
			System.out.println(ProtocolUtils.getResponseData(data));
			FileOutputStream writer = new FileOutputStream("C:\\EnergyICT\\WorkingDir\\ppm_profiles\\" + fileName + ".hex");
			writer.write(data);
			writer.close();
		}
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
		HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel,
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
		//        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
		//        String nodeId = discoverInfo.getNodeId();

		return rFactory.getSerialNumber();
	}

	static Map exception = new HashMap();
	static {
		exception.put("ERR1", "Invalid Command/Function type e.g. other than W1, R1 etc");
		exception.put("ERR2", "Invalid Data Identity Number e.g. Data id does not exist" + " in the meter");
		exception.put("ERR3", "Invalid Packet Number");
		exception.put("ERR5", "Data Identity is locked - password timeout");
		exception.put("ERR6", "General Comms error");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energyict.protocolimpl.base.MeterExceptionInfo#getExceptionInfo(java.lang.String)
	 */
	public String getExceptionInfo(String id) {
		String exceptionInfo = (String) exception.get(id);
		if (exceptionInfo != null) {
			return id + ", " + exceptionInfo;
		} else {
			return "No meter specific exception info for " + id;
		}
	}

	/* ___ Implement interface RegisterProtocol ___ */

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energyict.protocol.RegisterProtocol#readRegister(com.energyict.obis.ObisCode)
	 */
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
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

	public OpusConnection getOpusConnection() {
		return opusConnection;
	}

	public boolean isOpus() {
		return "1".equals(pOpus);
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

	public void log(String message) {
		this.logger.log(Level.INFO, message);
	}

	public void log(Level level, String message) {
		this.logger.log(level, message);
	}

	public void doExtendedLogging( ) throws IOException {
		if( "1".equals(pExtendedLogging ) ) {
			this.logger.info( rFactory.getRegisterInformation().getExtendedLogging() + " \n" );
		}
	}

}
