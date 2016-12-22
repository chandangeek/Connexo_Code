package com.energyict.protocolimpl.dlms.g3.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;

import com.energyict.cbo.ApplicationException;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.g3.G3Clock;
import com.energyict.protocolimpl.dlms.g3.G3ProfileType;
import com.energyict.protocolimpl.dlms.g3.G3Properties;
import com.energyict.protocolimpl.dlms.g3.events.G3Events;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.ContactorMessages;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.ForceSyncClockMessage;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.LoadProfileMessages;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.LogObjectListMessage;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.LogbookMessages;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.PlcOfdmMacSetupMessages;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.SecurityConfigurationMessages;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.SixLoWPanMessages;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.WriteClockMessage;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.WriteConsumerProducerModeMessage;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.WritePlcG3TimeoutMessage;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.WritePlcPskMessage;
import com.energyict.protocolimpl.dlms.g3.messaging.messages.WriteProfileIntervalMessage;
import com.energyict.protocolimpl.dlms.idis.IDISMessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.AnnotatedMessaging;
import com.energyict.protocolimpl.messaging.RtuMessageHandler;
import com.energyict.protocolimpl.messaging.messages.FirmwareUpdateMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 6/29/12
 * Time: 2:48 PM
 */
public class G3Messaging extends AnnotatedMessaging {

    @SuppressWarnings("unchecked")
    public static final Class<? extends AnnotatedMessage>[] MESSAGES = new Class[]{

            // Firmware upgrade message
            FirmwareUpdateMessage.class,

            // Contactor messages
            ContactorMessages.OpenContactorMessage.class,
            ContactorMessages.ArmContactorMessage.class,
            ContactorMessages.CloseContactorMessage.class,

            //Reset logbooks
            LogbookMessages.ResetMainLogbookMessage.class,
            LogbookMessages.ResetCoverLogbookMessage.class,
            LogbookMessages.ResetBreakerLogbookMessage.class,
            LogbookMessages.ResetCommunicationLogbookMessage.class,
            LogbookMessages.ResetVoltageCutLogbookMessage.class,
            LogbookMessages.ResetLqiLogbookMessage.class,

            //Reset loadprofiles
            LoadProfileMessages.ResetActiveImportLPMessage.class,
            LoadProfileMessages.ResetActiveExportLPMessage.class,
            LoadProfileMessages.ResetDailyProfileMessage.class,
            LoadProfileMessages.ResetMonthlyProfileMessage.class,

            // Misc. messages
            LogObjectListMessage.class,
            ForceSyncClockMessage.class,
            WriteClockMessage.class,
            WriteProfileIntervalMessage.class,
            WritePlcG3TimeoutMessage.class,
            WriteConsumerProducerModeMessage.class,
            WritePlcPskMessage.class,

            // PLC Counters messages
            PlcOfdmMacSetupMessages.ResetPlcOfdmMacCountersMessage.class,

            // PLC OFDM MAC setup messages
            PlcOfdmMacSetupMessages.SetToneMaskMessage.class,
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

            // Security messages
            SecurityConfigurationMessages.ChangeAuthenticationLevelMessage.class,
            SecurityConfigurationMessages.ActivateSecurityLevelMessage.class,
            SecurityConfigurationMessages.ChangeAuthenticationKeyMessage.class,
            SecurityConfigurationMessages.ChangeEncryptionKeyMessage.class,
            SecurityConfigurationMessages.ChangeLLSSecretMessage.class,
            SecurityConfigurationMessages.ChangeHLSSecretMessage.class,

            // 6LoWPan layer setup messages
            SixLoWPanMessages.SetMaxHopsMessage.class,
            SixLoWPanMessages.SetWeakLQIValueMessage.class,
            SixLoWPanMessages.SetLowLQIValueMessage.class,
            SixLoWPanMessages.SetHighLQIValueMessage.class,
            SixLoWPanMessages.SetSecurityLevel.class,
            SixLoWPanMessages.SetRoutingConfiguration.class,
            SixLoWPanMessages.SetBroadcastLogTableEntryTTLMessage.class,
            SixLoWPanMessages.SetRoutingTupleTTLMessage.class,
            SixLoWPanMessages.SetMaxJoinWaitTime.class,
            SixLoWPanMessages.SetPathDiscoveryTime.class,
            SixLoWPanMessages.SetMetricType.class,
            SixLoWPanMessages.SetCoordShortAddress.class,
            SixLoWPanMessages.SetDisableDefaultRouting.class,
            SixLoWPanMessages.SetDeviceType.class,
    };

