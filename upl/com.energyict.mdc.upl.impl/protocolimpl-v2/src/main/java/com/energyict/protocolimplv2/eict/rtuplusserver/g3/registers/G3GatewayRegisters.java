package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PLCOFDMType2MACSetupMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PLCOFDMType2PHYAndMACCountersMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.RegisterMapping;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.SixLowPanAdaptationLayerSetupMapping;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.custom.*;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers.mapping.GprsModemSetupMapping;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2014 - 14:28
 */
public class G3GatewayRegisters {

    private static final ObisCode GSM_FIELD_STRENGTH = ObisCode.fromString("0.0.96.12.5.255");
    private static final ObisCode FW_APPLICATION = ObisCode.fromString("1.0.0.2.0.255");
    private static final ObisCode FW_UPPER_MAC = ObisCode.fromString("1.1.0.2.0.255");
    private static final ObisCode FW_LOWER_MAC = ObisCode.fromString("1.2.0.2.0.255");

    private final RegisterMapping[] registerMappings;
    private final CustomRegisterMapping[] customRegisterMappings;
    private final DlmsSession session;

    public G3GatewayRegisters(DlmsSession dlmsSession) {
        this.session = dlmsSession;

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

        if (obisCode.equalsIgnoreBChannel(G3NetworkManagement.getDefaultObisCode()) && (obisCode.getB() != 0)) {
            return createFailureCollectedRegister(register, ResultType.InCompatible, "Register with obiscode " + obisCode + " cannot be read out, use the path request message for this.");
        }

        try {

            //First check if we can read it out as a normal register (there is only 1 normal register at this time)
            if (obisCode.equals(GSM_FIELD_STRENGTH)) {
                Quantity quantityValue = session.getCosemObjectFactory().getRegister(obisCode).getQuantityValue();
                RegisterValue registerValue = new RegisterValue(obisCode, quantityValue);
                return createCollectedRegister(registerValue, register);
            } else if (isFirmwareRegister(obisCode)) {
                AbstractDataType valueAttr = session.getCosemObjectFactory().getData(obisCode).getValueAttr();
                OctetString octetString = valueAttr.getOctetString();
                if (octetString == null) {
                    throw new IOException("Unexpected data type while reading out firmware version, expected OctetString");
                } else {
                    RegisterValue registerValue = new RegisterValue(obisCode, octetString.stringValue());
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
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXissue", register.getObisCode(), arguments[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
    }
}