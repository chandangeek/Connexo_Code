package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.SecurityMessage;

/**
 * Copyrights EnergyICT
 * Date: 2/03/11
 * Time: 16:40
 */
public class ActivateTemporaryKeyMessage extends AbstractMTU155Message {

    private static final String OBJECT_ID = "11.0.C";

    private static final int MIN_ACTIVE_TIME = 0;
    private static final int MAX_ACTIVE_TIME = 255;

    public ActivateTemporaryKeyMessage(Messaging messaging, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(messaging, collectedDataFactory, issueFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getSpecification().getId() == SecurityMessage.ACTIVATE_DEACTIVATE_TEMPORARY_ENCRYPTION_KEY.id();
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        Boolean keyTActivationStatus = SecurityMessage.KeyTUsage.fromDescription(getDeviceMessageAttribute(message, DeviceMessageConstants.keyTActivationStatusAttributeName).getValue());
        String activationTimeDurationString = getDeviceMessageAttribute(message, DeviceMessageConstants.SecurityTimeDurationAttributeName).getValue();
        int activationTimeDuration = validateActivationTimeDuration(activationTimeDurationString);

        if (keyTActivationStatus != null) {
            activatingOrDeactivatingKeyT(keyTActivationStatus, activationTimeDuration);
        }
        return null;
    }

    private void activatingOrDeactivatingKeyT(boolean keyTActivationStatus, int activationTimeDuration) throws CTRException {
        getLogger().warning("Changing KeyT activation status to [" + keyTActivationStatus + "] for a period of [" + activationTimeDuration + "]");
        getFactory().executeRequest(new CTRObjectID(OBJECT_ID), createRawData(keyTActivationStatus, activationTimeDuration));
        getLogger().warning("Successfully changed KeyT activation status to [" + keyTActivationStatus + "] for a period of [" + activationTimeDuration + "]");
    }

    private int validateActivationTimeDuration(String activationTimeDurationAttr) throws CTRException {
        int activationTimeDuration = Integer.valueOf(activationTimeDurationAttr);
        if ((activationTimeDuration < MIN_ACTIVE_TIME) && (activationTimeDuration > MAX_ACTIVE_TIME)) {
            String msg = "Invalid value [" + activationTimeDurationAttr + "] for the time duration. Value should be between " + MIN_ACTIVE_TIME + " and " + MAX_ACTIVE_TIME + ".";
            throw new CTRException(msg);
        }
        return activationTimeDuration;
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
}