    private static final String FIRMWARE_OPENING_TAG = "<FirmwareUpdate><IncludedFile>";
    private static final String FIRMWARE_UPDATE = "<FirmwareUpdate>";
    private static final String FIRMWARE_CLOSING_TAG = "</IncludedFile></FirmwareUpdate>";
    private static final ObisCode ACTIVITY_CALENDAR_OBISCODE = ObisCode.fromString("0.0.13.0.0.255");
    private static final ObisCode SPECIAL_DAYS_TABLE_OBISCODE = ObisCode.fromString("0.0.11.0.0.255");
    private static final String PROVIDER = "Provider";
    private static final String PUBLIC_NETWORK = "PublicNetwork";
    private static final String PLC = "plc";
    private static final String NORESUME = "noresume";
    private static final ObisCode PLC_G3_TIMEOUT_OBISCODE = ObisCode.fromString("0.0.94.33.10.255");
    private static final ObisCode PRODUCER_CONSUMER_MODE_OBISCODE = ObisCode.fromString("1.0.96.63.11.255");

    protected DlmsSession session;
    private G3Properties properties;

    public G3Messaging(final DlmsSession session, G3Properties properties) {
        this(session, MESSAGES);
        this.properties = properties;
    }

    public G3Messaging(final DlmsSession session, final Class<? extends AnnotatedMessage>... messages) {
        super(session != null ? session.getLogger() : null, messages);
        this.session = session;
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> messageCategories = getAnnotatedMessageCategories();
        messageCategories.add(new DummyGenericMessaging().getActivityCalendarCategory(PROVIDER));
        messageCategories.add(new DummyGenericMessaging().getActivityCalendarCategory(PUBLIC_NETWORK));
        return messageCategories;
    }

