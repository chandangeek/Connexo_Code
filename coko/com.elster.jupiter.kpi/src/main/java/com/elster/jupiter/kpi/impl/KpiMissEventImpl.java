/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.kpi.KpiEntry;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.kpi.KpiMissEvent;

public class KpiMissEventImpl implements KpiMissEvent {
    private final KpiMember member;
    private final KpiEntry entry;

    private long id;
    private int position;
    private long timestamp;

    @Override
    public KpiEntry getEntry() {
        return entry;
    }

    @Override
    public KpiMember getMember() {
        return member;
    }

    KpiMissEventImpl(KpiMemberImpl member, KpiEntry entry) {
        this.member = member;
        this.entry = entry;
        this.id = member.getKpi().getId();
        this.position = member.getPosition();
        this.timestamp = entry.getTimestamp().toEpochMilli();
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPosition() {
        return position;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
