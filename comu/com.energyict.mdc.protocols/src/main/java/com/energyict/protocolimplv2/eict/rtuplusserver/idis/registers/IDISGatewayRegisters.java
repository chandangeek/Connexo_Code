package com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers;

import com.energyict.dlms.protocolimplv2.DlmsSession;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;

import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping.GprsModemSetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping.LoggerSettingsMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.GatewaySetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.MasterboardSetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.NetworkManagementMapping;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;

/**
 * @author sva
 * @since 15/10/2014 - 12:20
 */
public class IDISGatewayRegisters {

    public static final ObisCode CLOCK_OBIS_CODE = ObisCode.fromString("0.0.1.0.0.255");
    public static final ObisCode SERIAL_NUMBER_OBIS = ObisCode.fromString("0.0.96.1.0.255");

    private final DlmsSession session;
    private final IssueService issueService;
    private final RegisterMapping[] registerMappings;

    public IDISGatewayRegisters(DlmsSession session, IssueService issueService) {
        this.session = session;
        this.issueService = issueService;

        this.registerMappings = new RegisterMapping[]{
                new GprsModemSetupMapping(session.getLogger(), session.getCosemObjectFactory()),
                new LoggerSettingsMapping(session.getLogger(), session.getCosemObjectFactory()),
                new GatewaySetupMapping(session.getLogger(), session.getCosemObjectFactory()),
                new NetworkManagementMapping(session.getLogger(), session.getCosemObjectFactory()),
                new MasterboardSetupMapping(session.getLogger(), session.getCosemObjectFactory())
        };
    }

    public CollectedRegister readRegister(OfflineRegister register) {
        try {
            for (RegisterMapping registerMapping : registerMappings) {
                if (registerMapping.canRead(register.getObisCode())) {
                    RegisterValue registerValue = registerMapping.readRegister(register.getObisCode());
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

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister register) {
        CollectedRegister collectedRegister = com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register), register.getReadingType());
        collectedRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        collectedRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return collectedRegister;
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... arguments) {
        CollectedRegister collectedRegister = com.energyict.mdc.protocol.api.CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register), register.getReadingType());
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, this.issueService.newWarning(register.getObisCode(), "registerXissue", register.getObisCode(), arguments));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, this.issueService.newWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode(), arguments));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterDataIdentifierByObisCodeAndDevice(offlineRtuRegister.getObisCode(), offlineRtuRegister.getAmrRegisterObisCode(), offlineRtuRegister.getDeviceIdentifier());
    }
}