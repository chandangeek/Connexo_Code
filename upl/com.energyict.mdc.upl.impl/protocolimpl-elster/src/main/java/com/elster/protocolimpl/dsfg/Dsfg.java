package com.elster.protocolimpl.dsfg;

import com.elster.protocolimpl.dsfg.connection.DsfgConnection;
import com.elster.protocolimpl.dsfg.objects.AbstractObject;
import com.elster.protocolimpl.dsfg.profile.ArchiveRecordConfig;
import com.elster.protocolimpl.dsfg.profile.DsfgProfile;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.protocol.*;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * ProtocolImplementation for DSfG devices. <br>
 * <br>
 * 
 * <b>General Description:</b><br>
 * <br>
 * <br>
 * <b>Data interface:</b><br>
 * <li>Optical interface according to IEC1107 <li>Internal GSM modem <br>
 * <br>
 * <b>Additional information:</b><br>
 * 
 * 
 * @author gh
 * @since 5-mai-2010
 * 
 */

public class Dsfg implements MeterProtocol, ProtocolLink {

	/** time zone of device */
	private TimeZone timeZone;
	/** reference to logger */
	private Logger logger;
	/** class of dsfg protocol */
	private DsfgConnection connection;
	/** profile class */
	private DsfgProfile profile = null;

	/** password from given properties */
	private String strPassword;

	/** compatibility properties..., currently not used */
	private int protocolRetriesProperty;
	@SuppressWarnings("unused")
	private int extendedLogging;
	@SuppressWarnings("unused")
	private int profileInterval;
	@SuppressWarnings("unused")
	private int requestHeader;
	@SuppressWarnings("unused")
	private int scaler;

	/** instance letter off registration instance */
	private String registrationInstance = "";
	/** instance letter of archive (in registration instance) */
	private String archiveInstance = "";
	/** mapping of archive boxes to channels */
	private String channelMap = "";
	/** factory for common data objects */
	private DsfgObjectFactory objectFactory = null;

	/** type of meter */
	private String meterType;

	/** archive structure definition */
	private ArchiveRecordConfig archiveStructure = null;

	/**
	 * initialization -> create connection class
	 */
	public void init(InputStream inputStream, OutputStream outputStream,
			TimeZone timezone, Logger logger) throws IOException {
		connection = new DsfgConnection(inputStream, outputStream);
		this.timeZone = timezone;
		this.logger = logger;
	}

	public String getProtocolVersion() {
		String rev = "$Revision: 99 $" + " - "
				+ "$Date: 2010-07-22 09:25:00 +0200 (do, 22 jul 2010) $";
		return "Revision "
				+ rev.substring(rev.indexOf("$Revision: ")
						+ "$Revision: ".length(), rev.indexOf("$ -"))
				+ "at "
				+ rev.substring(rev.indexOf("$Date: ") + "$Date: ".length(),
						rev.indexOf("$Date: ") + "$Date: ".length() + 19);
	}

	/**
	 * the implementation returns both the address and password key
	 * 
	 * @return a list of strings
	 */
	@SuppressWarnings("unchecked")
	public List getRequiredKeys() {
		List result = new ArrayList();
		result.add("RegistrationInstance");
		result.add("ArchiveInstance");
		result.add("ChannelMap");
		return result;
	}

	/**
	 * List of optional keys
	 * 
	 * @return a list of strings
	 */
	@SuppressWarnings("unchecked")
	public List getOptionalKeys() {
		List result = new ArrayList();
		result.add("Timeout");
		result.add("Retries");
		// result.add("SecurityLevel");
		// result.add("EchoCancelling");
		// result.add("IEC1107Compatible");
		// result.add("ExtendedLogging");
		// result.add("ChannelMap");
		// result.add("ForcedDelay");
		// result.add("Software7E1");
		// if needed, add following code lines into the overridden
		// doGetOptionalKeys() method
		// result.add("RequestHeader"));
		// result.add("Scaler"));
		List result2 = doGetOptionalKeys();
		if (result2 != null) {
			result.addAll(result2);
		}
		return result;
	}

	/**
	 * enable derived class to add more keys
	 * 
	 * @return a list of keys (Strings)
	 */
	@SuppressWarnings("unchecked")
	protected List doGetOptionalKeys() {
		return null;
	}

	/**
	 * set the protocol specific properties
	 * 
	 * @param properties
	 *            - properties to use
	 */
	public void setProperties(Properties properties)
			throws InvalidPropertyException, MissingPropertyException {
		validateProperties(properties);
	}

	public void connect() throws IOException {
		connection.connect();
		connection.signon(strPassword);

		// verify device type
		AbstractObject meterTypeObj = getObjectFactory().getMeterTypeObject();
		meterType = meterTypeObj.getValue();
		getLogger().info("-- Type of device: " + meterType);

		// DataBlock db = new DataBlock(getRegistrationInstance(),
		// 'A', 'J', 'V', new String[] {"caaa","czzz"});
		// @SuppressWarnings("unused")
		// DataBlock in = getDsfgConnection().request(db);
	}

	public void disconnect() throws IOException {
		// TODO Auto-generated method stub
	}

	public String getFirmwareVersion() throws IOException {
		return getObjectFactory().getSoftwareVersionObject().getValue();
	}

