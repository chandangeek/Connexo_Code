package com.energyict.mdc.engine.impl.core.offline;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 14/07/2014 - 15:24
 */
public enum ComJobState {

    /**
     * Comjob is waiting to be executed
     */
    Pending,

    /**
     * Comjob is being executed
     */
    Executing,

    /**
     * Comjob is being aborted
     */
    Aborting,

    /**
     * Comjob is executed, the results (collected data and comsession shadow) are waiting to be stored
     */
    AwaitingStore,

    /**
     * The results of the comjob are being stored
     */
    Storing,

    /**
     * Comjob is all done and should be removed from the UI
     */
    Done;

}