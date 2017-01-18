package com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
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
    private final CollectedDataFactory collectedDataFactory;
    private final RegisterMapping[] registerMappings;

    public IDISGatewayRegisters(DlmsSession session, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        this.session = session;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;

        this.registerMappings = new RegisterMapping[]{
                new GprsModemSetupMapping(session.getCosemObjectFactory()),
                new LoggerSettingsMapping(session.getCosemObjectFactory()),
                new GatewaySetupMapping(session.getCosemObjectFactory()),
                new NetworkManagementMapping(session.getCosemObjectFactory()),
                new MasterboardSetupMapping(session.getCosemObjectFactory())
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
        CollectedRegister collectedRegister = this.collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(register), register.getReadingType());
        collectedRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        collectedRegister.setCollectedTimeStamps(
                registerValue.getReadTime().toInstant(),
                registerValue.getFromTime().toInstant(),
                registerValue.getToTime().toInstant(),
                registerValue.getEventTime().toInstant());
        return collectedRegister;
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... arguments) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register), register.getReadingType());
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(
                    ResultType.InCompatible,
                    this.issueService.newWarning(
                            register.getObisCode(),
                            MessageSeeds.REGISTER_ISSUE,
                            register.getObisCode(), arguments));
        } else {
            collectedRegister.setFailureInformation(
                    ResultType.NotSupported,
                    this.issueService.newWarning(
                            register.getObisCode(),
                            MessageSeeds.REGISTER_NOT_SUPPORTED,
                            register.getObisCode(), arguments));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterDataIdentifierByObisCodeAndDevice(offlineRtuRegister.getObisCode(), offlineRtuRegister.getAmrRegisterObisCode(), offlineRtuRegister.getDeviceIdentifier());
    }

}