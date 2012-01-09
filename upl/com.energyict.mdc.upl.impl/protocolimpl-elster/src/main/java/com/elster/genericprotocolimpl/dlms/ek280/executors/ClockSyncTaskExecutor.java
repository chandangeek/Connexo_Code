package com.elster.genericprotocolimpl.dlms.ek280.executors;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights
 * Date: 15/06/11
 * Time: 11:53
 */
public class ClockSyncTaskExecutor extends AbstractExecutor<ClockSyncTaskExecutor.ClockSyncTask> {

    public ClockSyncTaskExecutor(AbstractExecutor executor) {
        super(executor);
    }

    @Override
    public void execute(ClockSyncTask task) throws IOException {
        long timeDifference = Math.abs(getEk280().getTimeDifference());
        if (timeDifference < task.getMinimumClockDifference()) {
            info("Time difference [" + timeDifference + " ms] too low compared to minimum threshold [" + task.getMinimumClockDifference() + " ms]. ");
        } else if (timeDifference > task.getMaximumClockDifference()) {
            info("Time difference [" + timeDifference + " ms] too high compared to maximum threshold [" + task.getMaximumClockDifference() + " ms]. ");
        } else {
            severe("Device time out of sync [" + timeDifference + " ms]. Setting time [" + new Date() + "]");
            getDlmsProtocol().setTime();
        }
    }

    protected static class ClockSyncTask {

        private final int minimumClockDifference;
        private final int maximumClockDifference;

        public ClockSyncTask(int minimumClockDifference, int maximumClockDifference) {
            this.minimumClockDifference = minimumClockDifference;
            this.maximumClockDifference = maximumClockDifference;
        }

        public long getMaximumClockDifference() {
            return maximumClockDifference * 1000;
        }

        public long getMinimumClockDifference() {
            return minimumClockDifference * 1000;
        }
    }
}
