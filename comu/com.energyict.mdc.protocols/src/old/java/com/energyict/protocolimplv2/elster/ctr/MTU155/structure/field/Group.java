/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class Group extends AbstractField<Group> {

    private int group;
    private static final int LENGTH = 1;

    public Group(int group) {
        this.group = group;
    }

    public Group() {
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public byte[] getBytes() {
        return getBytesFromInt(group, LENGTH);
    }

    public Group parse(byte[] rawData, int offset) throws CTRParsingException {
        setGroup(getIntFromBytes(rawData, offset, LENGTH));
        return this;
    }

    public int getLength() {
        return LENGTH;
    }
}