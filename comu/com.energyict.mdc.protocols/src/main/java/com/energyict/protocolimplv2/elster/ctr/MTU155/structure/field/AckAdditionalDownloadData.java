/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public class AckAdditionalDownloadData extends AbstractField<AckAdditionalDownloadData> {

    private byte[] additionalData;
    private Identify identify;
    private Group group_s;
    private Group group_r;
    private Segment segment;


    public AckAdditionalDownloadData() {
        additionalData = new byte[getLength()];
        identify = new Identify();
        group_s = new Group();
        group_r = new Group();
        segment = new Segment();
    }

    public int getLength() {
        return 24;
    }

    public byte[] getBytes() {
        return additionalData;
    }

    public AckAdditionalDownloadData parse(byte[] rawData, int offset) throws CTRParsingException {
        additionalData = new byte[getLength()];
        System.arraycopy(rawData, offset, additionalData, 0, getLength());

        identify.setIdentify(getIntFromBytes(rawData, offset, 4));
        offset += 4;
        group_s.setGroup(getIntFromBytes(rawData, offset, 1));
        offset += 1;
        group_r.setGroup(getIntFromBytes(rawData, offset, 1));
        offset  += 1;

        // Last correct received segment. (= All segments up to this one are correct received and are ACKED).
        segment = new Segment().parse(rawData, offset);

        return this;
    }

    public byte[] getAdditionalData() {
        return additionalData;
    }

    public Identify getIdentify() {
        return identify;
    }

    public Group getGroup_s() {
        return group_s;
    }

    public Group getGroup_r() {
        return group_r;
    }

    public Segment getSegment() {
        return segment;
    }
}