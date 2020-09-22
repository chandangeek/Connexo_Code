package com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.attributes.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.dlms.idis.registers.AlarmBitsRegister;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast.MulticastUpgradeState;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.RegisterIdentifierById;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/06/2015 - 16:22
 */
public class Beacon3100RegisterFactory {

    private final DlmsSession dlmsSession;
    private Beacon3100G3RegisterMapper beacon3100G3RegisterMapper;
    private static final ObisCode MULTICAST_FIRMWARE_UPGRADE_OBISCODE = ObisCode.fromString("0.0.44.0.128.255");
    private static final ObisCode MULTICAST_METER_PROGRESS = ProtocolTools.setObisCodeField(MULTICAST_FIRMWARE_UPGRADE_OBISCODE, 1, (byte) (-1 * ImageTransfer.ATTRIBUTE_UPGRADE_PROGRESS));

    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public static final String ALARM_BITS_REGISTER = "0.0.97.98.0.255";
    public static final String ALARM_FILTER = "0.0.97.98.10.255";
    public static final String ALARM_DESCRIPTOR = "0.0.97.98.20.255";


    // list of all registers which ComServer asked us to read
    List<OfflineRegister> allRegisters;

    //Map of attributes (value, unit, captureTime) per register
    Map<ObisCode, ComposedRegister> composedRegisterMap = new HashMap<>();

    //List of all attributes that need to be read out
    List<DLMSAttribute> dlmsAttributes = new ArrayList<>();

    // the list of all collected registers
    List<CollectedRegister> collectedRegisters = new ArrayList<>();

    public Beacon3100RegisterFactory(DlmsSession dlmsSession, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.dlmsSession = dlmsSession;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> allRegisters) {

        // save what we have to read, for easier access
        setRegistersToRead(allRegisters);

        // parse the requests and build the composed objects and list of attributes to read
        prepareReading();

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(this.getDlmsSession(), this.getDlmsSession().getProperties().isBulkRequest(), getDLMSAttributes());

        // do the actual reading and parsing of
        for (OfflineRegister offlineRegister : allRegisters) {
            if (offlineRegister.getObisCode().equals(MULTICAST_METER_PROGRESS)) {
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, "Register with obiscode " + offlineRegister.getObisCode() + " cannot be read out, use the 'read DC multicast progress' message on the Beacon protocol for this."));
                continue;
            }

            ComposedRegister composedRegister = getComposedRegisterMap().get(offlineRegister.getObisCode());

            if (composedRegister == null) {
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                continue;
            }

            G3Mapping g3Mapping = getBeacon3100G3RegisterMapper().getG3Mapping(offlineRegister.getObisCode());
            final ObisCode baseObisCode = g3Mapping == null ? offlineRegister.getObisCode() : g3Mapping.getBaseObisCode();
            final UniversalObject universalObject;
            try {
                universalObject = dlmsSession.getMeterConfig().findObject(baseObisCode);
            } catch (NotInObjectListException e) {
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                continue;   //Move on to the next register, this one is not supported by the DC
            }

            try {
                RegisterValue registerValue = null;

                if (g3Mapping != null) {
                    if (composedRegister.getRegisterValueAttribute() != null) {
                        registerValue = g3Mapping.parse(composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute()));
                    } else {
                        registerValue = g3Mapping.readRegister(getDlmsSession().getCosemObjectFactory());
                    }
                } else {
                    registerValue = parseRegisterReading(universalObject, composedCosemObject, offlineRegister, composedRegister, baseObisCode);
                }

