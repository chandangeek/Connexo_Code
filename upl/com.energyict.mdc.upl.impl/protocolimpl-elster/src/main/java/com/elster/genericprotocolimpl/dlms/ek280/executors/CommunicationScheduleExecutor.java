package com.elster.genericprotocolimpl.dlms.ek280.executors;

import com.elster.genericprotocolimpl.dlms.ek280.EK280;
import com.elster.genericprotocolimpl.dlms.ek280.executors.ClockSyncTaskExecutor.ClockSyncTask;
import com.elster.genericprotocolimpl.dlms.ek280.executors.ProfileTaskExecuter.ProfileTask;
import com.elster.genericprotocolimpl.dlms.ek280.executors.RegisterTaskExecutor.RegisterTask;
import com.elster.genericprotocolimpl.dlms.ek280.journal.MeterAmrLogging;
import com.energyict.cbo.BusinessException;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RtuRegisterGroup;
import com.energyict.mdw.core.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Copyrights
 * Date: 8/06/11
 * Time: 16:23
 */
public class CommunicationScheduleExecutor extends AbstractExecutor<CommunicationScheduler> {

    private MeterAmrLogging meterAmrLogging;

    public CommunicationScheduleExecutor(EK280 ek280) {
        super(ek280);
    }

    public void execute(CommunicationScheduler cs) {

        try {
            if (isPending(cs)) {

                cs.startCommunication();
                cs.startReadingNow();

                getLogger().info("Executing communication schedule [" + cs.displayString() + "]");
                CommunicationProfile profile = cs.getCommunicationProfile();

                //TODO: check if we can store data
                //TODO: check clock to see if it's out of sync and check if we can store data
                boolean storeData = profile.getStoreData();
                boolean collectOutsideBoundary = profile.getCollectOutsideBoundary();

                // Profile data
                if (profile.getReadDemandValues()) {
                    ProfileTask task = new ProfileTask(profile.getCheckChannelConfig(), profile.getReadMeterEvents());
                    new ProfileTaskExecuter(this).execute(task);
                }

                // Device events
                if (profile.getReadMeterEvents()) {
                    new EventTaskExecutor(this).execute(getRtu());
                }

                // Register data
                if (profile.getReadMeterReadings()) {
                    List<Register> registersToRead = getRegistersFromGroups(getRtu().getRegisters(), profile.getRtuRegisterGroups());
                    RegisterTask task = new RegisterTask(registersToRead);
                    new RegisterTaskExecutor(this).execute(task);
                }

                // Clock sync
                if (profile.getWriteClock()) {
                    ClockSyncTask task = new ClockSyncTask(profile.getMinimumClockDifference(), profile.getMaximumClockDifference());
                    new ClockSyncTaskExecutor(this).execute(task);
                }

                // Device messages
                if (profile.getSendRtuMessage()) {
                    new MessageExecutor(this).execute(getEk280().getRtu());
                }
            }

            logSuccess(cs);

        } catch (IOException e) {
            severe(e.getMessage());
            logFailure(cs);
        } catch (SQLException e) {
            severe(e.getMessage());
            logFailure(cs);
        } catch (BusinessException e) {
            severe(e.getMessage());
            logFailure(cs);
        }
    }

    /**
     * Filter all the registers we should read, based on the configured register groups
     *
     * @param registers The list of registers to filter
     * @param groups    The list of groups to read
     * @return A filtered list of RtuRegisters
     */
    private List<Register> getRegistersFromGroups(List<Register> registers, List<RtuRegisterGroup> groups) {
        List<Register> registersToRead = new ArrayList<Register>();
        if ((groups == null) || (groups.isEmpty())) {
            registersToRead.addAll(registers);
        } else {
            for (RtuRegisterGroup group : groups) {
                for (Register register : registers) {
                    RtuRegisterGroup registerGroup = register.getGroup();
                    if ((registerGroup != null) && (registerGroup.getId() == group.getId())) {
                        registersToRead.add(register);
                    }
                }
            }
            for (Register register : registers) {
                if (register.getGroup() == null) {
                    registersToRead.add(register);
                }
            }
        }
        return registersToRead;
    }

    /**
     * Check if a schedule is pending
     *
     * @param scheduler The schedule to check
     * @return true if it's pending
     */
    private boolean isPending(CommunicationScheduler scheduler) {
        if (scheduler == null) {
            getLogger().severe("CommunicationScheduler was 'null'. Cannot execute!");
            return false;
        }

        if (scheduler.getNextCommunication() == null) {
            getLogger().severe("CommunicationScheduler [" + scheduler.displayString() + "] has no next communication date. Skipping.");
            return false;
        }

        if (scheduler.getNextCommunication().after(new Date())) {
            getLogger().severe("CommunicationScheduler [" + scheduler.displayString() + "] next communication date not reached yet. Skipping.");
            return false;
        }

        return true;
    }


    /**
     * Log a successful event
     *
     * @param commSchedule
     */
    private void logSuccess(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, getEk280().getConnectTimeInSeconds()));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "" + getEk280().getTimeDifference()));
        journal.add(new AmrJournalEntry(getMeterAmrLogging().containsConfigurationErrors() ? AmrJournalEntry.CC_CONFIGURATION : AmrJournalEntry.CC_OK));
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

    /**
     * Log a failed event
     *
     * @param commSchedule
     */
    private void logFailure(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, getEk280().getConnectTimeInSeconds()));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "-"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "" + getEk280().getTimeDifference()));
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

    private Date getNow() {
        return new Date();
    }

    /**
     * Log a failed event
     *
     * @param commSchedule
     */
    private void logConfigurationError(CommunicationScheduler commSchedule) {
        List<AmrJournalEntry> journal = new ArrayList<AmrJournalEntry>();
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.CONNECTTIME, getEk280().getConnectTimeInSeconds()));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.PROTOCOL_LOG, "See logfile of [" + getRtu().toString() + "]"));
        journal.add(new AmrJournalEntry(getNow(), AmrJournalEntry.TIMEDIFF, "" + getEk280().getTimeDifference()));
        journal.add(new AmrJournalEntry(AmrJournalEntry.CC_CONFIGURATION));
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

}
