package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

/**
 * @author sva
 * @since 4/07/13 - 10:01
 */
//public class CTRUpdateDeviceMessage extends UpdateDeviceMessage {
public class CTRUpdateDeviceMessage {

//    public CTRUpdateDeviceMessage(DeviceProtocolMessageAcknowledgement messageAcknowledgement, ComTaskExecution comTaskExecution) {
//        super(messageAcknowledgement, comTaskExecution);
//    }
//
//    @Override
//    public void doExecute(ComServerDAO comServerDAO) {
//        Optional<OfflineDeviceMessage> deviceMessage = comServerDAO.findOfflineDeviceMessage(getMessageIdentifier());
//        if (deviceMessage.isPresent()) {
//            String messageProtocolInfo = deviceMessage.getProtocolInfo();
//            String wdb = ((DeviceMessageIdentifierByDeviceAndProtocolInfoParts) getMessageIdentifier()).getMessageProtocolInfoParts()[1];
//
//            if (messageProtocolInfo.split(",").length > 1) {
//                // Protocol info contains multiple sms ids - we cannot set the message successful yet (we should wait until all smses are received)
//                // instead, we update the protocol info (remove the id of the confirmed sms)
//                messageProtocolInfo = messageProtocolInfo.replace(" " + wdb + ",", "");
//                messageProtocolInfo = messageProtocolInfo.replace(", " + wdb, "");
//
//                comServerDAO.updateDeviceMessageInformation(getMessageIdentifier(), DeviceMessageStatus.SENT, messageProtocolInfo);
//            } else {
//                comServerDAO.updateDeviceMessageInformation(getMessageIdentifier(), getDeviceMessageStatus(), getProtocolInfo());
//            }
//        } else {
//            addIssue(CompletionCode.ConfigurationWarning,
//                    this.getIssuesService().newWarning(this, MessageSeeds.UNKNOWN_DEVICE_MESSAGE.getKey(), getMessageIdentifier())
//            );
//        }
//    }
//
//    @Override
//    public String getDescriptionTitle() {
//        return "Update device message";
//    }

}