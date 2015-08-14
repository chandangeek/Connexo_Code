package com.elster.protocolimpl.lis200;

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
import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author heuckeg
 *         <p/>
 *         common protocol driver for LIS200
 *         <p/>
 */
public class LIS200 extends AbstractIEC1107Protocol {

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

    public LIS200() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings(value = {"unchecked"})
    protected List doGetOptionalKeys() {
        List keys = new ArrayList();
        /* Define the Records in one requestBlock, default this is 10 */
        keys.add(PROFILE_REQUEST_BLOCK_SIZE);
        keys.add(USE_LOCK);
        keys.add(METER_INDEX);
        keys.add(ARCHIVE_TO_READOUT);
        keys.add(ARCHIVE_STRUCTURE);
        keys.add(ARCHIVE_INTERVAL_ADDRESS);
        keys.add(SUPPRESS_WAKEUP_SEQUENCE);
        keys.add(DISABLE_AUTO_LOGOFF);
        keys.add(DELAY_AFTER_CHECK);
        return keys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doValidateProperties(Properties properties)
            throws MissingPropertyException, InvalidPropertyException {

        try {
            securityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "0").trim());  //Default
        } catch (Exception e) {
            throw new InvalidPropertyException(
                    String.format("Incorrect %s property. If the value is not empty, then only numeric values greater 0 are allowed.", "SecurityLevel"));
        }

        try {
            this.profileRequestBlockSize = Integer.parseInt(properties.getProperty(
                    PROFILE_REQUEST_BLOCK_SIZE, "10"));
        } catch (Exception e) {
            throw new InvalidPropertyException(
                    String.format("Incorrect %s property. If the value is not empty, then only numeric values greater 0 are allowed.", PROFILE_REQUEST_BLOCK_SIZE));
        }

        try {
            this.disableAutoLogoff = Integer.parseInt(properties.getProperty(DISABLE_AUTO_LOGOFF, "0")) > 0;
        } catch (Exception e) {
            throw new InvalidPropertyException(
                    String.format("Incorrect %s property. If the value is not empty, then only 0 and 1 is allowed.", DISABLE_AUTO_LOGOFF));
        }

        try {
            suppressWakeupSequence = Integer.parseInt(properties.getProperty(SUPPRESS_WAKEUP_SEQUENCE, "0")) != 0;
        } catch (Exception ignored) {
        }

