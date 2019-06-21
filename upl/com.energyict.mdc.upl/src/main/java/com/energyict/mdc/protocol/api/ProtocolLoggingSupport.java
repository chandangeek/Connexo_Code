/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

/**
 * Adds capability to transmit a protocol logger
 */
public interface ProtocolLoggingSupport  {

    default void setProtocolJournaling(ProtocolJournal protocolJournal){
        //let individual protocols implement this
    }

    default void journal(String message){
        // let individual protocols implement this
    }
}
