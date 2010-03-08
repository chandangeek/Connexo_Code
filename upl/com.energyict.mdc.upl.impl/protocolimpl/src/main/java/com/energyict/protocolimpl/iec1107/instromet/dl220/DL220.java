/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.AbstractObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.DLObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.SoftwareVersionObject;

/**
 * ProtocolImplementation for the Elster DL220 meter. <br>
 * <br>
 * 
 * <b>General Description:</b><br>
 * The Data Logger DL220 is intended to be used as a battery operated, compact device for the acquisition and storage of
 * metering pulses and / or level changes for various types of energy. <br>
 * <br>
 * <b>Data interface:</b><br>
 * <li>Optical interface according to IEC1107 <li>Internal GSM modem
 * <br><br>
 * <b>Additional information:</b><br>
 * Before getting or setting some values in the meter, it is required to enable a lock (suppliers, customers, ...)
 * 
 * @author gna
 * @since 8-feb-2010
 * 
 */
public class DL220 extends AbstractIEC1107Protocol {

	/** The used {@link DL220Profile} */
	private DL220Profile profile;
	
	/** The used {@link ObjectFactory} */
	private ObjectFactory objectFactory;

	/** The index of the measurement. This is a zero based value of the two indexes */
	private int meterIndex;
	/** The size of the profileRequestBlocks */
	private int profileRequestBlockSize;
	/**
	 * Default constructor
	 */
	public DL220() {
		super();
	}

	public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger)
			throws IOException {
		setTimeZone(timeZone);
		setLogger(logger);
		try {
			flagIEC1107Connection = new DL220Connection(inputStream, outputStream, iec1107TimeoutProperty,
					protocolRetriesProperty, forcedDelay, echoCancelling, iec1107Compatible, software7E1);
		} catch (ConnectionException e) {
			logger.severe("IndigoPlus, init, " + e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getProtocolVersion() {
		return "$Date$";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doConnect() throws IOException {
		AbstractObject sco = getObjectFactory().getSuppliersCombination();
		sco.setValue(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() throws NestedIOException {
		try {
			getObjectFactory().getSuppliersCombination().setLock();
			getFlagIEC1107Connection().disconnectMAC();
		} catch (FlagIEC1107ConnectionException e) {
			getLogger().severe("disconnect() error, " + e.getMessage());
		} catch (ConnectionException e) {
			getLogger().severe("disconnect() error - setLock ConnectionError, " + e.getMessage());
		} catch (IOException e) {
			getLogger().severe("disconnect() error - setLock IOException, " + e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings(value = { "unchecked" })
	protected List doGetOptionalKeys() {
		List keys = new ArrayList();
		keys.add("ProfileRequestBlockSize");
		return keys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doValidateProperties(Properties properties) throws MissingPropertyException,	InvalidPropertyException {
		
		/* The NodeAddress is used to indicate which input is connected */
		if(nodeId.equalsIgnoreCase("")){
			meterIndex = 0;
		} else if ((Integer.parseInt(nodeId) > 2) && ((Integer.parseInt(nodeId) < 1))){
			throw new InvalidPropertyException("Incorrect NodeAddress property. If the value is not empty, then only 1 or 2 is allowed. " +
					"If NodeAddress is empty, then default 1 will be used.");
		} else {
			meterIndex = Integer.parseInt(nodeId) - 1;
		}
		
		this.profileRequestBlockSize = Integer.parseInt(properties.getProperty("ProfileRequestBlockSize", "10"));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFirmwareVersion() throws IOException, UnsupportedException {

		AbstractObject mo = getObjectFactory().getManufacturerObject();
		AbstractObject mt = getObjectFactory().getMeterTypeObject();
		SoftwareVersionObject sv = getObjectFactory().getSoftwareVersionObject();

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Manufacturer : ");
		strBuilder.append(mo.getValue());
		strBuilder.append(" - DeviceType : ");
		strBuilder.append(mt.getValue());
		strBuilder.append(" - SoftwareVersion : ");
		strBuilder.append(sv.getValue(2));
		strBuilder.append(" ");
		strBuilder.append(sv.getValue(3));
		return strBuilder.toString();
	}

	/**
	 * Validate the serialNumber of the device.
	 * 
	 * @throws IOException
	 *             if the serialNumber doesn't match the one from the Rtu
	 */
	protected void validateSerialNumber() throws IOException {
		DLObject serialNubmer = DLObject.constructObject(this, DLObject.SA_SERIALNUMBER);
		String meterSerialNumber = serialNubmer.getValue(1);
		if (this.serialNumber != null && !this.serialNumber.equals(meterSerialNumber)) {
			throw new IOException("Wrong serialnumber, EIServer settings: " + this.serialNumber + " - Meter settings: " + meterSerialNumber);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getTime() throws IOException {
		return getObjectFactory().getClockObject().getDateTime().getTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTime() throws IOException {
		getObjectFactory().getClockObject().writeClock();
	}

	/**
	 * {@inheritDoc}
	 */
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getProfileObject().getNumberOfChannels();
     }
    
    /**
     * 
     * {@inheritDoc}
     */
    public int getProfileInterval() throws UnsupportedException, IOException {
        return getProfileObject().getInterval();
    }
	
    /**
     * {@inheritDoc}
     */
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getProfileData(lastReading, new Date(), includeEvents);
	}

	/**
	 * {@inheritDoc}
	 */
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
		ProfileData profileData = new ProfileData();

		//TODO set channelInfos
		profileData.setChannelInfos(getProfileObject().buildChannelInfos());
		
		//TODO set intervalData
		profileData.setIntervalDatas(getProfileObject().getIntervalData(from, to));
		
		//TODO set events
		if(includeEvents){
			profileData.setMeterEvents(getProfileObject().getMeterEventList(from));
		}
		
		return profileData;
	}
    
    /**
     * @return	the {@link DL220Profile}
     */
    protected DL220Profile getProfileObject(){
    	if(this.profile == null){
    		//TODO measurement1 should be adjustable according to the meterindex
    		this.profile = new DL220Profile(this, meterIndex, Archives.MEASUREMENT1, profileRequestBlockSize);
    	}
    	return this.profile;
    }
    
	/**
	 * Getter for the {@link ObjectFactory}
	 * 
	 * @return the ObjectFactory
	 */
	protected ObjectFactory getObjectFactory() {
		if (this.objectFactory == null) {
			this.objectFactory = new ObjectFactory(this);
		}
		return this.objectFactory;
	}
}
