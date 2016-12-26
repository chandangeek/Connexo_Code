package test.com.energyict.protocolimplv2.coronis.muc;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.ExchangeMode;
import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisParameterException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.properties.Temporals;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 4/06/13
 * Time: 16:15
 * Author: khe
 */
public class Messaging implements DeviceMessageSupport {

    private final WebRTUWavenisGateway protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public Messaging(WebRTUWavenisGateway protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
                    ConfigurationChangeDeviceMessage.WriteExchangeStatus.get(this.propertySpecService, this.nlsService, this.converter),
                    ConfigurationChangeDeviceMessage.WriteRadioAcknowledge.get(this.propertySpecService, this.nlsService, this.converter),
                    ConfigurationChangeDeviceMessage.WriteRadioUserTimeout.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.collectedDataFactory.createCollectedMessageList(pendingMessages);

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
                    collectedMessage.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(pendingMessage, "DeviceMessage.notSupported"));
                }
                result.addCollectedMessage(collectedMessage);
            }
        } catch (IOException e) {      //Timeout, abort session
            throw ConnectionCommunicationException.numberOfRetriesReached(e, 1);    //No retries on protocol level, TCP handles this
        }
        return result;
    }

    private CollectedMessage writeExchangeStatus(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            Integer value = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
            protocol.getWavenisStack().getWaveCard().setExchangeStatus(ExchangeMode.fromValue(value));
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } catch (WavenisParameterException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(pendingMessage, "WriteWavecardParameters.writeExchangeStatusFailed", e.getMessage()));
        }
        return collectedMessage;
    }

    private CollectedMessage witeRadioAcknowledge(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            boolean value = Boolean.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
            protocol.getWavenisStack().getWaveCard().setRadioAcknowledge(value);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } catch (WavenisParameterException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(pendingMessage, "WriteWavecardParameters.writeRadioAcknowledgeFailed", e.getMessage()));
        }
        return collectedMessage;
    }

    private CollectedMessage writeRadioUserTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
        try {
            Integer value = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
            protocol.getWavenisStack().getWaveCard().setRadioUserTimeoutInSeconds(value);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        } catch (WavenisParameterException e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, this.issueFactory.createWarning(pendingMessage, "WriteWavecardParameters.writeRadioUserTimeoutFailed", e.getMessage()));
        }
        return collectedMessage;
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage pendingMessage) {
        return this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(pendingMessage.getDeviceMessageId()));
    }

    private boolean isWriteExchangeStatus(OfflineDeviceMessage pendingMessage) {
        return pendingMessage.getSpecification().getId() == ConfigurationChangeDeviceMessage.WriteExchangeStatus.id();
    }

    private boolean isWriteRadioAcknowledge(OfflineDeviceMessage pendingMessage) {
        return pendingMessage.getSpecification().getId() == ConfigurationChangeDeviceMessage.WriteRadioAcknowledge.id();
    }

    private boolean isWriteRadioUserTimeout(OfflineDeviceMessage pendingMessage) {
        return pendingMessage.getSpecification().getId() == ConfigurationChangeDeviceMessage.WriteRadioUserTimeout.id();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here...
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.WriteExchangeStatus)) {
            return String.valueOf(((BigDecimal) messageAttribute).intValue());
        } else if (propertySpec.getName().equals(DeviceMessageConstants.WriteRadioAcknowledge)) {
            return String.valueOf(messageAttribute);
        } else if (propertySpec.getName().equals(DeviceMessageConstants.WriteRadioUserTimeout)) {
            return String.valueOf(Temporals.toSeconds((TemporalAmount) messageAttribute));
        }
        return "";
    }

    @Override
    public String prepareMessageContext(OfflineDevice offlineDevice, com.energyict.mdc.upl.messages.DeviceMessage deviceMessage) {
        return "";
    }

}