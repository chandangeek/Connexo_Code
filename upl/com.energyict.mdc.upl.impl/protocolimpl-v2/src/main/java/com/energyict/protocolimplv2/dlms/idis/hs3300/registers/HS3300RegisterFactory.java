package com.energyict.protocolimplv2.dlms.idis.hs3300.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.RegisterIdentifierById;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.idis.hs3300.HS3300;
import com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model.DeltaElectricalPhaseType;
import com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model.InitiatorElectricalPhaseType;
import com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model.PANConnectionStatus;
import com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model.PLCG3BandplanType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class HS3300RegisterFactory implements DeviceRegisterSupport {

    private static final ObisCode PLC_G3_BANDPLAN = ObisCode.fromString("0.0.94.43.128.255");
    private static final ObisCode INITIATOR_ELECTRICAL_PHASE = ObisCode.fromString("0.0.96.62.0.255");
    private static final ObisCode DELTA_ELECTRICAL_PHASE = ObisCode.fromString("0.0.96.62.1.255");
    private static final ObisCode PAN_CONNECTION_STATUS = ObisCode.fromString("0.0.94.43.131.255");
    private static final ObjectMapper mapper = new ObjectMapper();
    private final HS3300 hs3300;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private HS3300PLCRegisterMapper plcRegisterMapper;

    // Map of attributes (value, unit, captureTime) per register
    private Map<ObisCode, ComposedRegister> composedRegisterMap = new HashMap<>();

    // List of all attributes that need to be read out
    private List<DLMSAttribute> dlmsAttributes = new ArrayList<>();

    // The list of all collected registers
    private List<CollectedRegister> collectedRegisters = new ArrayList<>();

    public HS3300RegisterFactory(HS3300 hs3300, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.hs3300 = hs3300;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    public final List<CollectedRegister> readRegisters(final List<OfflineRegister> offlineRegisterList) {

        // parse the requests and build the composed objects and list of attributes to read
        prepareReading(offlineRegisterList);

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(getDlmsSession(), getDlmsSession().getProperties().isBulkRequest(), getDLMSAttributes());
        composedCosemObject.setUseAccessService(true);

        for (OfflineRegister offlineRegister : offlineRegisterList) {

            ComposedRegister composedRegister = getComposedRegisterMap().get(offlineRegister.getObisCode());
            if (composedRegister == null) {
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                continue;
            }

            G3Mapping g3Mapping = getPLCRegisterMapper().getG3Mapping(offlineRegister.getObisCode());
            final ObisCode baseObisCode = g3Mapping == null ? offlineRegister.getObisCode() : g3Mapping.getBaseObisCode();
            final UniversalObject universalObject;
            try {
                universalObject = getDlmsSession().getMeterConfig().findObject(baseObisCode);
            } catch (final NotInObjectListException e) {
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                continue;   // Move on to the next register, this one is not supported by the meter
            }

            try {
                RegisterValue registerValue;

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
        RegisterValue registerValue;

        if (universalObject.getClassID() == DLMSClassId.DATA.getClassId()) {
            // Generic parsing for all data registers
            final AbstractDataType attribute = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());

            if (attribute.isOctetString()) {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getOctetString().stringValue());
            } else if (attribute.isVisibleString()) {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getVisibleString().getStr());
            } else if (attribute.isArray()) {
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.toString());
            } else if (attribute.isStructure()) {
                if (offlineRegister.getObisCode().equals(PAN_CONNECTION_STATUS)) {
                    final PANConnectionStatus panConnectionStatus = new PANConnectionStatus(attribute.getStructure());
                    registerValue = new RegisterValue(offlineRegister.getObisCode(), mapper.writeValueAsString(panConnectionStatus));
                } else {
                    registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.toString());
                }
            } else if (attribute.isTypeEnum()) {
                if (offlineRegister.getObisCode().equals(PLC_G3_BANDPLAN)) {
                    final String bandplan = PLCG3BandplanType.getDescription(attribute.getTypeEnum().getValue());
                    registerValue = new RegisterValue(offlineRegister.getObisCode(), bandplan);
                } else if (offlineRegister.getObisCode().equals(INITIATOR_ELECTRICAL_PHASE)) {
                    final String initiatorElecPhase = InitiatorElectricalPhaseType.getDescription(attribute.getTypeEnum().getValue());
                    registerValue = new RegisterValue(offlineRegister.getObisCode(), initiatorElecPhase);
                } else if (offlineRegister.getObisCode().equals(DELTA_ELECTRICAL_PHASE)) {
                    final String deltaElecPhase = DeltaElectricalPhaseType.getDescription(attribute.getTypeEnum().getValue());
                    registerValue = new RegisterValue(offlineRegister.getObisCode(), deltaElecPhase);
                } else {
                    registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getTypeEnum().getValue() + "");
                }
            } else {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(attribute.toBigDecimal(), Unit.get(BaseUnit.UNITLESS)));
            }
        } else {
            // Generic parsing for all registers & extended registers
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
    private void prepareReading(final List<OfflineRegister> offlineRegisters) {

        for (OfflineRegister register : offlineRegisters) {

            G3Mapping g3Mapping = getPLCRegisterMapper().getG3Mapping(register.getObisCode());
            final UniversalObject universalObject;
            final ObisCode baseObisCode = g3Mapping == null ? register.getObisCode() : g3Mapping.getBaseObisCode();
            try {
                universalObject = getDlmsSession().getMeterConfig().findObject(baseObisCode);
            } catch (final NotInObjectListException e) {
                continue;   // Move on to the next register, this one is not supported by the meter
            }

            if (g3Mapping != null) {
                prepareMappedRegister(register, g3Mapping);
            } else {
                prepareStandardRegister(register, universalObject);
            }
        }

    }

    /**
     * Constructs a composed register from a mapped register
     *
     * @param register
     * @param g3Mapping
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

    /**
     * Prepare reading of all standard DLMS Classes (1=DATA, 3=REGISTER, 4=EXTENDED_REGISTER, 5=DEMAND_REGISTER)
     *
     * @param register
     * @param universalObject
     */
    private void prepareStandardRegister(OfflineRegister register, UniversalObject universalObject) {
        ComposedRegister composedRegister = new ComposedRegister();
        final int classId = universalObject.getClassID();

        if (classId == DLMSClassId.DATA.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), DataAttributes.VALUE.getAttributeNumber(), classId);
            composedRegister.setRegisterValue(valueAttribute);
        }

        if (classId == DLMSClassId.REGISTER.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), RegisterAttributes.VALUE.getAttributeNumber(), classId);
            DLMSAttribute scalerUnitAttribute = new DLMSAttribute(register.getObisCode(), RegisterAttributes.SCALER_UNIT.getAttributeNumber(), classId);
            composedRegister.setRegisterValue(valueAttribute);
            composedRegister.setRegisterUnit(scalerUnitAttribute);
        }

        if (classId == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), ExtendedRegisterAttributes.VALUE.getAttributeNumber(), classId);
            DLMSAttribute scalerUnitAttribute = new DLMSAttribute(register.getObisCode(), ExtendedRegisterAttributes.UNIT.getAttributeNumber(), classId);
            DLMSAttribute captureTimeAttribute = new DLMSAttribute(register.getObisCode(), ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), classId);
            composedRegister.setRegisterValue(valueAttribute);
            composedRegister.setRegisterUnit(scalerUnitAttribute);
            composedRegister.setRegisterCaptureTime(captureTimeAttribute);
        }

        if (classId == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber(), classId);
            DLMSAttribute scalerUnitAttribute = new DLMSAttribute(register.getObisCode(), DemandRegisterAttributes.UNIT.getAttributeNumber(), classId);
            DLMSAttribute captureTimeAttribute = new DLMSAttribute(register.getObisCode(), DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), classId);
            composedRegister.setRegisterValue(valueAttribute);
            composedRegister.setRegisterUnit(scalerUnitAttribute);
            composedRegister.setRegisterCaptureTime(captureTimeAttribute);
        }

        if (composedRegister.getRegisterValueAttribute() != null) {
            addAttributesToRead(composedRegister.getAllAttributes());
            addComposedRegister(register.getObisCode(), composedRegister);
        }
    }

    private void addAttributesToRead(List<DLMSAttribute> allAttributes) {
        getDLMSAttributes().addAll(allAttributes);
    }

    private void addAttributeToRead(DLMSAttribute dlmsAttribute) {
        getDLMSAttributes().add(dlmsAttribute);
    }

    private List<DLMSAttribute> getDLMSAttributes() {
        return dlmsAttributes;
    }

    private void addComposedRegister(ObisCode obisCode, ComposedRegister composedRegister) {
        getLogger().finest(" - adding for " + obisCode + " > " + composedRegister.toString());
        composedRegisterMap.put(obisCode, composedRegister);
    }

    private Map<ObisCode, ComposedRegister> getComposedRegisterMap() {
        return composedRegisterMap;
    }

    private HS3300PLCRegisterMapper getPLCRegisterMapper() {
        if (plcRegisterMapper == null) {
            plcRegisterMapper = new HS3300PLCRegisterMapper(getDlmsSession());
        }
        return plcRegisterMapper;
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
            collectedRegister.setFailureInformation(ResultType.NotSupported, issueFactory.createWarning(register.getObisCode(),
                    "Register " + register.getObisCode().toString() + " is not supported.", register.getObisCode()));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode(), new DeviceIdentifierById(offlineRtuRegister.getDeviceId()));
    }

    private void addResult(CollectedRegister collectedRegister) {
        collectedRegisters.add(collectedRegister);
    }

    private List<CollectedRegister> getCollectedRegisters() {
        return collectedRegisters;
    }

    private DlmsSession getDlmsSession() {
        return hs3300.getDlmsSession();
    }

    private Logger getLogger() {
        return getDlmsSession().getLogger();
    }
}