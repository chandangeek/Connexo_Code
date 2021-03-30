package com.energyict.protocolimplv2.dlms.common.writers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;
import com.energyict.protocolimplv2.dlms.common.writers.impl.AbstractMessage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MessageHandler implements DeviceMessageSupport {

    private final CollectedDataFactory collectedDataFactory ;
    private final IssueFactory issueFactory;
    private final List<Message> messages;

    public MessageHandler(CollectedDataFactory collectedDataFactory,IssueFactory issueFactory, List<Message> messages) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.messages = messages;
        //validate(messages); TODO
    }

    // this is kind of ugly but needed due to model of messages specs/id's and FF principle
    private void validate(List<Message> messages) {
        long count = messages.stream().map(f -> f.asMessageSpec().getId()).distinct().count();
        if (count != messages.size()) {
            throw new RuntimeException("Development error: configured multiple messages with same id");
        }
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return messages.stream().map(Message::asMessageSpec).collect(Collectors.toList());
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList collectedMessageList = collectedDataFactory.createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage: pendingMessages) {
            Message message = messages.stream().filter(f -> f.asMessageSpec().equals(pendingMessage.getSpecification())).findFirst().orElse(new NotSupportedMessage(collectedDataFactory, issueFactory, pendingMessage));
            collectedMessageList.addCollectedMessage(message.execute(pendingMessage));
        }
        return collectedMessageList;
    }

    /**
     * This is almost the same implemenation for all protocols. Not knowing exactly what should do and also there is no common behavior except as implemented
     * (not doing anything).
     * If needed otherwise please override in specific protocol.
     * @param sentMessages the sent messages
     * @return empty message list all the time.
     */
    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return collectedDataFactory.createEmptyCollectedMessageList();
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object messageAttribute) {
        Message message = this.messages.stream().filter(f -> f.asMessageSpec().equals(offlineDeviceMessage.getSpecification())).findFirst().orElse(new NotSupportedMessage(collectedDataFactory, issueFactory, offlineDeviceMessage));
        return message.format(propertySpec, messageAttribute);
    }

    /**
     * Not clear what should we do here but looks like default behavior is not doing anything. Extend/override this is specific protocol if needed.
     * @param device The Device for which the DeviceMessage is intended
     * @param offlineDevice The offline version of the Device
     * @param deviceMessage The DeviceMessage
     * @return empty optional all the time.
     */
    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    /**
     * This implemenation is needed only to simplify MessageHandler therefore it is good to keep it here.
     */
    private static class NotSupportedMessage extends AbstractMessage {

        private final OfflineDeviceMessage message;

        protected NotSupportedMessage(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, OfflineDeviceMessage message) {
            super(collectedDataFactory, issueFactory);
            this.message = message;
        }

        @Override
        public CollectedMessage execute(OfflineDeviceMessage message) {
            return super.createNotSupportedMessage(message);
        }

        /**
         * This class is not a proper implementation while it is just for a special case: when no real message implementation is found.
         * It is safe to return null since this method will not be used.
         * @return null always
         */
        @Override
        public DeviceMessageSpec asMessageSpec() {
            return null;
        }

        @Override
        public String format(PropertySpec propertySpec, Object messageAttribute) {
            return messageAttribute.toString();
        }
    }
}
