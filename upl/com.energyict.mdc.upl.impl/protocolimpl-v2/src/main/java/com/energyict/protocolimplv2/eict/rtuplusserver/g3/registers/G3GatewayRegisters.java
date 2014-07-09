package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifierById;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PLCOFDMType2MACSetupMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PLCOFDMType2PHYAndMACCountersMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.SixLowPanAdaptationLayerSetupMapping;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.CustomRegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.FirewallSetupCustomRegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.G3NetworkManagementCustomRegisterMapping;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping.GprsModemSetupMapping;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2014 - 14:28
 */
public class G3GatewayRegisters {

    private static final ObisCode GSM_FIELD_STRENGTH = ObisCode.fromString("0.0.96.12.5.255");
    private final RegisterMapping[] registerMappings;
    private final CustomRegisterMapping[] customRegisterMappings;
    private final List<ObisCode> normalRegisters;
    private final DlmsSession session;

    public G3GatewayRegisters(DlmsSession dlmsSession) {
        this.session = dlmsSession;
        this.normalRegisters = new ArrayList<>();
        normalRegisters.add(GSM_FIELD_STRENGTH);

        this.customRegisterMappings = new CustomRegisterMapping[]{
                new FirewallSetupCustomRegisterMapping(dlmsSession.getCosemObjectFactory()),
                new G3NetworkManagementCustomRegisterMapping(dlmsSession.getCosemObjectFactory()),
        };

        this.registerMappings = new RegisterMapping[]{
                new PLCOFDMType2MACSetupMapping(dlmsSession.getLogger(), dlmsSession.getCosemObjectFactory()),
                new PLCOFDMType2PHYAndMACCountersMapping(dlmsSession.getLogger(), dlmsSession.getCosemObjectFactory()),
                new SixLowPanAdaptationLayerSetupMapping(dlmsSession.getLogger(), dlmsSession.getCosemObjectFactory()),
                new GprsModemSetupMapping(dlmsSession.getLogger(), dlmsSession.getCosemObjectFactory())
        };
    }

    public CollectedRegister readRegister(OfflineRegister register) {
        ObisCode obisCode = register.getObisCode();

        try {
            for (ObisCode supportedObisCode : normalRegisters) {
                if (obisCode.equals(supportedObisCode)) {
                    Quantity quantityValue = session.getCosemObjectFactory().getRegister(obisCode).getQuantityValue();
                    RegisterValue registerValue = new RegisterValue(obisCode, quantityValue);
                    return createCollectedRegister(registerValue, register);
                }
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

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister register) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register));
        collectedRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        collectedRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... arguments) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXissue", register.getObisCode(), arguments));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode(), arguments));
        }
        return collectedRegister;
    }
}