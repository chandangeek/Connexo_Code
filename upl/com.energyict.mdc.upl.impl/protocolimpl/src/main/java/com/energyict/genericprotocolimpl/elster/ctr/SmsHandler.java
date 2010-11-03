package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Sms;
import com.energyict.dialer.core.*;
import com.energyict.genericprotocolimpl.common.*;
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
import com.energyict.protocolimpl.debug.DebugUtils;

import javax.jms.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * Copyrights EnergyICT
 * Date: 20-sep-2010
 * Time: 9:58:20
 *
 */
public class SmsHandler extends AbstractGenericProtocol implements MessageHandler {

    private Rtu rtu;
    private MTU155Properties properties = new MTU155Properties();
    private Logger logger;
    private MeterAmrLogging meterAmrLogging;
    private final Date now = new Date();
    private final StoreObject storeObject = new StoreObject();
    private ObisCodeMapper obisCodeMapper;
    private GprsRequestFactory requestFactory;
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

        this.sms = (Sms) om.getObject();
        
        try {
            execute(null, setupLink(), Logger.getLogger(SmsHandler.class.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LinkException e) {
            e.printStackTrace();
        }
    }

    public void processMessage(Sms sms) throws JMSException, BusinessException, SQLException {

        this.sms = sms;
        try {
            execute(null, setupLink(), Logger.getLogger(SmsHandler.class.getName()));
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (LinkException e1) {
            e1.printStackTrace();
        }
    }

    private void logFailure(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, "0"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
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

    public GprsRequestFactory getRequestFactory() {
        if (requestFactory == null) {
            requestFactory = new GprsRequestFactory(getLink(), getLogger(), getProtocolProperties(), TimeZone.getDefault());
        }
        return requestFactory;
    }


    private void logSuccess(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, "0"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "0"));
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


    public void processSms(SMSFrame smsFrame) throws BusinessException, IOException, SQLException, LinkException {

        List<CommunicationScheduler> communicationSchedulers = getRtu().getCommunicationSchedulers();

        if (communicationSchedulers.size() == 0) {
            log("Rtu '" + getRtu().getName() + "' has no CommunicationSchedulers. Skipping.");
        } else {
            for (CommunicationScheduler cs : communicationSchedulers) {
                String csName = cs.displayString();
                meterAmrLogging = null;
                if (cs.getNextCommunication() == null) {
                    log("CommunicationScheduler '" + csName + "' nextCommunication is 'null'. Skipping.");
                } else if (cs.getNextCommunication().after(getNow())) {
                    log("CommunicationScheduler '" + csName + "' nextCommunication not reached yet. Skipping.");
                } else {
                    log("CommunicationScheduler '" + csName + "' nextCommunication reached. Executing scheduler.");
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

    private Link setupLink() throws BusinessException, IOException, SQLException, LinkException {

        int baudRate = 9600;
        int dataBits = SerialCommunicationChannel.DATABITS_8;
        int parity = SerialCommunicationChannel.PARITY_NONE;
        int stopBits = SerialCommunicationChannel.STOPBITS_1;

        return DebugUtils.getConnectedDirectDialer("COM1", baudRate, dataBits, parity, stopBits);
    }

    public SMSFrame decrypt(Sms sms) throws CTRParsingException, IOException {

        SMSFrame smsFrame = new SMSFrame().parse(sms.getMessage(), 0);
        CTREncryption ctrEncryption = new CTREncryption(properties);

        try {
            return (SMSFrame) ctrEncryption.decryptFrame((Frame) smsFrame);
        } catch (CtrCipheringException e) {
            throw new CTRConnectionException("An error occurred in the secure connection!", e);
        }

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

        //Store event array
        if (smsFrame.getData() instanceof ArrayEventsQueryResponseStructure) {
            ArrayEventsQueryResponseStructure data = (ArrayEventsQueryResponseStructure) smsFrame.getData();

            if (!data.getPdr().getValue().equals(pdr)) {
                logWarning("The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue());
                //throw new CTRException("The PDR is wrong.");
            }

            if (communicationProfile.getReadMeterEvents()) {
                log("Storing events for meter with serial number: " + getRtuSerialNumber());
                CTRMeterEvent ctrMeterEvent = new CTRMeterEvent(getRequestFactory());
                List<MeterEvent> meterEvents = ctrMeterEvent.convertToMeterEvents(Arrays.asList(data.getEvento_Short()));
                ProfileData profileData = new ProfileData();
                profileData.setMeterEvents(ProtocolUtils.checkOnOverlappingEvents(meterEvents));
                storeObject.add(getRtu(), profileData);
            }


        //Store Profile data from trace_C response: channel values = profile data
        } else if (smsFrame.getData() instanceof Trace_CQueryResponseStructure) {
            Trace_CQueryResponseStructure data = (Trace_CQueryResponseStructure) smsFrame.getData();

            if (!data.getPdr().getValue().equals(pdr)) {
                logWarning("The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue());
                //throw new CTRException("The PDR is wrong.");
            }

            List<Channel> channelList = getRtu().getChannels();
            for (Channel channel : channelList) {
                try {
                    if (getProtocolProperties().getChannelConfig().getChannelId(data.getId().toString()) == (channel.getLoadProfileIndex() - 1)) {
                        ProfileChannelForSms profileForSms = new ProfileChannelForSms(logger, properties, channel, data, getMeterInfo().getTime());
                        ProfileData pd = profileForSms.getProfileData();
                        storeObject.add(channel, pd);
                        log("Added profile data for channel " + channel.toString() + ". Data ID is " + data.getId().toString());
                    } else {
                        log("Found profile data (" + data.getId().toString() + ", " + CTRObjectInfo.getSymbol(data.getId().toString()) + "), but not for channel " + channel.toString());
                    }
                } catch (CTRException e) {
                    logWarning("Unable to read channelValues for channel [......]" + e.getMessage());
                }
            }

        //Store register data from DECF table response
        } else if (smsFrame.getData() instanceof TableDECFQueryResponseStructure) {
            TableDECFQueryResponseStructure data = (TableDECFQueryResponseStructure) smsFrame.getData();

            if (!data.getPdr().getValue().equals(pdr)) {
                logWarning("The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue());
                //throw new CTRException("The PDR is wrong.");
            }

            if (communicationProfile.getReadMeterReadings()) {
                log("Getting registers for meter with serial number: " + getRtuSerialNumber());
                storeObject.addAll(doReadRegisters(communicationProfile, data));
            }

        //Store register data from DEC table response
        } else if (smsFrame.getData() instanceof TableDECQueryResponseStructure) {
            TableDECQueryResponseStructure data = (TableDECQueryResponseStructure) smsFrame.getData();

            if (!data.getPdr().getValue().equals(pdr)) {
                log("The meter PDR is " + pdr + ", but the pdr in the sms response was " + data.getPdr().getValue());
                //throw new CTRException("The PDR is wrong.");
            }

            if (communicationProfile.getReadMeterReadings()) {
                log("Getting registers for meter with serial number: " + getRtuSerialNumber());
                storeObject.addAll(doReadRegisters(communicationProfile, data));
            }

        } else {
            throw new CTRException("Unrecognized data structure: " + smsFrame.getData().getClass().getSimpleName() + "\n" + "Expected: Array of events, Trace_C response or TableDEC(F) response.");
        }

        try {
            storeObject.doExecute();
        } catch (BusinessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private MeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new MeterInfo(getRequestFactory(), getLogger(), getTimeZone());
        }
        return meterInfo;
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
                    obisCode = rtuRegister.getRtuRegisterSpec().getObisCode();  //Get the obiscode per register
                    try {
                        RegisterValue registerValue = getObisCodeMapper().readRegister(obisCode, list);
                        registerValue.setRtuRegisterId(rtuRegister.getId());
                        if (rtuRegister.getReadingAt(registerValue.getReadTime()) == null) {
                            regValueMap.put(rtuRegister, registerValue);
                        }
                    } catch (NoSuchRegisterException e) {
                        log(Level.FINEST, e.getMessage());
                        log("ObisCode " + obisCode + " is not supported by the meter.");
                    }
                }
            } catch (IOException e) {
                //TODO if the connection is out you should not try and read the others as well...
                log(Level.FINEST, e.getMessage());
                log("Reading register with obisCode " + obisCode + " FAILED.");
            }
        }
        return regValueMap;
    }
    
    public ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            this.obisCodeMapper = new ObisCodeMapper(getRequestFactory());
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

    @Override
    protected void doExecute() throws IOException, BusinessException, SQLException {


        rtu = CommonUtils.findDeviceByPhoneNumber(sms.getFrom());
        properties.addProperties(rtu.getRtuType().getProtocol().getProperties());
        properties.addProperties(rtu.getProperties());

        try {
            processSms(decrypt(this.sms));
        } catch (LinkException e) {
            e.printStackTrace();
        }
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