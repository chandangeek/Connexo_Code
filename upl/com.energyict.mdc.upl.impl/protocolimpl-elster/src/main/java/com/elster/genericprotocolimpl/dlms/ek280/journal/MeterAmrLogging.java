package com.elster.genericprotocolimpl.dlms.ek280.journal;

import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.obis.ObisCode;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 24-jun-2010
 * Time: 13:33:03
 */
public class MeterAmrLogging {

    private List<AmrJournalEntry> journalEntries;

    /**
     * Getter and lazy initializer for the journalEntries property
     *
     * @return
     */
    public List<AmrJournalEntry> getJournalEntries() {
        if (journalEntries == null) {
            journalEntries = new ArrayList<AmrJournalEntry>();
        }
        return journalEntries;
    }

    /**
     * Add a 'NoSuchRegister' entry in the AmrLogbook of the device
     *
     * @param message
     */
    public void logRegisterFailure(String message, ObisCode obisCode) {
        getJournalEntries().add(new AmrJournalEntry(now(), AmrJournalEntry.NO_SUCH_REGISTER, "Unable to read " + obisCode + ": " + message));
    }

    /**
     * Add a 'NoSuchRegister' entry in the AmrLogbook of the device
     *
     * @param exception
     * @param obisCode
     */
    public void logRegisterFailure(Exception exception, ObisCode obisCode) {
        logRegisterFailure(exception.getMessage(), obisCode);
    }

    /**
     * Add a 'NoSuchRegister' entry in the AmrLogbook of the device
     *
     * @param obisCode
     */
    public void logRegisterFailure(ObisCode obisCode) {
        logRegisterFailure((String) null, obisCode);
    }

    /**
     * Add a 'Detail' entry in the AmrLogbook of the device
     *
     * @param message
     */
    public void logInfo(String message) {
        getJournalEntries().add(new AmrJournalEntry(now(), AmrJournalEntry.DETAIL, message));
    }

    /**
     * Add a 'Detail' entry in the AmrLogbook of the device
     *
     * @param exception
     */
    public void logInfo(Exception exception) {
        logInfo(exception.getMessage());
    }

    /**
     * Util method to get the current date
     *
     * @return
     */
    private Date now() {
        return new Date();
    }

    public boolean containsConfigurationErrors() {
        List<AmrJournalEntry> entries = getJournalEntries();
        for (AmrJournalEntry entry : entries) {
            if (entry.getCode() == AmrJournalEntry.NO_SUCH_REGISTER) {
                return true;
            }
        }
        return false;
    }
}
