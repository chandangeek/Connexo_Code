/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.LoadProfileJournalReading;

import java.time.Instant;

public class LoadProfileJournalReadingImpl extends LoadProfileReadingImpl implements LoadProfileJournalReading {
    private Instant journalTime;
    private String userName;
    private boolean isActive = false;
    private long version;

    @Override
    public Instant getJournalTime() {
        return journalTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public boolean getActive() {
        return isActive;
    }

    @Override
    public long getVersion() {
        return version;
    }

    public void setJournalTime(Instant journalTime) {
        this.journalTime = journalTime;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }


    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}