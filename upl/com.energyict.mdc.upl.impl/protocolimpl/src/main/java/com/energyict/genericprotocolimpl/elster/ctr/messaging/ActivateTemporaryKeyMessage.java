package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;

import static com.energyict.protocolimpl.utils.ProtocolTools.getBooleanFromString;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class ActivateTemporaryKeyMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "11.0.C";

    private static final String MESSAGE_TAG = "ActivateTemporaryKey";
    private static final String MESSAGE_DESCRIPTION = "Activate/deactivate the temporary encryption key";

    private static final String ATTR_ACTIVATION_STATUS_KEYT = "ActivationStatusKeyT";
    private static final String ATTR_ACTIVATED_TIME = "ActivatedTime";
    private static final int MIN_ACTIVE_TIME = 0;
    private static final int MAX_ACTIVE_TIME = 255;

    public ActivateTemporaryKeyMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String activationStatusKeyTAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_ACTIVATION_STATUS_KEYT);
        String activatedTimeAttr = MessagingTools.getContentOfAttribute(messageEntry, ATTR_ACTIVATED_TIME);

        boolean keyTEnabled = validateActivation(activationStatusKeyTAttr);
        int activatedTime = validateActivatedTime(activatedTimeAttr);

        try {
            getLogger().warning("Changing KeyT activation status to [" + keyTEnabled + "] for a period of [" + activatedTime + "]");
            getFactory().executeRequest(new CTRObjectID(OBJECT_ID), createRawData(keyTEnabled, activatedTime));
            getLogger().warning("Successfully changed KeyT activation status to [" + keyTEnabled + "] for a period of [" + activatedTime + "]");
        } catch (CTRException e) {
            throw new BusinessException("Unable to (de)activate the temporary key: " + e.getMessage());
        }

    }

    /**
     * Validate the activated time. If the value is null or "-", the default '0' value is returned.
     *
     * @param activatedTimeAttr
     * @return
     * @throws BusinessException
     */
    private int validateActivatedTime(String activatedTimeAttr) throws BusinessException {
        int activeTime = 0;
        if ((activatedTimeAttr == null) || (activatedTimeAttr.trim().equalsIgnoreCase("-"))) {
            return activeTime;
        }
        activatedTimeAttr = activatedTimeAttr.trim();
        try {
            activeTime = Integer.valueOf(activatedTimeAttr);
        } catch (NumberFormatException e) {
            throw new BusinessException("Attribute [" + ATTR_ACTIVATED_TIME + "] should be a number and not [" + activatedTimeAttr + "]. " + e.getMessage());
        }
        if ((activeTime < MIN_ACTIVE_TIME) && (activeTime > MAX_ACTIVE_TIME)) {
            throw new BusinessException("Invalid value [" + activatedTimeAttr + "] for the attribute [" + ATTR_ACTIVATED_TIME + "]. Value should lay between " + MIN_ACTIVE_TIME + " and " + MAX_ACTIVE_TIME + ".");
        }
        return activeTime;
    }

    /**
     * Valiedate the activation status. This should be a boolean
     *
     * @param activationStatusKeyTAttr
     * @return
     * @throws BusinessException
     */
    private boolean validateActivation(String activationStatusKeyTAttr) throws BusinessException {
        if (activationStatusKeyTAttr == null) {
            throw new BusinessException("Attribute [" + ATTR_ACTIVATION_STATUS_KEYT + "] cannot be null!");
        }
        return getBooleanFromString(activationStatusKeyTAttr);
    }

    /**
     * Create the raw data send to the device using an execute function
     *
     * @param keyTEnabled
     * @param activatedTime
     * @return
     */
    private byte[] createRawData(boolean keyTEnabled, int activatedTime) {
        byte[] rawData = new byte[2];
        rawData[0] = (keyTEnabled ? (byte) 0x01 : (byte) 0x10);
        rawData[1] = (byte) activatedTime;
        return rawData;
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_ACTIVATION_STATUS_KEYT, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_ACTIVATED_TIME, false));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
