package com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping.GprsModemSetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping.LoggerSettingsMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.GatewaySetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.MasterboardSetupMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.mappings.NetworkManagementMapping;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
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
    private final RegisterMapping[] registerMappings;

    public IDISGatewayRegisters(DlmsSession session) {
        this.session = session;

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
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register));
        collectedRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        collectedRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return collectedRegister;
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... arguments) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXissue", register.getObisCode(), arguments[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }
}