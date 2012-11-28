package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Sms;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dialer.core.LinkException;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.CTREncryption;
import com.energyict.genericprotocolimpl.elster.ctr.events.CTRMeterEvent;
import com.energyict.genericprotocolimpl.elster.ctr.exception.*;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.SMSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Function;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.profile.ProfileChannelForSms;
import com.energyict.genericprotocolimpl.elster.ctr.structure.*;
import com.energyict.genericprotocolimpl.elster.ctr.util.CTRObjectInfo;
import com.energyict.genericprotocolimpl.webrtuz3.MeterAmrLogging;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.*;
import com.energyict.mdw.messaging.MessageHandler;
import com.energyict.mdw.shadow.DeviceMessageShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import javax.jms.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Copyrights EnergyICT
 * Date: 20-sep-2010
 * Time: 9:58:20
 */
public class SmsHandler implements MessageHandler {

    private static final String SMS = "SMS";

    private Device rtu;
    private MTU155Properties properties = new MTU155Properties();
    private Logger logger;
    private MeterAmrLogging meterAmrLogging;
    private final Date now = new Date();
    private CtrSmsStoreObject storeObject;
    private ObisCodeMapper obisCodeMapper;
    private Sms sms;

    public Date getNow() {
        return now;
    }

    private MTU155Properties getProtocolProperties() {
        return properties;
    }

