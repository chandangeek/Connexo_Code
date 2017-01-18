package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Messages concerning the communication setup.
 *
 * @author alex
 */
public final class BasicIntervalSetup extends PrimeMessageExecutor {

    /**
     * Create a new instance.
     *
     * @param session The session.
     */
    public BasicIntervalSetup(DlmsSession session) {
        super(session);
    }

    /**
     * Root tag for setting the multicast addresses.
     */
    private static final String ROOT_TAG_CHANGE_INTERVAL = "ChangeProfileInterval";

    /**
     * The new interval value
     */
    private static final String ATTRIBUTE_INTERVAL = "interval";

    /**
     * The obis code of the profile to change the interval from
     */
    private static final String ATTRIBUTE_OBIS_CODE = "obisCode";

    /**
     * Gets the message category for the power quality messages.
     *
     * @return The message category for the power quality messages.
     */
    public static final MessageCategorySpec getCategorySpec() {
        final MessageCategorySpec spec = new MessageCategorySpec("Profile setup");

        spec.addMessageSpec(addBasicMsgWithAttributes("Change basic profile interval", ROOT_TAG_CHANGE_INTERVAL, true, ATTRIBUTE_INTERVAL, ATTRIBUTE_OBIS_CODE));

        return spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean canHandle(MessageEntry messageEntry) {
        return isMessageTag(ROOT_TAG_CHANGE_INTERVAL, messageEntry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final MessageResult execute(final MessageEntry messageEntry) throws IOException {

        final String intervalAttribute = getAttributeValue(ATTRIBUTE_INTERVAL, messageEntry.getContent());
        final String obisCodeAttribute = getAttributeValue(ATTRIBUTE_OBIS_CODE, messageEntry.getContent());

        final int interval;
        try {
            interval = Integer.parseInt(intervalAttribute);
        } catch (NumberFormatException e) {
            final String msg = "Invalid interval [" + intervalAttribute + " min]: " + e.getMessage();
            getLogger().log(Level.SEVERE, msg, e);
            return MessageResult.createFailed(messageEntry, msg);
        }

        final ObisCode obisCode;
        try {
            obisCode = ObisCode.fromString(obisCodeAttribute);
        } catch (IllegalArgumentException e) {
            final String msg = "Invalid obisCode [" + obisCodeAttribute + "]: " + e.getMessage();
            getLogger().log(Level.SEVERE, msg, e);
            return MessageResult.createFailed(messageEntry, msg);
        }

        try {
            final CosemObjectFactory objectFactory = getSession().getCosemObjectFactory();
            final ProfileGeneric profileGeneric = objectFactory.getProfileGeneric(obisCode);
            final Unsigned32 intervalInSeconds = new Unsigned32(interval);
            profileGeneric.setCapturePeriodAttr(intervalInSeconds);
        } catch (IOException e) {
            final String msg = "Unable to change interval from profile [" + obisCode + "] to [" + interval + " seconds]: " + e.getMessage();
            getLogger().log(Level.SEVERE, msg, e);
            return MessageResult.createFailed(messageEntry, msg);
        }

        final String msg = "Interval from profile [" + obisCode + "] successfully changed to [" + interval + " seconds]";
        getLogger().log(Level.INFO, msg);
        return MessageResult.createSuccess(messageEntry, msg);

    }

    private final boolean writeProfileInterval(final ObisCode obisCode, final String password) {
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
