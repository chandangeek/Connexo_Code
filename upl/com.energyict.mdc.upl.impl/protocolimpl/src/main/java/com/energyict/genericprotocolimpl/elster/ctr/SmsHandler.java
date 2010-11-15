package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Sms;
import com.energyict.dialer.core.LinkException;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.CTREncryption;
import com.energyict.genericprotocolimpl.elster.ctr.events.CTRMeterEvent;
import com.energyict.genericprotocolimpl.elster.ctr.exception.*;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.SMSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.profile.ProfileChannelForSms;
import com.energyict.genericprotocolimpl.elster.ctr.structure.*;
import com.energyict.genericprotocolimpl.elster.ctr.util.CTRObjectInfo;
import com.energyict.genericprotocolimpl.elster.ctr.util.MeterInfo;
import com.energyict.genericprotocolimpl.webrtuz3.MeterAmrLogging;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.*;
import com.energyict.mdw.messaging.MessageHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import javax.jms.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Copyrights EnergyICT
 * Date: 20-sep-2010
 * Time: 9:58:20
 */
public class SmsHandler implements MessageHandler {

    private Rtu rtu;
    private MTU155Properties properties = new MTU155Properties();
    private Logger logger;
    private MeterAmrLogging meterAmrLogging;
    private final Date now = new Date();
    private final StoreObject storeObject = new StoreObject();
    private ObisCodeMapper obisCodeMapper;
    private MeterInfo meterInfo;
    private Sms sms;

    public Date getNow() {
        return now;
    }