        /* check for lock to open... */
        usedLock = LockObject.CustomerLock;
        final String lockName = properties.getProperty(USE_LOCK, "");
        if (lockName.length() > 0)
        {
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
                boolean first = true;
                for (LockObject lock : getLockObjects())
                {
                    if (!first)
                    {
                        msg.append(",");
                    }
                    msg.append("'");
                    msg.append(lock.getName());
                    msg.append("'");
                    first = false;
                }
                msg.append(". If UseLock is empty, then default 'CustomerLock' will be used.");

                throw new InvalidPropertyException(msg.toString());
            }
        }

        /* check which archive to readout... */
        String strMeterIndex = properties.getProperty(METER_INDEX, "");
        String strArchive = properties.getProperty(ARCHIVE_TO_READOUT, "");

        /* property MeterIndex set ? */
        if ((strMeterIndex != null) && (strMeterIndex.length() > 0)) {
            meterIndex = Integer.parseInt(strMeterIndex);
            if (meterIndex > maxMeterIndex || meterIndex < 1) {
                throw new InvalidPropertyException(
                        "Incorrect MeterIndex property. If the value is not empty, then only values between 1 to "
                                + maxMeterIndex
                                + " are allowed. "
                                + "If MeterIndex is empty, then default 1 will be used.");
            }
        }

        /* property ArchiveToReadout set? */
        if ((strArchive != null) && (strArchive.length() > 0)) {
            archiveIndex = Integer.parseInt(strArchive);
            if (archiveIndex < 1) {
                throw new InvalidPropertyException(
                        "Incorrect ArchiveToReadout property. If the value is not empty, then only values greater 0 are allowed. ");
            }
        }

        /* check if archive structure is given by property */
        String struct = properties.getProperty("ArchiveStructure", "");
        if ((struct != null) && (struct.length() > 0))
        {
            archiveStructure = struct;
        }

        archiveIntervalAddr = properties.getProperty("ArchiveIntervalAddress",
                "");
        if (archiveIntervalAddr.length() > 0) {
            if (!LIS200Utils.isValidLis200Address(archiveIntervalAddr)) {
                throw new InvalidPropertyException(
                        "Incorrect ArchiveIntervalAddress property. Value is not a valid LIS200 address.");
            }
        }

        try {
            delayAfterCheck = Integer.parseInt(properties.getProperty(DELAY_AFTER_CHECK, "0"));
        }
        catch (NumberFormatException ignore) {

        }


    }

    /**
     * {@inheritDoc}
     */
    public void init(InputStream inputStream, OutputStream outputStream,
                     TimeZone timeZone, Logger logger) throws IOException {

        setLogger(logger);

        int vma;
        int vmi;
        try {
            vma = com.elster.utils.VersionInfo.getVersionMajor();
            vmi = com.elster.utils.VersionInfo.getVersionMinor();
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

    /**
     * {@inheritDoc}
     */
    public String getProtocolVersion()
    {
        return "$Date: 2013-01-08 11:00:00 +0200 (do, 1 Sep 2011) $";
    }

    /**
     * {@inheritDoc}
     */
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
        if (archiveStructure.length() == 0) {
            archiveStructure = getStructureForType(archiveTypeNo);
        }

        if ((archiveIntervalAddr.length() == 0) && (archiveTypeNo != 0)) {
            archiveIntervalAddr = getAddressOfInterval(archiveTypeNo);
        }

        getLogger().info(
                "-- Requested Archive structure: <" + archiveStructure + ">");
//        IntervalArchiveRecordConfig recordConfig = new IntervalArchiveRecordConfig(archiveStructure);

        if (archiveIntervalAddr.length() == 0)
        {
            throw new IOException(
                    "ArchiveIntervalAddress is empty. Correct archive to read out or set ArchiveIntervalAddress as property");
        }

    }

    /**
     * Validate the serialNumber of the device.
     *
     * @throws IOException if the serialNumber doesn't match the one from the Rtu
     */
    protected void validateSerialNumber() throws IOException {
        getLogger().info(
                "-- verifying serial number...");
        AbstractObject serialNumber = getObjectFactory()
                .getSerialNumberObject();
        String meterSerialNumber = serialNumber.getValue();
        if ((this.serialNumber != null)
                && (!this.serialNumber.equals(meterSerialNumber))) {
            throw new IOException("Wrong serialnumber, EIServer settings: "
                    + this.serialNumber + " - Meter settings: "
                    + meterSerialNumber);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getFirmwareVersion()
            throws IOException
    {
        return "Manufacturer : " + getObjectFactory().getManufacturerObject().getValue() +
               " - DeviceType : " + getObjectFactory().getMeterTypeObject().getValue() +
               " - SoftwareVersion : " + getObjectFactory().getSoftwareVersionObject().getValue();
    }

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
    public int getProfileInterval() throws IOException {
        return getProfileObject().getInterval();
    }

    /*
      * Default, we ask for 2 months of profile data!
      */

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar calendar = Calendar.getInstance(getTimeZone());
        /* maximum readout range set to 2 year - 6/18/2010 gh */
        calendar.add(Calendar.MONTH, -24);
        return getProfileData(calendar.getTime(), includeEvents);
    }

    /**
     * {@inheritDoc}
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents)
            throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    /**
     * {@inheritDoc}
     */
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException {

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
                * to create additional intervalStateBits (especially the clock
                * statuses)
                */
            getProfileObject().applyEvents(profileData.getMeterEvents(), profileData.getIntervalDatas());
        }

        return profileData;
    }

    /**
     * Getter for meterIndex
     *
     * @return meterIndex
     */
    public int getMeterIndex() {
        return meterIndex;
    }

    /**
     * Getter for meter type in derived classes
     *
     * @return read meter type
     */
    protected String getMeterType() {
        return meterType;
    }

    /**
     * Getter for the ObjectFactory
     *
     * @return the ObjectFactory
     */
    protected Lis200ObjectFactory getObjectFactory() {
        if (this.objectFactory == null) {
            this.objectFactory = new Lis200ObjectFactory(this);
        }
        return this.objectFactory;
    }

    /**
     * @return the {@link Lis200Profile}
     */
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
     * @throws com.energyict.protocol.InvalidPropertyException
     *          - in case of a wrong meterIndex value
     */
    protected int meterIndexToArchiveInstance(String mType)
            throws InvalidPropertyException {

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
        if (meterType.equalsIgnoreCase("EK210")) {
            return 4;
        } else if (meterType.equalsIgnoreCase("EK220")
                || meterType.equalsIgnoreCase("TVC220")) {
            return 4;
        } else if (meterType.equalsIgnoreCase("EK230")
                || meterType.equalsIgnoreCase("TVC230")) {
            return 4;
        } else if (meterType.equalsIgnoreCase("EK240")) {
            return 4;
        } else if (meterType.equalsIgnoreCase("EK260")) {
            return 4;
        } else if (meterType.equalsIgnoreCase("DL210")) {
            return 10;
        } else if (meterType.toUpperCase().startsWith("DL220")) {
            return 10;
        } else if (meterType.equalsIgnoreCase("DL230")) {
            return 10;
        } else if (meterType.equalsIgnoreCase("DL240")) {
            return 10;
        }
        return 0;
    }

    /**
     * Setter for eventInterpreter
     *
     * @param eventInterpreter - class what interprets the events
     */
    public void setEventInterpreter(EventInterpreter eventInterpreter) {
        this.eventInterpreter = eventInterpreter;
    }

    /**
     * Getter for eventInterpreter. If no value was set by derived class, use
     * default class.
     *
     * @return EventInterpreter
     */
    public EventInterpreter getEventInterpreter() {
        if (eventInterpreter == null) {
            eventInterpreter = new EventInterpreter();
        }
        return eventInterpreter;
    }

    public void setMeterIndex(int index) {
        meterIndex = index;
    }

    /**
     * Setter for maximum allowed meterIndex (used by derived classes)
     *
     * @param maxIndex - max. value for MeterIndex
     */
    public void setMaxMeterIndex(int maxIndex) {
        maxMeterIndex = maxIndex;
    }

    /**
     * Getter for maximum allowed meterIndex
     *
     * @return maxMeterIndex
     */
    @SuppressWarnings({"unused"})
    public int getMaxMeterIndex() {
        return maxMeterIndex;
    }

    // *******************************************************************************************
    // * R e g i s t e r P r o t o c o l i n t e r f a c e
    // *******************************************************************************************/

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

    /**
     * interface function to read a register value
     *
     * @param obisCode - code for the register to read
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        RegisterReader rr = getObisCodeMapper();
        if (rr != null) {
            return rr.getRegisterValue(obisCode, getCurrentDate());
        } else {
            return null;
        }
    }

    /**
     * Getter for the ObisCodeMapper. getRegisterMap() has to be
     * overridden by the derived class.
     *
     * @return the used ObisCodeMapper}
     */
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
