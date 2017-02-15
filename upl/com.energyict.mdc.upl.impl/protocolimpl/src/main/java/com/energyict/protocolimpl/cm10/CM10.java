package com.energyict.protocolimpl.cm10;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CM10 extends AbstractProtocol {

    private static final String IS_C10_METER = "CM_10_meter";
    private static final int MAX_CLOCK_DEVIATION = 59;  // max 59 sec deviation

    private CM10Connection cm10Connection = null;
	private CM10Profile cm10Profile = null;
    private CommandFactory commandFactory=null;
    private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
    private RegisterFactory registerFactory;

    private StatusTable statusTable;
    private FullPersonalityTable fullPersonalityTable;
    private CurrentDialReadingsTable currentDialReadingsTable;
    private PowerFailDetailsTable powerFailDetailsTable;

    private int outstationID;
    private int delayAfterConnect;

    private boolean isCM10Meter;

    public CM10(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getCM10Profile().getProfileData(from, to, includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
    	Calendar cal=Calendar.getInstance(getTimeZone());
		return getProfileData(lastReading, cal.getTime(), includeEvents);
    }

    @Override
    public int getProfileInterval() throws IOException {
		return 60 * getFullPersonalityTable().getIntervalInMinutes();
    }

    int getOutstationId() {
    	return this.outstationID;
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getStatusTable().getNumberOfChannels();
    }

    @Override
	protected void doConnect() throws IOException {
		ProtocolUtils.delayProtocol(delayAfterConnect);
	}

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("DelayAfterConnect", PropertyTranslationKeys.CM10_DELAY_AFTER_CONNECT, false));
        propertySpecs.add(this.integerSpec(IS_C10_METER, PropertyTranslationKeys.CM10_IS_C10_METER, false));
        return propertySpecs;
    }

    @Override
    protected boolean serialNumberIsRequired() {
        return true;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        this.outstationID = Integer.parseInt(properties.getTypedProperty("SerialNumber"));
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(PROP_TIMEOUT, "5000").trim()));
        setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getTypedProperty(PROP_RETRIES, "3").trim()));
        this.delayAfterConnect = Integer.parseInt(properties.getTypedProperty("DelayAfterConnect", "1000"));
        this.isCM10Meter = !"0".equals(properties.getTypedProperty(IS_C10_METER));
    }

	PowerFailDetailsTable getPowerFailDetailsTable() throws IOException {
		if (powerFailDetailsTable == null) {
			getLogger().info("read power fail details");
			Response response = commandFactory.getReadPowerFailDetailsCommand().invoke();
			powerFailDetailsTable = new PowerFailDetailsTable(this);
			powerFailDetailsTable.parse(response.getData());
		}
		return powerFailDetailsTable;
	}

	CurrentDialReadingsTable getCurrentDialReadingsTable() throws IOException {
		if (currentDialReadingsTable == null) {
			getLogger().info("read current dial readings");
			Response response = commandFactory.getReadCurrentDialReadingsCommand().invoke();
			currentDialReadingsTable = new CurrentDialReadingsTable(this);
			currentDialReadingsTable.parse(response.getData());
		}
		return currentDialReadingsTable;
	}

	FullPersonalityTable getFullPersonalityTable() throws IOException {
		if (fullPersonalityTable == null) {
			getLogger().info("read full personality table");
			Response response = commandFactory.getReadFullPersonalityTableCommand().invoke();
			fullPersonalityTable = new FullPersonalityTable(this);
			fullPersonalityTable.parse(response.getData());
		}
		return fullPersonalityTable;
	}

	StatusTable getStatusTable() throws IOException {
		if (statusTable == null) {
			getLogger().info("read status");
			Response response = commandFactory.getReadStatusCommand().invoke();
			statusTable = new StatusTable(this);
			statusTable.parse(response.getData());
		}
		return statusTable;
	}

	private TimeTable getTimeTable() throws IOException {
		getLogger().info("read meter time");
		Response response = commandFactory.getReadTimeCommand().invoke();
		TimeTable timeTable = new TimeTable(this);
		timeTable.parse(response.getData());
		return timeTable;
	}

    @Override
	protected void doDisconnect() throws IOException {
	}

	public RegisterFactory getRegisterFactory() throws IOException {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(this);
            registerFactory.init();
        }
        return registerFactory;
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling,
                                        int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        this.cm10Connection = new CM10Connection(
                inputStream,
                outputStream,
                timeoutProperty,
                protocolRetriesProperty,
                forcedDelay,
                echoCancelling,
                halfDuplexController,
                this
        );
        this.commandFactory = new CommandFactory(this);
        this.cm10Profile = new CM10Profile(this);
        return this.cm10Connection;
    }

    private CM10Profile getCM10Profile() {
        return cm10Profile;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
		if (!isCM10Meter) {
			return "CM32 (firmware version not available)";
		} else {
			try {
				getLogger().info("read memory direct");
				Response response = commandFactory.getReadMemoryDirectCommand().invoke();
				this.getLogger().info("memory direct: " + ProtocolUtils.outputHexString(response.getData()));
				getLogger().info("end read mem direct");
				return "CM10_" + new String(response.getData());
			} catch (InvalidCommandException e) {
				throw new IOException("Check property '" + IS_C10_METER + "'!, This meter is no CM10 meter.");
			}
		}
	}

    @Override
    public String getProtocolVersion() {
		return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
	}

    @Override
	public Date getTime() throws IOException {
		return getTimeTable().getTime();
	}

    @Override
    public void setTime() throws IOException {
        /* Set time is only possible on commissioning or after loading a new personality table
         * Use only trimmer.
         * The value sent to the meter is added on the RTC value in the meter */
        byte result;
        Calendar systemTimeCal = Calendar.getInstance(getTimeZone());
        Calendar meterTimeCal = Calendar.getInstance(getTimeZone());
        meterTimeCal.setTime(getTime());
        long meterTimeInMillis = meterTimeCal.getTimeInMillis();
        long systemTimeInMilis = systemTimeCal.getTimeInMillis();
        if (Math.abs((meterTimeInMillis - systemTimeInMilis) / 1000) < MAX_CLOCK_DEVIATION) {
            result = (byte) ((int) ((systemTimeInMilis - meterTimeInMillis) / 1000) & 0x000000FF);
        } else {
            result = MAX_CLOCK_DEVIATION;
            if (meterTimeInMillis > systemTimeInMilis) {
                result = -MAX_CLOCK_DEVIATION;
            }
        }

        getLogger().info("send trim time");
        commandFactory.getTrimClockCommand(result).invoke();
    }

    protected final CommandFactory getCommandFactory() {
        return commandFactory;
    }

    final CM10Connection getCM10Connection() {
        return cm10Connection;
    }

}