package com.energyict.protocolimpl.iec1107.siemenss4s;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

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
import java.util.Date;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;

public class SiemensS4s extends AbstractIEC1107Protocol implements SerialNumberSupport {

	private S4sObjectFactory objectFactory;
	private SiemensS4sProfile profileObject;
	private SiemensS4sObisCodeMapper obisCodeMapper;

	private String nodeAddress;
	private String deviceId;
	private String passWord;

    private boolean requestDataReadout;

	private int securityLevel;
	private int channelMap;

	private byte[] dataReadout;

	/**
	 * Creates a new instance of the SiemesS4s protocol
	 */
	public SiemensS4s(PropertySpecService propertySpecService){
		super(new SiemensS4sEncryptor(), propertySpecService);
	}

    @Override
    public String getSerialNumber() {
        try {
            return getObjectFactory().getSerialNumberObject().getSerialNumber();
        } catch (IOException e) {
           throw ProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

	@Override
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

	@Override
	protected void doConnect() throws IOException {
		initLocalObjects();
	}

	@Override
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

    @Override
    public void setProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        super.setProperties(properties);
        try {
            this.deviceId = properties.getTypedProperty(ADDRESS.getName());
            this.passWord = properties.getTypedProperty(PASSWORD.getName(), "4281602592");
            if("".equalsIgnoreCase(this.passWord)){
                this.passWord = "4281602592";
            }
            //TODO set the level in the encryptor
            this.securityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(),"2").trim());
            this.nodeAddress = properties.getTypedProperty(NODEID.getName(),"");
            this.channelMap = Integer.parseInt(properties.getTypedProperty("ChannelMap","1"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
	public Date getTime() throws IOException {
		Calendar s4sDateTime = getObjectFactory().getDateTimeObject().getMeterTime();
		return s4sDateTime.getTime();
	}

    @Override
	public int getProfileInterval() throws IOException {
		return this.profileObject.getProfileInterval();
	}

    @Override
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return this.profileObject.getProfileData(lastReading, includeEvents);
	}

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    private SiemensS4sObisCodeMapper getObisCodeMapper(){
    	if(this.obisCodeMapper == null){
    		this.obisCodeMapper = new SiemensS4sObisCodeMapper(this.getObjectFactory());
    	}
    	return this.obisCodeMapper;
    }

	private S4sObjectFactory getObjectFactory(){
		return this.objectFactory;
	}

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:00 +0200 (Thu, 26 Nov 2015)$";
    }

}