    protected List<MessageCategorySpec> getAnnotatedMessageCategories() {
        return super.getMessageCategories();
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        if (msgTag.getName().contains(RtuMessageConstant.TOU_ACTIVITY_CAL) || msgTag.getName().contains(RtuMessageConstant.TOU_SPECIAL_DAYS)) {
            StringBuilder builder = new StringBuilder();

            // a. Opening tag
            builder.append("<");
            builder.append(msgTag.getName());
            builder.append(">");

            String name = "";
            String activationDate = "1";
            int codeId = 0;

            // b. Attributes
            for (MessageAttribute att : msgTag.getAttributes()) {
                if (RtuMessageConstant.TOU_ACTIVITY_NAME.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        name = att.getValue();
                    }
                } else if (RtuMessageConstant.TOU_ACTIVITY_DATE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        activationDate = att.getValue();
                    }
                } else if (RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        codeId = Integer.valueOf(att.getValue());
                    }
                }
            }

            Date actDate = new Date(Long.valueOf(activationDate));
            if (codeId > 0) {
                try {
                    if (msgTag.getName().contains(RtuMessageConstant.TOU_ACTIVITY_CAL) && Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().after(actDate)) {
                        throw new ApplicationException("Invalid activation date, should be in the future");
                    }
                    String xmlContent = CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(codeId, actDate.getTime(), name);
                    addChildTag(builder, IDISMessageHandler.RAW_CONTENT, ProtocolTools.compress(xmlContent));
                } catch (ParserConfigurationException e) {
                    getLogger().severe(e.getMessage());
                } catch (IOException e) {
                    getLogger().severe(e.getMessage());
                }
            }

            // d. Closing tag
            builder.append("</");
            builder.append(msgTag.getName());
            builder.append(">");
            return builder.toString();
        } else {
            return super.writeTag(msgTag);
        }
    }

    /**
     * Adds a child tag to the given {@link StringBuffer}.
     *
     * @param buf     The string builder to whose contents the child tag needs to be added.
     * @param tagName The name of the child tag to add.
     * @param value   The contents (value) of the tag.
     */
    protected void addChildTag(StringBuilder buf, String tagName, Object value) {
        buf.append(System.getProperty("line.separator"));
        buf.append("<");
        buf.append(tagName);
        buf.append(">");
        buf.append(value);
        buf.append("</");
        buf.append(tagName);
        buf.append(">");
    }

    @Override
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        if (isFirmwareUpgrade(messageEntry)) {
            //Manual parsing, no annotated messaging available yet for firmware upgrade message...
            return executeFirmwareUpgrade(messageEntry);
        } else if (isWritePublicNetworkSpecialDays(messageEntry)) {
            return writeSpecialDays(messageEntry, 2);
        } else if (isWritePublicNetworkActivityCalendar(messageEntry)) {
            return writeActivityCalendar(messageEntry, 2);
        } else if (isWriteProviderSpecialDays(messageEntry)) {
            return writeSpecialDays(messageEntry, 1);
        } else if (isWriteProviderActivityCalendar(messageEntry)) {
            return writeActivityCalendar(messageEntry, 1);
        } else {
            return super.queryMessage(messageEntry);      //Use annotated messages
        }
    }

    private boolean isFirmwareUpgrade(MessageEntry messageEntry) {
        return messageEntry.getContent().contains(FIRMWARE_UPDATE);
    }

    private boolean isWriteProviderSpecialDays(MessageEntry messageEntry) {
        return messageEntry.getContent().contains(PROVIDER + RtuMessageConstant.TOU_SPECIAL_DAYS);
    }

    private boolean isWriteProviderActivityCalendar(MessageEntry messageEntry) {
        return messageEntry.getContent().contains(PROVIDER + RtuMessageConstant.TOU_ACTIVITY_CAL);
    }

    private boolean isWritePublicNetworkSpecialDays(MessageEntry messageEntry) {
        return messageEntry.getContent().contains(PUBLIC_NETWORK + RtuMessageConstant.TOU_SPECIAL_DAYS);
    }

    private boolean isWritePublicNetworkActivityCalendar(MessageEntry messageEntry) {
        return messageEntry.getContent().contains(PUBLIC_NETWORK + RtuMessageConstant.TOU_ACTIVITY_CAL);
    }

    @Override
    public void applyMessages(List messageEntries) throws IOException {
        List<AnnotatedMessage> annotatedMessages = new ArrayList<>(messageEntries.size());
        for (Object msgObject : messageEntries) {
            if (msgObject instanceof MessageEntry) {
                MessageEntry messageEntry = (MessageEntry) msgObject;
                try {
                    if (!isTimeOfUseMessage(messageEntry)) {
                        AnnotatedMessage annotatedMessage = createAnnotatedMessage(messageEntry);
                        annotatedMessages.add(annotatedMessage);
                    }
                } catch (IOException e) {
                    getLogger().severe("Unable to create annotated message from message entry with content [" + messageEntry.getContent() + "]! " + e.getMessage());
                }
            }
        }
        applyAnnotatedMessages(annotatedMessages);
    }

    private boolean isTimeOfUseMessage(MessageEntry messageEntry) {
        return isWriteProviderActivityCalendar(messageEntry) || isWriteProviderSpecialDays(messageEntry) || isWritePublicNetworkActivityCalendar(messageEntry) || isWritePublicNetworkSpecialDays(messageEntry);
    }

    private MessageResult writeActivityCalendar(MessageEntry messageEntry, int bField) throws IOException {
        ObisCode activityCalendarObisCode = ProtocolTools.setObisCodeField(ACTIVITY_CALENDAR_OBISCODE, 1, (byte) bField);
        ObisCode specialDaysCalendarObisCode = ProtocolTools.setObisCodeField(SPECIAL_DAYS_TABLE_OBISCODE, 1, (byte) bField);
        ActivityCalendarController activityCalendarController = new G3ActivityCalendarController(session.getCosemObjectFactory(), session.getTimeZone(), activityCalendarObisCode, specialDaysCalendarObisCode);
        activityCalendarController.parseContent(messageEntry.getContent());
        activityCalendarController.writeCalendarName("");
        activityCalendarController.writeCalendar();
        getLogger().log(Level.INFO, "Activity calendar was successfully written");
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeSpecialDays(MessageEntry messageEntry, int bField) throws IOException {
        ObisCode activityCalendarObisCode = ProtocolTools.setObisCodeField(ACTIVITY_CALENDAR_OBISCODE, 1, (byte) bField);
        ObisCode specialDaysCalendarObisCode = ProtocolTools.setObisCodeField(SPECIAL_DAYS_TABLE_OBISCODE, 1, (byte) bField);
        ActivityCalendarController activityCalendarController = new G3ActivityCalendarController(session.getCosemObjectFactory(), session.getTimeZone(), activityCalendarObisCode, specialDaysCalendarObisCode);
        activityCalendarController.parseContent(messageEntry.getContent());
        activityCalendarController.writeSpecialDaysTable();
        getLogger().log(Level.INFO, "Special days were successfully written");
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult executeFirmwareUpgrade(final MessageEntry messageEntry) throws IOException {

        ObisCode imageTransferObisCode = ObisCode.fromString("0.0.44.0.0.255");
        String trackingId = messageEntry.getTrackingId();
        boolean resume = true;     //Default always resume
        if (shouldUseSecondInterface(trackingId)) {
            imageTransferObisCode = ProtocolTools.setObisCodeField(imageTransferObisCode, 1, (byte) 128);//Upgrade G3 PLC firmware, other obiscode
        }
        if ((trackingId != null) && trackingId.toLowerCase().contains(NORESUME)) {
            resume = false;
        }

        String[] splitContent = messageEntry.getContent().split(FIRMWARE_OPENING_TAG);
        if (!(splitContent.length > 1)) {
            return MessageResult.createFailed(messageEntry, "Firmware upgrade failed, invalid message content");
        }
        String[] splitContent2 = splitContent[1].split(FIRMWARE_CLOSING_TAG);
        if (!(splitContent2.length > 0)) {
            return MessageResult.createFailed(messageEntry, "Firmware upgrade failed, invalid message content");
        }

        this.session.getLogger().info("Sending firmware upgrade message");
        byte[] firmwareBytes = new Base64EncoderDecoder().decode(splitContent2[0]);
        ImageTransfer imageTransfer = this.session.getCosemObjectFactory().getImageTransfer(imageTransferObisCode);
        imageTransfer.setUsePollingVerifyAndActivate(true);         //Use polling to check the result of the image verification
        if (resume) {
            int lastTransferredBlockNumber = imageTransfer.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                imageTransfer.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }

        //Start the actual transfer
        if (!imageTransfer.isResume()) {
            imageTransfer.writeImageTransferEnabledState(true);
        }
        if (imageTransfer.getImageTransferEnabledState().getState()) {
            imageTransfer.initializeAndTransferBlocks(firmwareBytes, false, getImageIdentifier(firmwareBytes));

            //Flush the inputstream to get rid of unused action-result ACK's (timed out attempts that still gave a unexpected response)
            getLogger().log(Level.FINE, "Flushing the inputstream...");
            session.flushInputStream();

            //Go on with the other steps
            imageTransfer.checkAndSendMissingBlocks();
            getLogger().log(Level.INFO, "Verification of image using polling method ...");
            try {
                imageTransfer.verifyAndPollForSuccess();
            } catch (DataAccessResultException e) {
                getLogger().log(Level.WARNING, "Verification of image failed: " + e.getMessage());
                return MessageResult.createFailed(messageEntry, e.getMessage());
            }
            getLogger().log(Level.INFO, "Verification of the image was successful at : " + new Date());
            try {
                imageTransfer.setUsePollingVerifyAndActivate(false);    //Don't use polling for the activation, the meter reboots immediately!
                this.session.getLogger().info("Starting image activation...");
                imageTransfer.imageActivation();
            } catch (IOException e) {
                if (isTemporaryFailure(e) || isTemporaryFailure(e.getCause())) {
                    //Move on in case of temporary failure
                    this.session.getLogger().info("Image activation returned 'temporary failure'. The activation is in progress, moving on.");
                } else if (e.getMessage().toLowerCase().contains("timeout")) {
                    this.session.getLogger().info("Image activation timed out, meter is rebooting. Moving on.");
                } else {
                    throw e;
                }
            }
        } else {
            throw new IOException("Could not perform the upgrade because meter does not allow it.");
        }

        this.session.getLogger().info("Executed image activation successfully, meter will reboot");
        this.session.getLogger().info("Firmware upgrade message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected boolean shouldUseSecondInterface(String trackingId) {
        return (trackingId != null) && trackingId.toLowerCase().contains(PLC);
    }

    /**
     * Fetch the image identifier (e.g. ASP10.11.01-11670) from the image
     *
     * @param firmwareBytes
     * @return
     */
    protected String getImageIdentifier(byte[] firmwareBytes) {
        byte length = firmwareBytes[0]; //Should be shorter than 31 bytes
        byte[] idBytes = ProtocolTools.getSubArray(firmwareBytes, 1, 1 + length);
        return new String(idBytes);     //ASCII representation of the identifier is found at the start of the image bytes
    }

    private boolean isTemporaryFailure(Throwable e) {
        if (e == null) {
            return false;
        } else if (e instanceof DataAccessResultException) {
            return (((DataAccessResultException) e).getDataAccessResult() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode());
        } else {
            return false;
        }
    }

    @RtuMessageHandler
    public final MessageResult closeContactor(final ContactorMessages.CloseContactorMessage message) throws IOException {
        final DisconnectControl disconnectControl = new DisconnectControl(this.session);
        disconnectControl.close();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult armContactor(final ContactorMessages.ArmContactorMessage message) throws IOException {
        final DisconnectControl disconnectControl = new DisconnectControl(this.session);
        disconnectControl.arm();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult openContactor(final ContactorMessages.OpenContactorMessage message) throws IOException {
        final DisconnectControl disconnectControl = new DisconnectControl(this.session);
        disconnectControl.open();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetMainLogbook(final LogbookMessages.ResetMainLogbookMessage message) throws IOException {
        getLogger().info("Received [ResetMainLogbookMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3Events.MAIN_LOG).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetCoverLogbook(final LogbookMessages.ResetCoverLogbookMessage message) throws IOException {
        getLogger().info("Received [ResetCoverLogbookMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3Events.COVER_LOG).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetBreakerLogbook(final LogbookMessages.ResetBreakerLogbookMessage message) throws IOException {
        getLogger().info("Received [ResetBreakerLogbookMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3Events.BREAKER_LOG).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetCommunicationLogbook(final LogbookMessages.ResetCommunicationLogbookMessage message) throws IOException {
        getLogger().info("Received [ResetCommunicationLogbookMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3Events.COMMUNICATION_LOG).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetVoltageCutLogbook(final LogbookMessages.ResetVoltageCutLogbookMessage message) throws IOException {
        getLogger().info("Received [ResetVoltageCutLogbookMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3Events.VOLTAGE_CUT_LOG).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetLqiLogbook(final LogbookMessages.ResetLqiLogbookMessage message) throws IOException {
        getLogger().info("Received [ResetLqiLogbookMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3Events.LQI_EVENT_LOG).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetActiveImportLP(final LoadProfileMessages.ResetActiveImportLPMessage message) throws IOException {
        getLogger().info("Received [ResetActiveImportLPMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3ProfileType.IMPORT_ACTIVE_POWER_PROFILE.getObisCode()).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetActiveExportLP(final LoadProfileMessages.ResetActiveExportLPMessage message) throws IOException {
        getLogger().info("Received [ResetActiveExportLPMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3ProfileType.EXPORT_ACTIVE_POWER_PROFILE.getObisCode()).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetDailyLP(final LoadProfileMessages.ResetDailyProfileMessage message) throws IOException {
        getLogger().info("Received [ResetDailyProfileMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3ProfileType.DAILY_PROFILE.getObisCode()).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetMonthlyLP(final LoadProfileMessages.ResetMonthlyProfileMessage message) throws IOException {
        getLogger().info("Received [ResetMonthlyProfileMessage]. Resetting.");
        session.getCosemObjectFactory().getProfileGeneric(G3ProfileType.MONTHLY_PROFILE.getObisCode()).reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult resetPlcMacCounters(final PlcOfdmMacSetupMessages.ResetPlcOfdmMacCountersMessage message) throws IOException {
        getLogger().info("Received [ResetPlcOfdmMacCountersMessage]. Resetting.");
        this.session.getCosemObjectFactory().getPLCOFDMType2PHYAndMACCounters().reset();
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult changeHLSSecret(SecurityConfigurationMessages.ChangeHLSSecretMessage message) throws IOException {
        getLogger().info("Received [ChangeHLSSecretMessage]. Writing new value of [" + message.getHLSSecret() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getAssociationLN().changeHLSSecret(ProtocolTools.getBytesFromHexString(message.getHLSSecret()));
        return MessageResult.createSuccess(message.getMessageEntry());       //TODO test: hex input?
    }

    @RtuMessageHandler
    public final MessageResult changeLLSSecret(SecurityConfigurationMessages.ChangeLLSSecretMessage message) throws IOException {
        getLogger().info("Received [ChangeLLSSecretMessage]. Writing new value of [" + message.getLLSSecret() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getAssociationLN().writeSecret(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(message.getLLSSecret(), "")));
        return MessageResult.createSuccess(message.getMessageEntry());     //TODO test: hex input?
    }

    @RtuMessageHandler
    public final MessageResult changeEncryptionKey(SecurityConfigurationMessages.ChangeEncryptionKeyMessage message) throws IOException {
        String wrappedEncryptionKeyString = message.getNewWrappedEncryptionKey();
        String oldGlobalKey = ProtocolTools.getHexStringFromBytes(session.getProperties().getSecurityProvider().getGlobalKey(), "");
        byte[] wrappedEncryptionKey = ProtocolTools.getBytesFromHexString(wrappedEncryptionKeyString, "");
        getLogger().info("Received [ChangeEncryptionKeyMessage], wrapped key is '" + wrappedEncryptionKeyString + "'");
        Array encryptionKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: encryptionKey (global key)
        keyData.addDataType(OctetString.fromByteArray(wrappedEncryptionKey));
        encryptionKeyArray.addDataType(keyData);

        getSecuritySetup().transferGlobalKey(encryptionKeyArray);

        //Update the key in the security provider, it is used instantly
        session.getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(message.getNewEncryptionKey(), ""));

        //Reset frame counter, only if a different key has been written
        if (!oldGlobalKey.equalsIgnoreCase(message.getNewEncryptionKey())) {
            session.getAso().getSecurityContext().setFrameCounter(1);
        }

        return MessageResult.createSuccess(message.getMessageEntry());
    }

    private SecuritySetup getSecuritySetup() throws IOException {
        return this.session.getCosemObjectFactory().getSecuritySetup();
    }

    @RtuMessageHandler
    public final MessageResult changeAuthenticationKey(SecurityConfigurationMessages.ChangeAuthenticationKeyMessage message) throws IOException {
        String wrappedAuthenticationKeyString = message.getNewWrappedAuthenticationKey();
        byte[] wrappedAuthenticationKeysBytes = ProtocolTools.getBytesFromHexString(wrappedAuthenticationKeyString, "");
        getLogger().info("Received [ChangeAuthenticationKeyMessage], wrapped key is '" + wrappedAuthenticationKeyString + "'");
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(wrappedAuthenticationKeysBytes));
        globalKeyArray.addDataType(keyData);

        getSecuritySetup().transferGlobalKey(globalKeyArray);

        //Update the key in the security provider, it is used instantly
        session.getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(message.getNewAuthenticationKey(), ""));

        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult activateSecurityLevel(SecurityConfigurationMessages.ActivateSecurityLevelMessage message) throws IOException {
        getLogger().info("Received [ActivateSecurityLevelMessage]. Writing new value of [" + message.getSecurityLevel() + "].");
        getSecuritySetup().activateSecurity(new TypeEnum(message.getSecurityLevel()));
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult changeAuthenticationLevel(SecurityConfigurationMessages.ChangeAuthenticationLevelMessage message) throws IOException {
        int newAuthLevel = message.getAuthenticationLevel();
        getLogger().info("Received [ChangeAuthenticationLevelMessage]. Writing new value of [" + newAuthLevel + "].");
        if (newAuthLevel < 3 || newAuthLevel > 5) {
            getLogger().warning("Invalid authentication level: " + newAuthLevel + ". Should be 3, 4 or 5");
            return MessageResult.createFailed(message.getMessageEntry());
        }

        Data enexisConfiguration = session.getCosemObjectFactory().getData(ObisCode.fromString("0.1.94.31.3.255"));
        Structure structure = enexisConfiguration.getValueAttr().getStructure();
        BitString bitString = structure.getDataType(1).getBitString();
        if (bitString.get(bitString.getNrOfBits() - (newAuthLevel + 2))) {  //Check if the relevant bit is already set
            return MessageResult.createSuccess(message.getMessageEntry(), "New authenticationLevel is the same as the one that is already configured in the device, new level will not be written.");
        } else {
            bitString.set(bitString.getNrOfBits() - 5, false);      //HLS 3
            bitString.set(bitString.getNrOfBits() - 6, false);      //HLS 4
            bitString.set(bitString.getNrOfBits() - 7, false);      //HLS 5
            bitString.set(bitString.getNrOfBits() - (newAuthLevel + 2), true);
            structure.setDataType(1, bitString);
            enexisConfiguration.setValueAttr(structure);
            return MessageResult.createSuccess(message.getMessageEntry());
        }
    }

    @RtuMessageHandler
    public final MessageResult writeMaxHops(SixLoWPanMessages.SetMaxHopsMessage message) throws IOException {
        getLogger().info("Received [SetMaxHopsMessage]. Writing new value of [" + message.getMaxHops() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxHops(message.getMaxHops());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult writeWeakLQIValue(SixLoWPanMessages.SetWeakLQIValueMessage message) throws IOException {
        getLogger().info("Received [SetWeakLQIValueMessage]. Writing new value of [" + message.getWeakLQIValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeWeakLqiValue(message.getWeakLQIValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult writeLowLQIValue(SixLoWPanMessages.SetLowLQIValueMessage message) throws IOException {
        getLogger().info("Received [SetLowLQIValueMessage]. Writing new value of [" + message.getLowLQIValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeLowLqiValue(message.getLowLQIValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult writeHighLQIValue(SixLoWPanMessages.SetHighLQIValueMessage message) throws IOException {
        getLogger().info("Received [SetHighLQIValueMessage]. Writing new value of [" + message.getHighLQIValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeHighLqiValue(message.getHighLQIValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setSecurityLevel(SixLoWPanMessages.SetSecurityLevel message) throws IOException {
        getLogger().info("Received [SetSecurityLevel]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeSecurityLevel(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setRoutingConfiguration(SixLoWPanMessages.SetRoutingConfiguration message) throws IOException {
        getLogger().info("Received [SetRoutingConfiguration].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeRoutingConfiguration(message.adp_net_traversal_time(),
                message.adp_routing_table_entry_TTL(),
                message.adp_routing_tuple_TTL(),
                message.adp_Kr(),
                message.adp_Km(),
                message.adp_Kc(),
                message.adp_Kq(),
                message.adp_Kh(),
                message.adp_Krt(),
                message.adp_RREQ_retries(),
                message.adp_RREQ_RERR_wait(),
                message.adp_Blacklist_table_entry_TTL(),
                message.adp_unicast_RREQ_gen_enable(),
                message.adp_RLC_enabled(),
                message.adp_add_rev_link_cost()
        );

        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult writeBroadcastLogTableEntryTTL(SixLoWPanMessages.SetBroadcastLogTableEntryTTLMessage message) throws IOException {
        getLogger().info("Received [SetBroadcastLogTableEntryTTLMessage]. Writing new value of [" + message.getBroadcastLogTableEntryTTL() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeBroadcastLogTableTTL(message.getBroadcastLogTableEntryTTL());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult writeRoutingTupleTTLEntryTTL(SixLoWPanMessages.SetRoutingTupleTTLMessage message) throws IOException {
        getLogger().info("Received [SetRoutingTupleTTLMessage]. Writing new value of [" + message.getRoutingTupleTTL() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeRoutingTupleTTL(message.getRoutingTupleTTL());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setMaxJoinWaitTime(SixLoWPanMessages.SetMaxJoinWaitTime message) throws IOException {
        getLogger().info("Received [SetMaxJoinWaitTime]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxJoinWaitTime(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setPathDiscoveryTime(SixLoWPanMessages.SetPathDiscoveryTime message) throws IOException {
        getLogger().info("Received [SetPathDiscoveryTime]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writePathDiscoveryTime(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setMetricType(SixLoWPanMessages.SetMetricType message) throws IOException {
        getLogger().info("Received [SetMetricType]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMetricType(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setCoordShortAddress(SixLoWPanMessages.SetCoordShortAddress message) throws IOException {
        getLogger().info("Received [SetCoordShortAddress]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeCoordShortAddress(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setDisableDefaultRouting(SixLoWPanMessages.SetDisableDefaultRouting message) throws IOException {
        getLogger().info("Received [SetDisableDefaultRouting]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDisableDefaultRouting(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setDeviceType(SixLoWPanMessages.SetDeviceType message) throws IOException {
        getLogger().info("Received [SetDeviceType]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDeviceType(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setToneMaskMessage(PlcOfdmMacSetupMessages.SetToneMaskMessage message) throws IOException {
        getLogger().info("Received [SetToneMaskMessage].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeToneMask(message.getToneMask());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setTMRTTL(PlcOfdmMacSetupMessages.SetTMRTTL message) throws IOException {
        getLogger().info("Received [SetTMRTTL]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeTMRTTL(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setMaxFrameRetries(PlcOfdmMacSetupMessages.SetMaxFrameRetries message) throws IOException {
        getLogger().info("Received [SetMaxFrameRetries]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxFrameRetries(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setNeighbourTableEntryTTL(PlcOfdmMacSetupMessages.SetNeighbourTableEntryTTL message) throws IOException {
        getLogger().info("Received [SetNeighbourTableEntryTTL]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeNeighbourTableEntryTTL(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setHighPriorityWindowSize(PlcOfdmMacSetupMessages.SetHighPriorityWindowSize message) throws IOException {
        getLogger().info("Received [SetHighPriorityWindowSize]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeHighPriorityWindowSize(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setCSMAFairnessLimit(PlcOfdmMacSetupMessages.SetCSMAFairnessLimit message) throws IOException {
        getLogger().info("Received [SetCSMAFairnessLimit]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeCSMAFairnessLimit(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setBeaconRandomizationWindowLength(PlcOfdmMacSetupMessages.SetBeaconRandomizationWindowLength message) throws IOException {
        getLogger().info("Received [SetBeaconRandomizationWindowLength]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeBeaconRandomizationWindowLength(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setMacA(PlcOfdmMacSetupMessages.SetMacA message) throws IOException {
        getLogger().info("Received [SetMacA]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacA(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setMacK(PlcOfdmMacSetupMessages.SetMacK message) throws IOException {
        getLogger().info("Received [SetMacK]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacK(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setMinimumCWAttempts(PlcOfdmMacSetupMessages.SetMinimumCWAttempts message) throws IOException {
        getLogger().info("Received [SetMinimumCWAttempts]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinCWAttempts(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setMaxBe(PlcOfdmMacSetupMessages.SetMaxBe message) throws IOException {
        getLogger().info("Received [SetMaxBe]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxBE(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setMaxCSMABackOff(PlcOfdmMacSetupMessages.SetMaxCSMABackOff message) throws IOException {
        getLogger().info("Received [SetMaxBeSetMaxCSMABackOff]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxCSMABackOff(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult setMinBe(PlcOfdmMacSetupMessages.SetMinBe message) throws IOException {
        getLogger().info("Received [SetMinBe]. Writing new value of [" + message.getValue() + "].");
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinBE(message.getValue());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    /**
     * Force a clock sync by writing the system time to the device
     *
     * @throws java.io.IOException
     */
    @RtuMessageHandler
    public final MessageResult forceClockSync(final ForceSyncClockMessage message) throws IOException {
        getLogger().info("Forcing clock sync to system time [" + new Date() + "] ...");
        new G3Clock(this.session).setTime();
        getLogger().info("Clock successfully synced to system time.");
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult forceClockSync(final WriteClockMessage message) throws IOException {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        formatter.setTimeZone(session.getTimeZone());
        Date date;
        try {
            date = formatter.parse(message.getDateTime());
        } catch (ParseException e) {
            getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            return MessageResult.createFailed(message.getMessageEntry());
        }

        getLogger().info("Setting clock date and time to [" + date.toString() + "] ...");
        new G3Clock(this.session).setTime(date);
        getLogger().info("Clock date and time was successfully set.");
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    /**
     * Writes the interval of the chosen load profile (attribute 4)
     *
     * @throws java.io.IOException mostly timeout errors
     */
    @RtuMessageHandler
    public final MessageResult writeProfileInterval(final WriteProfileIntervalMessage message) throws IOException {
        int intervalInSeconds = message.getIntervalInSeconds();
        ObisCode obisCode = properties.getProfileType().getObisCode();
        getLogger().info("Setting profile interval of LP '" + obisCode + "'to " + intervalInSeconds);
        session.getCosemObjectFactory().getProfileGeneric(obisCode).setCapturePeriodAttr(new Unsigned32(intervalInSeconds));
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult writePlcG3Timeout(final WritePlcG3TimeoutMessage message) throws IOException {
        int timeout = message.getTimeout();
        getLogger().info("Setting PLC G3 timeout to " + timeout + " minutes.");
        session.getCosemObjectFactory().getData(PLC_G3_TIMEOUT_OBISCODE).setValueAttr(new Unsigned16(timeout));
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult writeConsumerProducerMode(final WriteConsumerProducerModeMessage message) throws IOException {
        int mode = message.getMode();
        getLogger().info("Setting mode to " + (mode == 0 ? "consumer (0)" : "consumer/producer (1)"));
        session.getCosemObjectFactory().getData(PRODUCER_CONSUMER_MODE_OBISCODE).setValueAttr(new TypeEnum(mode));
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @RtuMessageHandler
    public final MessageResult writePlcPSK(final WritePlcPskMessage message) throws IOException {
        final byte[] psk = message.getPSK();
        getLogger().info("Writing new PSK [" + ProtocolTools.getHexStringFromBytes(psk, "") + "] to meter.");
        session.getCosemObjectFactory().getG3PlcSetPSK().setKey(psk);
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    /**
     * Read out the object list, and log it in a human readable format
     *
     * @throws java.io.IOException
     */
    @RtuMessageHandler
    public final MessageResult logObjectList(final LogObjectListMessage message) throws IOException {
        getLogger().info("Reading complete object list ...");
        final AssociationLN associationLN = this.session.getCosemObjectFactory().getAssociationLN();
        final UniversalObject[] buffer = associationLN.getBuffer();
        final StringBuilder sb = new StringBuilder();
        sb.append("Meter object list:\n");
        for (final UniversalObject uo : buffer) {
            sb.append("  >  ").append(uo.getDescription()).append('\n');
        }
        getLogger().info(sb.toString());
        return MessageResult.createSuccess(message.getMessageEntry());
    }

    @Override
    protected void applyAnnotatedMessages(List<? extends AnnotatedMessage> messages) {

    }

}
