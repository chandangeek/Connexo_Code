package com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping.GprsModemSetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping.LoggerSettingsMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.GatewaySetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.IDISGatewaySFSKMacCountersMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.MasterboardSetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.NetworkManagementMapping;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;

import java.io.IOException;

/**
 * @author sva
 * @since 15/10/2014 - 12:20
 */
public class IDISGatewayRegisters {

    public static final ObisCode CLOCK_OBIS_CODE = ObisCode.fromString("0.0.1.0.0.255");
    public static final ObisCode SERIAL_NUMBER_OBIS = ObisCode.fromString("0.0.96.1.0.255");

    private final DlmsSession session;
    private final RegisterMapping[] registerMappings;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public IDISGatewayRegisters(DlmsSession session, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.session = session;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;

        this.registerMappings = new RegisterMapping[]{
                new GprsModemSetupMapping(session.getCosemObjectFactory()),
                new LoggerSettingsMapping(session.getCosemObjectFactory()),
                new GatewaySetupMapping(session.getCosemObjectFactory()),
                new NetworkManagementMapping(session.getCosemObjectFactory()),
                new MasterboardSetupMapping(session.getCosemObjectFactory()),
                new IDISGatewaySFSKMacCountersMapping(session.getCosemObjectFactory())
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
            if (DLMSIOExceptionHandler.isUnexpectedResponse(e, session.getProperties().getRetries() + 1)) {
                if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
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
        CollectedRegister collectedRegister = this.collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(register));
        collectedRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        collectedRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return collectedRegister;
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... arguments) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, this.issueFactory.createWarning(register.getObisCode(), "registerXissue", register.getObisCode(), arguments[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }
}