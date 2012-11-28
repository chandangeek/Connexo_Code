package com.energyict.genericprotocolimpl.webrtuz3;

import com.energyict.cbo.*;
import com.energyict.cpo.*;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.common.DLMSProtocol;
import com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers.MbusObisCodeMapper;
import com.energyict.genericprotocolimpl.webrtuz3.historical.HistoricalRegisterReadings;
import com.energyict.genericprotocolimpl.webrtuz3.messagehandling.MbusMessageExecutor;
import com.energyict.genericprotocolimpl.webrtuz3.messagehandling.MbusMessages;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.*;
import com.energyict.mdw.amr.*;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
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

public class MbusDevice extends MbusMessages implements GenericProtocol {

    /**
     * Property names
     */
    private static final String PROPERTY_READ_REGULAR_DEMAND_VALUES = "ReadRegularDemandValues";
    private static final String PROPERTY_READ_DAILY_VALUES = "ReadDailyValues";
    private static final String PROPERTY_READ_MONTHLY_VALUES = "ReadMonthlyValues";
    private static final String PROPERTY_RUNTESTMETHOD = "RunTestMethod";

    /**
     * Property default values
     */
    private static final String DEFAULT_READ_REGULAR_DEMAND_VALUES = "1";
    private static final String DEFAULT_READ_DAILY_VALUES = "1";
    private static final String DEFAULT_READ_MONTHLY_VALUES = "1";
    private static final String DEFAULT_RUNTESTMETHOD = "0";

    private static final ObisCode PROFILE_OBISCODE = ObisCode.fromString("0.0.24.3.0.255");
    public static final ObisCode DAILY_PROFILE_OBIS = ObisCode.fromString("0.0.24.3.1.255");
    public static final ObisCode MONTHLY_PROFILE_OBIS = ObisCode.fromString("0.0.24.3.2.255");

    private long mbusAddress = -1;        // this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
    private int physicalAddress = -1;        // this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
    private int medium = 15;                // value of an unknown medium
    private String customerID;
    private boolean valid;

    public Device mbus;
    public CommunicationProfile commProfile;
    private WebRTUZ3 webRtu;
    private Logger logger;
    private Unit mbusUnit;
    private MbusObisCodeMapper mocm = null;

    private HistoricalRegisterReadings historicalRegisters;
    private MeterAmrLogging meterAmrLogging;
    private Properties properties = new Properties();

    private boolean readRegular;
    private boolean runTestMethod;
    private boolean readDaily;
    private boolean readMonthly;

    public MbusDevice() {
        this.valid = false;
    }