    public CtrSmsStoreObject getStoreObject() {
        if (storeObject == null) {
            storeObject = new CtrSmsStoreObject(getProtocolProperties().getChannelBacklog());
        }
        return storeObject;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public Device getRtu() {
        return rtu;
    }

    /**
     * processes a given message containing an sms object
     *
     * @param message: the given message
     * @param logger:  the logger
     * @throws JMSException
     * @throws BusinessException
     * @throws SQLException
     */
    public void processMessage(Message message, Logger logger) throws JMSException, BusinessException, SQLException {
        this.logger = logger;
        ObjectMessage om = (ObjectMessage) message;
        processMessage((Sms) om.getObject());
    }

    /**
     * Processes a given Sms
     *
     * @param sms: the given sms
     * @throws JMSException
     * @throws BusinessException
     * @throws SQLException
     */
    public void processMessage(Sms sms) throws JMSException, BusinessException, SQLException {
        this.sms = sms;
        doExecute();
        storeObject = null; //reset
    }

    /**
     * Log a failed event
     *
     * @param commSchedule
     */
    private void logFailure(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, "0"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_PROTOCOLERROR));
        journal.addAll(getMeterAmrLogging().getJournalEntries());
        try {
            commSchedule.journal(journal);
            commSchedule.logFailure(new Date(), "");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Log a successful event
     *
     * @param commSchedule
     */
    private void logSuccess(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, "0"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_OK));
        journal.addAll(getMeterAmrLogging().getJournalEntries());
        try {
            commSchedule.logSuccess(new Date());
            commSchedule.journal(journal);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    public MeterAmrLogging getMeterAmrLogging() {
        if (meterAmrLogging == null) {
            meterAmrLogging = new MeterAmrLogging();
        }
        return meterAmrLogging;
    }

    /**
     * Processes a given sms frame containing event records, profile data or register data
     *
     * @param smsFrame: the sms frame
     * @throws BusinessException
     * @throws SQLException
     * @throws LinkException
     */
    private boolean processSmsFrame(SMSFrame smsFrame) throws BusinessException, SQLException, LinkException {
        List<CommunicationScheduler> communicationSchedulers = getRtu().getCommunicationSchedulers();
        if (communicationSchedulers.size() == 0) {
            log("Device '" + getRtu().getName() + "' has no CommunicationSchedulers. Skipping.");
        } else {
            for (CommunicationScheduler cs : communicationSchedulers) {
                if (processSMSFrameSingleSchedule(smsFrame, cs)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean processSMSFrameSingleSchedule(SMSFrame smsFrame, CommunicationScheduler cs) {
        boolean csExecuted = false;
        String csName = cs.displayString();
        if (isInboundSmsProfile(cs)) {
            meterAmrLogging = null;
            if (cs.getNextCommunication() == null) {
                log("CommunicationScheduler '" + csName + "' nextCommunication is 'null'. Skipping.");
            } else {
                log("Executing communicationScheduler '" + csName + "'.");
                try {
                    cs.startCommunication();
                    cs.startReadingNow();
                    csExecuted = true;
                    processSchedule(smsFrame, cs.getCommunicationProfile());
                    logSuccess(cs);
                } catch (CTRException e) {
                    getMeterAmrLogging().logInfo(e);
                    logFailure(cs);
                } catch (SQLException e) {
                    getMeterAmrLogging().logInfo(e);
                    logFailure(cs);
                } catch (IOException e) {
                    getMeterAmrLogging().logInfo(e);
                    logFailure(cs);
                } catch (BusinessException e) {
                    getMeterAmrLogging().logInfo(e);
                    logFailure(cs);
                } catch (Exception e) {
                    getMeterAmrLogging().logInfo(e);
                    e.printStackTrace();
                }
            }
        } else {
            log(Level.FINE, "CommunicationScheduler '" + csName + "' is no SMS communication profile. The name should contain '" + SMS + "'. Skipping.");
        }
        return csExecuted;
    }

    /**
     * Decrypt and parse the sms data using the rtu properties
     *
     * @param sms: the sms that needs to be decrypted
     * @return the decrypted and parsed sms frame
     * @throws CTRParsingException
     * @throws CtrCipheringException
     */
    public SMSFrame parseAndDecryptSms(Sms sms) throws CTRParsingException, CtrCipheringException {
        SMSFrame smsFrame = new SMSFrame().parse(sms.getMessage(), 0);
        CTREncryption ctrEncryption = new CTREncryption(properties);
        return (SMSFrame) ctrEncryption.decryptFrame((Frame) smsFrame);
    }

    private String getRtuSerialNumber() {
        return getRtu().getSerialNumber();
    }

    /**
     * Check out the communication profile, see what to do with a given sms
     *
     * @param smsFrame:             the given sms frame
     * @param communicationProfile: the meter's communication profile
     * @throws CTRException
     */
    private void processSchedule(SMSFrame smsFrame, CommunicationProfile communicationProfile) throws CTRException {

        if (communicationProfile == null) {
            throw new CTRConfigurationException("There was no communication profile.");
        }

        smsFrame.doParse();
        String pdr = getRtu().getDialHomeId();

        if (smsFrame.getData() instanceof ArrayEventsQueryResponseStructure) {
            ArrayEventsQueryResponseStructure data = (ArrayEventsQueryResponseStructure) smsFrame.getData();
            parseAndStoreEventArray(communicationProfile, pdr, data);
        } else if (smsFrame.getData() instanceof Trace_CQueryResponseStructure) {
            Trace_CQueryResponseStructure data = (Trace_CQueryResponseStructure) smsFrame.getData();
            parseAndStoreTrace_C(pdr, data, communicationProfile);
        } else if (smsFrame.getData() instanceof TableDECFQueryResponseStructure) {
            TableDECFQueryResponseStructure data = (TableDECFQueryResponseStructure) smsFrame.getData();
            parseAndStoreDECFTableData(communicationProfile, pdr, data);
        } else if (smsFrame.getData() instanceof TableDECQueryResponseStructure) {
            TableDECQueryResponseStructure data = (TableDECQueryResponseStructure) smsFrame.getData();
            parseAndStoreDECTableData(communicationProfile, pdr, data);
        } else if (smsFrame.getData() instanceof AckStructure) {
            AckStructure data = (AckStructure) smsFrame.getData();
            String message = handleACK(data);
            log(message);
            getMeterAmrLogging().logInfo(message);
        } else if (smsFrame.getData() instanceof NackStructure) {
            NackStructure data = (NackStructure) smsFrame.getData();
            String message = handleNACK(data);
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        } else {
            String message = "Unrecognized data structure in SMS. Expected array of event records, trace_C response or tableDEC(F) response.";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
            throw new CTRException(message);
        }

        storeDataToEIServer();
    }

    private String handleACK(AckStructure data) {
        String message;
        int wdb = -1;
        if (data.getFunctionCode().getFunction().equals(Function.WRITE) || data.getFunctionCode().getFunction().equals(Function.SECRET)) {
            wdb = data.getAdditionalData().getBytes()[0];
        } else if (data.getFunctionCode().getFunction().equals(Function.EXECUTE)) {
            wdb = data.getAdditionalData().getBytes()[2];
        }

        if (wdb != -1) {
            DeviceMessageFilter filter = new DeviceMessageFilter();
            filter.setTrackingIdMask("*#" + wdb + "*");
            Set aSet = new HashSet();
            aSet.add(DeviceMessageState.SENT);
            aSet.add(DeviceMessageState.FAILED);
            filter.setStates(aSet);

            List<DeviceMessage> rtuMessageList = mw().getRtuMessageFactory().findByRtuAndFilter(getRtu(), filter);
            if (rtuMessageList.size() != 0) {
                DeviceMessage rtuMessage = rtuMessageList.get(0);
                String trackingId = rtuMessage.getTrackingId();

                Pattern p = Pattern.compile("#");
                Matcher m = p.matcher(trackingId);
                int count = 0;
                while (m.find()) {
                    count += 1;
                }

                DeviceMessageShadow shadow = rtuMessage.getShadow();
                message = "Received ACK for rtu with id " + getRtu().getId() + " - ACK of function " + data.getFunctionCode().getFunction();
                shadow.setTrackingId(trackingId.replace("#" + wdb, ""));
                if (count > 1) {
                    message += " - rtuMessage with ID " + rtuMessage.getId() + " still has " + (count - 1) + " SMS messages pending.";
                } else {
                    if (shadow.getState() != DeviceMessageState.FAILED) {
                        shadow.setState(DeviceMessageState.CONFIRMED);
                        message += " - rtuMessage with ID " + rtuMessage.getId() + " will be set Confirmed.";
                    } else {
                        message += " - rtuMessage with ID " + rtuMessage.getId() + " already in state Failed.";
                    }
                }
                try {
                    rtuMessage.update(shadow);
                } catch (SQLException e) {
                    message += " - Failed to update rtuMessage with ID " + rtuMessage.getId() + ".";
                } catch (BusinessException e) {
                    message += " - Failed to update rtuMessage with ID " + rtuMessage.getId() + ".";
                }
            } else {
                message = "Received ACK for rtu with id " + getRtu().getId() + " - ACK of function " + data.getFunctionCode().getFunction() +
                        " - Could not find the corresponding rtuMessage.";
            }
        } else {
            message = "Received ACK for rtu with id " + getRtu().getId() + " - ACK of function " + data.getFunctionCode().getFunction() +
                    " - Could not parse the WriteDataBlock identifier from the response.";
        }
        return message;
    }

    private String handleNACK(NackStructure data) {
        String message;
        int wdb = -1;
        if (((data.getReason().getReason() == 0x42) && (data.getFunctionCode().getFunction() == Function.EXECUTE)) ||
                ((data.getReason().getReason() == 0x47) && (data.getFunctionCode().getFunction() == Function.EXECUTE)) ||
                ((data.getReason().getReason() == 0x47) && (data.getFunctionCode().getFunction() == Function.WRITE)) ||
                data.getReason().getReason() == 0x43 ||
                data.getReason().getReason() == 0x4B ||
                data.getReason().getReason() == 0x4F) {
            wdb = data.getAdditionalData().getBytes()[2];
        }
        if (data.getReason().getReason() == 0x4A ||
                data.getReason().getReason() == 0x4E) {
            wdb = data.getAdditionalData().getBytes()[0];
        }
        if (data.getReason().getReason() == 0x50 ||
                data.getReason().getReason() == 0x51 ||
                data.getReason().getReason() == 0x52) {
            wdb = data.getAdditionalData().getBytes()[1];
        }

        if (wdb != -1) {
            DeviceMessageFilter filter = new DeviceMessageFilter();
            filter.setTrackingIdMask("*#" + wdb + "*");
            Set aSet = new HashSet();
            aSet.add(DeviceMessageState.SENT);
            filter.setStates(aSet);

            List<DeviceMessage> rtuMessageList = mw().getRtuMessageFactory().findByRtuAndFilter(getRtu(), filter);
            if (rtuMessageList.size() != 0) {
                DeviceMessage rtuMessage = rtuMessageList.get(0);
                message = "Received NACK for rtu with id " + getRtu().getId() + " - NACK of function " + data.getFunctionCode().getFunction() + " for reason: " + data.getReason() +
                        " - rtuMessage with ID " + rtuMessage.getId() + " will be set Failed.";
                DeviceMessageShadow shadow = rtuMessage.getShadow();
                shadow.setTrackingId(rtuMessage.getTrackingId().replace("#" + wdb, ""));
                shadow.setState(DeviceMessageState.FAILED);
                try {
                    rtuMessage.update(shadow);
                } catch (SQLException e) {
                    message += " - Failed to update rtuMessage with ID " + rtuMessage.getId() + ".";
                } catch (BusinessException e) {
                    message += " - Failed to update rtuMessage with ID " + rtuMessage.getId() + ".";
                }
            } else {
                message = "Received NACK for rtu with id " + getRtu().getId() + " - NACK of function " + data.getFunctionCode().getFunction() + " for reason: " + data.getReason() +
                        " - Could not find the corresponding rtuMessage.";
            }
        } else {
            message = "Received ACK for rtu with id " + getRtu().getId() + " - ACK of function " + data.getFunctionCode().getFunction() + " -for reason: " + data.getReason() +
                    " - Could not parse the WriteDataBlock identifier from the response.";
        }
        return message;
    }

    /**
     * Parse and store the data in the dec table (received via sms)
     *
     * @param communicationProfile: the meter's communication profile
     * @param pdr:                  the meter's pdr number
     * @param data:                 sent by the meter via sms
     * @throws CTRException
     */
    private void parseAndStoreDECTableData(CommunicationProfile communicationProfile, String pdr, TableDECQueryResponseStructure data) throws CTRException {
        if (!data.getPdr().getValue(0).getValue().equals(pdr)) {
            String message = "The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue(0).getValue().toString();
            logWarning(message);
            throw new CTRException(message);
        }

        if (communicationProfile.getReadMeterReadings()) {
            log("Getting registers for meter with serial number: " + getRtuSerialNumber());
            getStoreObject().add(doReadRegisters(communicationProfile, data), getRtu());
        } else {
            String message = "Received SMS with register data, but register readings in EIServer are disabled for this meter ";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        }
    }

    /**
     * Parse and store the data in the decf table (received via sms)
     *
     * @param communicationProfile: the meter's communication profile
     * @param pdr:                  the meter's pdr number
     * @param data:                 sent by the meter via sms
     * @throws CTRException
     */
    private void parseAndStoreDECFTableData(CommunicationProfile communicationProfile, String pdr, TableDECFQueryResponseStructure data) throws CTRException {
        if (!data.getPdr().getValue(0).getValue().equals(pdr)) {
            String message = "The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue(0).getValue().toString();
            logWarning(message);
            throw new CTRException(message);
        }

        if (communicationProfile.getReadMeterReadings()) {
            log("Getting registers for meter with serial number: " + getRtuSerialNumber());
            getStoreObject().add(doReadRegisters(communicationProfile, data), getRtu());
        } else {
            String message = "Received SMS with register data, but register readings in EIServer are disabled for this meter ";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        }
    }

    /**
     * Store the parsed data to EiServer, via a store object.
     */
    private void storeDataToEIServer() {
        try {
            getStoreObject().doExecute();
        } catch (BusinessException e) {
            String message = "An error happened storing the data to EIServer: " + e.getMessage();
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        } catch (SQLException e) {
            String message = "An error happened storing the data to the Oracle Database:" + e.getMessage();
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        }
    }

    /**
     * parse and store the trace_c data (received via sms)
     *
     * @param communicationProfile: the meter's communication profile
     * @param pdr:                  the meter's pdr number
     * @param data:                 sent by the meter via sms
     * @throws CTRException
     */
    private void parseAndStoreTrace_C(String pdr, Trace_CQueryResponseStructure data, CommunicationProfile communicationProfile) throws CTRException {
        if (!data.getPdr().getValue(0).getValue().equals(pdr)) {
            String message = "The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue(0).getValue().toString();
            logWarning(message);
            throw new CTRException(message);
        }
        if (communicationProfile.getReadDemandValues()) {
            getLogger().log(Level.INFO, "Getting profile data for meter with serialnumber: " + getRtuSerialNumber());
            List<Channel> channelList = getRtu().getChannels();
            for (Channel channel : channelList) {
                try {
                    if (hasDataForThisChannel(data, channel)) {
                        boolean totalizerIncluded = hasExtraTotalizerForThisChannel(data, channel);
                        ProfileChannelForSms profileForSms = new ProfileChannelForSms(logger, properties, channel, data, getTimeZone(), totalizerIncluded);
                        ProfileData pd = profileForSms.getProfileData();
                        getStoreObject().add(channel, pd);
                        String dataId = data.getId().toString();
                        if (totalizerIncluded) {
                            dataId = getDailyTotObjectId(dataId);
                        }
                        log("Added profile data for channel " + channel.toString() + ". Data ID is " + dataId);
                    } else {
                        String message = "Found profile data (" + data.getId().toString() + ", " + CTRObjectInfo.getSymbol(data.getId().toString()) + "), but not for channel " + channel.toString();
                        log(message);
                        getMeterAmrLogging().logInfo(message);
                    }
                } catch (CTRException e) {
                    String message = "Unable to read channelValues for channel [" + channel.getName() + "] " + e.getMessage();
                    logWarning(message);
                    getMeterAmrLogging().logInfo(message);
                }
            }
        } else {
            String message = "Received SMS with profile data, but profile readings in EIServer are disabled for this meter ";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        }

    }

    private boolean hasDataForThisChannel(Trace_CQueryResponseStructure data, Channel channel) {
        // Get the channel ID from the data received in the SMS
        String dataObjectId = data.getId().toString();
        int dataChannelId = getProtocolProperties().getChannelConfig().getChannelId(dataObjectId);

        // Get the eiserver channel id we're checking against
        int eiserverChannelId = channel.getLoadProfileIndex() - 1;

        // Return true if the channel can store the data OR if the channel can store the total value
        return (dataChannelId == eiserverChannelId) || hasExtraTotalizerForThisChannel(data, channel);
    }

    private boolean hasExtraTotalizerForThisChannel(Trace_CQueryResponseStructure data, Channel channel) {
        // Does the data also contains a daily TotVx value? Get the channel ID for the daily channel
        String dataObjectId = data.getId().toString();
        String dailyDataObjectId = getDailyTotObjectId(dataObjectId);
        int dailyDataChannelId = getProtocolProperties().getChannelConfig().getChannelId(dailyDataObjectId);
        int eiserverChannelId = channel.getLoadProfileIndex() - 1;
        return (dailyDataChannelId == eiserverChannelId);
    }

    /**
     * Translate the hourly object id to the daily total object id that is also included in the same sms
     *
     * @param dataObjectId The object ID of the hourly Vm or Vb value
     * @return The correct TotalVx object id, or NA if there is no total value.
     */
    private String getDailyTotObjectId(String dataObjectId) {
        if ("1.0.2".equalsIgnoreCase(dataObjectId)) {
            return "2.0.3";
        } else if ("1.2.2".equalsIgnoreCase(dataObjectId)) {
            return "2.1.3";
        }
        return "NA";
    }

    /**
     * parse and store the event data (received via sms)
     *
     * @param communicationProfile: the meter's communication profile
     * @param pdr:                  the meter's pdr number
     * @param data:                 sent by the meter via sms
     * @throws CTRException
     */
    private void parseAndStoreEventArray(CommunicationProfile communicationProfile, String pdr, ArrayEventsQueryResponseStructure data) throws CTRException {
        if (!data.getPdr().getValue(0).getValue().equals(pdr)) {
            String message = "The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue(0).getValue().toString();
            logWarning(message);
            throw new CTRException(message);
        }

        if (communicationProfile.getReadMeterEvents()) {
            log("Storing events for meter with serial number: " + getRtuSerialNumber());
            CTRMeterEvent ctrMeterEvent = new CTRMeterEvent(getTimeZone());
            List<MeterEvent> meterEvents = ctrMeterEvent.convertToMeterEvents(Arrays.asList(data.getEvento_Short()));
            ProfileData profileData = new ProfileData();
            profileData.setMeterEvents(ProtocolUtils.checkOnOverlappingEvents(meterEvents));
            getStoreObject().add(getRtu(), profileData);
        } else {
            String message = "Received SMS with event records, but event readings in EIServer are disabled for this meter ";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        }
    }

    /**
     * @return the meter's {@link TimeZone}
     */
    public TimeZone getTimeZone() {
        if (getRtu() == null) {
            return TimeZone.getDefault();
        }
        return getRtu().getDeviceTimeZone();
    }

    /**
     * Read the register data from a received DEC(F) table
     *
     * @param cp:       the meter's communication profile
     * @param response: the table containing register data
     * @throws CTRException
     * @return: register values
     */
    private MeterReadingData doReadRegisters(CommunicationProfile cp, AbstractTableQueryResponseStructure response) throws CTRException {

        MeterReadingData meterReadingData = new MeterReadingData();
        Iterator<com.energyict.mdw.amr.Register> rtuRegisterIterator = getRtu().getRegisters().iterator();
        List groups = cp.getRtuRegisterGroups();
        List<AbstractCTRObject> list = response.getObjects();

        //Checks all registers in EIServer for that RTU. See if the SMS provides data for these registers.
        while (rtuRegisterIterator.hasNext()) {
            ObisCode obisCode = null;
            try {
                Register rtuRegister = rtuRegisterIterator.next();
                if (CommonUtils.isInRegisterGroup(groups, rtuRegister)) {
                    obisCode = rtuRegister.getRtuRegisterSpec().getObisCode();  //Get the obis code per register
                    try {
                        RegisterValue registerValue = getObisCodeMapper().readRegister(obisCode, list);
                        registerValue.setRtuRegisterId(rtuRegister.getId());
                        if (rtuRegister.getReadingAt(registerValue.getReadTime()) == null) {
                            meterReadingData.add(registerValue);
                        }
                    } catch (NoSuchRegisterException e) {
                        String message = "Received no data for " + rtuRegister.toString() + "(" + obisCode + ") " + e.getMessage();
                        getMeterAmrLogging().logInfo(message);
                        log(message);
                    }
                }
            } catch (IOException e) {
                //TODO if the connection is out you should not try and read the others as well...
                getMeterAmrLogging().logInfo("An error occurred in the connection! " + e.getMessage());
                log(Level.FINEST, e.getMessage());
                log("Reading register with obisCode " + obisCode + " FAILED.");
            }
        }
        return meterReadingData;
    }

    public ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            this.obisCodeMapper = new ObisCodeMapper(null, getMeterAmrLogging());
        }
        return obisCodeMapper;
    }

    protected void log(Level level, String message) {
        getLogger().log(level, message);
    }

    protected void log(String message) {
        log(Level.INFO, message);
    }

    protected void logWarning(String message) {
        log(Level.WARNING, message);
    }

    public String getVersion() {
        return "$Date$";
    }

    /**
     * Find the RTU by telephone number
     * Check the RTU properties
     * Start processing the sms for the RTU
     *
     * @throws BusinessException
     * @throws SQLException
     */
    protected void doExecute() throws BusinessException, SQLException {
        try {
            rtu = CommonUtils.findDeviceByPhoneNumber(sms.getFrom());
        } catch (IOException e) {
            try {
                rtu = CommonUtils.findDeviceByPhoneNumber(checkFormat(sms.getFrom()));     //try again, with other phone number format
            } catch (IOException e1) {
                String message = "Failed to find a unique RTU with phone number " + sms.getFrom() + ". Process stopped. SMS message [from " + sms.getFrom() + "] will be dropped.";
                logWarning(message);
                getMeterAmrLogging().logInfo(message);
                rtu = null;
            }
        }

        if (rtu != null) {
            getProtocolProperties().addProperties(rtu.getProtocol().getProperties().toStringProperties());
            getProtocolProperties().addProperties(rtu.getProperties().toStringProperties());

            try {
                if (!processSmsFrame(parseAndDecryptSms(this.sms))) {
                    String message = "No active Inbound SMS CommunicationScheduler found. SMS message [from " + sms.getFrom() + "] will be dropped.";
                    logWarning(message);
                }
            } catch (LinkException e) {
                String message = "An error occurred in the connection!" + e.getMessage();
                log(message);
                getMeterAmrLogging().logInfo(message);
            } catch (CTRParsingException e) {
                String message = "An error occurred while parsing the data" + e.getMessage();
                log(message);
                getMeterAmrLogging().logInfo(message);
            } catch (CtrCipheringException e) {
                String message = "An error occurred while decrypting the data" + e.getMessage();
                log(message);
                getMeterAmrLogging().logInfo(message);
            }
        }
    }

    /**
     * Replace +XY by 0, e.g. +32 = 0, +39 = 0
     *
     * @param from: a given telephone number
     * @return the modified telephone number
     * @throws IOException
     */
    private String checkFormat(String from) throws IOException {

        if ("".equals(sms.getFrom())) {
            throw new IOException("Invalid (empty) phone number!");
        }
        if ("+".equals(Character.toString(from.charAt(0)))) {
            from = "0" + from.substring(3);
        }
        return from;
    }

    @Override
    public void addProperties(TypedProperties properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Collections.emptyList();
    }


    /**
     * Short notation for MeteringWarehouse.getCurrent()
     *
     * @return the current metering warehouse
     */
    private MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
    }

    public static boolean isSmsProfile(CommunicationScheduler cs) {
        String displayName = cs != null ? cs.displayString() : null;
        return (displayName != null) && displayName.contains(SMS);
    }

    public static boolean isInboundSmsProfile(CommunicationScheduler cs) {
        String displayName = cs != null ? cs.displayString() : null;
        return (displayName != null) && displayName.contains(SMS) && cs.getModemPool().getInbound();
    }
}