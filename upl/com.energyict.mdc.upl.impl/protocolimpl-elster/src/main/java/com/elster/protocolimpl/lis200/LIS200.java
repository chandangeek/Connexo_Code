package com.elster.protocolimpl.lis200;

import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.elster.protocolimpl.dlms.util.ElsterProtocolIOExceptionHandler;
import com.elster.protocolimpl.lis200.commands.AbstractCommand;
import com.elster.protocolimpl.lis200.objects.AbstractObject;
import com.elster.protocolimpl.lis200.objects.HistoricalValueObject;
import com.elster.protocolimpl.lis200.objects.IntervalObject;
import com.elster.protocolimpl.lis200.objects.LockObject;
import com.elster.protocolimpl.lis200.objects.LockObject.STATE;
import com.elster.protocolimpl.lis200.objects.MaxDemandObject;
import com.elster.protocolimpl.lis200.objects.SimpleObject;
import com.elster.protocolimpl.lis200.objects.StatusObject;
import com.elster.protocolimpl.lis200.profile.Lis200Profile;
import com.elster.protocolimpl.lis200.registers.HistoricRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.IRegisterReadable;
import com.elster.protocolimpl.lis200.registers.Lis200RegisterN;
import com.elster.protocolimpl.lis200.registers.MaxRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.RegisterDefinition;
import com.elster.protocolimpl.lis200.registers.RegisterMapN;
import com.elster.protocolimpl.lis200.registers.RegisterReader;
import com.elster.protocolimpl.lis200.registers.SimpleRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.StateRegisterDefinition;
import com.elster.protocolimpl.lis200.registers.ValueRegisterDefinition;
import com.elster.protocolimpl.lis200.utils.RawArchiveLineInfo;
import com.elster.utils.lis200.events.EventInterpreter;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.google.common.collect.Range;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.elster.utils.VersionInfo.getVersionMajor;
import static com.elster.utils.VersionInfo.getVersionMinor;

/**
 * @author heuckeg
 *         <p/>
 *         common protocol driver for LIS200
 *         <p/>
 */
public class LIS200 extends AbstractIEC1107Protocol implements SerialNumberSupport {

    private static final String PROFILE_REQUEST_BLOCK_SIZE = "ProfileRequestBlockSize";
    /*
    * controls usage of wakeup sequence (sequence of binary 0's
    */
    private static final String SUPPRESS_WAKEUP_SEQUENCE = "SuppressWakeupSequence";
    /*
    * Lock to open (there are more possible locks, default is customer
    * lock)
    */
    private static final String USE_LOCK = "UseLock";
    /*
    * Normally we should use NodeAddress or DeviceId, but the framework
    * uses these for the IEC1107Connection
    */
    private static final String METER_INDEX = "MeterIndex";
    /*
    * Archive to readout, but MeterIndex overrides archive ! if
    * ArchiveToReadout and MeterIndex are empty, we assume MeterIndex = 1
    */
    private static final String ARCHIVE_TO_READOUT = "ArchiveToReadout";
    /*
    * external definition of archive structure
    */
    private static final String ARCHIVE_STRUCTURE = "ArchiveStructure";
    /*
    * address of interval object for archive
    */
    private static final String ARCHIVE_INTERVAL_ADDRESS = "ArchiveIntervalAddress";
    /*
    * try if end device is still "on line", and disable sending B0 at end
    */
    private static final String DISABLE_AUTO_LOGOFF = "DisableLogOff";
    /*
     * if DisableLogOff, this value is a delay after checking if device is still 'online'
     */
    private static final String DELAY_AFTER_CHECK = "DelayAfterCheck";

    private Lis200Profile profile;

    /**
     * The used ObjectFactory
     */
    private Lis200ObjectFactory objectFactory;

