package com.energyict.protocolimplv2.eict.rtuplusserver.g3.registers;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
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
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2014 - 14:28
 */
public class G3GatewayRegisters {

    private static final ObisCode GSM_FIELD_STRENGTH = ObisCode.fromString("0.0.96.12.5.255");
    public static final ObisCode FW_APPLICATION = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode FW_UPPER_MAC = ObisCode.fromString("1.1.0.2.0.255");
    private static final ObisCode FW_LOWER_MAC = ObisCode.fromString("1.2.0.2.0.255");

    private final RegisterMapping[] registerMappings;
    private final CustomRegisterMapping[] customRegisterMappings;
    private final DlmsSession session;

    /**
     * Keeps track of the different FirmwareVersions so we don't fetch them multiple times ...
     */
    private Map<ObisCode, String> firmwareVersions = new HashMap<>(3);
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public G3GatewayRegisters(DlmsSession dlmsSession, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.session = dlmsSession;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;

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
            }  else if (isFirmwareRegister(obisCode)) {
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

    public String getFirmwareVersionString(ObisCode firmwareObisCode) throws IOException {
        String firmwareVersion = firmwareVersions.get(firmwareObisCode);
        if(firmwareVersion == null){
            AbstractDataType valueAttr = session.getCosemObjectFactory().getData(firmwareObisCode).getValueAttr();
            OctetString octetString = valueAttr.getOctetString();
            if (octetString != null) {
                firmwareVersion = octetString.stringValue();
                firmwareVersions.put(firmwareObisCode, firmwareVersion);
            } else {
                throw new ProtocolException("Unexpected data type while reading out firmware version, expected OctetString");
            }
        }
        return firmwareVersion;
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister register) {
        CollectedRegister collectedRegister = this.collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(register));
        collectedRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        collectedRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
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
}