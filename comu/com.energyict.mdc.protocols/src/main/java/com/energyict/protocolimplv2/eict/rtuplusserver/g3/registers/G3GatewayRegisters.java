/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PLCOFDMType2MACSetupMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PLCOFDMType2PHYAndMACCountersMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.SixLowPanAdaptationLayerSetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.AdditionalInfoCustomRegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.CustomRegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.FirewallSetupCustomRegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.G3NetworkManagementCustomRegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.PushEventNotificationSetupRegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.UplinkPingSetupCustomRegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping.GprsModemSetupMapping;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class G3GatewayRegisters {

    public static final ObisCode FW_APPLICATION = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode FW_UPPER_MAC = ObisCode.fromString("1.1.0.2.0.255");
    public static final ObisCode FW_LOWER_MAC = ObisCode.fromString("1.2.0.2.0.255");
    private static final ObisCode GSM_FIELD_STRENGTH = ObisCode.fromString("0.0.96.12.5.255");

    private final RegisterMapping[] registerMappings;
    private final CustomRegisterMapping[] customRegisterMappings;
    private final DlmsSession session;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;

    /**
     * Keeps track of the different FirmwareVersions so we don't fetch them mulitple times ...
     */
    private Map<ObisCode, String> firmwareVersions = new HashMap<>(3);

    public G3GatewayRegisters(DlmsSession dlmsSession, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        this.session = dlmsSession;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;

        this.customRegisterMappings = new CustomRegisterMapping[]{
                new FirewallSetupCustomRegisterMapping(session.getCosemObjectFactory()),
                new G3NetworkManagementCustomRegisterMapping(session.getCosemObjectFactory()),
                new UplinkPingSetupCustomRegisterMapping(session.getCosemObjectFactory()),
                new AdditionalInfoCustomRegisterMapping(session.getCosemObjectFactory()),
                new PushEventNotificationSetupRegisterMapping(session.getCosemObjectFactory()),
        };

        this.registerMappings = new RegisterMapping[]{
                new PLCOFDMType2MACSetupMapping(dlmsSession.getCosemObjectFactory()),
                new PLCOFDMType2PHYAndMACCountersMapping(dlmsSession.getCosemObjectFactory()),
                new SixLowPanAdaptationLayerSetupMapping(dlmsSession.getCosemObjectFactory()),
                new GprsModemSetupMapping(dlmsSession.getCosemObjectFactory())
        };
    }

    private boolean isFirmwareRegister(ObisCode obisCode) {
        return obisCode.equals(FW_APPLICATION) || obisCode.equals(FW_UPPER_MAC) || obisCode.equals(FW_LOWER_MAC);
    }

    public CollectedRegister readRegister(OfflineRegister register) {
        ObisCode obisCode = register.getObisCode();
        try {
            //First check if we can read it out as a normal register (there is only 1 normal register at this time)
            if (obisCode.equals(GSM_FIELD_STRENGTH)) {
                Quantity quantityValue = session.getCosemObjectFactory().getRegister(obisCode).getQuantityValue();
                RegisterValue registerValue = new RegisterValue(obisCode, quantityValue);
                return createCollectedRegister(registerValue, register);
            } else if (isFirmwareRegister(obisCode)) {
                String firmwareVersionString = getFirmwareVersionString(obisCode);
                RegisterValue registerValue = new RegisterValue(obisCode, firmwareVersionString);
                return createCollectedRegister(registerValue, register);
            }

            for (CustomRegisterMapping customRegisterMapping : customRegisterMappings) {
                if (customRegisterMapping.getObisCode().equals(obisCode)) {
                    RegisterValue registerValue = customRegisterMapping.readRegister();
                    return createCollectedRegister(registerValue, register);
                }
            }

            for (RegisterMapping registerMapping : registerMappings) {
                if (registerMapping.canRead(obisCode)) {
                    RegisterValue registerValue = registerMapping.readRegister(obisCode);
                    return createCollectedRegister(registerValue, register);
                }
            }
        } catch (IOException e) {
            if (IOExceptionHandler.isUnexpectedResponse(e, session)) {
                if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                    return createFailureCollectedRegister(register, ResultType.NotSupported);
                } else {
                    return createFailureCollectedRegister(register, ResultType.InCompatible, e.getMessage());
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return createFailureCollectedRegister(register, ResultType.InCompatible, e.getMessage());
        }

        return createFailureCollectedRegister(register, ResultType.NotSupported);
    }

    public String getFirmwareVersionString(ObisCode firmwareObisCode) throws IOException {
        String firmwareVersion = firmwareVersions.get(firmwareObisCode);
        if(firmwareVersion == null){
            AbstractDataType valueAttr = session.getCosemObjectFactory().getData(firmwareObisCode).getValueAttr();
            OctetString octetString = valueAttr.getOctetString();
            if (octetString != null) {
                firmwareVersion = octetString.stringValue();
                firmwareVersions.put(firmwareObisCode, firmwareVersion);
            } else {
                throw new IOException("Unexpected data type while reading out firmware version, expected OctetString");
            }
        }
        return firmwareVersion;
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister register) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(register),
                register.getReadingType());
        collectedRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        collectedRegister.setCollectedTimeStamps(
                registerValue.getReadTime().toInstant(),
                registerValue.getFromTime().toInstant(),
                registerValue.getToTime().toInstant(),
                registerValue.getEventTime().toInstant());
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterDataIdentifierByObisCodeAndDevice(offlineRtuRegister.getObisCode(), offlineRtuRegister.getAmrRegisterObisCode(), offlineRtuRegister.getDeviceIdentifier());
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... arguments) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register),
                register.getReadingType());
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(
                    ResultType.InCompatible,
                    this.issueService.newWarning(
                            register.getObisCode(),
                            MessageSeeds.REGISTER_ISSUE,
                            register.getObisCode(),
                            arguments));
        } else {
            collectedRegister.setFailureInformation(
                    ResultType.NotSupported,
                    this.issueService.newWarning(
                            register.getObisCode(),
                            MessageSeeds.REGISTER_NOT_SUPPORTED,
                            register.getObisCode(),
                            arguments));
        }
        return collectedRegister;
    }

}