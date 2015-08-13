package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleGprsModemSetupObject;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:27:15
 */
public class ApnSetupMessage extends AbstractDlmsMessage {

    public static final int APN_MAX_LENGTH = 40;
    public static final int PASS_MAX_LENGTH = 30;
    public static final int USER_MAX_LENGTH = 30;

    /**
     * RtuMessage tags for the GPRS modem setup message
     */
    public static final String GPRS_MODEM_SETUP = "GPRS_modem_setup";
    public static final String GPRS_APN = "APN";
    public static final String GPRS_USERNAME = "Username";
    public static final String GPRS_PASSWORD = "Password";

    public ApnSetupMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(GPRS_MODEM_SETUP, messageEntry.getContent());
    }

    /**
     * Send the message (containing the apn configuration) to the meter.
     *
     * @param messageEntry: the message containing the apn configuration
     * @throws com.energyict.cbo.BusinessException:
     *          when a parameter is null or too long
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String apn = MessagingTools.getContentOfAttribute(messageEntry, GPRS_APN);
        String user = MessagingTools.getContentOfAttribute(messageEntry, GPRS_USERNAME);
        String password = MessagingTools.getContentOfAttribute(messageEntry, GPRS_PASSWORD);
        validateApnSetupMessage(apn, user, password);

        try {
            write(apn, user, password);
        } catch (IOException e) {
            throw new BusinessException("Unable to set GPRS modem setup data: " + e.getMessage());
        }
    }

    protected void write(String apn, String user, String password) throws IOException
    {
        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        SimpleGprsModemSetupObject gprsModemSetup = objectManager.getSimpleCosemObject(Ek280Defs.GPRS_MODEM_SETUP,
                                                                                 SimpleGprsModemSetupObject.class);
        gprsModemSetup.setApn(apn);
        gprsModemSetup.setApnUser(user);
        gprsModemSetup.setApnPassword(password);
    }


    protected void validateApnSetupMessage(String apn, String user, String pssw) throws BusinessException {
        if ("".equals(apn) || apn == null) {
            throw new BusinessException("Parameter APN was 'null'.");
        } else if ("".equals(pssw) || pssw == null) {
            throw new BusinessException("Parameter password was 'null'.");
        } else if ("".equals(user) || user == null) {
            throw new BusinessException("Parameter username was 'null'.");
        } else if (apn.length() > APN_MAX_LENGTH) {
            throw new BusinessException("Parameter APN exceeded the maximum length (40 characters).");
        } else if (user.length() > USER_MAX_LENGTH) {
            throw new BusinessException("Parameter username exceeded the maximum length (30 characters).");
        } else if (pssw.length() > PASS_MAX_LENGTH) {
            throw new BusinessException("Parameter password exceeded the maximum length (30 characters).");
        }
    }

    public static MessageSpec getMessageSpec(String messageName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(messageName, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(GPRS_MODEM_SETUP);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        // Add 3 attributes: APN, username and password
        tagSpec.add(new MessageAttributeSpec(GPRS_APN, false));
        tagSpec.add(new MessageAttributeSpec(GPRS_USERNAME, false));
        tagSpec.add(new MessageAttributeSpec(GPRS_PASSWORD, false));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
