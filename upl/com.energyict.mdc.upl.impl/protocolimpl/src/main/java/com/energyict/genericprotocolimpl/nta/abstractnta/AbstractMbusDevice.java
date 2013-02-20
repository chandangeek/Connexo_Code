package com.energyict.genericprotocolimpl.nta.abstractnta;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.common.pooling.AbstractGenericMbusPoolingProtocol;
import com.energyict.genericprotocolimpl.common.pooling.CommunicationSchedulerFullProtocolShadow;
import com.energyict.genericprotocolimpl.common.pooling.RtuRegisterFullProtocolShadow;
import com.energyict.genericprotocolimpl.nta.messagehandling.MbusMessageExecutor;
import com.energyict.genericprotocolimpl.nta.profiles.MbusDailyMonthlyProfile;
import com.energyict.genericprotocolimpl.nta.profiles.MbusEventProfile;
import com.energyict.genericprotocolimpl.nta.profiles.MbusProfile;
import com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers.MbusObisCodeMapper;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.OldDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gna
 *         Changes:
 *         GNA |27012009| Instead of using the nodeAddress as channelnumber we search for the channelnumber by looking at the mbusSerialNumbers
 *         GNA |28012009| Added the connect/disconnect messages. There is an option to enter an activationDate but there is no Object description for the
 *         Mbus disconnect controller yet ...
 *         GNA |04022009| Mbus connect/disconnect can be applied with a scheduler. We use 0.x.24.6.0.255 as the ControlScheduler and 0.x.24.7.0.255 as ScriptTable
 *         GNA |19022009| Added a message to change to connectMode of the disconnectorObject;
 *         Changed all messageEntrys in date-form to a UnixTime entry;
 */

public abstract class AbstractMbusDevice extends AbstractGenericMbusPoolingProtocol {

    /**
     * Extra protocol settings for a <b>subclassed NTA protocol</b>
     * Can be used to override a default custom property or add specific custom properties.
     *
     * @param properties
     */
    protected abstract void doValidateProperties(Properties properties) throws InvalidPropertyException;

    /**
     * Add extra optional keys
     *
     * @return a List<String> with optional key parameters, return null if no additionals are required
     */
    protected abstract List<String> doGetOptionalKeys();

    /**
     * Add extra required keys
     *
     * @return a List<String> with required key parameters, return null if no additionals are required
     */
    protected abstract List<String> doGetRequiredKeys();


    /**
     * Getter for the {@link MbusObisCodeProvider}
     *
     * @return the {@link MbusObisCodeProvider}
     */
    public abstract MbusObisCodeProvider getObiscodeProvider() throws IOException;

    private long mbusAddress = -1;        // this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
    private int physicalAddress = -1;        // this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
    private int medium = 15;                // value of an unknown medium
    private String customerID;
    private boolean valid;

    public Device mbus;
    private AbstractNTAProtocol webRtu;
    private Unit mbusUnit;
    private MbusObisCodeMapper mocm = null;
    private Properties properties;
    private CommunicationSchedulerFullProtocolShadow mbusFullShadow;


    public AbstractMbusDevice() {
        this.valid = false;
    }

    public AbstractMbusDevice(String serial, Device mbusRtu, Logger logger) {
        this(0, 0, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
    }

    public AbstractMbusDevice(String serial, int physicalAddress, Device mbusRtu, Logger logger) {
        this(0, physicalAddress, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
    }

    public AbstractMbusDevice(long mbusAddress, int phyaddress, String serial, int medium, Device mbusRtu, Unit mbusUnit, Logger logger) {
        super.setLogger(logger);
        this.mbusAddress = mbusAddress;
        this.physicalAddress = phyaddress;
        this.medium = medium;
        this.customerID = serial;
        this.mbusUnit = mbusUnit;
        this.mbus = mbusRtu;
        this.valid = true;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getWebRTU().getCosemObjectFactory();
    }

    protected DLMSMeterConfig getMeterConfig() {
        return getWebRTU().getMeterConfig();
    }

    public boolean isValid() {
        return valid;
    }

    public String getCustomerID() {
        return this.customerID;
    }

    public String getVersion() {
        return "$Date$";
    }

    protected MeterReadingData doReadRegisters(final List<RtuRegisterFullProtocolShadow> rtuRegisterFullProtocolShadowList) throws IOException {
        MeterReadingData mrd = new MeterReadingData();
        ObisCode oc = null;
        RegisterValue rv = null;
        for (RtuRegisterFullProtocolShadow register : rtuRegisterFullProtocolShadowList) {
            oc = register.getRegisterObisCode();
            try {
                rv = readRegister(adjustToMbusChannelObisCode(oc));

                rv.setRtuRegisterId(register.getRtuRegisterId());

                mrd.add(rv);
            } catch (NoSuchRegisterException e) {
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "ObisCode " + oc + " is not supported by the meter.");
            } catch (IOException e) {
                log(Level.FINEST, e.getMessage());
                getLogger().log(Level.INFO, "Reading register with obisCode " + oc + " FAILED.");
//				throw new IOException(e.getMessage());
            }    //TODO if we get a connectionException then we should stop and fail!

        }
        return mrd;
    }

    protected ObisCode adjustToMbusChannelObisCode(ObisCode oc) {
        return new ObisCode(oc.getA(), getPhysicalAddress() + 1, oc.getC(), oc.getD(), oc.getE(), oc.getF());
    }

    private RegisterValue readRegister(ObisCode oc) throws IOException {
        if (this.mocm == null) {
            this.mocm = new MbusObisCodeMapper(getCosemObjectFactory());
        }
        return mocm.getRegisterValue(oc);
    }

    @Override
    public Device getMbusRtu() {
        return this.mbus;
    }

    public int getPhysicalAddress() {
        return this.physicalAddress;
    }
//
//    @Override
//    public void addProperties(TypedProperties properties) {
//        addProperties(properties.toStringProperties());
//    }
//
//    @Override
//    public List<PropertySpec> getRequiredProperties() {
//        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
//    }
//
//    @Override
//    public List<PropertySpec> getOptionalProperties() {
//        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
//    }

    public void addProperties(Properties properties) {
        this.properties = properties;
    }

    public List<String> getOptionalKeys() {
        List<String> result = new ArrayList(30);
        List<String> protocolKeys = doGetOptionalKeys();
        if (protocolKeys != null) {
            result.addAll(protocolKeys);
        }
        return result;
    }

    public List<String> getRequiredKeys() {
        List<String> result = new ArrayList(30);
        List<String> protocolKeys = doGetRequiredKeys();
        if (protocolKeys != null) {
            result.addAll(protocolKeys);
        }
        return result;
    }

    public void setWebRtu(AbstractNTAProtocol webRTU) {
        this.webRtu = webRTU;
    }

    public AbstractNTAProtocol getWebRTU() {
        return this.webRtu;
    }

    /**
     * {@inheritDoc}
     */
    public void validateProperties() throws MissingPropertyException, InvalidPropertyException {
        Iterator<String> iterator = getRequiredKeys().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (getProperties().getProperty(key) == null) {
                throw new MissingPropertyException(key + " key missing");
            }
        }
        doValidateProperties(getProperties());
    }

