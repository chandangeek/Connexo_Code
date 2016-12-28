package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;

import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.dlms.g3.messaging.G3Messaging;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.PlcOfdmMacSetupMessages;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.SixLoWPanMessages;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.WritePlcG3TimeoutMessage;
import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * <p/>
 * This class is a combination of the G3 PLC message functionality (using the annotated framework) and the
 * DSMR4.0 messages (using the old XML framework).
 *
 * @author khe
 * @since 5/06/2014 - 14:38
 */
public class AM540Messaging extends G3Messaging {

    /**
     * The annotated messages. These all relate to the G3 PLC configuration.
     * Note that the other messages are provided by the Dsmr40Messaging class.
     */
    @SuppressWarnings("unchecked")
    public static final Class<? extends AnnotatedMessage>[] ANNOTATED_MESSAGES = new Class[]{

            // Misc. messages
            WritePlcG3TimeoutMessage.class,

            // PLC OFDM MAC setup messages
            PlcOfdmMacSetupMessages.SetTMRTTL.class,
            PlcOfdmMacSetupMessages.SetMaxFrameRetries.class,
            PlcOfdmMacSetupMessages.SetNeighbourTableEntryTTL.class,
            PlcOfdmMacSetupMessages.SetHighPriorityWindowSize.class,
            PlcOfdmMacSetupMessages.SetCSMAFairnessLimit.class,
            PlcOfdmMacSetupMessages.SetBeaconRandomizationWindowLength.class,
            PlcOfdmMacSetupMessages.SetMacA.class,
            PlcOfdmMacSetupMessages.SetMacK.class,
            PlcOfdmMacSetupMessages.SetMinimumCWAttempts.class,
            PlcOfdmMacSetupMessages.SetMaxBe.class,
            PlcOfdmMacSetupMessages.SetMaxCSMABackOff.class,
            PlcOfdmMacSetupMessages.SetMinBe.class,

            // 6LoWPan layer setup messages
            SixLoWPanMessages.SetMaxHopsMessage.class,
            SixLoWPanMessages.SetWeakLQIValueMessage.class,
            SixLoWPanMessages.SetSecurityLevel.class,
            SixLoWPanMessages.SetRoutingConfiguration.class,
            SixLoWPanMessages.SetBroadcastLogTableEntryTTLMessage.class,
            SixLoWPanMessages.SetMaxJoinWaitTime.class,
            SixLoWPanMessages.SetPathDiscoveryTime.class,
            SixLoWPanMessages.SetMetricType.class,
            SixLoWPanMessages.SetCoordShortAddress.class,
            SixLoWPanMessages.SetDisableDefaultRouting.class,
            SixLoWPanMessages.SetDeviceType.class,
    };

    protected final AM540 protocol;
    private Dsmr40Messaging dsmr40Messaging;

    public AM540Messaging(AM540 protocol) {
        this(protocol, ANNOTATED_MESSAGES);
    }

    public AM540Messaging(AM540 protocol, Class<? extends AnnotatedMessage>[] messages) {
        super(protocol.getDlmsSession(), calendarFinder, extractor, messages);
        this.protocol = protocol;
    }

    @Override
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        if (isAnnotatedMessage(messageEntry)) {
            return super.queryMessage(messageEntry);      //Use annotated messages
        } else {
            return getDsmr40Messaging().queryMessage(messageEntry);
        }
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> allMessages = new ArrayList<MessageCategorySpec>();

        //Annotated messages
        allMessages.addAll(super.getAnnotatedMessageCategories());

        //DSMR4.0 XML messsages
        allMessages.addAll(getDsmr40Messaging().getMessageCategories());

        return allMessages;
    }

    /**
     * Check if the given message entry can be executed by the annotated messaging framework
     */
    private boolean isAnnotatedMessage(MessageEntry messageEntry) {
        try {
            return createAnnotatedMessage(messageEntry) != null;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String writeValue(MessageValue value) {
        return getDsmr40Messaging().writeValue(value);
    }

    @Override
    public String writeMessage(Message msg) {
        return getDsmr40Messaging().writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return getDsmr40Messaging().writeTag(tag);
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        getDsmr40Messaging().applyMessages(messageEntries);
    }

    /**
     * Re-uses all the DSMR4.0 messages, except for the ones related to GPRS, MBus, reset and limiter
     */
    private Dsmr40Messaging getDsmr40Messaging() {
        if (dsmr40Messaging == null) {
            dsmr40Messaging = new Dsmr40Messaging(getMessageExecutor());
            dsmr40Messaging.setSupportMBus(false);
            dsmr40Messaging.setSupportGPRS(false);
            dsmr40Messaging.setSupportMeterReset(false);
            dsmr40Messaging.setSupportsLimiter(false);
            dsmr40Messaging.setSupportResetWindow(false);
            dsmr40Messaging.setSupportXMLConfig(false);
        }
        return dsmr40Messaging;
    }

    protected Dsmr50MessageExecutor getMessageExecutor() {
        return new Dsmr50MessageExecutor(protocol);
    }

    @Override
    protected void applyAnnotatedMessages(List<? extends AnnotatedMessage> messages) {
        //Nothing to do here...
    }
}