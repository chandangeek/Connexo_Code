package com.energyict.protocolimpl.cm10;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;

import java.io.*;
import java.util.*;

public class CM10 extends AbstractProtocol {

    final static String IS_C10_METER = "CM_10_meter";
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

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getCM10Profile().getProfileData(from, to, includeEvents);
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
    	Calendar cal=Calendar.getInstance(getTimeZone());
		return getProfileData(lastReading, cal.getTime(), includeEvents);
    }
    
    public int getProfileInterval() throws IOException {
		return 60 * getFullPersonalityTable().getIntervalInMinutes();
    }
    
    public int getOutstationId() {
    	return this.outstationID;
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    } 
    
    public int getNumberOfChannels() throws IOException {
        return getStatusTable().getNumberOfChannels();
    }
    
    protected void validateSerialNumber() throws IOException {

    }
    
	protected void doConnect() throws IOException {
		ProtocolUtils.delayProtocol(delayAfterConnect);
	}

    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        try {
            this.outstationID = Integer.parseInt(properties.getProperty("SerialNumber"));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The node address field has not been filled in");
        }

        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty(PROP_TIMEOUT, "5000").trim()));
        setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getProperty(PROP_RETRIES, "3").trim()));
        this.delayAfterConnect = Integer.parseInt(properties.getProperty("DelayAfterConnect", "1000"));
        this.isCM10Meter = !"0".equals(properties.getProperty("CM_10_meter"));
    }

    public List getOptionalKeys() {
		ArrayList list = new ArrayList();
		list.add("Timeout");
		list.add("Retries");
		list.add("DelayAfterConnect");
		list.add(IS_C10_METER);
		return list;
	}
	
	public PowerFailDetailsTable getPowerFailDetailsTable() throws IOException {
		if (powerFailDetailsTable == null) {
			getLogger().info("read power fail details");
			Response response = commandFactory.getReadPowerFailDetailsCommand().invoke();
			powerFailDetailsTable = new PowerFailDetailsTable(this);
			powerFailDetailsTable.parse(response.getData());
		}
		return powerFailDetailsTable;
	}
	
	public CurrentDialReadingsTable getCurrentDialReadingsTable() throws IOException {
		if (currentDialReadingsTable == null) {
			getLogger().info("read current dial readings");
			Response response = commandFactory.getReadCurrentDialReadingsCommand().invoke();
			currentDialReadingsTable = new CurrentDialReadingsTable(this);
			currentDialReadingsTable.parse(response.getData());
		}
		return currentDialReadingsTable;
	}
	
	public FullPersonalityTable getFullPersonalityTable() throws IOException {
		if (fullPersonalityTable == null) {
			getLogger().info("read full personality table");
			Response response = commandFactory.getReadFullPersonalityTableCommand().invoke();
			fullPersonalityTable = new FullPersonalityTable(this);
			fullPersonalityTable.parse(response.getData());
		}
		return fullPersonalityTable;
	}

	public StatusTable getStatusTable() throws IOException {
		if (statusTable == null) {
			getLogger().info("read status");
			Response response = commandFactory.getReadStatusCommand().invoke();
			statusTable = new StatusTable(this);
			statusTable.parse(response.getData());
		}
		return statusTable;
	}
	
	public TimeTable getTimeTable() throws IOException {
		getLogger().info("read meter time");
		Response response = commandFactory.getReadTimeCommand().invoke();
		TimeTable timeTable = new TimeTable(this);
		timeTable.parse(response.getData());
		return timeTable;
	}
	
	protected void doDisConnect() throws IOException {
	}

	protected List doGetOptionalKeys() {
		return new ArrayList();
	}
	
	public RegisterFactory getRegisterFactory() throws IOException {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(this);
            registerFactory.init();
        }
        return registerFactory;
    }

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

    public CM10Profile getCM10Profile() {
        return cm10Profile;
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

    }

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

	public String getProtocolVersion() {
		return "$Date$";
	}

	public Date getTime() throws IOException {
		return getTimeTable().getTime();
	}

    /**
     * Set time is only possible on commissioning or after loading a new personality table
     * Use only trimmer.
     * The value sent to the meter is added on the RTC value in the meter
     *
     * @throws IOException
     */
    public void setTime() throws IOException {
        byte result = 0;
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

    protected  final CM10Connection getCM10Connection() {
        return cm10Connection;
    }

}
