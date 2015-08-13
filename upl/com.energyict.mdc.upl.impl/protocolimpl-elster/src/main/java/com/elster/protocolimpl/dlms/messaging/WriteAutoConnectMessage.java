package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.classes.class29.AutoConnectModeEnum;
import com.elster.dlms.cosem.classes.common.TimeWindow;
import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleAutoConnectObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;
import java.util.regex.Pattern;

import static com.elster.protocolimpl.dlms.util.RepetitiveDate.checkRepetitiveDate;
import static com.elster.protocolimpl.dlms.util.RepetitiveDate.dateStringToDlmsDateTime;

/**
 * User: heuckeg
 * Date: 30.09.11
 * Time: 14:57
 */
public class WriteAutoConnectMessage extends AbstractDlmsMessage {

    /**
     * RtuMessage tags for the key change message
     */
    public static final String MESSAGE_TAG = "SetAutoConnect";
    public static final String MESSAGE_DESCRIPTION = "Change auto connect data";
    public static final String ATTR_AUTOCONNECT_ID = "AutoConnectId";
    public static final String ATTR_AUTOCONNECT_MODE = "AutoConnectMode";
    public static final String ATTR_AUTOCONNECT_START = "AutoConnectStart";
    public static final String ATTR_AUTOCONNECT_END = "AutoConnectEnd";
    public static final String ATTR_DESTINATION1 = "Destination1";
    public static final String ATTR_DESTINATION2 = "Destination2";

    private static final String ValidIpAddressRegex = "((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))";
    private static final String ValidHostnameRegex = "((([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9]))";
    private static final String OptionalPort = "(\\:[0-9]+)";


    public WriteAutoConnectMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    /**
     * Send the message  to the meter.
     *
     * @param messageEntry: the message containing the new keys
     * @throws com.energyict.cbo.BusinessException:
     *          when a parameter is null or too long
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {

        String autoConnectId = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AUTOCONNECT_ID);
        String autoConnectMode = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AUTOCONNECT_MODE);
        String autoConnectStart = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AUTOCONNECT_START);
        String autoConnectEnd = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AUTOCONNECT_END);
        String autoConnectDest1 = MessagingTools.getContentOfAttribute(messageEntry, ATTR_DESTINATION1);
        String autoConnectDest2 = MessagingTools.getContentOfAttribute(messageEntry, ATTR_DESTINATION2);
        validateMessageData(autoConnectId, autoConnectMode, autoConnectStart, autoConnectEnd, autoConnectDest1, autoConnectDest2);
        try {
            write(autoConnectId, autoConnectMode, autoConnectStart, autoConnectEnd, autoConnectDest1, autoConnectDest2);
        } catch (IOException e) {
            throw new BusinessException("Unable to set auto connect data: " + e.getMessage());
        }

    }

    private void write(String autoConnectId, String autoConnectMode,
                       String autoConnectStart, String autoConnectEnd,
                       String autoConnectDest1, String autoConnectDest2) throws BusinessException, IOException {

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        int acId = Integer.parseInt(autoConnectId);

        final SimpleAutoConnectObject autoConnect =
                objectManager.getSimpleCosemObject(Ek280Defs.AUTO_CONNECT_1.derive(1, acId),
                        SimpleAutoConnectObject.class);

        autoConnect.setMode(AutoConnectModeEnum.getValues()[Integer.parseInt(autoConnectMode)]);

        DlmsDateTime start = dateStringToDlmsDateTime(autoConnectStart);
        System.out.println("AutoConnect start:" + start.toString());

        DlmsDateTime end;
        if ((autoConnectEnd != null) && (autoConnectEnd.length() > 0)) {
            end = dateStringToDlmsDateTime(autoConnectEnd);
        } else {
            end = DlmsDateTime.NOT_SPECIFIED_DATE_TIME;
        }
        autoConnect.setCallingWindow(new TimeWindow[] {new TimeWindow(start, end)});

        String[] destinationList;
        if ((autoConnectDest2 != null) && (autoConnectDest2.length() > 0)) {
            destinationList = new String[] {autoConnectDest1, autoConnectDest2};
        } else {
            destinationList = new String[] {autoConnectDest1};
        }
        autoConnect.setDestinationList(destinationList);
    }


    private void validateMessageData(String autoConnectId, String autoConnectMode,
                                     String autoConnectStart, String autoConnectEnd,
                                     String autoConnectDest1, String autoConnectDest2) throws BusinessException {
        checkInt(autoConnectId, "AutoConnect id", 1, 2);
        checkInt(autoConnectMode, "AutoConnect mode", 1, 2);
        checkRepetitiveDate(autoConnectStart, "AutoConnect start");
        if ((autoConnectEnd != null) && (autoConnectEnd.length() > 0)) {
            checkRepetitiveDate(autoConnectEnd, "AutoConnect end");
        }
        checkDestination(autoConnectDest1, "AutoConnect destination 1");
        if ((autoConnectDest2 != null) && (autoConnectDest2.length() > 0)) {
            checkDestination(autoConnectDest2, "AutoConnect destination 2");
        }
    }

    public void checkDestination(String dest, String name) throws BusinessException {

        Pattern pattern = Pattern.compile("^(" + ValidIpAddressRegex + "|" + ValidHostnameRegex + ")" + OptionalPort + "$");
        if (!pattern.matcher(dest).matches()) {
            throw new BusinessException(name + ": error in definition");
        }
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_AUTOCONNECT_ID, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_AUTOCONNECT_MODE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_AUTOCONNECT_START, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_AUTOCONNECT_END, false));
        tagSpec.add(new MessageAttributeSpec(ATTR_DESTINATION1, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_DESTINATION2, false));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
