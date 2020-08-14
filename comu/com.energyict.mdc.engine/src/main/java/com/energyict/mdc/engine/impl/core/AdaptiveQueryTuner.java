package com.energyict.mdc.engine.impl.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class AdaptiveQueryTuner implements QueryTuner {

    private static final Logger LOGGER = Logger.getLogger(AdaptiveQueryTuner.class.getName());

    enum Direction {
        UP,
        DOWN
    }
    private Direction direction = Direction.UP;
    private int durationPerJob; // milliseconds spent by the query per retrieved job
    private int previousDurationPerJob = -1;
    private int factor = 2;
    private Map<Integer, Long> queryDurationPerFactor = new HashMap<>(); // keeps a record of query duration per factor

    public int getTuningFactor() {
        return factor;
    }

    /**
     * Calculates a limiting factor for the pending communication tasks query, based on the current configuration and
     * the results of the last  query. The calculated factor will be multiplied with the number of simultaneous connections
     * and the result is used as limit for the query of pending tasks.
     *
     * The algorithm is based on a feedback loop:
     *  - save a record of query duration for the current factor
     * - calculate the current kpi based on the received parameters (query duration/number of retrieved jobs)
     * - if the current kpi is better than the previous one, modify the factor towards the current direction (i.e. increment or decrement) - but
     * only do that if we have a record of a shorter query duration for the calculated factor
     *
     * @param queryDuration
     * @param jobsSize
     * @param nbConnections
     * @return
     */
    public void calculateFactor(long queryDuration, int jobsSize, int nbConnections) {
        if (jobsSize > 0) {
            queryDurationPerFactor.put(factor, queryDuration);
            durationPerJob = (int) (queryDuration * 10 / jobsSize);
            LOGGER.warning("perf - previousDurationPerJob=" + previousDurationPerJob + " durationPerJob=" + durationPerJob);
            if (previousDurationPerJob == -1) {
                previousDurationPerJob = durationPerJob + 1;
            }
            if (durationPerJob == previousDurationPerJob) {
                return;
            }
            if (durationPerJob > previousDurationPerJob) {
                switchDirection();
            }
            previousDurationPerJob = durationPerJob;
            modifyFactor(queryDuration, jobsSize, nbConnections);
        } else {
            // there is no schedule running, return the lowest possible factor to have the shortest possible query
            factor = 8;
        }
    }

    private void modifyFactor(long queryDuration, int jobsSize, int nbConnections) {
        if (Direction.UP.equals(direction)) {
//            if (jobsSize < nbConnections
//                    && newFactorHasBetterRecord(factor + 1, queryDuration)
//            ) {
                ++factor;
//            }
        } else {
            if (factor > 1
//                    && newFactorHasBetterRecord(factor - 1, queryDuration)
            ) {
                --factor;
            }
        }
    }

    private boolean newFactorHasBetterRecord(int factor, long queryDuration) {
        if (queryDurationPerFactor.get(factor) == null ||
                (queryDurationPerFactor.get(factor) != null && queryDurationPerFactor.get(factor) < queryDuration)) {
            return true;
        }
        return false;
    }

    private void switchDirection() {
        if (Direction.UP.equals(direction)) {
            direction = Direction.DOWN;
        } else {
            direction = Direction.UP;
        }
    }
}