    /**
     * The index of the measurement. This is a one based value of the max. 4
     * indexes
     */
    private int meterIndex = 0;
    /**
     * max. meterIndex allowed
     */
    private int maxMeterIndex = 4;
    /**
     * The index of an archive to readout (one based)
     */
    private int archiveIndex = 0;
    /**
     * The instance of the archive to read out
     */
    private int archiveInstance = 0;
    /**
     * structure of the archive to readout
     */
    private String archiveStructure = "";
    /**
     * address to retrieve interval of archive
     */
    private String archiveIntervalAddr = "";

    /**
     * number of lock to use (0 - manufacturer, 1 - supplier, 2 - customer ...
     */
    private LockObject usedLock;
    /**
     * state of lock
     */
    private STATE lockState = LockObject.STATE.undefined;

    /**
     * The size of the profileRequestBlocks
     */
    private int profileRequestBlockSize;

    private boolean disableAutoLogoff = false;

    private int delayAfterCheck = 0;

    /**
     * interpreter for events
     */
    private EventInterpreter eventInterpreter = null;

    private String meterType;

    private int softwareVersion = 0;

    /**
     * Mapper for register obis codes to lis200 objects
     */
    protected RegisterReader obisCodeMapper = null;

    /**
     * Flag, if driver should send wakeup sequence
     */
    private boolean suppressWakeupSequence = false;

