package com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers;

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
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.MemoryManagementAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NotInObjectListException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/06/2015 - 16:22
 */
public class RegisterFactory {

    private final DlmsSession dlmsSession;
    private Beacon3100G3RegisterMapper beacon3100G3RegisterMapper;

    public RegisterFactory(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> allRegisters) {

        //Map of attributes (value, unit, captureTime) per register
        Map<ObisCode, ComposedRegister> composedRegisterMap = new HashMap<>();

        //List of all attributes that need to be read out
        List<DLMSAttribute> dlmsAttributes = new ArrayList<>();

        for (OfflineRegister register : allRegisters) {

            G3Mapping g3Mapping = getBeacon3100G3RegisterMapper().getG3Mapping(register.getObisCode());
            final UniversalObject universalObject;
            final ObisCode baseObisCode = g3Mapping == null ? register.getObisCode() : g3Mapping.getBaseObisCode();
            try {
                universalObject = dlmsSession.getMeterConfig().findObject(baseObisCode);
            } catch (NotInObjectListException e) {
                continue;   //Move on to the next register, this one is not supported by the DC
            }

            if (g3Mapping != null) {
                ComposedRegister composedRegister = new ComposedRegister();
                int[] attributeNumbers = g3Mapping.getAttributeNumbers();
                for (int index = 0; index < attributeNumbers.length; index++) {
                    int attributeNumber = attributeNumbers[index];
                    DLMSAttribute dlmsAttribute = new DLMSAttribute(g3Mapping.getBaseObisCode(), attributeNumber, g3Mapping.getDLMSClassId());
                    dlmsAttributes.add(dlmsAttribute);

                    //If the mapping contains more than 1 attribute, the order is always value, unit, captureTime
                    if (index == 0) {
                        composedRegister.setRegisterValue(dlmsAttribute);
                    } else if (index == 1) {
                        composedRegister.setRegisterUnit(dlmsAttribute);
                    } else if (index == 2) {
                        composedRegister.setRegisterCaptureTime(dlmsAttribute);
                    }
                }
                composedRegisterMap.put(register.getObisCode(), composedRegister);
            } else {

                ComposedRegister composedRegister = new ComposedRegister();

                if (universalObject.getClassID() == DLMSClassId.DATA.getClassId()) {
                    DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), DataAttributes.VALUE.getAttributeNumber(), universalObject.getClassID());
                    composedRegister.setRegisterValue(valueAttribute);
                } else if (universalObject.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                    DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), RegisterAttributes.VALUE.getAttributeNumber(), universalObject.getClassID());
                    DLMSAttribute scalerUnitAttribute = new DLMSAttribute(register.getObisCode(), RegisterAttributes.SCALER_UNIT.getAttributeNumber(), universalObject.getClassID());
                    composedRegister.setRegisterValue(valueAttribute);
                    composedRegister.setRegisterUnit(scalerUnitAttribute);
                } else if (universalObject.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                    DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), ExtendedRegisterAttributes.VALUE.getAttributeNumber(), universalObject.getClassID());
                    DLMSAttribute scalerUnitAttribute = new DLMSAttribute(register.getObisCode(), ExtendedRegisterAttributes.UNIT.getAttributeNumber(), universalObject.getClassID());
                    DLMSAttribute captureTimeAttribute = new DLMSAttribute(register.getObisCode(), ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), universalObject.getClassID());
                    composedRegister.setRegisterValue(valueAttribute);
                    composedRegister.setRegisterUnit(scalerUnitAttribute);
                    composedRegister.setRegisterCaptureTime(captureTimeAttribute);
                } else if (universalObject.getClassID() == DLMSClassId.MEMORY_MANAGEMENT.getClassId()) {
                    DLMSAttribute memoryStatisticsAttribute = new DLMSAttribute(register.getObisCode(), MemoryManagementAttributes.MEMORY_STATISTICS.getAttributeNumber(), universalObject.getClassID());
                    composedRegister.setRegisterValue(memoryStatisticsAttribute);
                }
                dlmsAttributes.addAll(composedRegister.getAllAttributes());
                composedRegisterMap.put(register.getObisCode(), composedRegister);
            }
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(this.getDlmsSession(), true, dlmsAttributes);
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister offlineRegister : allRegisters) {
            ComposedRegister composedRegister = composedRegisterMap.get(offlineRegister.getObisCode());

            if (composedRegister == null) {
                result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
            } else {

                G3Mapping g3Mapping = getBeacon3100G3RegisterMapper().getG3Mapping(offlineRegister.getObisCode());
                final ObisCode baseObisCode = g3Mapping == null ? offlineRegister.getObisCode() : g3Mapping.getBaseObisCode();
                final UniversalObject universalObject;
                try {
                    universalObject = dlmsSession.getMeterConfig().findObject(baseObisCode);
                } catch (NotInObjectListException e) {
                    result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                    continue;   //Move on to the next register, this one is not supported by the DC
                }

                try {
                    RegisterValue registerValue;

                    if (g3Mapping != null) {
                        registerValue = g3Mapping.parse(composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute()));
                    } else if (universalObject.getClassID() == DLMSClassId.MEMORY_MANAGEMENT.getClassId()) {
                        AbstractDataType memoryStatisticsAttribute = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());

                        if (!memoryStatisticsAttribute.isStructure() || memoryStatisticsAttribute.getStructure().nrOfDataTypes() != 4) {
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, "Cannot parse the memory statistics, should be a structure of with 4 elements"));
                            continue;
                        } else {
                            //Special parsing for this structure
                            final String unit = memoryStatisticsAttribute.getStructure().getDataType(3).getOctetString().stringValue();
                            registerValue = new RegisterValue(offlineRegister.getObisCode(),
                                    "Used space: " + memoryStatisticsAttribute.getStructure().getDataType(0).toBigDecimal() + " " + unit +
                                            ", free space: " + memoryStatisticsAttribute.getStructure().getDataType(1).toBigDecimal() + " " + unit +
                                            ", total space: " + memoryStatisticsAttribute.getStructure().getDataType(2).toBigDecimal() + " " + unit);
                        }

                    } else if (universalObject.getClassID() == DLMSClassId.DATA.getClassId()) {
                        //Generic parsing for all data registers

                        final AbstractDataType attribute = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());

                        if (attribute.isOctetString()) {
                            registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getOctetString().stringValue());
                        } else if (attribute.isVisibleString()) {
                            registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getVisibleString().getStr());
                        } else if (attribute.isArray() || attribute.isStructure()) {
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                            continue;
                        } else {
                            registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(attribute.toBigDecimal(), Unit.get(BaseUnit.UNITLESS)));
                        }
                    } else {
                        //Generic parsing for all registers & extended registers

                        Unit unit = Unit.get(BaseUnit.UNITLESS);
                        if (composedRegister.getRegisterUnitAttribute() != null) {
                            unit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute())).getEisUnit();
                        }
                        Date captureTime = null;
                        if (composedRegister.getRegisterCaptureTime() != null) {
                            AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
                            captureTime = captureTimeOctetString.getOctetString().getDateTime(getDlmsSession().getTimeZone()).getValue().getTime();
                        }

                        AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
                        registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(attributeValue.toBigDecimal(), unit), captureTime);
                    }

                    result.add(createCollectedRegister(registerValue, offlineRegister));
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, getDlmsSession())) {
                        if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                        } else {
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage()));
                        }
                    } else {
                        throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, getDlmsSession().getProperties().getRetries() + 1);
                    }
                }
            }
        }

        return result;
    }

    private Beacon3100G3RegisterMapper getBeacon3100G3RegisterMapper() {
        if (beacon3100G3RegisterMapper == null) {
            beacon3100G3RegisterMapper = new Beacon3100G3RegisterMapper(getDlmsSession().getCosemObjectFactory(), getDlmsSession().getProperties().getTimeZone(), getDlmsSession().getLogger());
        }
        return beacon3100G3RegisterMapper;
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }
}