/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.itron.em620.registers;

import com.energyict.mdc.identifiers.RegisterIdentifierById;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.itron.em620.EM620;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EM620RegisterFactory implements DeviceRegisterSupport {

    public static final int EXTENDED_REGISTER_CAPTURE_OBJECT_ATTRIBUTE_INDEX = ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber();
    public static final int DEMAND_REGISTER_CAPTURE_OBJECT_ATTRIBUTE_INDEX = DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber();

    public static final ObisCode BILLING_PROFILE_OBIS_CODE = ObisCode.fromString("0.0.98.1.0.255");

    private final EM620 em620;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    // Map of attributes (value, unit, captureTime) per register
    private final Map<ObisCode, ComposedRegister> composedRegisterMap = new HashMap<>();

    // List of all attributes that need to be read out
    private final List<DLMSAttribute> dlmsAttributes = new ArrayList<>();

    // The list of all collected registers
    private final List<CollectedRegister> collectedRegisters = new ArrayList<>();

    public EM620RegisterFactory(EM620 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.em620 = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> offlineRegisterList) {
        // parse the requests and build the composed objects and list of attributes to read
        prepareReading(offlineRegisterList);

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(em620.getDlmsSession(), em620.getDlmsSession().getProperties().isBulkRequest(), getDLMSAttributes());

        for (OfflineRegister offlineRegister : offlineRegisterList) {
            ObisCode obisCode = offlineRegister.getObisCode();

            if (isBillingLoadProfileObisCode(obisCode)) {
                try {
                    HistoricalValue historicalValue = em620.getStoredValues().getHistoricalValue(obisCode);
                    RegisterValue registerValue = new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime());
                    addResult(createCollectedRegister(registerValue, offlineRegister));
                } catch (NotInObjectListException e) {
                    addResult(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage()));
                } catch (NoSuchRegisterException e) {
                    addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                } catch (IOException e) {
                    getLogger().warning("Error while reading " + offlineRegister + ": " + e.getMessage());
                    addResult(createFailureCollectedRegister(offlineRegister, ResultType.Other, e.getMessage()));
                    final ProtocolException protocolException = new ProtocolException(e, "Error while reading out " + obisCode.toString() + ": " + e.getMessage());
                    throw com.energyict.protocol.exception.ConnectionCommunicationException.unExpectedProtocolError(protocolException); // this leaves the connection intact
                }
            } else {
                ComposedRegister composedRegister = getComposedRegisterMap().get(offlineRegister.getObisCode());
                if (composedRegister == null) {
                    addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                    continue;
                }
                final UniversalObject universalObject;
                try {
                    universalObject = em620.getDlmsSession().getMeterConfig().findObject(offlineRegister.getObisCode());
                } catch (final NotInObjectListException e) {
                    addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                    continue;   // Move on to the next register, this one is not supported by the meter
                }
                try {
                    RegisterValue registerValue = parseRegisterReading(universalObject, composedCosemObject, offlineRegister, composedRegister, offlineRegister.getObisCode());

                    if (registerValue != null) {
                        addResult(createCollectedRegister(registerValue, offlineRegister));
                    }
                } catch (IOException e) {
                    getLogger().warning("Error while reading " + offlineRegister + ": " + e.getMessage());
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, em620.getDlmsSession().getProperties().getRetries() + 1)) {
                        if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                            addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                        } else {
                            addResult(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage()));
                        }
                    } else {
                        throw com.energyict.protocol.exception.ConnectionCommunicationException.numberOfRetriesReachedWithConnectionStillIntact(
                                e, em620.getDlmsSession().getProperties().getRetries() + 1);
                    }
                } catch (Exception ex) {
                    getLogger().warning("Error while reading " + offlineRegister + ": " + ex.getMessage());
                    addResult(createFailureCollectedRegister(offlineRegister, ResultType.Other, ex.getMessage()));
                    final ProtocolException protocolException = new ProtocolException(ex, "Error while reading out " + offlineRegister.getObisCode().toString() + ": " + ex.getMessage());
                    throw com.energyict.protocol.exception.ConnectionCommunicationException.unExpectedProtocolError(protocolException); // this leaves the connection intact
                }
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
            } else if (attribute.isArray() || attribute.isStructure()) {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.toString());
            } else if (attribute.isTypeEnum()) {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getTypeEnum().getValue() + "");
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
                captureTime = captureTimeOctetString.getOctetString().getDateTime(em620.getDlmsSession().getTimeZone()).getValue().getTime();
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
    protected void prepareReading(final List<OfflineRegister> offlineRegisters) {
        for (OfflineRegister register : offlineRegisters) {
            final UniversalObject universalObject;
            try {
                universalObject = em620.getDlmsSession().getMeterConfig().findObject(register.getObisCode());
            } catch (final NotInObjectListException e) {
                continue;   // Move on to the next register, this one is not supported by the meter
            }
            prepareStandardRegister(register, universalObject);
        }
    }

    /**
     * Prepare reading of all standard DLMS Classes (1=DATA, 3=REGISTER, 4=EXTENDED_REGISTER, 5=DEMAND_REGISTER)
     *
     * @param register
     * @param universalObject
     */
    protected void prepareStandardRegister(OfflineRegister register, UniversalObject universalObject) {
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

    protected void addAttributesToRead(List<DLMSAttribute> allAttributes) {
        getDLMSAttributes().addAll(allAttributes);
    }

    protected void addComposedRegister(ObisCode obisCode, ComposedRegister composedRegister) {
        getLogger().finest(" - adding for " + obisCode + " > " + composedRegister.toString());
        composedRegisterMap.put(obisCode, composedRegister);
    }

    protected Logger getLogger() {
        return em620.getDlmsSession().getLogger();
    }

    protected List<DLMSAttribute> getDLMSAttributes() {
        return dlmsAttributes;
    }

    protected CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = this.collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRegister) {
        return new RegisterIdentifierById(offlineRegister.getRegisterId(), offlineRegister.getObisCode(), offlineRegister.getDeviceIdentifier());
    }

    protected CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(
                    register.getObisCode(), register.getObisCode().toString() + ": " + errorMessage[0].toString(), register.getObisCode(), errorMessage[0])
            );
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(register.getObisCode(),
                    "Register " + register.getObisCode().toString() + " is not supported.", register.getObisCode()));
        }
        return collectedRegister;
    }

    protected Map<ObisCode, ComposedRegister> getComposedRegisterMap() {
        return composedRegisterMap;
    }

    protected void addResult(CollectedRegister collectedRegister) {
        collectedRegisters.add(collectedRegister);
    }

    protected List<CollectedRegister> getCollectedRegisters() {
        return collectedRegisters;
    }

    private boolean isBillingLoadProfileObisCode(ObisCode obisCode) {
        return obisCode.getF() != 255;
    }
}
