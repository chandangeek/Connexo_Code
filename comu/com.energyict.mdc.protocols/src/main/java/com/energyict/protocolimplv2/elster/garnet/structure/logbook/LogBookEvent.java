/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

/**
 * @author sva
 * @since 12/06/2014 - 8:50
 */
public interface LogBookEvent {

    public String getEventDescription();

    public byte[] getBytes();

}