    /**
     * Getter for the properties. (fetch them ones from the device)
     *
     * @return the properties
     */
    private Properties getProperties() {
        if (this.properties == null) {
            this.properties = getFullShadow().getRtuShadow().getRtuProperties();
        }
        return this.properties;
    }

    public long getTimeDifference() {
        return 0;
    }

    /**
     * Check if a wakeUp needs to be called to the meter, and execute what is necessary
     */
    @Override
    protected void executeWakeUpSequence() throws BusinessException, IOException, SQLException {
        // nothing to do
    }

    /**
     * Initialize some global variables.<br>
     * We assume that no time-consuming actions will take place. If this would not apply to your implementation, then please call the {@link #releaseConnectionFromPool()}
     * so proper connectionPooling can take place.
     */
    @Override
    protected void init() throws IOException, DLMSConnectionException {
        // nothing to do
    }

    /**
     * Fetch and construct the default MbusProfile
     *
     * @return the mbusProfile
     */
    @Override
    protected ProfileData getMbusProfile() throws IOException {
        getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getFullShadow().getRtuShadow().getSerialNumber());
        MbusProfile mp = new MbusProfile(this);
        return mp.getProfile(getWebRTU().getMeterConfig().getMbusProfile(getPhysicalAddress()).getObisCode());
    }

    /**
     * Fetch and construct the eventProfile
     *
     * @return the eventProfile
     */
    @Override
    protected ProfileData getEventProfile() throws IOException {
        getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + getFullShadow().getRtuShadow().getSerialNumber());
        MbusEventProfile mep = new MbusEventProfile(this);
        return mep.getEvents();
    }

    /**
     * Fetch and construct the dailyProfile
     */
    @Override
    protected ProfileData readDailyProfiles() throws IOException {

        if (getWebRTU().isReadDaily()) {
            MbusDailyMonthlyProfile mdm = new MbusDailyMonthlyProfile(this);
            getLogger().log(Level.INFO, "Getting Daily values for meter with serialnumber: " + getFullShadow().getRtuShadow().getSerialNumber());
            return mdm.getDailyProfile(getMeterConfig().getDailyProfileObject().getObisCode());
        }
        return null;
    }

    /**
     * Fetch and construct the monthlyProfile
     */
    @Override
    protected ProfileData readMonthlyProfiles() throws IOException {
        if (getWebRTU().isReadMonthly()) {
            MbusDailyMonthlyProfile mdm = new MbusDailyMonthlyProfile(this);
            getLogger().log(Level.INFO, "Getting Monthly values for meter with serialnumber: " + getFullShadow().getRtuShadow().getSerialNumber());
            return mdm.getMonthlyProfile(getMeterConfig().getMonthlyProfileObject().getObisCode());
        }
        return null;
    }

    /**
     * Send the given RtuMessages
     *
     * @param rtuMessageList
     */
    @Override
    protected void sendMeterMessages(final List<OldDeviceMessage> rtuMessageList) throws BusinessException, SQLException {
        MbusMessageExecutor messageExecutor = new MbusMessageExecutor(this);

        Iterator<OldDeviceMessage> it = rtuMessageList.iterator();
        OldDeviceMessage rm = null;
        while (it.hasNext()) {
            rm = it.next();
            messageExecutor.doMessage(rm);
        }
    }

    @Override
    protected void setMbusFullShadow(final CommunicationSchedulerFullProtocolShadow mbusFullShadow) {
        this.mbusFullShadow = mbusFullShadow;
    }

    @Override
    public CommunicationSchedulerFullProtocolShadow getFullShadow() {
        return mbusFullShadow;
    }

    // Commented this method, it's possible we need it again in the future
//    private void verifySerialNumber() throws IOException {
//        String serial;
//        String eiSerial = getFullShadow().getRtuShadow().getSerialNumber();
//        try {
//            serial = getCosemObjectFactory().getGenericRead(getMeterConfig().getMbusSerialNumber(physicalAddress)).getString();
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new IOException("Could not retrieve the serialnumber of meter " + eiSerial + e);
//        }
//        if (!eiSerial.equals(serial)) {
//            throw new IOException("Wrong serialnumber, EIServer settings: " + eiSerial + " - Meter settings: " + serial);
//        }
//    }
}