                if (registerValue != null) {
                    addResult(createCollectedRegister(registerValue, offlineRegister));
                }
            } catch (IOException e) {
                getLogger().warning("Error while reading " + offlineRegister + ": " + e.getMessage());
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSession().getProperties().getRetries() + 1)) {
                    if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                        addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                    } else {
                        addResult(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage()));
                    }
                } else {
                    throw ConnectionCommunicationException.numberOfRetriesReached(e, getDlmsSession().getProperties().getRetries() + 1);
                }
            } catch (Exception ex) {
                getLogger().warning("Error while reading " + offlineRegister + ": " + ex.getMessage());
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.Other, ex.getMessage()));
            }
        }
        return getCollectedRegisters();
    }

    private RegisterValue parseRegisterReading(UniversalObject universalObject, ComposedCosemObject composedCosemObject, OfflineRegister offlineRegister, ComposedRegister composedRegister, ObisCode baseObisCode) throws IOException {
        RegisterValue registerValue = null;

        if (baseObisCode.equals(MULTICAST_FIRMWARE_UPGRADE_OBISCODE) && universalObject.getClassID() == DLMSClassId.IMAGE_TRANSFER.getClassId()) {
            //read out upgrade_state, attribute -3
            AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());

            if (attributeValue instanceof Structure) {
            	final Structure upgradeProgress = attributeValue.getStructure();

            	if (upgradeProgress.nrOfDataTypes() > 0 && upgradeProgress.getDataType(0).isTypeEnum()) {
                    final int value = ((TypeEnum)upgradeProgress.getDataType(0)).getValue();

                    final String description = MulticastUpgradeState.fromValue(value).getDescription();
                registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(value, Unit.get(BaseUnit.UNITLESS)), null, null, new Date(), new Date(), 0, description);
            } else {
                    this.addResult(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, "Received an incompatible structure (upgrade_progress) : expected the first element to be an Enumeration."));

                return null;
            }
            } else {
                this.addResult(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, "Received an incompatible datatype (upgrade_progress) : expected a structure, instead got a " + attributeValue.getClass()));

                return null;
            }
        } else if (universalObject.getClassID() == DLMSClassId.MEMORY_MANAGEMENT.getClassId()) {
                AbstractDataType memoryManagementAttribute = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());

                if (memoryManagementAttribute.isStructure() && memoryManagementAttribute.getStructure().nrOfDataTypes() == 4) {
                    //Special parsing for memory statistics
                    final String unit = memoryManagementAttribute.getStructure().getDataType(3).getOctetString().stringValue();
                    registerValue = new RegisterValue(offlineRegister.getObisCode(),
                            "Used space: " + memoryManagementAttribute.getStructure().getDataType(0).toBigDecimal() + " " + unit +
                                    ", free space: " + memoryManagementAttribute.getStructure().getDataType(1).toBigDecimal() + " " + unit +
                                    ", total space: " + memoryManagementAttribute.getStructure().getDataType(2).toBigDecimal() + " " + unit);
                } else if (memoryManagementAttribute.isArray()) {
                    //flash devices
                    registerValue = new RegisterValue(offlineRegister.getObisCode(),
                            "Flash devices: " + memoryManagementAttribute.getArray().toString());
                } else {
                    addResult(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, "Cannot parse memory management register, should be a structure with 4 or 8 elements"));
                    return null;
                }
        } else if (universalObject.getClassID() == DLMSClassId.DATA.getClassId() || universalObject.getClassID() == DLMSClassId.NTP_SERVER_ADDRESS.getClassId()) {
            //Generic parsing for all data registers
            final AbstractDataType attribute = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());

            if (attribute.isOctetString()) {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getOctetString().stringValue());
            } else if (attribute.isVisibleString()) {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getVisibleString().getStr());
            } else if (attribute.isArray() || attribute.isStructure()) {
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.toString());
            } else if (attribute.isTypeEnum()) {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getTypeEnum().getValue()+"");
            } else if (attribute.isBitString()) {
                if (isAlarmRegister(universalObject)) {
                    BitString value = attribute.getBitString();
                    if (value != null) {
                        AlarmBitsRegister alarmBitsRegister = new AlarmBitsRegister(universalObject.getObisCode(), attribute.getBitString().toBigDecimal().longValue());
                        registerValue = alarmBitsRegister.getRegisterValue();
                    }
                } else {
                    registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(attribute.toBigDecimal(), Unit.get(BaseUnit.UNITLESS)));
                }
            } else {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(attribute.toBigDecimal(), Unit.get(BaseUnit.UNITLESS)));
            }
        } else {
            //Generic parsing for all registers & extended registers
            Unit unit = Unit.get(BaseUnit.UNITLESS);
            if (composedRegister.getRegisterUnitAttribute() != null) {
                try {
                    unit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute())).getEisUnit();
                } catch (Exception ex) {
                    getLogger().warning("Cannot get unit from " + universalObject.getObisCode() + ": " + ex.getMessage());
                }

            }
            Date captureTime = null;
            if (composedRegister.getRegisterCaptureTime() != null) {
                AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
                captureTime = captureTimeOctetString.getOctetString().getDateTime(getDlmsSession().getTimeZone()).getValue().getTime();
            }

            AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
            registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(attributeValue.toBigDecimal(), unit), captureTime);
        }

        return registerValue;
    }


    /**
     * Preparation phase before reading the registers.
     * Will parse all registers to be read and will create a list of actual DLMS attributes to read.
     * Also for composed registers will create sets of (value, unit, capturedTime)
     */
    private void prepareReading() {

        for (OfflineRegister register : getAllRegistersToRead()) {

            G3Mapping g3Mapping = getBeacon3100G3RegisterMapper().getG3Mapping(register.getObisCode());
            final UniversalObject universalObject;
            final ObisCode baseObisCode = g3Mapping == null ? register.getObisCode() : g3Mapping.getBaseObisCode();
            try {
                universalObject = dlmsSession.getMeterConfig().findObject(baseObisCode);
            } catch (NotInObjectListException e) {
                continue;   //Move on to the next register, this one is not supported by the DC
            }

            if (g3Mapping != null) {
                prepareMappedRegister(register, g3Mapping);
            } else {
                prepareStandardRegister(register, universalObject);
                prepareSpecialMappedRegisters(register, universalObject, baseObisCode);
            }
        }

    }

    /**
     * Prepare the reading of some lazy-mapped obis codes
     *
     * @param register
     * @param universalObject
     * @param baseObisCode
     */
    private void prepareSpecialMappedRegisters(OfflineRegister register, UniversalObject universalObject, ObisCode baseObisCode) {
        ComposedRegister composedRegister = new ComposedRegister();

        if (universalObject.getClassID() == DLMSClassId.NTP_SERVER_ADDRESS.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), NPTServerAddressAttributes.NTP_SERVER_NAME.getAttributeNumber(), universalObject.getClassID());
            composedRegister.setRegisterValue(valueAttribute);
        } else if (universalObject.getClassID() == DLMSClassId.NTP_SETUP.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), NTPSetupAttributes.SERVER_ADDRESS.getAttributeNumber(), universalObject.getClassID());
            composedRegister.setRegisterValue(valueAttribute);
        } else if (baseObisCode.equals(MULTICAST_FIRMWARE_UPGRADE_OBISCODE) && universalObject.getClassID() == DLMSClassId.IMAGE_TRANSFER.getClassId()) {
            //read out upgrade_state, attribute -1
            DLMSAttribute valueAttribute = new DLMSAttribute(baseObisCode, ImageTransfer.ATTRIBUTE_UPGRADE_PROGRESS, universalObject.getClassID());
            composedRegister.setRegisterValue(valueAttribute);
        } else if (universalObject.getClassID() == DLMSClassId.MEMORY_MANAGEMENT.getClassId()) {
            DLMSAttribute memoryStatisticsAttribute = new DLMSAttribute(register.getObisCode(), MemoryManagementAttributes.MEMORY_STATISTICS.getAttributeNumber(), universalObject.getClassID());
            composedRegister.setRegisterValue(memoryStatisticsAttribute);
        }

        if (composedRegister.getRegisterValueAttribute() != null) {
            addAttributesToRead(composedRegister.getAllAttributes());
            addComposedRegister(register.getObisCode(), composedRegister);
        }
    }

    /**
     * Prepare reading of all standard DLMS Classes (1=DATA, 3=REGISTER, 4=EXTENDED_REGISTER
     *
     * @param register
     * @param universalObject
     */
    private void prepareStandardRegister(OfflineRegister register, UniversalObject universalObject) {
        ComposedRegister composedRegister = new ComposedRegister();

        if (universalObject.getClassID() == DLMSClassId.DATA.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), DataAttributes.VALUE.getAttributeNumber(), universalObject.getClassID());
            composedRegister.setRegisterValue(valueAttribute);
        }

        if (universalObject.getClassID() == DLMSClassId.REGISTER.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), RegisterAttributes.VALUE.getAttributeNumber(), universalObject.getClassID());
            DLMSAttribute scalerUnitAttribute = new DLMSAttribute(register.getObisCode(), RegisterAttributes.SCALER_UNIT.getAttributeNumber(), universalObject.getClassID());
            composedRegister.setRegisterValue(valueAttribute);
            composedRegister.setRegisterUnit(scalerUnitAttribute);
        }

        if (universalObject.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), ExtendedRegisterAttributes.VALUE.getAttributeNumber(), universalObject.getClassID());
            DLMSAttribute scalerUnitAttribute = new DLMSAttribute(register.getObisCode(), ExtendedRegisterAttributes.UNIT.getAttributeNumber(), universalObject.getClassID());
            DLMSAttribute captureTimeAttribute = new DLMSAttribute(register.getObisCode(), ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), universalObject.getClassID());
            composedRegister.setRegisterValue(valueAttribute);
            composedRegister.setRegisterUnit(scalerUnitAttribute);
            composedRegister.setRegisterCaptureTime(captureTimeAttribute);
        }

        if (composedRegister.getRegisterValueAttribute() != null) {
            addAttributesToRead(composedRegister.getAllAttributes());
            addComposedRegister(register.getObisCode(), composedRegister);
        }
    }


    /**
     * Constructs a composed register from a mapped register
     *
     * @param g3Mapping
     * @return
     */
    private void prepareMappedRegister(OfflineRegister register, G3Mapping g3Mapping) {
        ComposedRegister composedRegister = new ComposedRegister();

        // the default value attribute to be read-out
        DLMSAttribute valueAttribute = new DLMSAttribute(g3Mapping.getBaseObisCode(), g3Mapping.getValueAttribute(), g3Mapping.getDLMSClassId());
        addAttributeToRead(valueAttribute);
        composedRegister.setRegisterValue(valueAttribute);

        // optional - the unit attribute
        if (g3Mapping.getUnitAttribute() != 0) {
            DLMSAttribute unitAttribute = new DLMSAttribute(g3Mapping.getBaseObisCode(), g3Mapping.getUnitAttribute(), g3Mapping.getDLMSClassId());
            addAttributeToRead(unitAttribute);
            composedRegister.setRegisterUnit(unitAttribute);
        }

        // optional - the value attribute
        if (g3Mapping.getCaptureTimeAttribute() != 0) {
            DLMSAttribute ctAttribute = new DLMSAttribute(g3Mapping.getBaseObisCode(), g3Mapping.getCaptureTimeAttribute(), g3Mapping.getDLMSClassId());
            addAttributeToRead(ctAttribute);
            composedRegister.setRegisterCaptureTime(ctAttribute);
        }

        addComposedRegister(register.getObisCode(), composedRegister);
    }


    private boolean isAlarmRegister(UniversalObject universalObject) {
        return universalObject.getObisCode().equals(ObisCode.fromString(ALARM_BITS_REGISTER)) ||
                universalObject.getObisCode().equals(ObisCode.fromString(ALARM_FILTER)) ||
                universalObject.getObisCode().equals(ObisCode.fromString(ALARM_DESCRIPTOR));
    }

    private Beacon3100G3RegisterMapper getBeacon3100G3RegisterMapper() {
        if (beacon3100G3RegisterMapper == null) {
            beacon3100G3RegisterMapper = new Beacon3100G3RegisterMapper(getDlmsSession().getCosemObjectFactory(), getDlmsSession().getProperties().getTimeZone(), getLogger());
        }
        return beacon3100G3RegisterMapper;
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, issueFactory.createWarning(
                    register.getObisCode(), register.getObisCode().toString() + ": " + errorMessage[0].toString(), register.getObisCode(), errorMessage[0])
            );
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, issueFactory.createWarning(register.getObisCode(), register.getObisCode().toString() + " is not supported.", register.getObisCode()));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode(), new DeviceIdentifierById(offlineRtuRegister.getDeviceId()));
    }

    private List<OfflineRegister> getAllRegistersToRead() {
        return allRegisters;
    }

    private void setRegistersToRead(List<OfflineRegister> allRegisters) {
        this.allRegisters = allRegisters;
    }


    private void addAttributesToRead(List<DLMSAttribute> allAttributes) {
        getDLMSAttributes().addAll(allAttributes);
    }

    private void addAttributeToRead(DLMSAttribute dlmsAttribute) {
        getDLMSAttributes().add(dlmsAttribute);
    }

    public List<DLMSAttribute> getDLMSAttributes() {
        return dlmsAttributes;
    }


    public void addComposedRegister(ObisCode obisCode, ComposedRegister composedRegister) {
        getLogger().finest(" - adding for " + obisCode + " > " + composedRegister.toString());
        this.composedRegisterMap.put(obisCode, composedRegister);
    }

    public void addResult(CollectedRegister collectedRegister) {
        this.collectedRegisters.add(collectedRegister);
    }

    public List<CollectedRegister> getCollectedRegisters() {
        return this.collectedRegisters;
    }

    private Logger getLogger() {
        return dlmsSession.getLogger();
    }

    public Map<ObisCode, ComposedRegister> getComposedRegisterMap() {
        return this.composedRegisterMap;
    }
}