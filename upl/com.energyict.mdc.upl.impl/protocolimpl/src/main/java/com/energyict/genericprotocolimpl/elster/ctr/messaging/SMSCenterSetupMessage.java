package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimpl.utils.MessagingTools;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:57:19
 */
public class SMSCenterSetupMessage extends AbstractMTU155Message {

    private static final int SMSC_NUMBER_MAX_LENGTH = 14;

    public SMSCenterSetupMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(RtuMessageConstant.SMS_CHANGE_SMSC, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String smscNumber = MessagingTools.getContentOfAttribute(messageEntry, RtuMessageConstant.SMS_SMSC_NUMBER);
        validateParameters(smscNumber);

        // TODO: implement message


    }

    private void validateParameters(String smscNumber) throws BusinessException {
        if (smscNumber == null || "".equals(smscNumber)) {
            throw new BusinessException("SMS center number is missing. SMSCenterSetupMessage needs a SMSC number. [smscNumber='" + smscNumber + "']");
        } else if (smscNumber.trim().length() <= 0) {
            throw new BusinessException("SMS center number is missing. SMSCenterSetupMessage needs a SMSC number. [smscNumber='" + smscNumber + "']");
        } else if (smscNumber.length() > SMSC_NUMBER_MAX_LENGTH) {
            throw new BusinessException("SMS center number is too long. Max length is " + SMSC_NUMBER_MAX_LENGTH + ". [smscNumber='" + smscNumber + "']");
        }
    }
}
