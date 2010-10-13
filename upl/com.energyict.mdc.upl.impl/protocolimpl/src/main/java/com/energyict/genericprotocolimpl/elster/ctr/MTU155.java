package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.*;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConfigurationException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Channel;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.EncryptionStatus;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.webrtuz3.MeterAmrLogging;
import com.energyict.mdw.core.*;
import com.energyict.protocolimpl.debug.DebugUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 24-sep-2010
 * Time: 11:43:45
 */
public class MTU155 extends AbstractGenericProtocol {

    private final Date now = new Date();

    private final StoreObject storeObject = new StoreObject();
    private final MTU155Properties properties = new MTU155Properties();
    private GprsRequestFactory requestFactory;
    private ObisCodeMapper obisCodeMapper;
    private Rtu rtu;
    private MeterAmrLogging meterAmrLogging;

    public String getVersion() {
        return "$Date$";
    }

    public List<String> getRequiredKeys() {
        return properties.getRequiredKeys();
    }

    public List<String> getOptionalKeys() {
        return properties.getOptionalKeys();
    }

    @Override
    public void initProperties() {
        properties.addProperties(getProperties());
    }

    @Override
    protected void doExecute() throws IOException, BusinessException, SQLException {
        this.requestFactory = new GprsRequestFactory(getLink(), getLogger(), getProtocolProperties());
        this.obisCodeMapper = new ObisCodeMapper(getRequestFactory());
        this.rtu = identifyRtu();
        log("Rtu with name '" + getRtu().getName() + "' connected successfully.");
        getProtocolProperties().addProperties(rtu.getProtocol().getProperties());
        getProtocolProperties().addProperties(rtu.getProperties());
        readDevice();
        getStoreObject().doExecute();
    }

    private void readDevice() {
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
                        cs.startCommunication(0);
                        cs.startReadingNow();
                        executeCommunicationSchedule(cs);
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
                    }
                }
            }
        }
    }

    private void executeCommunicationSchedule(CommunicationScheduler communicationScheduler) throws IOException {
        CommunicationProfile communicationProfile = communicationScheduler.getCommunicationProfile();
        String csName = communicationScheduler.displayString();
        if (communicationProfile == null) {
            throw new CTRConfigurationException("CommunicationScheduler '" + csName + "' has no communication profile.");
        }

        // Check if the time is greater then allowed, if so then no data can be stored...
        // Don't do this when a forceClock is scheduled
        if (!communicationProfile.getForceClock() && !communicationProfile.getAdHoc()) {
            // TODO: implement method
        }

        // Read the events
        if (communicationProfile.getReadMeterEvents()) {
            getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + getRtuSerialNumber());
            // TODO: implement method
        }

        // Read the register values
        if (communicationProfile.getReadMeterReadings()) {
            getLogger().log(Level.INFO, "Getting registers for meter with serialnumber: " + getRtuSerialNumber());

        }

        // Read the profiles
        if (communicationProfile.getReadDemandValues()) {
            getLogger().log(Level.INFO, "Getting profile data for meter with serialnumber: " + getRtuSerialNumber());
            // TODO: implement method
        }

        //Send the meter messages
        if (communicationProfile.getSendRtuMessage()) {
            getLogger().log(Level.INFO, "Sending messages to meter with serialnumber: " + getRtuSerialNumber());
            // TODO: implement method
        }

    }

    private String getRtuSerialNumber() {
        return getRtu().getSerialNumber();
    }

    private Rtu identifyRtu() throws CTRException {
        String pdr = readPdr();
        log("MTU155 with pdr='" + pdr + "' connected.");

        List<Rtu> rtus = CommonUtils.mw().getRtuFactory().findByDialHomeId(pdr);
        switch (rtus.size()) {
            case 0:
                throw new CTRConfigurationException("No rtu found in EiServer with callhomeId='" + pdr + "'");
            case 1:
                return rtus.get(0);
            default:
                throw new CTRConfigurationException("Found " + rtus.size() + " rtu's in EiServer with callhomeId='" + pdr + "', but only one allowed. Skipping communication until fixed.");
        }

    }

    /**
     * @return the pdr value as String
     * @throws CTRException
     */
    private String readPdr() throws CTRException {
/*
        return "12345678900000";
*/
        log("Requesting IDENTIFICATION structure from device");
        String pdr = getRequestFactory().readIdentificationStructure().getPdr().getValue();
        if (pdr == null) {
            throw new CTRException("Unable to detect meter. PDR value was 'null'!");
        }
        return pdr;
    }

    private void testEncryption() {
        try {

            GPRSFrame readRequest = new GPRSFrame();
            readRequest.getFunctionCode().setFunction(Function.QUERY);
            readRequest.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
            readRequest.getStructureCode().setStructureCode(StructureCode.REGISTER);
            readRequest.setChannel(new Channel(0));
            readRequest.getProfi().setProfi(0x00);
            readRequest.getProfi().setLongFrame(false);

            Data data = new Data();
            byte[] pssw = ProtocolTools.getBytesFromHexString("$30$30$30$30$30$31");
            byte[] nrObjects = ProtocolTools.getBytesFromHexString("$01");
            AttributeType type = new AttributeType(0);
            type.setHasValueFields(true);
            byte[] attributeType = type.getBytes();
            byte[] id1 = new CTRObjectID("C.0.0").getBytes();

            byte[] rawData = ProtocolTools.concatByteArrays(pssw, nrObjects, attributeType, id1, new byte[128]);
            data.parse(rawData, 0);
            data.parse(ProtocolTools.getBytesFromHexString("$30$30$30$30$30$31$01$02$0C$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"), 0);
            readRequest.setData(data);
            readRequest.generateAndSetCpa(getProtocolProperties().getKeyCBytes());

            System.out.println(readRequest);

            GPRSFrame response = getRequestFactory().getConnection().sendFrameGetResponse(readRequest);
            System.out.println(response);

        } catch (CTRException e) {
            e.printStackTrace();
        }
    }

    private MTU155Properties getProtocolProperties() {
        return properties;
    }

    public static void main(String[] args) throws IOException, LinkException, BusinessException, SQLException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException {
        int baudRate = 9600;
        int dataBits = SerialCommunicationChannel.DATABITS_8;
        int parity = SerialCommunicationChannel.PARITY_NONE;
        int stopBits = SerialCommunicationChannel.STOPBITS_1;

        Dialer dialer = DebugUtils.getConnectedDirectDialer("COM1", baudRate, dataBits, parity, stopBits);

        MTU155 mtu155 = new MTU155();
        mtu155.execute(null, dialer, Logger.getLogger(MTU155.class.getName()));

    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    public Rtu getRtu() {
        return rtu;
    }

    public StoreObject getStoreObject() {
        return storeObject;
    }

    public Date getNow() {
        return now;
    }

    public MeterAmrLogging getMeterAmrLogging() {
        if (meterAmrLogging == null) {
            meterAmrLogging = new MeterAmrLogging();
        }
        return meterAmrLogging;
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

    public ObisCodeMapper getObisCodeMapper() {
        return obisCodeMapper;
    }
}