    public MbusDevice(String serial, Device mbusRtu, Logger logger) {
        this(0, 0, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
    }

    public MbusDevice(String serial, int physicalAddress, Device mbusRtu, Logger logger) {
        this(0, physicalAddress, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
    }

    public MbusDevice(long mbusAddress, int phyaddress, String serial, int medium, Device mbusRtu, Unit mbusUnit, Logger logger) {
        this.mbusAddress = mbusAddress;
        this.physicalAddress = phyaddress;
        this.medium = medium;
        this.customerID = serial;
        this.mbusUnit = mbusUnit;
        this.mbus = mbusRtu;
        this.logger = logger;
        this.valid = true;
    }

    private void verifySerialNumber() throws IOException {
        String serial;
        String eiSerial = getMbus().getSerialNumber();
        try {
            Data serialDataObject = getCosemObjectFactory().getData(ProtocolTools.setObisCodeField(WebRTUZ3.SERIALNR_OBISCODE, 1, (byte) physicalAddress));
            OctetString serialOctetString = serialDataObject.getAttrbAbstractDataType(2).getOctetString();
            serial = serialOctetString != null ? serialOctetString.stringValue() : null;
        } catch (IOException e) {
            throw new IOException("Could not retrieve the serialnumber of meter " + eiSerial + ": " + e);
        }
        if (!eiSerial.equals(serial)) {
            throw new IOException("Wrong serialnumber, EIServer settings: " + eiSerial + " - Meter settings: " + serial);
        }
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getWebRTU().getCosemObjectFactory();
    }

    private DLMSMeterConfig getMeterConfig() {
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

    public void validateProperties() throws MissingPropertyException, InvalidPropertyException {
        this.readRegular = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_READ_REGULAR_DEMAND_VALUES, DEFAULT_READ_REGULAR_DEMAND_VALUES) == 1) ? true : false;
        this.readDaily = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_READ_DAILY_VALUES, DEFAULT_READ_DAILY_VALUES) == 1) ? true : false;
        this.readMonthly = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_READ_MONTHLY_VALUES, DEFAULT_READ_MONTHLY_VALUES) == 1) ? true : false;
        this.runTestMethod = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_RUNTESTMETHOD, DEFAULT_RUNTESTMETHOD) == 1) ? true : false;
    }

    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        this.commProfile = scheduler.getCommunicationProfile();
        validateProperties();

        testMethod();

        // Before reading data, check the serialnumber
        verifySerialNumber();

        // import profile
        if (commProfile.getReadDemandValues() && isReadRegular()) {
            getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getMbus().getSerialNumber());
            MbusProfile mp = new MbusProfile(this);
            ProfileData pd = mp.getProfile(getProfileObisCode());
            if (this.webRtu.isBadTime()) {
                pd.markIntervalsAsBadTime();
            }
            this.webRtu.getStoreObject().add(pd, getMbus());
        }

        if (commProfile.getReadMeterEvents()) {
            getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + getMbus().getSerialNumber());
            MbusEventProfile mep = new MbusEventProfile(this);
            ProfileData eventPd = mep.getEvents();
            this.webRtu.getStoreObject().add(eventPd, getMbus());
        }

        // import daily/monthly
        if (commProfile.getReadDemandValues()) {
            MbusDailyMonthly mdm = new MbusDailyMonthly(this);

            if (isReadDaily()) {
                getLogger().log(Level.INFO, "Getting Daily values for meter with serialnumber: " + getMbus().getSerialNumber());
                ProfileData dailyPd = mdm.getDailyProfile(getCorrectedObisCode(DAILY_PROFILE_OBIS));
                this.webRtu.getStoreObject().add(dailyPd, getMbus());
            }

            if (isReadMonthly()) {
                getLogger().log(Level.INFO, "Getting Monthly values for meter with serialnumber: " + getMbus().getSerialNumber());
                ProfileData montProfileData = mdm.getMonthlyProfile(getCorrectedObisCode(MONTHLY_PROFILE_OBIS));
                this.webRtu.getStoreObject().add(montProfileData, getMbus());

            }
        }

        // Read register readings
        if (commProfile.getReadMeterReadings()) {
            getLogger().log(Level.INFO, "Getting registers from Mbus meter " + (getPhysicalAddress()));
            doReadRegisters();
        }

        //send rtuMessages
        if (commProfile.getSendRtuMessage()) {
            sendMeterMessages();
        }
    }

    private void testMethod() {
        if (isRunTestMethod()) {
            StringBuffer sb = new StringBuffer();
            sb.append("\r\n\r\n").append("MbusDevice objects = ").append("\r\n");
            try {
                UniversalObject[] objects = getMeterConfig().getInstantiatedObjectList();
                for (int i = 0; i < objects.length; i++) {
                    UniversalObject object = objects[i];
                    if (object.getObisCode().getB() == physicalAddress) {
                        sb.append(object.getDescription()).append("\r\n");
                    }
                }
            } catch (Exception e) {
                sb.append("\r\n");
                sb.append("An error occured while reading the objectList: ").append(e.getMessage());
            }
            getLogger().warning(sb.toString());
        }
    }

    /**
     * We don't use the {@link DLMSProtocol#doReadRegisters()} method because we need to adjust the mbusChannel
     */
    private void doReadRegisters() {
        Iterator registerIterator = getMbus().getRegisters().iterator();
        List rtuRegisterGroups = this.commProfile.getRtuRegisterGroups();

        while (registerIterator.hasNext()) {
            ObisCode obisCode = null;
            try {
                com.energyict.mdw.amr.Register rtuRegister = (Register) registerIterator.next();
                if (CommonUtils.isInRegisterGroup(rtuRegisterGroups, rtuRegister)) {
                    obisCode = rtuRegister.getRtuRegisterSpec().getObisCode();
                    try {
                        RegisterValue registerValue = readRegister(obisCode);
                        if (registerValue != null) {
                            registerValue.setRtuRegisterId(rtuRegister.getId());
                            if (rtuRegister.getReadingAt(registerValue.getReadTime()) == null) {
                                getWebRTU().getStoreObject().add(rtuRegister, registerValue);
                            }
                        } else {
                            throw new NoSuchRegisterException("Register returned null");
                        }
                    } catch (NoSuchRegisterException e) {
                        getMeterAmrLogging().logRegisterFailure(e, obisCode);
                        getLogger().log(Level.INFO, "ObisCode " + obisCode + " is not supported by the meter. [" + e.getMessage() + "]");
                    }
                }
            } catch (IOException e) {
                getMeterAmrLogging().logRegisterFailure(e, obisCode);
                getLogger().log(Level.INFO, "Reading register with obisCode " + obisCode + " FAILED. [" + e.getMessage() + "]");
            }
        }
    }

    private void sendMeterMessages() throws BusinessException, SQLException {
        MbusMessageExecutor messageExecutor = new MbusMessageExecutor(this);
        List<DeviceMessage> pendingMessages = getMbus().getPendingMessages();
        for (int i = 0; i < pendingMessages.size(); i++) {
            DeviceMessage rtuMessage = pendingMessages.get(i);
            messageExecutor.doMessage(rtuMessage);
        }
    }

    private RegisterValue readRegister(ObisCode obisCode) throws IOException {
        RegisterValue registerValue = null;

        try {
            ObisCode physicalObisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) getPhysicalAddress());
            if ((physicalObisCode.getF() >= 0) && (physicalObisCode.getF() <= 11)) { // Monthly billing value
                registerValue = getHistoricalRegisters().readHistoricalMonthlyRegister(physicalObisCode);
            } else if ((physicalObisCode.getF() >= 12) && (physicalObisCode.getF() <= 90)) { // Daily billing value
                physicalObisCode = ProtocolTools.setObisCodeField(physicalObisCode, 5, (byte) (physicalObisCode.getF() - 12));
                registerValue = getHistoricalRegisters().readHistoricalDailyRegister(physicalObisCode);
            } else { //Another register
                if (this.mocm == null) {
                    this.mocm = new MbusObisCodeMapper(getCosemObjectFactory());
                }
                registerValue = mocm.getRegisterValue(physicalObisCode);
            }
        } catch (Exception e) {
            throw new NoSuchRegisterException(e.getMessage());
        }

        if (registerValue == null) {
            throw new NoSuchRegisterException("Register " + obisCode.toString() + " returned 'null'!");
        }

        return ProtocolTools.setRegisterValueObisCode(registerValue, obisCode);
    }

    public Device getMbus() {
        return this.mbus;
    }

    public int getPhysicalAddress() {
        return this.physicalAddress;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        addProperties(properties.toStringProperties());
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    public void addProperties(Properties properties) {
        if (properties != null) {
            getProperties().putAll(properties);
        }
    }

    public List<String> getOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        optionalKeys.add(PROPERTY_READ_REGULAR_DEMAND_VALUES);
        optionalKeys.add(PROPERTY_READ_DAILY_VALUES);
        optionalKeys.add(PROPERTY_READ_MONTHLY_VALUES);
        optionalKeys.add(PROPERTY_RUNTESTMETHOD);
        return optionalKeys;
    }

    public List<String> getRequiredKeys() {
        List<String> requiredKeys = new ArrayList<String>(0);
        return requiredKeys;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setWebRtu(WebRTUZ3 webRTUKP) {
        this.webRtu = webRTUKP;
    }

    public WebRTUZ3 getWebRTU() {
        return this.webRtu;
    }

    public long getTimeDifference() {
        return 0;
    }

    public ObisCode getProfileObisCode() {
        return ProtocolTools.setObisCodeField(PROFILE_OBISCODE, 1, (byte) physicalAddress);
    }

    @Override
    public String toString() {
        return "[" + physicalAddress + "] " + customerID;
    }

    /**
     * @param baseObisCode
     * @return
     */
    public ObisCode getCorrectedObisCode(ObisCode baseObisCode) {
        return ProtocolTools.setObisCodeField(baseObisCode, 1, (byte) physicalAddress);
    }

    public HistoricalRegisterReadings getHistoricalRegisters() {
        if (historicalRegisters == null) {
            historicalRegisters = new HistoricalRegisterReadings(getCosemObjectFactory(), getCorrectedObisCode(DAILY_PROFILE_OBIS), getCorrectedObisCode(MONTHLY_PROFILE_OBIS), getLogger());
        }
        return historicalRegisters;
    }

    /**
     * @return
     */
    public MeterAmrLogging getMeterAmrLogging() {
        if (meterAmrLogging == null) {
            meterAmrLogging = new MeterAmrLogging();
        }
        return meterAmrLogging;
    }

    public Properties getProperties() {
        return properties;
    }

    public boolean isRunTestMethod() {
        return runTestMethod;
    }

    public boolean isReadRegular() {
        return readRegular;
    }

    public boolean isReadDaily() {
        return readDaily;
    }

    public boolean isReadMonthly() {
        return readMonthly;
    }
}
