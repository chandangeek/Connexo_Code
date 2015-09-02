package test.com.energyict.protocolimplv2.coronis.muc;

import com.energyict.cbo.TimeDuration;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.ExchangeMode;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisParameterException;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 16:15
 * Author: khe
 */
public class Messaging implements DeviceMessageSupport {

    private final WebRTUWavenisGateway protocol;

    public Messaging(WebRTUWavenisGateway protocol) {
        this.protocol = protocol;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        List<DeviceMessageSpec> result = new ArrayList<>();
        result.add(ConfigurationChangeDeviceMessage.WriteExchangeStatus);
        result.add(ConfigurationChangeDeviceMessage.WriteRadioAcknowledge);
        result.add(ConfigurationChangeDeviceMessage.WriteRadioUserTimeout);
        return result;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        try {
            for (OfflineDeviceMessage pendingMessage : pendingMessages) {
                CollectedMessage collectedMessage;
                if (isWriteExchangeStatus(pendingMessage)) {
                    collectedMessage = writeExchangeStatus(pendingMessage);
                } else if (isWriteRadioAcknowledge(pendingMessage)) {
                    collectedMessage = witeRadioAcknowledge(pendingMessage);
                } else if (isWriteRadioUserTimeout(pendingMessage)) {
                    collectedMessage = writeRadioUserTimeout(pendingMessage);
                } else {
                    collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(pendingMessage, "DeviceMessage.notSupported"));
                }
                result.addCollectedMessage(collectedMessage);
            }
        } catch (IOException e) {      //Timeout, abort session
            throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, 1);    //No retries on protocol level, TCP handles this
        }
        return result;
    }

    private CollectedMessage writeExchangeStatus(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            Integer value = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
            protocol.getWavenisStack().getWaveCard().setExchangeStatus(ExchangeMode.fromValue(value));
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } catch (WavenisParameterException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(pendingMessage, "WriteWavecardParameters.writeExchangeStatusFailed", e.getMessage()));
        }
        return collectedMessage;
    }

    private CollectedMessage witeRadioAcknowledge(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            boolean value = Boolean.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
            protocol.getWavenisStack().getWaveCard().setRadioAcknowledge(value);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } catch (WavenisParameterException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(pendingMessage, "WriteWavecardParameters.writeRadioAcknowledgeFailed", e.getMessage()));
        }
        return collectedMessage;
    }

    private CollectedMessage writeRadioUserTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            Integer value = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
            protocol.getWavenisStack().getWaveCard().setRadioUserTimeoutInSeconds(value);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } catch (WavenisParameterException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(pendingMessage, "WriteWavecardParameters.writeRadioUserTimeoutFailed", e.getMessage()));
        }
        return collectedMessage;
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage pendingMessage) {
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(pendingMessage.getDeviceMessageId()));
    }

    private boolean isWriteExchangeStatus(OfflineDeviceMessage pendingMessage) {
        return pendingMessage.getDeviceMessageSpecPrimaryKey().equals(ConfigurationChangeDeviceMessage.WriteExchangeStatus.getPrimaryKey().getValue());
    }

    private boolean isWriteRadioAcknowledge(OfflineDeviceMessage pendingMessage) {
        return pendingMessage.getDeviceMessageSpecPrimaryKey().equals(ConfigurationChangeDeviceMessage.WriteRadioAcknowledge.getPrimaryKey().getValue());
    }

    private boolean isWriteRadioUserTimeout(OfflineDeviceMessage pendingMessage) {
        return pendingMessage.getDeviceMessageSpecPrimaryKey().equals(ConfigurationChangeDeviceMessage.WriteRadioUserTimeout.getPrimaryKey().getValue());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here...
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.WriteExchangeStatus)) {
            return String.valueOf(((BigDecimal) messageAttribute).intValue());
        } else if (propertySpec.getName().equals(DeviceMessageConstants.WriteRadioAcknowledge)) {
            return String.valueOf(((Boolean) messageAttribute));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.WriteRadioUserTimeout)) {
            return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
        }
        return "";
    }
}