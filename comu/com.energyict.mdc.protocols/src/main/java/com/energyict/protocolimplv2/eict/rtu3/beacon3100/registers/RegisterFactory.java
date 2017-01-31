/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtu3.beacon3100.registers;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.io.ConnectionCommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.NotInObjectListException;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.tasks.support.DeviceRegisterSupport;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.DisconnectControlAttribute;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.MemoryManagementAttributes;
import com.energyict.dlms.cosem.attributes.NPTServerAddressAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast.MulticastUpgradeState;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterFactory implements DeviceRegisterSupport {

    private final DlmsSession dlmsSession;
    private Beacon3100G3RegisterMapper beacon3100G3RegisterMapper;
    private static final ObisCode MULTICAST_FIRMWARE_UPGRADE_OBISCODE = ObisCode.fromString("0.0.44.0.128.255");
    private static final ObisCode MULTICAST_METER_PROGRESS = ProtocolTools.setObisCodeField(MULTICAST_FIRMWARE_UPGRADE_OBISCODE, 1, (byte) (-1 * ImageTransfer.ATTRIBUTE_UPGRADE_PROGRESS));

    public static final ObisCode DISCONNECT_CONTROL_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    public static final ObisCode CONNECT_CONTROL_MODE = ObisCode.fromString("0.0.96.3.128.255");
    public static final ObisCode CONNECT_CONTROL_STATE = ObisCode.fromString("0.0.96.3.129.255");
    public static final ObisCode CONNECT_CONTROL_BREAKER_STATE = ObisCode.fromString("0.0.96.3.130.255");

    public static final ObisCode USB_SETUP_OBISCODE = ObisCode.fromString("0.0.128.0.28.255");
    public static final ObisCode USB_STATE = ObisCode.fromString("0.2.128.0.28.255");
    public static final ObisCode USB_ACTIVITY = ObisCode.fromString("0.3.128.0.28.255");
    public static final ObisCode LAST_ACTIVITY_TIMESTAMP = ObisCode.fromString("0.4.128.0.28.255");

    public static final boolean DISABLED = false;
    public static final boolean ENABLED = true;
    public static final boolean IDLE = false;
    public static final boolean ACTIVE = true;

    public static final ObisCode GPRS_MODEM_SETUP = ObisCode.fromString("0.0.25.4.0.255");

    public static final ObisCode INSTANTANEOUS_VOLTAGE_L1 = ObisCode.fromString("1.0.32.7.0.255");
    public static final ObisCode INSTANTANEOUS_VOLTAGE_L2 = ObisCode.fromString("1.0.52.7.0.255");
    public static final ObisCode INSTANTANEOUS_VOLTAGE_L3 = ObisCode.fromString("1.0.72.7.0.255");
    public static final ObisCode INSTANTANEOUS_FREQUENCY = ObisCode.fromString("1.0.34.7.0.255");
    public static final ObisCode INSTANTANEOUS_PHASE_ANGLE_L1 = ObisCode.fromString("1.0.81.7.0.255");
    public static final ObisCode INSTANTANEOUS_PHASE_ANGLE_L2 = ObisCode.fromString("1.0.81.7.1.255");
    public static final ObisCode INSTANTANEOUS_PHASE_ANGLE_L3 = ObisCode.fromString("1.0.81.7.2.255");

    public static final ObisCode RESERVE_ENERGY = ObisCode.fromString("0.0.96.6.1.255");
    public static final ObisCode TEMPERATURE = ObisCode.fromString("0.0.96.9.0.255");

    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;


    public RegisterFactory(DlmsSession dlmsSession, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        this.dlmsSession = dlmsSession;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
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
                } else if (universalObject.getClassID() == DLMSClassId.NTP_SERVER_ADDRESS.getClassId()) {
                    DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), NPTServerAddressAttributes.NTP_SERVER_NAME.getAttributeNumber(), universalObject.getClassID());
                    composedRegister.setRegisterValue(valueAttribute);
                } else if (register.getObisCode().equals(CONNECT_CONTROL_MODE)) {
                    DLMSAttribute valueAttribute = new DLMSAttribute(DISCONNECT_CONTROL_OBISCODE, DisconnectControlAttribute.CONTROL_MODE.getAttributeNumber(), universalObject.getClassID());
                    composedRegister.setRegisterValue(valueAttribute);
                } else if (register.getObisCode().equals(CONNECT_CONTROL_STATE)) {
                    DLMSAttribute valueAttribute = new DLMSAttribute(DISCONNECT_CONTROL_OBISCODE, DisconnectControlAttribute.CONTROL_STATE.getAttributeNumber(), universalObject.getClassID());
                    composedRegister.setRegisterValue(valueAttribute);
                } else if (register.getObisCode().equals(CONNECT_CONTROL_BREAKER_STATE)) {
                    DLMSAttribute valueAttribute = new DLMSAttribute(DISCONNECT_CONTROL_OBISCODE, DisconnectControlAttribute.OUTPUT_STATE.getAttributeNumber(), universalObject.getClassID());
                    composedRegister.setRegisterValue(valueAttribute);
                } else if (baseObisCode.equals(MULTICAST_FIRMWARE_UPGRADE_OBISCODE) && universalObject.getClassID() == DLMSClassId.IMAGE_TRANSFER.getClassId()) {
                    //read out upgrade_state, attribute -1
                    DLMSAttribute valueAttribute = new DLMSAttribute(baseObisCode, ImageTransfer.ATTRIBUTE_PREVIOUS_UPGRADE_STATE, universalObject.getClassID());
                    composedRegister.setRegisterValue(valueAttribute);
                }
                dlmsAttributes.addAll(composedRegister.getAllAttributes());
                composedRegisterMap.put(register.getObisCode(), composedRegister);
            }
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(this.getDlmsSession(), this.getDlmsSession().getProperties().isBulkRequest(), dlmsAttributes);
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister offlineRegister : allRegisters) {
            if (offlineRegister.getObisCode().equals(MULTICAST_METER_PROGRESS)) {
                result.add(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, "Register with obiscode " + offlineRegister.getObisCode() + " cannot be read out, use the 'read DC multicast progress' message on the Beacon protocol for this."));
                continue;
            }

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
                    RegisterValue registerValue = null;

                    if (g3Mapping != null) {
                        registerValue = g3Mapping.parse(composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute()));
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
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, "Cannot parse memory management register, should be a structure with 4 or 8 elements"));
                            continue;
                        }
                    } else if (baseObisCode.equals(MULTICAST_FIRMWARE_UPGRADE_OBISCODE) && universalObject.getClassID() == DLMSClassId.IMAGE_TRANSFER.getClassId()) {
                        //read out upgrade_state, attribute -1
                        AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
                        if (attributeValue instanceof TypeEnum) {
                            int value = ((TypeEnum) attributeValue).getValue();
                            String description = MulticastUpgradeState.fromValue(value).getDescription();
                            registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(value, Unit.get(BaseUnit.UNITLESS)), null, null, new Date(), new Date(), 0, description);
                        } else {
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, "Cannot parse attribute " + ImageTransfer.ATTRIBUTE_PREVIOUS_UPGRADE_STATE + " (previous_upgrade_state) of object " + MULTICAST_FIRMWARE_UPGRADE_OBISCODE
                                    .toString() + ", should be of type Enum."));
                            continue;
                        }
                    } else if (universalObject.getClassID() == DLMSClassId.DATA.getClassId()
                            || universalObject.getClassID() == DLMSClassId.NTP_SERVER_ADDRESS.getClassId()) {
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
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSession().getProperties().getRetries() + 1)) {
                        if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                        } else {
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage()));
                        }
                    } else {
                        throw new ConnectionCommunicationException(getDlmsSession().getProperties().getRetries() + 1);
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
        CollectedRegister deviceRegister = this.collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister), offlineRegister.getReadingType());
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime().toInstant(), registerValue.getFromTime().toInstant(), registerValue.getToTime().toInstant(), registerValue.getEventTime().toInstant());
        return deviceRegister;
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register),
                register.getReadingType());
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(
                    ResultType.InCompatible,
                    this.issueService.newWarning(
                            register.getObisCode(),
                            MessageSeeds.REGISTER_ISSUE,
                            register.getObisCode(),
                            errorMessage));
        } else {
            collectedRegister.setFailureInformation(
                    ResultType.NotSupported,
                    this.issueService.newWarning(
                            register.getObisCode(),
                            MessageSeeds.REGISTER_NOT_SUPPORTED,
                            register.getObisCode(),
                            errorMessage));
        }
        return collectedRegister;
    }

    public RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterDataIdentifierByObisCodeAndDevice(offlineRtuRegister.getObisCode(), offlineRtuRegister.getObisCode(), offlineRtuRegister.getDeviceIdentifier());
    }
}