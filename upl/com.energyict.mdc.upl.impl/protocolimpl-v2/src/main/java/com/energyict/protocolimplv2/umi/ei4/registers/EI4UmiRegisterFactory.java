package com.energyict.protocolimplv2.umi.ei4.registers;

import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.RegisterIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;

import com.energyict.cbo.Quantity;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolcommon.Pair;
import com.energyict.protocolimplv2.umi.ei4.EI4Umi;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiwanConfiguration;
import com.energyict.protocolimplv2.umi.packet.payload.ReadObjRspPayload;
import com.energyict.protocolimplv2.umi.types.ResultCode;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class EI4UmiRegisterFactory implements DeviceRegisterSupport {
    private static final String PRIMARY_HOST = "0.0.94.39.4.255";
    private static final String SEC_HOST = "0.0.94.39.8.255";
    private static final String GATE_HOST = "0.0.94.39.10.255";
    private static final String INACTIVE_TIMEOUT = "0.0.94.39.12.255";
    private static final String SESSION_TIMEOUT = "0.0.94.39.14.255";
    private static final String PREFERRED_TIME_OF_DAY = "0.0.94.39.16.255";
    private static final String CALL_DISTANCE = "0.0.94.39.17.255";
    private static final String SHORT_RETRY_DISTANCE = "0.0.94.39.18.255";
    private static final String LONG_RETRY_DISTANCE = "0.0.94.39.19.255";
    private static final String RANDOM_ZONE = "0.0.94.39.20.255";
    private static final String CONTROL_FLAGS = "0.0.94.39.31.255";
    private static final String PRIMARY_PORT = "0.0.94.39.32.255";
    private static final String SEC_PORT = "0.0.94.39.44.255";
    private static final String GATE_PORT = "0.0.94.39.46.255";
    private static final String MAX_SHORT_RETRIES = "0.0.94.39.48.255";
    private static final String MAX_LONG_RETRIES = "0.0.94.39.50.255";

    private static List<String> supportedRegisters = new ArrayList<>();

    static {
        supportedRegisters.add(PRIMARY_HOST);
        supportedRegisters.add(SEC_HOST);
        supportedRegisters.add(GATE_HOST);
        supportedRegisters.add(INACTIVE_TIMEOUT);
        supportedRegisters.add(SESSION_TIMEOUT);
        supportedRegisters.add(PREFERRED_TIME_OF_DAY);
        supportedRegisters.add(CALL_DISTANCE);
        supportedRegisters.add(SHORT_RETRY_DISTANCE);
        supportedRegisters.add(LONG_RETRY_DISTANCE);
        supportedRegisters.add(RANDOM_ZONE);
        supportedRegisters.add(CONTROL_FLAGS);
        supportedRegisters.add(PRIMARY_PORT);
        supportedRegisters.add(SEC_PORT);
        supportedRegisters.add(GATE_PORT);
        supportedRegisters.add(MAX_SHORT_RETRIES);
        supportedRegisters.add(MAX_LONG_RETRIES);
    }

//    public static List<ObisCode> getSupportedRegisters() {
//        return supportedRegisters;
//    }


    private final IssueFactory issueFactory;
    private EI4Umi ei4Umi;
    private final CollectedDataFactory collectedDataFactory;


    public EI4UmiRegisterFactory(EI4Umi ei4Umi, IssueFactory issueFactory, CollectedDataFactory collectedDataFactory) {
        this.ei4Umi = ei4Umi;
        this.issueFactory = issueFactory;
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        List<CollectedRegister> result = new ArrayList<>();
        try {
            UmiwanConfiguration umiwanConfiguration = getUmiwanConfiguration();
            for (OfflineRegister offlineRegister : registers) {
                CollectedRegister collectedRegister = readRegister(offlineRegister, umiwanConfiguration);
                result.add(collectedRegister);
            }
        } catch (IOException e) {
            Issue problem = issueFactory.createWarning(e.getMessage());
//            .setFailureInformation(ResultType.DataIncomplete, problem);
        }
        return result;
    }

    private CollectedRegister readRegister(OfflineRegister offlineRegister, UmiwanConfiguration umiwanConfiguration) {
        switch (offlineRegister.getObisCode().getValue()) {
            case PRIMARY_HOST:
                return createRegister(offlineRegister, umiwanConfiguration.getPrimHost());
            case SEC_HOST:
                return createRegister(offlineRegister, umiwanConfiguration.getSecHost());
            case GATE_HOST:
                return createRegister(offlineRegister, umiwanConfiguration.getGateHost());
            case INACTIVE_TIMEOUT:
                return createRegister(offlineRegister, umiwanConfiguration.getInactiveTimeout());
            case SESSION_TIMEOUT:
                return createRegister(offlineRegister, umiwanConfiguration.getSessionTimeout());
            case PREFERRED_TIME_OF_DAY:
                return createRegister(offlineRegister, umiwanConfiguration.getPreferredTimeOfDay());
            case CALL_DISTANCE:
                return createRegister(offlineRegister, umiwanConfiguration.getCallDistance());
            case SHORT_RETRY_DISTANCE:
                return createRegister(offlineRegister, umiwanConfiguration.getShortRetryDistance());
            case LONG_RETRY_DISTANCE:
                return createRegister(offlineRegister, umiwanConfiguration.getLongRetryDistance());
            case RANDOM_ZONE:
                return createRegister(offlineRegister, umiwanConfiguration.getRandomZone());
            case CONTROL_FLAGS:
                return createRegister(offlineRegister, umiwanConfiguration.getControlFlags());
            case PRIMARY_PORT:
                return createRegister(offlineRegister, umiwanConfiguration.getPrimPort());
            case SEC_PORT:
                return createRegister(offlineRegister, umiwanConfiguration.getSecPort());
            case GATE_PORT:
                return createRegister(offlineRegister, umiwanConfiguration.getGatePort());
            case MAX_SHORT_RETRIES:
                return createRegister(offlineRegister, umiwanConfiguration.getMaxShortRetries());
            case MAX_LONG_RETRIES:
                return createRegister(offlineRegister, umiwanConfiguration.getMaxLongRetries());
            default:
                throw new IllegalStateException("Obis code: " + offlineRegister.getObisCode() + " not supported.");
        }
    }

    private CollectedRegister createRegister(OfflineRegister offlineRegister, String value) {
        RegisterValue registerValue = new RegisterValue(offlineRegister, value);
        getProtocol().journal(Level.FINE, "registerValue = " + registerValue.getText());
        return createCollectedRegister(registerValue, offlineRegister);
    }

    private CollectedRegister createRegister(OfflineRegister offlineRegister, Number value) {
        RegisterValue registerValue = new RegisterValue(offlineRegister, new Quantity(value, offlineRegister.getUnit()));
        getProtocol().journal(Level.FINE, "registerValue = " + registerValue.getQuantity());
        return createCollectedRegister(registerValue, offlineRegister);
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode(), new DeviceIdentifierById(offlineRtuRegister.getDeviceId()));
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = collectedDataFactory.createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }

    public EI4Umi getProtocol() {
        return ei4Umi;
    }

    public UmiwanConfiguration getUmiwanConfiguration() throws IOException {
        try {
            Pair<ResultCode, ReadObjRspPayload> pair = ei4Umi.getUmiSession().readObject(UmiwanConfiguration.UMIWAN_CONFIGURATION_UMI_CODE);
            if (pair.getFirst() != ResultCode.OK) {
                ei4Umi.journal(Level.WARNING, "Read umiwan configuration operation failed. " + pair.getFirst().getDescription());
                throw new ProtocolException("Reading of umiwan configuration " + UmiwanConfiguration.UMIWAN_CONFIGURATION_UMI_CODE.getCode() + " failed. " + pair.getFirst().getDescription());
            } else {
                return new UmiwanConfiguration(pair.getLast().getValue());
            }
        } catch (GeneralSecurityException e) {
            // should not occur in EI4
        }
        return null;
    }

    public UmiwanConfiguration setUmiwanConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        try {
            Map<String, Object> objectMap = new HashMap<>();
            pendingMessage.getDeviceMessageAttributes().forEach(offlineDeviceMessageAttribute -> objectMap.put(offlineDeviceMessageAttribute.getName(), offlineDeviceMessageAttribute.getValue()));
            UmiwanConfiguration umiwanConfiguration = new UmiwanConfiguration(objectMap);
            ResultCode resultCode = ei4Umi.getUmiSession().writeObject(UmiwanConfiguration.UMIWAN_CONFIGURATION_UMI_CODE, umiwanConfiguration.getRaw());
            if (resultCode != ResultCode.OK) {
                ei4Umi.journal(Level.WARNING, "Write umiwan configuration operation failed. " + resultCode.getDescription());
                throw new ProtocolException("Writing of umiwan configuration " + UmiwanConfiguration.UMIWAN_CONFIGURATION_UMI_CODE.getCode() + " failed. " + resultCode.getDescription());
            } else {
                return umiwanConfiguration;
            }
        } catch (GeneralSecurityException e) {
            // should not occur in EI4
        }
        return null;
    }
}
