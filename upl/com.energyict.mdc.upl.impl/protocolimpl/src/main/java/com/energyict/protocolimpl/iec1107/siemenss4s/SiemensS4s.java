package com.energyict.protocolimpl.iec1107.siemenss4s;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectFactory;
import com.energyict.protocolimpl.iec1107.siemenss4s.security.SiemensS4sEncryptor;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class SiemensS4s extends AbstractIEC1107Protocol implements SerialNumberSupport {

	private S4sObjectFactory objectFactory;
	private SiemensS4sProfile profileObject;
	private SiemensS4sObisCodeMapper obisCodeMapper;

	private String nodeAddress;
	private String deviceId;
	private String passWord;
	private String serialNumber;

	private boolean requestDataReadout;

	private int securityLevel;
	private int channelMap;

	private byte[] dataReadout;

	/**
	 * Creates a new instance of the SiemesS4s protocol
	 */
	public SiemensS4s(){
		super(new SiemensS4sEncryptor());
	}

    @Override
    public String getSerialNumber() {
        try {
            return getObjectFactory().getSerialNumberObject().getSerialNumber();
        } catch (IOException e) {
           throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    public void connect() throws IOException {
        try {
            if (requestDataReadout) {
               dataReadout = getFlagIEC1107Connection().dataReadout(deviceId,nodeAddress);
               getFlagIEC1107Connection().disconnectMAC();
            }
            getFlagIEC1107Connection().connectMAC(deviceId,passWord,securityLevel,nodeAddress);
            doConnect();
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new ProtocolConnectionException(e.getMessage(), e.getReason());
        }

    }

	protected void doConnect() throws IOException {
		initLocalObjects();
	}

    public int getNumberOfChannels(){
        return this.channelMap;
     }

	/**
	 * Initialize local objects
	 */
	private void initLocalObjects(){
		this.objectFactory = new S4sObjectFactory(getFlagIEC1107Connection());
		this.profileObject = new SiemensS4sProfile(this.objectFactory);
	}

	protected List<String> doGetOptionalKeys() {
		return Collections.emptyList();
	}

	/**
	 * Set certain properties before doing anything
	 */
	protected void doValidateProperties(Properties properties)
	throws MissingPropertyException, InvalidPropertyException {
		this.deviceId = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName());
		this.passWord = properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), "4281602592");
		if("".equalsIgnoreCase(this.passWord)){
			this.passWord = "4281602592";
		}
		//TODO set the level in the encryptor
		this.securityLevel=Integer.parseInt(properties.getProperty("SecurityLevel","2").trim());
		this.nodeAddress=properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(),"");
		this.serialNumber=properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName());
		this.channelMap = Integer.parseInt(properties.getProperty("ChannelMap","1"));
	}

	/**
	 * Read the time from the meter.
	 */
	public Date getTime() throws IOException {
		Calendar s4sDateTime = getObjectFactory().getDateTimeObject().getMeterTime();
		return s4sDateTime.getTime();
	}

	/**
	 * @return the meter his current profileInterval.
	 */
	public int getProfileInterval() throws IOException {
		return this.profileObject.getProfileInterval();
	}

	/**
	 * Create the profileObject
	 * @param lastReading - the from date from where to start reading
	 * @param includeEvents - indicates whether we need to read the events
	 * @return the requested loadProfile
	 */
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return this.profileObject.getProfileData(lastReading, includeEvents);
	}

	/**
	 * Read a register given the obisCode
	 * @param obisCode - the ObisCode of the register
	 * @return a registerValue containing necessary register information
	 */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    /**
     * Getter for the obisCodeMapper. If he doesn't exist, then create ONE.
     * @return the registerObisCodeMapper
     */
    private SiemensS4sObisCodeMapper getObisCodeMapper(){
    	if(this.obisCodeMapper == null){
    		this.obisCodeMapper = new SiemensS4sObisCodeMapper(this.getObjectFactory());
    	}
    	return this.obisCodeMapper;
    }

	/**
	 * @return the current objectFactory
	 */
	private S4sObjectFactory getObjectFactory(){
		return this.objectFactory;
	}

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:00 +0200 (Thu, 26 Nov 2015)$";
    }
}