    private MTU155Properties getProtocolProperties() {
        return properties;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public Rtu getRtu() {
        return rtu;
    }

    public void processMessage(Message message, Logger logger) throws JMSException, BusinessException, SQLException {
        this.logger = logger;
        ObjectMessage om = (ObjectMessage) message;
        processMessage((Sms) om.getObject());
    }

    public void processMessage(Sms sms) throws JMSException, BusinessException, SQLException {
        this.sms = sms;
        doExecute();
    }


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

    private void logSuccess(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, "0"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_OK));
        journal.addAll(getMeterAmrLogging().getJournalEntries());
        try {
            commSchedule.journal(journal);
            commSchedule.logSuccess(new Date());
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


    public void processSmsFrame(SMSFrame smsFrame) throws BusinessException, SQLException, LinkException {

        List<CommunicationScheduler> communicationSchedulers = getRtu().getCommunicationSchedulers();

        if (communicationSchedulers.size() == 0) {
            log("Rtu '" + getRtu().getName() + "' has no CommunicationSchedulers. Skipping.");
        } else {
            for (CommunicationScheduler cs : communicationSchedulers) {
                String csName = cs.displayString();
                meterAmrLogging = null;
                if (cs.getNextCommunication() == null) {
                    log("CommunicationScheduler '" + csName + "' nextCommunication is 'null'. Skipping.");
                } else {
                    log("Executing communicationScheduler '" + csName + "'.");
                    try {
                        cs.startCommunication();
                        cs.startReadingNow();
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
            }
        }
    }

    public SMSFrame parseAndDecryptSms(Sms sms) throws CTRParsingException, CtrCipheringException {
        SMSFrame smsFrame = new SMSFrame().parse(sms.getMessage(), 0);
        CTREncryption ctrEncryption = new CTREncryption(properties);
        return (SMSFrame) ctrEncryption.decryptFrame((Frame) smsFrame);
    }

    private String getRtuSerialNumber() {
        return getRtu().getSerialNumber();
    }


    private void processSchedule(SMSFrame smsFrame, CommunicationProfile communicationProfile) throws CTRException, CTRParsingException {

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
        } else {
            String message = "Unrecognized data structure in SMS. Expected array of event records, trace_C response or tableDEC(F) response.";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
            throw new CTRException(message);
        }

        storeDataToEIServer();
    }

    private void parseAndStoreDECTableData(CommunicationProfile communicationProfile, String pdr, TableDECQueryResponseStructure data) throws CTRException {
        if (!data.getPdr().getValue(0).getValue().equals(pdr)) {
            String message = "The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue(0).getValue().toString();
            logWarning(message);
            throw new CTRException(message);
        }

        if (communicationProfile.getReadMeterReadings()) {
            log("Getting registers for meter with serial number: " + getRtuSerialNumber());
            storeObject.addAll(doReadRegisters(communicationProfile, data));
        } else {
            String message = "Received SMS with register data, but register readings in EIServer are disabled for this meter ";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        }
    }

    private void parseAndStoreDECFTableData(CommunicationProfile communicationProfile, String pdr, TableDECFQueryResponseStructure data) throws CTRException {
        if (!data.getPdr().getValue(0).getValue().equals(pdr)) {
            String message = "The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue(0).getValue().toString();
            logWarning(message);
            throw new CTRException(message);
        }

        if (communicationProfile.getReadMeterReadings()) {
            log("Getting registers for meter with serial number: " + getRtuSerialNumber());
            storeObject.addAll(doReadRegisters(communicationProfile, data));
        } else {
            String message = "Received SMS with register data, but register readings in EIServer are disabled for this meter ";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        }
    }

    private void storeDataToEIServer() {
        try {
            storeObject.doExecute();
        } catch (BusinessException e) {
            String message = "An error happened storing the data to EIServer";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        } catch (SQLException e) {
            String message = "An error happened storing the data to the Oracle Database";
            logWarning(message);
            getMeterAmrLogging().logInfo(message);
        }
    }

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
                    if (getProtocolProperties().getChannelConfig().getChannelId(data.getId().toString()) == (channel.getLoadProfileIndex() - 1)) {
                        ProfileChannelForSms profileForSms = new ProfileChannelForSms(logger, properties, channel, data, getTimeZone(), getMeterAmrLogging());
                        ProfileData pd = profileForSms.getProfileData();
                        storeObject.add(channel, pd);
                        log("Added profile data for channel " + channel.toString() + ". Data ID is " + data.getId().toString());
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
            storeObject.add(getRtu(), profileData);
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
            TimeZone tz = TimeZone.getDefault();
            getLogger().warning("Rtu not available! Using the default timeZone [" + tz.getID() + "] until rtu is available.");
            return tz;
        }
        return getRtu().getDeviceTimeZone();
    }


    private Map<RtuRegister, RegisterValue> doReadRegisters(CommunicationProfile cp, AbstractTableQueryResponseStructure response) throws CTRException {

        HashMap<RtuRegister, RegisterValue> regValueMap = new HashMap<RtuRegister, RegisterValue>();
        Iterator<RtuRegister> rtuRegisterIterator = getRtu().getRegisters().iterator();
        List groups = cp.getRtuRegisterGroups();
        List<AbstractCTRObject> list = response.getObjects();

        //Checks all registers in EIServer for that RTU. See if the SMS provides data for these registers.
        while (rtuRegisterIterator.hasNext()) {
            ObisCode obisCode = null;
            try {
                RtuRegister rtuRegister = rtuRegisterIterator.next();
                if (CommonUtils.isInRegisterGroup(groups, rtuRegister)) {
                    obisCode = rtuRegister.getRtuRegisterSpec().getObisCode();  //Get the obis code per register
                    try {
                        RegisterValue registerValue = getObisCodeMapper().readRegister(obisCode, list);
                        registerValue.setRtuRegisterId(rtuRegister.getId());
                        if (rtuRegister.getReadingAt(registerValue.getReadTime()) == null) {
                            regValueMap.put(rtuRegister, registerValue);
                        }
                    } catch (NoSuchRegisterException e) {
                        String message = "Received no data for " + rtuRegister.toString() + "(" + obisCode + ") " + e.getMessage();
                        getMeterAmrLogging().logInfo(message);
                        log(message);
                    }
                }
            } catch (IOException e) {
                //TODO if the connection is out you should not try and read the others as well...
                getMeterAmrLogging().logInfo("An error occurred in the connection!");
                log(Level.FINEST, e.getMessage());
                log("Reading register with obisCode " + obisCode + " FAILED.");
            }
        }
        return regValueMap;
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
        return "1.0";
    }

    protected void doExecute() throws BusinessException, SQLException {
        try {
            rtu = CommonUtils.findDeviceByPhoneNumber(sms.getFrom());
        } catch (IOException e) {
            try {
                rtu = CommonUtils.findDeviceByPhoneNumber(checkFormat(sms.getFrom()));     //try again, with other phone number format
            } catch (IOException e1) {
                String message = "Failed to find a unique RTU with phone number " + sms.getFrom() + ". Process stopped.";
                logWarning(message);
                getMeterAmrLogging().logInfo(message);
                rtu = null;
            }
        }

        if (rtu != null) {
            getProtocolProperties().addProperties(rtu.getProtocol().getProperties());
            getProtocolProperties().addProperties(rtu.getProperties());

            try {
                processSmsFrame(parseAndDecryptSms(this.sms));
            } catch (LinkException e) {
                String message = "An error occurred in the connection!";
                log(message);
                getMeterAmrLogging().logInfo(message);
            } catch (CTRParsingException e) {
                String message = "An error occurred while parsing the data";
                log(message);
                getMeterAmrLogging().logInfo(message);
            } catch (CtrCipheringException e) {
                String message = "An error occurred while decrypting the data";
                log(message);
                getMeterAmrLogging().logInfo(message);
            }
        }
    }

    //Replace +XY by 0, e.g. +32 = 0, +39 = 0
    private String checkFormat(String from) throws IOException {

        if ("".equals(sms.getFrom())){
            throw new IOException();
        }
        if ("+".equals(Character.toString(from.charAt(0)))) {
            from = "0" + from.substring(3);
        }
        return from;
    }

    public void addProperties(Properties properties) {

    }

    public List getRequiredKeys() {
        return new ArrayList();
    }

    public List getOptionalKeys() {
        return new ArrayList();
    }
}