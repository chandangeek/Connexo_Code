/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.CosemObjectFactory;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Messages concerning the communication setup.
 *
 * @author alex
 */
public final class PasswordSetup extends PrimeMessageExecutor {

    /**
     * Create a new instance.
     *
     * @param session The session.
     */
    public PasswordSetup(DlmsSession session) {
        super(session);
    }

    /**
     * Root tag for setting the multicast addresses.
     */
    private static final String ROOT_TAG_CHANGE_PASSWORDS = "ChangePasswords";

    /**
     * The first address.
     */
    private static final String ATTRIBUTE_READING = "reading";

    /**
     * The second address.
     */
    private static final String ATTRIBUTE_MANAGEMENT = "management";

    /**
     * The third address.
     */
    private static final String ATTRIBUTE_FIRMWARE = "firmware";

    /**
     * Gets the message category for the power quality messages.
     *
     * @return The message category for the power quality messages.
     */
    public static final MessageCategorySpec getCategorySpec() {
        final MessageCategorySpec spec = new MessageCategorySpec("Password setup");

        spec.addMessageSpec(addBasicMsgWithAttributes("Change passwords", ROOT_TAG_CHANGE_PASSWORDS, true, ATTRIBUTE_READING, ATTRIBUTE_MANAGEMENT, ATTRIBUTE_FIRMWARE));

        return spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean canHandle(MessageEntry messageEntry) {
        return isMessageTag(ROOT_TAG_CHANGE_PASSWORDS, messageEntry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final MessageResult execute(final MessageEntry messageEntry) throws IOException {
        final String messageContent = messageEntry.getContent();

        final String readingPassword = getAttributeValue(ATTRIBUTE_READING, messageContent);
        final String managementPassword = getAttributeValue(ATTRIBUTE_MANAGEMENT, messageContent);
        final String firmwarePassword = getAttributeValue(ATTRIBUTE_FIRMWARE, messageContent);

        boolean success = true;
        if (readingPassword != null && readingPassword.length() == 8) {
            success &= writePassword(ObisCode.fromString("0.0.40.0.2.255"), readingPassword);
        }

        if (managementPassword != null && managementPassword.length() == 8) {
            success &= writePassword(ObisCode.fromString("0.0.40.0.3.255"), managementPassword);
        }

        if (firmwarePassword != null && firmwarePassword.length() == 8) {
            success &= writePassword(ObisCode.fromString("0.0.40.0.4.255"), firmwarePassword);
        }

        return success ? MessageResult.createSuccess(messageEntry) : MessageResult.createFailed(messageEntry);
    }

    private final boolean writePassword(final ObisCode obisCode, final String password) {
        getLogger().log(Level.INFO, "Writing new password [" + password + "] to obis [" + obisCode + "].");
        try {
            final CosemObjectFactory objectFactory = getSession().getCosemObjectFactory();
            final AssociationLN associationLN = objectFactory.getAssociationLN(obisCode);
            associationLN.writeSecret(OctetString.fromString(password));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Unable to change LLS secret for association LN [" + obisCode + "] to [" + password + "]", e);
            return false;
        }
        return true;
    }

}