	public int getNumberOfChannels() throws IOException {
		return getProfileObject().getNumberOfChannels();
	}

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		Calendar calendar = Calendar.getInstance(getTimeZone());
		/* maximum readout range set to 2 year - 6/18/2010 gh */
		calendar.add(Calendar.MONTH, -24);
		return getProfileData(calendar.getTime(), includeEvents);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents)
			throws IOException {
		return getProfileData(lastReading, new Date(), includeEvents);
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
			throws IOException {

		getLogger().info("getProfileData(" + from + "," + to + ")");

		ProfileData profileData = new ProfileData();

		profileData.setChannelInfos(getProfileObject().buildChannelInfos());

		profileData.setIntervalDatas(getProfileObject().getIntervalData(from,
				to));

		return profileData;
	}

	public int getProfileInterval() throws IOException {
		/* interval time of archive can't be easily read out as a value */
		return 3600;
	}

	public String getRegister(String arg0) throws IOException,
			NoSuchRegisterException {
		/* dsfg register instances have no register values ! */
		throw new NoSuchRegisterException(
				"Dsfg devices have no register to read out!");
	}

	public Date getTime() throws IOException {
		return getObjectFactory().getClockObject().getDateTime();
	}

	public void initializeDevice() throws IOException {
		// TODO Auto-generated method stub

	}

	public void release() throws IOException {
		// TODO Auto-generated method stub

	}

	public void setRegister(String arg0, String arg1) throws IOException,
			NoSuchRegisterException {
		/* dsfg register instances have no register values ! */
		throw new NoSuchRegisterException(
				"Dsfg devices have no register to set!");

	}

	public void setTime() throws IOException {
		/* It is not possible to set clock ! */
		throw new IOException("Clock not setable in dsfg devices!");
	}

	// *******************************************************************************************
	// * C l a s s i m p l e m e n t a t i o n c o d e
	// *******************************************************************************************/

	/**
	 * Validate certain protocol specific properties
	 * 
	 * @param properties
	 *            - The properties fetched from the Rtu
	 * @throws MissingPropertyException
	 * @throws InvalidPropertyException
	 */
	@SuppressWarnings( { "unchecked" })
	private void validateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		try {
			Iterator iterator = getRequiredKeys().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if (properties.getProperty(key) == null) {
					throw new MissingPropertyException(key + " key missing");
				}
			}
			strPassword = properties.getProperty(MeterProtocol.PASSWORD);
			protocolRetriesProperty = Integer.parseInt(properties.getProperty(
                    "Retries", "5").trim());
			extendedLogging = Integer.parseInt(properties.getProperty(
                    "ExtendedLogging", "0").trim());
			profileInterval = Integer.parseInt(properties.getProperty(
                    "ProfileInterval", "900").trim());
			requestHeader = Integer.parseInt(properties.getProperty(
                    "RequestHeader", "0").trim());
			scaler = Integer.parseInt(properties.getProperty("Scaler", "0")
                    .trim());

			/* DSfG specific properties */
			registrationInstance = properties.getProperty(
					"RegistrationInstance", "0").toUpperCase().substring(0, 1);
			if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ[^".indexOf(registrationInstance) < 0) {
				throw new InvalidPropertyException(
						" validateProperties, RegistrationInstance ("
								+ registrationInstance
								+ ") out of Range (A-Z).");
			}

			archiveInstance = properties.getProperty("ArchiveInstance", "0")
					.toLowerCase().substring(0, 1);
			if ("abcdefghijklmnopqrstuvwxyz".indexOf(archiveInstance) < 0) {
				throw new InvalidPropertyException(
						" validateProperties, ArchiveInstance ("
								+ archiveInstance + ") out of Range (a-y).");
			}

			try {
				channelMap = properties.getProperty("ChannelMap", "");
				archiveStructure = new ArchiveRecordConfig(archiveInstance,
						channelMap);
			} catch (Exception e) {
				throw new InvalidPropertyException(
						" validateProperties, ChannelMap is not valid ("
								+ channelMap + ")");
			}

			doValidateProperties(properties);
		} catch (NumberFormatException e) {
			throw new InvalidPropertyException(
					" validateProperties, NumberFormatException, "
							+ e.getMessage());
		}
	}

	/**
	 * Getter for the ObjectFactory
	 * 
	 * @return the current ObjectFactory
	 */
	protected DsfgObjectFactory getObjectFactory() {
		if (this.objectFactory == null) {
			this.objectFactory = new DsfgObjectFactory(this);
		}
		return this.objectFactory;
	}

	/**
	 * Getter for dsfg profile object
	 * 
	 * @return DsfgProfile object
	 */
	protected DsfgProfile getProfileObject() {
		if (this.profile == null) {
			this.profile = new DsfgProfile(this, archiveStructure);
		}
		return this.profile;
	}

	public void doValidateProperties(Properties properties) {
	}

	// *******************************************************************************************
	// *
	// * Interface ProtocolLink
	// *
	// *******************************************************************************************/
	public byte[] getDataReadout() {
		// TODO Auto-generated method stub
		return null;
	}

	public DsfgConnection getDsfgConnection() {
		return connection;
	}

	public Logger getLogger() {
		return logger;
	}

	public int getNrOfRetries() {
		return protocolRetriesProperty;
	}

	public String getPassword() {
		return strPassword;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public boolean isIEC1107Compatible() {
		return false;
	}

	public boolean isRequestHeader() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getArchiveInstance() {
		return this.archiveInstance;
	}

	public String getRegistrationInstance() {
		return this.registrationInstance;
	}

	// *******************************************************************************************
	// *
	// * not yet used methods
	// *
	// *******************************************************************************************/
	public Object fetchCache(int arg0) throws SQLException, BusinessException {
		return null;
	}

	public Object getCache() {
		return null;
	}

	public void updateCache(int arg0, Object arg1) throws SQLException,
			BusinessException {
	}

	public void setCache(Object arg0) {
	}

	// *******************************************************************************************
	// *
	// * deprecicated methods
	// *
	// *******************************************************************************************/
	public Quantity getMeterReading(int arg0) throws IOException {
		return null;
	}

	public Quantity getMeterReading(String arg0) throws IOException {
		return null;
	}

}