    public LIS200(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec(PROFILE_REQUEST_BLOCK_SIZE));
        propertySpecs.add(this.integerSpec(DISABLE_AUTO_LOGOFF));
        propertySpecs.add(this.integerSpec(SUPPRESS_WAKEUP_SEQUENCE));
        propertySpecs.add(this.stringSpec(USE_LOCK));
        propertySpecs.add(this.integerRangeSpec(METER_INDEX, false, Range.closed(1, this.maxMeterIndex)));
        propertySpecs.add(this.integerRangeSpec(ARCHIVE_TO_READOUT, false, Range.atLeast(1)));
        propertySpecs.add(this.integerRangeSpec(ARCHIVE_STRUCTURE, false, Range.atLeast(1)));
        propertySpecs.add(LIS200Utils.propertySpec(ARCHIVE_INTERVAL_ADDRESS, false));
        propertySpecs.add(this.integerSpec(DELAY_AFTER_CHECK));
        return propertySpecs;
    }

    private PropertySpec integerRangeSpec(String name, boolean required, Range<Integer> range) {
        PropertySpecBuilder<Integer> specBuilder = UPLPropertySpecFactory.specBuilder(name, required, this.getPropertySpecService()::integerSpec);
        UPLPropertySpecFactory.addIntegerValues(specBuilder, range);
        return specBuilder.finish();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        super.setUPLProperties(properties);
        try {
            securityLevel = Integer.parseInt(properties.getTypedProperty("SecurityLevel", "0").trim());  //Default
            this.profileRequestBlockSize = Integer.parseInt(properties.getTypedProperty(PROFILE_REQUEST_BLOCK_SIZE, "10"));
            this.disableAutoLogoff = Integer.parseInt(properties.getTypedProperty(DISABLE_AUTO_LOGOFF, "0")) > 0;
            suppressWakeupSequence = Integer.parseInt(properties.getTypedProperty(SUPPRESS_WAKEUP_SEQUENCE, "0")) != 0;

        /* check for lock to open... */
            usedLock = LockObject.CustomerLock;
            final String lockName = properties.getTypedProperty(USE_LOCK, "");
            if (!lockName.isEmpty()) {
                usedLock = null;
                for (LockObject lock : getLockObjects())
                {
                    if (lockName.equalsIgnoreCase(lock.getName()))
                    {
                        usedLock = lock;
                        break;
                    }
                }
                if (usedLock == null)
                {
                    StringBuilder msg = new StringBuilder("Incorrect UseLock property. Valid value are: ");
                    boolean notFirst = false;
                    for (LockObject lock : getLockObjects())
                    {
                        if (notFirst)
                        {
                            msg.append(",");
                        }
                        msg.append("'");
                        msg.append(lock.getName());
                        msg.append("'");
                        notFirst = true;
                    }
                    msg.append(". If UseLock is empty, then default 'CustomerLock' will be used.");

                    throw new InvalidPropertyException(msg.toString());
                }
            }

        /* check which archive to readout... */
            String strMeterIndex = properties.getTypedProperty(METER_INDEX, "");
            String strArchive = properties.getTypedProperty(ARCHIVE_TO_READOUT, "");

        /* property MeterIndex set ? */
            if ((strMeterIndex != null) && (!strMeterIndex.isEmpty())) {
                meterIndex = Integer.parseInt(strMeterIndex);
            }

        /* property ArchiveToReadout set? */
            if ((strArchive != null) && (!strArchive.isEmpty())) {
                archiveIndex = Integer.parseInt(strArchive);
            }

        /* check if archive structure is given by property */
            String struct = properties.getTypedProperty(ARCHIVE_STRUCTURE, "");
            if ((struct != null) && (!struct.isEmpty())) {
                archiveStructure = struct;
            }

            archiveIntervalAddr = properties.getTypedProperty(ARCHIVE_INTERVAL_ADDRESS, "");

            delayAfterCheck = Integer.parseInt(properties.getTypedProperty(DELAY_AFTER_CHECK, "0"));
        }
        catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public String getSerialNumber() {
        AbstractObject serialNumber = getObjectFactory()
                .getSerialNumberObject();
        try {
            return serialNumber.getValue();
        } catch (IOException e) {
            throw ElsterProtocolIOExceptionHandler.handle(e, getNrOfRetries() + 1);
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timeZone, Logger logger) throws IOException {

        setLogger(logger);

        int vma;
        int vmi;
        try {
            vma = getVersionMajor();
            vmi = getVersionMinor();
        } catch (NoClassDefFoundError de) {
            String msg = "Wrong version of ElsterUtils. Needed: V1.4  Found: ?";
            logger.severe("LIS200.init - " + msg);
            throw new IOException(msg);
        }

        final String euVersion = vma + "." + vmi;
        if ((vma * 100 + (vmi % 100)) < 104)
        {
            throw new IOException("Wrong version of ElsterUtils. Needed: V1.4  Found: " + euVersion);
        }

        logger.info("LIS200.init - ElsterUtils V" + euVersion);

        setTimeZone(timeZone);
        try {
            flagIEC1107Connection = new Lis200Connection(inputStream,
                    outputStream, iec1107TimeoutProperty,
                    protocolRetriesProperty, forcedDelay, echoCancelling,
                    iec1107Compatible, software7E1, suppressWakeupSequence, disableAutoLogoff, delayAfterCheck);
            flagIEC1107Connection
                    .setErrorSignature(AbstractCommand.ERROR_INDICATION);
        } catch (ConnectionException e) {
            logger.severe("LIS200.init - " + e.getMessage());
        }
    }

    @Override
    public String getProtocolVersion()
    {
        return "$Date: 2015-11-26 15:24:25 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    protected void doConnect() throws IOException {
        getLogger().info("--- entering doConnect....");

        // get state of lock
        usedLock.setLink(this);
        lockState = usedLock.getLockState();
        if (lockState != LockObject.STATE.open)
        {
            lockState = usedLock.openLock(this.strPassword);
            }
        getLogger().info("-- Lock " + usedLock.getName() + " " + lockState + "!");

        if ((lockState == STATE.closed) || (lockState == STATE.undefined))
        {
            throw new IOException("Wrong password to open lock " + usedLock.getName() + "!");
        }

        // verify device type
        AbstractObject meterTypeObj = getObjectFactory().getMeterTypeObject();
        /* 1/9/2010: there are devices with spaces in meter type - gh */
        meterType = meterTypeObj.getValue().trim();
        getLogger().info("-- Type of device: " + meterType);

        String sv = getObjectFactory().getSoftwareVersionObject().getValue();
        softwareVersion = (int) Math.round(Double.parseDouble(sv) * 100);
        getLogger().info("-- SW version of device: " + (softwareVersion / 100.0));

        /* get instance of archive to readout */
        if (archiveIndex > 0) {
            archiveInstance = archiveIndex;
        } else {
            if (meterIndex == 0) {
                meterIndex = 1;
            }
            archiveInstance = meterIndexToArchiveInstance(meterType);
        }

        /*
           * get type of archive. if unknown type, property archiveStructure has
           * to be defined
           */
        SimpleObject archiveTypeObject = new SimpleObject(this,
                archiveInstance, "A32.0");
        String archiveType = archiveTypeObject.getValue();
        int archiveTypeNo = Integer.parseInt(archiveType);
        getLogger().info(
                "-- Requested Archive instance: " + archiveInstance
                        + ". Archive type is " + archiveType);
        if (archiveStructure.isEmpty()) {
            archiveStructure = getStructureForType(archiveTypeNo);
        }

        if ((archiveIntervalAddr.isEmpty()) && (archiveTypeNo != 0)) {
            archiveIntervalAddr = getAddressOfInterval(archiveTypeNo);
        }

        getLogger().info(
                "-- Requested Archive structure: <" + archiveStructure + ">");
//        IntervalArchiveRecordConfig recordConfig = new IntervalArchiveRecordConfig(archiveStructure);

        if (archiveIntervalAddr.isEmpty()){
            throw new IOException(
                    "ArchiveIntervalAddress is empty. Correct archive to read out or set ArchiveIntervalAddress as property");
        }

    }

    @Override
    public String getFirmwareVersion()
            throws IOException
    {
        return "Manufacturer : " + getObjectFactory().getManufacturerObject().getValue() +
               " - DeviceType : " + getObjectFactory().getMeterTypeObject().getValue() +
               " - SoftwareVersion : " + getObjectFactory().getSoftwareVersionObject().getValue();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        getLogger().info(
                "--- requested getNumberOfChannels:"
                        + getProfileObject().getNumberOfChannels());
        return getProfileObject().getNumberOfChannels();
    }

    public int getSoftwareVersion() {
        return this.softwareVersion;
    }

    public void setSoftwareVersion(int swv) {
        this.softwareVersion = swv;
    }

    @Override
    public void disconnect() throws NestedIOException {
        try {
            // close opened lock...
            if (lockState == LockObject.STATE.opened)
            {
                usedLock.closeLock();
                getLogger().info("-- Lock " + usedLock.getName() + " closed!");
            }
            // disconnect...
            if (!disableAutoLogoff) {
                getFlagIEC1107Connection().disconnectMAC();
            }

        } catch (FlagIEC1107ConnectionException e) {
            getLogger().severe("disconnect() error, " + e.getMessage());
        } catch (IOException e) {
            getLogger().severe(
                    "disconnect() error - setLock IOException, " + e.getMessage());
        }
    }

    @Override
    public Date getTime() throws IOException {
        return getObjectFactory().getClockObject().getDateTime().getTime();
    }

    @Override
    public void setTime() throws IOException {
        getObjectFactory().getClockObject().writeClock();
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getProfileObject().getInterval();
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        // Default, we ask for 2 months of profile data!
        Calendar calendar = Calendar.getInstance(getTimeZone());
        /* maximum readout range set to 2 year - 6/18/2010 gh */
        calendar.add(Calendar.MONTH, -24);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        getLogger().info("getProfileData(" + from + "," + to + ","  + includeEvents + ")");

        ProfileData profileData = new ProfileData();

        profileData.setChannelInfos(getProfileObject().buildChannelInfos());

        profileData.setIntervalDatas(getProfileObject().getIntervalData(from, to));

        if (includeEvents) {

            /* 1/9/2010 - read out logbook only if we know we have one! */
            if (getLogBookInstance() > 0) {

                List<MeterEvent> mel = getProfileObject().getMeterEvents(from);

                profileData.setMeterEvents(mel);
            }

            /*
             * Not all statuses are always mapped, therefore we use the events
             * to create additional intervalStateBits (especially the clock statuses)
             */
            getProfileObject().applyEvents(profileData.getMeterEvents(), profileData.getIntervalDatas());
        }

        return profileData;
    }

    public int getMeterIndex() {
        return meterIndex;
    }

    protected String getMeterType() {
        return meterType;
    }

    protected Lis200ObjectFactory getObjectFactory() {
        if (this.objectFactory == null) {
            this.objectFactory = new Lis200ObjectFactory(this);
        }
        return this.objectFactory;
    }

    protected Lis200Profile getProfileObject() {
        if (this.profile == null) {
            IntervalObject archiveIntervalObj = new IntervalObject(this,
                    archiveIntervalAddr);
            this.profile = new Lis200Profile(this, getArchiveInstance(),
                    archiveStructure, archiveIntervalObj, getLogBookInstance(),
                    profileRequestBlockSize, getEventInterpreter());
        }
        return this.profile;
    }

    protected int getArchiveInstance() {
        return archiveInstance;
    }

    protected LockObject[] getLockObjects()
    {
        return new LockObject[]{LockObject.ManufacturerLock, LockObject.SupplierLock, LockObject.CustomerLock};
    }

    /**
     * Translation of meterIndex to archive instance
     *
     * @param mType - type of device (Meter TYPE)
     * @return archive instance (to readout)
     * @throws InvalidPropertyException
     *          - in case of a wrong meterIndex value
     */
    protected int meterIndexToArchiveInstance(String mType) throws InvalidPropertyException {

        // DL210 -> 1 --> Archive 2
        if (mType.startsWith("DL210")) {
            if (meterIndex != 1)
            {
                throw new InvalidPropertyException(
                        "Incorrect MeterIndex property. For DL210 only 1 is allowed.");
            }
            return 2;
        }
        // DL220 & DL220W -> 1 & 2 --> Archive 2 and 4
        if (mType.startsWith("DL220")) {
            if ((meterIndex < 1) || meterIndex > 2)
            {
                throw new InvalidPropertyException(
                        "Incorrect MeterIndex property. For DL220 only 1 and 2 are allowed.");
            }
            return meterIndex * 2;
        }
        if (mType.startsWith("DL230"))
        {
            if ((meterIndex < 1) || meterIndex > 4)
            {
                throw new InvalidPropertyException(
                        "Incorrect MeterIndex property. For DL230 only 1 to 4 are allowed.");
            }
            return meterIndex * 2;
        }
        // DL220 & DL220W -> 1 - 4 --> Archive 2, 4, 6, 8
        if (mType.startsWith("DL240")) {
            if ((meterIndex < 1) || meterIndex > 4)
            {
                throw new InvalidPropertyException(
                        "Incorrect MeterIndex property. For DL240 only 1 to 4 are allowed.");
            }
            return meterIndex * 2;
        }

        // EK2x0 -> 1 --> Archive 3
        if (mType.startsWith("EK2") || mType.startsWith("TVC")) {
            if (meterIndex != 1)
            {
                throw new InvalidPropertyException(
                        "Incorrect MeterIndex property. For EK2x0 only 1 is allowed.");
            }
            return 3;
        }

        throw new InvalidPropertyException(
                "Incorrect MeterIndex property. No translation defined for device type "
                        + mType);
    }

    protected String getStructureForType(int archiveTypeNo) {

        /*
           * Attention: archive type string has to end with a non-empty-entry!!!
           */

        switch (archiveTypeNo) {
            /* interval archives */
            case 1: /* Archive %s (Meas. period archive DL240 V1.x) */
            case 49: /* Archive %s (Meas. period archive DL240 V2.x) */
            case 81: /* Archive %s (Meas. period archive DL220/DL210) */
            case 145: /* Archive %s (Meas. period archive DL210-ENC) */
                return ",,TST,CHN00[C],CHN01[C],ST1,STSY,EVNT,CKSUM";

            case 4: /* Archive %s (Daily archive DL220/DL210) */
                return ",,TST,CHN00[C],CHN01[C],ST1,STSY,EVNT,CKSUM";

            case 17: /* Archive %s (meas. period archive EK260 V1.x) */
                return ",,TST,CHN00,CHN01[C],CHN02[C],CHN03,CHN04,CHN05,ST1,ST2,ST3,ST4,STSY,EVNT,CKSUM";

            case 20: /* Archive %s (Daily archive EK260 V2.x, EK230, EK240) */
                return ",,TST,CHN00[C],CHN01[C],CHN02[C],CHN03[C],CHN04,CHN05,CHN06,CHN07,ST1,ST2,ST3,ST4,STSY,EVNT,CKSUM";

            case 33: /* Archive %s (Meas. period archive EK260 V2.x, EK230, EK240) */
            case 65: /* Archive %s (Meas. period archive EK260 V2.x, EK230, EK220) */
                return ",,TST,CHN00[C],CHN01[C],CHN02[C],CHN03[C],CHN04,CHN05,CHN06,CHN07,ST1,ST2,ST3,ST4,STSY,EVNT,CKSUM";

            case 97: /* Archive %s (Daily archive EK230 net management) */
                return ",,TST,CHN00[C],CHN01[C],CHN02[C],CHN03[C],CHN04,CHN05,CHN06,CHN07,ST1,ST2,ST3,ST4,STSY,,EVNT,CKSUM";

            case 113: /* Archive %s (Monthly archive TC210) */
                return ",TST,CHN00[C],CHN01[C],CHN03[C],CHN02[C],CHN04,CHN05,CHN06,,,STSY,EVNT,CKSUM";

            case 129: /* Archive %s (Meas. period archive EK240) */
                return ",,TST,CHN00[C],CHN01[C],CHN02[C],CHN03[C],CHN04,CHN05,CHN06,CHN07,ST1,ST2,ST3,ST4,STSY,,EVNT,CKSUM";

            case 161: /* Archive %s (Meas. period archive EK260 V2.x, EK230, EK220) */
                return ",,TST,CHN00[C],CHN01[C],CHN02[C],CHN03[C],CHN04,CHN05,CHN06,CHN07,ST1,ST2,ST3,ST4,STSY,EVNT,CKSUM";

            // Monthly archives
            // 2,",,TST,CH00,CH01,MP-MAX,TST MP-MAX,STAT MP-MAX,TG-MAX,TST TG-MAX,STAT TG-MAX,,STSY,"
            // 18,",,TST,CH00,VN.MCH04,TST VN.MCH04,STAT VN.MCH04,VN.TG,TST VN.TG,STAT VN.TG,VB,VB.MCH04,TST VB.MCH04,STAT VB.MCH04,VB.TG,TST VB.TG,STAT VB.TG,,STSY,"
            // 34,",,TST,QN-MAX,TST QN-MAX,STAT QN-MAX,QN-MIN,TST QN-MIN,STAT QN-MIN,QB-MAX,TST QB-MAX,STAT QB-MAX,QB-MIN,TST QB-MIN,STAT-QB-MIN,P.M,P.MAX,TST P.MAX,STAT P.MAX,P.MIN,TST P.MIN,STAT P.MIN,T.M,T.MAX,TST T.MAX,STAT T.MAX,T.MIN,TST T.MIN,STAT T.MIN,K.M,Z.M,SP,ST,SCH06,SC,"
            // 50,",,TST,CH00,CH01,VN.MCH04,TST VN.MCH04,STAT VN.MCH04,VN.TG,TST VN.TG,STAT VN.TG,VB,CH03,VB.MCH04,TST VB.MCH04,STAT VB.MCH04,VB.TG,TST VB.TG,STAT VB.TG,,STSY,"
            // 82,",,TST,CH00,CH01,MP-MAX,TST MP-MAX,STAT MP-MAX,MP-MIN,TST MP-MIN,STAT MP-MIN,,STSY,"

            // Log books
            // 3,",,TST,EVNT,"
            // 35,",,TST,Addr,ValOld,ValNew,ST_AL,ST_ML,ST_DL,ST_CL,"
            // 51,",,TST,CH00,VB,CH04,CH05,CH06,CH07,QN,QB,"
            // 67,",,TST,Addr,ValOld,ValNew,ST_AL,ST_ML,ST_DL,ST_CL,"
        }
        return "";
    }

    protected String getAddressOfInterval(int archiveTypeNo) {
        switch (archiveTypeNo) {
            /* interval archives */
            case 1: /* Archive %s (Meas. period archive DL240 V1.x) */
            case 49: /* Archive %s (Meas. period archive DL240 V2.x) */
            case 81: /* Archive %s (Meas. period archive DL220/DL210) */
            case 145: /* Archive %s (Meas. period archive DL210-ENC) */
                /* DL2xx: no of input = (archiveInstance + 1) / 2 */
                /* instance is no of input + 4 */
                int instance = ((archiveInstance + 1) / 2) + 4;
                return instance + ":150.0";

            case 4: /* Archive %s (Daily archive DL220/DL210) */
                return archiveInstance + ":CF0.0";

            case 17: /* Archive %s (meas. period archive EK260 V1.x) */
            case 33: /* Archive %s (Meas. period archive EK260 V2.x, EK230, EK240) */
            case 129: /* Archive %s (Meas. period archive EK240) */
            case 97: /* Archive %s (Daily archive EK230 net management) */
            case 65: /* Archive %s (Meas. period archive EK260 V2.x, EK230, EK220) */
            case 161: /* Archive %s (Meas. period archive EK260 V2.x, EK230, EK220) */
            case 20: /* Archive %s (Daily archive EK260 V2.x, EK230, EK240) */
            case 113: /* Archive %s (Monthly archive TC210) */
                return "4:150.0";

            // Monthly archives
            // 2,",,TST,CH00,CH01,MP-MAX,TST MP-MAX,STAT MP-MAX,TG-MAX,TST TG-MAX,STAT TG-MAX,,STSY,"
            // 18,",,TST,CH00,VN.MCH04,TST VN.MCH04,STAT VN.MV,VN.TG,TST VN.TG,STAT VN.TG,VB,VB.MCH04,TST VB.MCH04,STAT VB.MCH04,VB.TG,TST VB.TG,STAT VB.TG,,STSY,"
            // 34,",,TST,QN-MAX,TST QN-MAX,STAT QN-MAX,QN-MIN,TST QN-MIN,STAT QN-MIN,QB-MAX,TST QB-MAX,STAT QB-MAX,QB-MIN,TST QB-MIN,STAT-QB-MIN,P.M,P.MAX,TST P.MAX,STAT P.MAX,P.MIN,TST P.MIN,STAT P.MIN,T.M,T.MAX,TST T.MAX,STAT T.MAX,T.MIN,TST T.MIN,STAT T.MIN,K.M,Z.M,SP,ST,SCH06,SC,"
            // 50,",,TST,CH00,CH01,VN.MCH04,TST VN.MCH04,STAT VN.MV,VN.TG,TST VN.TG,STAT VN.TG,VB,CH03,VB.MCH04,TST VB.MCH04,STAT VB.MCH04,VB.TG,TST VB.TG,STAT VB.TG,,STSY,"
            // 82,",,TST,CH00,CH01,MP-MAX,TST MP-MAX,STAT MP-MAX,MP-MIN,TST MP-MIN,STAT MP-MIN,,STSY,"

            // Log books
            // 3,",,TST,EVNT,"
            // 35,",,TST,Addr,ValOld,ValNew,ST_AL,ST_ML,ST_DL,ST_CL,"
            // 51,",,TST,CH00,VB,CH04,CH05,CH06,CH07,QN,QB,"
            // 67,",,TST,Addr,ValOld,ValNew,ST_AL,ST_ML,ST_DL,ST_CL,"
        }
        return "";
    }

    protected int getLogBookInstance() {
        if ("EK210".equalsIgnoreCase(meterType)) {
            return 4;
        } else if ("EK220".equalsIgnoreCase(meterType)
                || "TVC220".equalsIgnoreCase(meterType)) {
            return 4;
        } else if ("EK230".equalsIgnoreCase(meterType)
                || "TVC230".equalsIgnoreCase(meterType)) {
            return 4;
        } else if ("EK240".equalsIgnoreCase(meterType)) {
            return 4;
        } else if ("EK260".equalsIgnoreCase(meterType)) {
            return 4;
        } else if ("DL210".equalsIgnoreCase(meterType)) {
            return 10;
        } else if (meterType.toUpperCase().startsWith("DL220")) {
            return 10;
        } else if ("DL230".equalsIgnoreCase(meterType)) {
            return 10;
        } else if ("DL240".equalsIgnoreCase(meterType)) {
            return 10;
        }
        return 0;
    }

    public void setEventInterpreter(EventInterpreter eventInterpreter) {
        this.eventInterpreter = eventInterpreter;
    }

    public EventInterpreter getEventInterpreter() {
        if (eventInterpreter == null) {
            eventInterpreter = new EventInterpreter();
        }
        return eventInterpreter;
    }

    public void setMeterIndex(int index) {
        meterIndex = index;
    }

    public void setMaxMeterIndex(int maxIndex) {
        maxMeterIndex = maxIndex;
    }

    @SuppressWarnings({"unused"})
    public int getMaxMeterIndex() {
        return maxMeterIndex;
    }

    public Date getCurrentDate() {
        return new Date();
    }

    /**
     * Gets a description for the obis code
     *
     * @param obisCode - obis code to get the description for
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        String result = "";

        RegisterReader rr = getObisCodeMapper();
        if (rr != null) {
            Lis200RegisterN reg = rr.getRegister(obisCode);
            result = (reg == null) ? "" : reg.getName();
        }
        return new RegisterInfo(result);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        RegisterReader rr = getObisCodeMapper();
        if (rr != null) {
            return rr.getRegisterValue(obisCode, getCurrentDate());
        } else {
            return null;
        }
    }

    protected RegisterReader getObisCodeMapper() {
        if ((this.obisCodeMapper == null) &&
                (this instanceof IRegisterReadable)) {
            this.obisCodeMapper = new RegisterReader((IRegisterReadable) this, getRegisterMapN((IRegisterReadable) this));
        }
        return this.obisCodeMapper;
    }

    /**
     * Gets an empty register map
     *
     * @param irr - interface to retrieve data from historical archives
     * @return an empty RegisterMap
     */
    protected RegisterMapN getRegisterMapN(IRegisterReadable irr) {

        RegisterMapN registerMap = new RegisterMapN();

        RegisterDefinition[] regs = irr.getRegisterDefinition();

        SimpleObject lisObj;
        for (RegisterDefinition reg : regs) {
            lisObj = null;
            if (reg instanceof SimpleRegisterDefinition) {
                lisObj = new IntervalObject(this, reg.getInstance(), reg.getAddress());
            }
            if (reg instanceof StateRegisterDefinition) {
                lisObj = new StatusObject(this, reg.getInstance(), reg.getAddress());
            }
            if (reg instanceof ValueRegisterDefinition) {
                lisObj = new SimpleObject(this, reg.getInstance(), reg.getAddress());
            }
            if (reg instanceof MaxRegisterDefinition) {
                lisObj = new MaxDemandObject(this, reg.getInstance(), reg.getAddress());
            }
            if (reg instanceof HistoricRegisterDefinition) {
                RawArchiveLineInfo ralInfo = irr.getArchiveLineInfo(reg.getInstance(), reg.getAddress());
                if (ralInfo != null) {
                    lisObj = new HistoricalValueObject(this, reg.getInstance(), ralInfo);
                }
            }

            if (lisObj != null) {
                registerMap.add(new Lis200RegisterN(reg.getObiscode(), lisObj));
            }
        }

        return registerMap;
    }

}