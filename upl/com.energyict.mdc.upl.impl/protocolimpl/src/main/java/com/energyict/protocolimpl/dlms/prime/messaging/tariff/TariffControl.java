package com.energyict.protocolimpl.dlms.prime.messaging.tariff;

import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.dlms.prime.messaging.PrimeMessageExecutor;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.B04;
import com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml.Contract;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 27/02/12
 * Time: 14:32
 */
public class TariffControl extends PrimeMessageExecutor {

    private static final String WRITE_CONTRACTS = "WriteContracts";
    private static final String WRITE_DEMAND_CONTROL_THRESHOLD = "WriteControlThresholds";
    private static final String START_TAG = "<" + WRITE_CONTRACTS + ">";
    private static final String END_TAG = "</" + WRITE_CONTRACTS + ">";

    private static final ObisCode CONTRACT_THR_BASE_OBIS = ObisCode.fromString("0.1.94.34.0.255");
    private static final ObisCode ACTIVITY_CALENDAR_OBIS = ObisCode.fromString("0.0.13.0.1.255");

    public TariffControl(DlmsSession session) {
        super(session);
    }

    public static MessageCategorySpec getCategorySpec() {
        MessageCategorySpec spec = new MessageCategorySpec("Write contracts");

        spec.addMessageSpec(addBasicMsg("Write the contracts from XML file", WRITE_CONTRACTS, true));

        spec.addMessageSpec(
                addBasicMsgWithAttributes(
                        "Write demand control thresholds", WRITE_DEMAND_CONTROL_THRESHOLD, true,
                        "Threshold 1 (unit W)",
                        "Threshold 2 (unit W)",
                        "Threshold 3 (unit W)",
                        "Threshold 4 (unit W)",
                        "Threshold 5 (unit W)",
                        "Threshold 6 (unit W)",
                        "ActivationDate"
                )
        );

        return spec;
    }

    public boolean canHandle(MessageEntry messageEntry) {
        return isMessageTag(WRITE_CONTRACTS, messageEntry) || isMessageTag(WRITE_DEMAND_CONTROL_THRESHOLD, messageEntry);
    }

    public final MessageResult execute(MessageEntry messageEntry) throws IOException {
        try {

            if (isMessageTag(WRITE_CONTRACTS, messageEntry)) {
                return writeContracts(messageEntry);
            } else if (isMessageTag(WRITE_DEMAND_CONTROL_THRESHOLD, messageEntry)) {
                return writeDemandControlThresholds(messageEntry);
            }

        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "An error occurred while handling message [" + messageEntry.getContent() + "]: " + e.getMessage(), e);
            return MessageResult.createFailed(messageEntry);
        }
        getLogger().severe("Unable to handle message [" + messageEntry.getContent() + "] in [" + getClass().getSimpleName() + "]");
        return MessageResult.createFailed(messageEntry);
    }

    private MessageResult writeDemandControlThresholds(MessageEntry messageEntry) throws IOException {
        getLogger().info("Writing demand control thresholds");

        final String messageContent = messageEntry.getContent();
        final CosemObjectFactory cof = getSession().getCosemObjectFactory();

        for (int tariff = 1; tariff <= 6; tariff++) {
            final String attributeValue = getAttributeValue("Threshold " + tariff + " (unit W)", messageContent);
            if (attributeValue == null) {
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "No need to write threshold [" + tariff + "]. Skipping.");
                }
                continue;
            }

            try {
                final int thresholdValue = Integer.valueOf(attributeValue);
                final ObisCode correctObisCode = ProtocolTools.setObisCodeField(CONTRACT_THR_BASE_OBIS, 4, (byte) (10 + tariff));
                if (getLogger().isLoggable(Level.INFO)) {
                    getLogger().log(Level.INFO, "Writing new value [" + thresholdValue + "] to threshold tariff [" + tariff + "] in object with obisCode [" + correctObisCode + "].");
                }
                final Unsigned32 unsigned32Value = new Unsigned32(thresholdValue);
                cof.getRegister(correctObisCode).setValueAttr(unsigned32Value);
            } catch (NumberFormatException e) {
                throw new NestedIOException(e, "Unable to write threshold [" + tariff + "]! Value [" + attributeValue + "] is invalid.");
            }

        }

        final String activationDateAttr = getAttributeValue("ActivationDate", messageContent);

        if (activationDateAttr != null) {

            try {
                final long activationDate = Long.valueOf(activationDateAttr);
                getLogger().info("Writing activation time of [" + activationDate + "], [" + new Date(activationDate) + "] for the demand control thresholds.");

                final Calendar cal = Calendar.getInstance(getSession().getTimeZone());
                cal.setTimeInMillis(activationDate);
                final OctetString activationOctetString = new OctetString(new AXDRDateTime(cal).getBEREncodedByteArray(), 0);

                final ActivityCalendar activityCalendar = cof.getActivityCalendar(ACTIVITY_CALENDAR_OBIS);
                activityCalendar.writeActivatePassiveCalendarTime(activationOctetString);
                getLogger().info("Successfully wrote the activation time for the demand control thresholds (on the active calendar)");
            } catch (NumberFormatException e) {
                throw new NestedIOException(e, "Invalid activation time format [" + activationDateAttr + "]!" + e.getMessage());
            }

        } else {
            getLogger().info("No activation date found for demand control thresholds. Skipping activation!");
        }

        getLogger().info("Successfully wrote the demand control thresholds");
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeContracts(MessageEntry messageEntry) throws IOException {
        getLogger().info("Writing contract information from XML...");

        int startIndex = messageEntry.getContent().indexOf(START_TAG) + START_TAG.length();
        int endIndex = messageEntry.getContent().indexOf(END_TAG);

        final String b64Content = messageEntry.getContent().substring(startIndex, endIndex);
        final B04 b04 = ProtocolTools.deserializeFromBase64(b64Content);
        final List<Contract> contracts = b04.getContracts();
        for (final Contract contract : contracts) {
            getLogger().info("Writing contract [" + contract.getC() + "]");
            writeContract(contract);
        }

        return MessageResult.createSuccess(messageEntry);
    }

    /**
     * Write the given contract to the meter and activate it
     *
     * @param contract The contract to write to the meter
     * @throws java.io.IOException If there went something wrong
     */
    private void writeContract(final Contract contract) throws IOException {
        final PrimeActivityCalendarController activityCalendarController = new PrimeActivityCalendarController(getSession().getCosemObjectFactory(), getSession().getTimeZone());
        activityCalendarController.parseContent(contract);
        activityCalendarController.writeCalendarName();
        activityCalendarController.writeSpecialDaysTable();
        activityCalendarController.writeAndActivateCalendar();
    }

}