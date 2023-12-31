package com.energyict.protocolimplv2.eict.rtu3.beacon3100.logbooks;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.LogBookIdentifierByObisCodeAndDevice;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 */

public class Beacon3100ProtocolEventLog extends Beacon3100AbstractEventLog {
    public static final ObisCode SLAVE_DEVICE_PROTOCOL_LOGBOOK       = ObisCode.fromString("0.128.99.98.0.255");
    private final CollectedDataFactory collectedDataFactory;

    DateFormat dateFormat = setDateFormat();
    private Map<String, CollectedLogBook> slaveLogBooks = new HashMap<>();

    public Beacon3100ProtocolEventLog(DataContainer dc, TimeZone timeZone, CollectedDataFactory collectedDataFactory) {
        super(dc, timeZone);
        this.collectedDataFactory = collectedDataFactory;
    }

    private DateFormat setDateFormat() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        dateFormat.setLenient(false);
        return dateFormat;
    }

    @Override
    protected String getLogBookName() {
        return "Protocol event log";
    }

    /** https://confluence.eict.vpdc/display/G3IntBeacon3100/Protocol+execution
     *
     * Event structure
     0: unsigned64: timestamp
     1: octet-string: meter serial
     2: unsigned64: device identifier
     3: enum: task result (0=success, 1=failure, 2=unknown)
     4: array of journal entries.  A journal entry consist of an
     entry type (enum),
     timestamp (unsigned64),
     message (octet-string) and
     debug information (octet-string)

     * @return
     */

    public List<MeterEvent> getMeterEvents() throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        int size = this.dcEvents.getRoot().getNrOfElements();
        long deviceId = -1;
        Date eventTimeStamp = new Date();
        String serialNumber = null;
        Result taskResult = Result.UNKNOWN;
        List<JournalEntry> journalEntry = null;

        for (int i = 0; i < size; i++) {
            DataStructure eventStructure = this.dcEvents.getRoot().getStructure(i);

            if (eventStructure.isLong(0)) {
                eventTimeStamp.setTime(eventStructure.getLong(0));
            } else {
                throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure to be a Long value"));
            }

            if (eventStructure.isOctetString(1)) {
                serialNumber = eventStructure.getOctetString(1).toString();
            } else {
                throw DataParseException.ioException(new ProtocolException("Expected the second element of the received structure to be an OctetString"));
            }

            if (eventStructure.isLong(2)) {
                deviceId = eventStructure.getValue(2);
            } else {
                throw DataParseException.ioException(new ProtocolException("Expected the third element of the received structure to be a Long value"));
            }

            if (eventStructure.isInteger(3)) {
                taskResult = Result.fromValue(eventStructure.getValue(3) & 0xFFFFFFFF);
            } else {
                throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure to be an Integer"));
            }


            StringBuffer buffer = new StringBuffer(40);
            buffer.append("Serial: ");
            buffer.append(serialNumber);
            buffer.append(", MAC: ");
            buffer.append(Long.toHexString(deviceId));
            buffer.append(", Result: ");
            buffer.append(taskResult.getDescription());

            buildMeterEvent(meterEvents, eventTimeStamp, buffer.toString());
        }
        return meterEvents;
    }


    /** Returns a list of collected log books, sorted by each slave device
     *
     * @return list of slave protocol logbooks
     */
    public List<CollectedLogBook> geSlaveLogBooks() {
        int size = this.dcEvents.getRoot().getNrOfElements();
        long deviceId = -1;
        Date eventTimeStamp = new Date();
        String serialNumber = null;
        Result taskResult = Result.UNKNOWN;
        List<JournalEntry> journalEntry = null;

        for (int i = 0; i < size; i++) {
            DataStructure eventStructure = this.dcEvents.getRoot().getStructure(i);

            if (eventStructure.isLong(0)) {
                eventTimeStamp.setTime(eventStructure.getLong(0));
            } else {
                throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure to be a Long value"));
            }

            if (eventStructure.isOctetString(1)) {
                serialNumber = eventStructure.getOctetString(1).toString();
            } else {
                throw DataParseException.ioException(new ProtocolException("Expected the second element of the received structure to be an OctetString"));
            }

            if (eventStructure.isLong(2)) {
                deviceId = eventStructure.getValue(2);
            } else {
                throw DataParseException.ioException(new ProtocolException("Expected the third element of the received structure to be a Long value"));
            }

            if (eventStructure.isInteger(3)) {
                taskResult = Result.fromValue(eventStructure.getValue(3) & 0xFFFFFFFF);
            } else {
                throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure to be an Integer"));
            }

            if (eventStructure.isStructure(4)) {
                journalEntry = getJournalEntries(eventStructure.getStructure(4));
            } else {
                throw DataParseException.ioException(new ProtocolException("Expected the first element of the received structure to be a Structure"));
            }

            CollectedLogBook slaveLogBook = getSlaveLogBook(serialNumber);
            addSlaveEventsToLogBook(slaveLogBook, taskResult.getResult(), journalEntry);
        }
        return getSlaveLogBooks();
    }


    private CollectedLogBook getSlaveLogBook(String serialNumber) {
        if (!slaveLogBooks.keySet().contains(serialNumber)) {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifierBySerialNumber(serialNumber);
            LogBookIdentifier logBookIdentifier = new LogBookIdentifierByObisCodeAndDevice(deviceIdentifier, SLAVE_DEVICE_PROTOCOL_LOGBOOK);
            CollectedLogBook collectedLogBook = collectedDataFactory.createCollectedLogBook(logBookIdentifier);
            slaveLogBooks.put(serialNumber, collectedLogBook);
        }
        return slaveLogBooks.get(serialNumber);
    }

    private List<JournalEntry> getJournalEntries(DataStructure array) {
        int arraySize = array.getNrOfElements();
        List<JournalEntry> result = new ArrayList<JournalEntry>();
        DataStructure dataEntry;
        JournalEntry entry;

        for (int n = 0; n < arraySize; n++) {
            dataEntry = array.getStructure(n);
            entry = new JournalEntry((int) (dataEntry.getValue(0) & 0xFFFFFFFF), /* Entry type */
                    (dataEntry.getValue(1) & 0xFFFFFFFF), /* Timestamp of the event */
                    dataEntry.getOctetString(2).toString(), /* (optional) message */
                    dataEntry.getOctetString(3).toString()); /* (optional) additional debug information */
            result.add(entry);
        }
        return result;
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, String message) {
        meterEvents.add(new MeterEvent(eventTimeStamp, 0, 0, message));
    }

    protected void addSlaveEventsToLogBook(CollectedLogBook logBook, int code, List<JournalEntry> journalEntry) {
        List<MeterEvent> meterEvents = new ArrayList<>();
        for(JournalEntry entry : journalEntry){
            String msg = entry.type.getDescription();
            if (!entry.message.isEmpty()){
                msg += " " + entry.message;
            }
            if (!entry.debugInfo.isEmpty()){
                msg += " " +entry.debugInfo;
            }
            meterEvents.add(new MeterEvent(entry.entryTimeStamp, 0, code, msg, 0, entry.type.getType()));
        }

        logBook.addCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents));
    }


    public List<CollectedLogBook> getSlaveLogBooks() {
        List<CollectedLogBook> slaveLogBooksCollection = new ArrayList<>();

        for(Map.Entry<String, CollectedLogBook> slaveLogBookMap : slaveLogBooks.entrySet()){
            try {
                Logger.getAnonymousLogger().info("- EventLog for " + slaveLogBookMap.getKey() + " has " + slaveLogBookMap.getValue().getCollectedMeterEvents().size() + " events");
            } catch (Exception ex){
                // swallow any funny NPE
            }
            slaveLogBooksCollection.add(slaveLogBookMap.getValue());
        }

        return slaveLogBooksCollection;
    }

    public class JournalEntry{

        private EntryType type;
        private Date entryTimeStamp = new Date();
        private String message;
        private String debugInfo;

        protected JournalEntry (int type, long timeStamp, String message, String debugInfo) {
            this.type = EntryType.fromValue(type);
            this.entryTimeStamp.setTime(timeStamp);
            this.message = message;
            this.debugInfo = debugInfo;
        }
    }

    public enum Result {
        SUCCESS(0, "Success"),
        FAILURE(1, "Failure"),
        UNKNOWN(2, "Unknown");

        private int result;
        private String description;

        Result(int result, String description) {
            this.result = result;
            this.description = description;
        }

        public static Result fromValue(long type) {
            for (Result resultVal : values()) {
                if (resultVal.getResult() == type) {
                    return resultVal;
                }
            }
            return UNKNOWN;
        }

        public String getDescription() {
            return description;
        }

        public int getResult() {
            return result;
        }

        public String getResultString() {
            return "" + getResult();
        }
    }

    public enum EntryType {
        INFO(0, "Info "),
        EXCEPTION(1, "Exception "),
        PROTOCOL_ISSUE(3, "Protocol Issue "),
        PROTOCOL_CONNECTED(4, "Protocol Connected "),
        PROTOCOL_DISCONNECTED(5, "Protocol Disconnected "),
        LINK_CONNECTED(6, "Link Connected "),
        LINK_DISCONNECTED(7, "Link Disconnected "),
        INITIALIZED(8, "Initialized "),
        DATA_FETCHED(9, "Data Fetched "),
        INVALID(-1, "Invalid ");

        private int type;
        private String description;

        EntryType(int type, String description) {
            this.type = type;
            this.description = description;
        }

        public static EntryType fromValue(long type) {
            for (EntryType entryType : values()) {
                if (entryType.getType() == type) {
                    return entryType;
                }
            }
            return INVALID;
        }

        public String getDescription() {
            return description;
        }

        public int getType() {
            return type;
        }

        public String getTypeString() {
            return "" + getType();
        }
    }
}
