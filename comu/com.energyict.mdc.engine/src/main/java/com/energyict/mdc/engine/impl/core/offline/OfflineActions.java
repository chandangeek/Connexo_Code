package com.energyict.mdc.engine.impl.core.offline;

/**
 * Copyrights EnergyICT
 * List of actions that can be triggered from the UI and need to be executed by the comserver offline working thread.
 *
 * @author khe
 * @since 7/07/2014 - 12:52
 */
public enum OfflineActions {

    StoreCollectedData,
    QueryPendingComJobs,
    ComJobIsFinished;
}
