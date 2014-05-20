package com.elster.genericprotocolimpl.dlms.ek280.executors;

import com.energyict.mdw.core.Device;

/**
 * Copyrights
 * Date: 9/06/11
 * Time: 8:53
 */
public class MessageExecutor extends AbstractExecutor<Device> {

    /**
     * This class is used to execute all messages that are pending for an rtu
     * log and store all the results in the EIServer database
     *
     * @param executor - the executor...
     */
    public MessageExecutor(AbstractExecutor executor) {
        super(executor);
    }

    /**
     * Execute all messages that are pending for this particular rtu
     * log and store all the results in the EIServer database
     *
     * @param rtu The rtu with the pending messages
     */
    public void execute(Device rtu) {
        //List<OldDeviceMessage> messagesToQuery = new ArrayList<OldDeviceMessage>();
//        messagesToQuery.addAll(rtu.getOldPendingMessages());
//        messagesToQuery.addAll(rtu.getOldSentMessages());
//        if (!messagesToQuery.isEmpty()) {
//            applyMessages(messagesToQuery);
//            queryMessages(messagesToQuery);
//        } else {
//            info("No messages pending.");
//        }
    }

    /**
     * Call the applyMessages method on the protocol for each message that was pending
     *
     * @param pendingMessages The list of all the pending messages
     */
    //private void applyMessages(List<OldDeviceMessage> pendingMessages) {
    //    try {
    //        getDlmsProtocol().applyMessages(getMessageEntries(pendingMessages));
    //    } catch (IOException e) {
    //        severe("An exception occurred while calling applyMessages method: " + e.getMessage());
    //    }
    //}

    /**
     * Execute all the messages by calling the queryMessage method of the protocol for each OldDeviceMessage
     * and store the result of the message in EIServer
     *
     * @param pendingMessages The list of pending RtuMessages
     */
    //private void queryMessages(List<OldDeviceMessage> pendingMessages) {
    //    for (OldDeviceMessage message : pendingMessages) {
    //        MessageEntry messageEntry = new MessageEntry(message.getContents(), message.getTrackingId());
    //        try {
    //            MessageResult messageResult = getDlmsProtocol().queryMessage(messageEntry);
    //            storeMessageResult(message, messageResult);
    //        } catch (IOException e) {
    //            System.out.println("IOException: " + e.getMessage());
    //            storeMessageResult(message, MessageResult.createFailed(messageEntry));
    //        }
    //    }
    //}

    /**
     * Store the message result in the database, and log it if something goes wrong
     *
     * @param message       The message that just executed
     * @param messageResult The result of the message
     */
    //private void storeMessageResult(OldDeviceMessage message, MessageResult messageResult) {
    //    try {
    //        if (messageResult.isFailed()) {
    //            message.setFailed();
    //        } else if (messageResult.isSuccess()) {
    //            message.confirm();
    //        } else if (messageResult.isQueued()) {
    //            message.setSent();
    //        } else if (messageResult.isUnknown()) {
    //            message.setIndoubt();
    //        }
    //    } catch (BusinessException e) {
    //        severe("An error occurred while storing the MessageResult [" + messageResult + "]: " + e.getMessage());
    //    } catch (SQLException e) {
    //        severe("An error occurred while storing the MessageResult [" + messageResult + "]: " + e.getMessage());
    //    }
    //}

    /**
     * Convert a list of RtuMessages to a list of MessageEntries
     *
     * @param messages The list of RtuMessages to convert
     * @return The list of messageEntries
     */
    //private List<MessageEntry> getMessageEntries(List<OldDeviceMessage> messages) {
    //    List<MessageEntry> messageEntries = new ArrayList<MessageEntry>();
    //    for (OldDeviceMessage message : messages) {
    //        messageEntries.add(new MessageEntry(message.getContents(), message.getTrackingId()));
    //    }
    //    return messageEntries;
    //}

}
