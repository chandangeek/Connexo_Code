package com.energyict.protocolimpl.utils.communicationdump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a representation of a 'Full debug log file' from the CommServerJ
 */
public class CommunicationDumpFile {

    private List<CommunicationDumpEntry> entries = new ArrayList<CommunicationDumpEntry>();

    /**
     * Create a new CommunicationDumpFile from a given 'Full debug log file' from the CommServerJ
     *
     * @param fileName
     */
    public CommunicationDumpFile(String fileName) {
        this(new File(fileName));
    }

    /**
     * Create a new CommunicationDumpFile from a given 'Full debug log file' from the CommServerJ
     *
     * @param file
     */
    public CommunicationDumpFile(File file) {
        readEntriesFromFile(file);
    }

    /**
     * Open the given file, parse the data from it, and store it as a list of CommunicationDumpEntries
     *
     * @param file
     */
    private void readEntriesFromFile(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            int sequenceNumber = 0;
            while (reader.ready()) {
                String entryLine = reader.readLine();
                if (entryLine != null) {
                    CommunicationDumpEntry entry = CommunicationDumpEntry.getEntryFromString(entryLine, sequenceNumber++);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Getter for the CommunicationDumpEntries
     *
     * @return
     */
    public List<CommunicationDumpEntry> getEntries() {
        return entries;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        List entryList = getEntries();
        for (Iterator iterator = entryList.iterator(); iterator.hasNext();) {
            CommunicationDumpEntry entry = (CommunicationDumpEntry) iterator.next();
            if (entry != null) {
                sb.append(entry.toString());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Same as the toString() method, but the data is shown as readable ascii values where possible
     *
     * @return a human readable representation of the communicationDumpEntries
     */
    public String toStringAscii() {
        StringBuilder sb = new StringBuilder();
        List entryList = getEntries();
        for (Iterator iterator = entryList.iterator(); iterator.hasNext();) {
            CommunicationDumpEntry entry = (CommunicationDumpEntry) iterator.next();
            if (entry != null) {
                sb.append(entry.toStringAscii());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Getter for the RX CommunicationDumpEntries
     *
     * @return
     */
    public List<CommunicationDumpEntry> getRxEntries() {
        List<CommunicationDumpEntry> rxEntries = new ArrayList<CommunicationDumpEntry>();
        for (CommunicationDumpEntry cde : entries) {
            if (cde.isRx()) {
                rxEntries.add(cde);
            }
        }
        return rxEntries;
    }

    /**
     * Getter for the TX CommunicationDumpEntries
     *
     * @return
     */
    public List<CommunicationDumpEntry> getTxEntries() {
        List<CommunicationDumpEntry> txEntries = new ArrayList<CommunicationDumpEntry>();
        for (CommunicationDumpEntry cde : entries) {
            if (cde.isTx()) {
                txEntries.add(cde);
            }
        }
        return txEntries;
    }

    /**
     * Get an CommunicationDumpEntry by his sequence number
     *
     * @param sequence
     * @return the CommunicationDumpEntry or null if this sequenceNumber does not exist
     */
    public CommunicationDumpEntry getCommunicationDumpEntry(int sequence) {
        for (CommunicationDumpEntry entry : getEntries()) {
            if (entry.getSequenceNumber() == sequence) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Get the CommunicationDumpEntry before a given CommunicationDumpEntry
     *
     * @param communicationDumpEntry
     * @return
     */
    public CommunicationDumpEntry getPreviousEntry(CommunicationDumpEntry communicationDumpEntry) {
        return getCommunicationDumpEntry(communicationDumpEntry.getSequenceNumber() - 1);
    }

    /**
     * Get the CommunicationDumpEntry after a given CommunicationDumpEntry
     *
     * @param communicationDumpEntry
     * @return
     */
    public CommunicationDumpEntry getNextEntry(CommunicationDumpEntry communicationDumpEntry) {
        return getCommunicationDumpEntry(communicationDumpEntry.getSequenceNumber() + 1);
    }

    /**
     * Get the next RX CommunicationDumpEntry before a given TX CommunicationDumpEntry
     *
     * @param communicationDumpEntry
     * @return
     */
    public CommunicationDumpEntry getResponseOn(CommunicationDumpEntry communicationDumpEntry) {
        CommunicationDumpEntry response = null;
        if (communicationDumpEntry.isTx()) {
            CommunicationDumpEntry nextEntry = getNextEntry(communicationDumpEntry);
            if ((nextEntry != null) && (nextEntry.isTx())) {
                getResponseOn(nextEntry);
            } else {
                response = nextEntry;
            }
        }
        return response;
    }

}
