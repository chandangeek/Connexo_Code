/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.field.BcdEncodedField;

/**
 * @author sva
 * @since 26/08/2014 - 12:00
 */
public class ChannelGroupVisibility extends BcdEncodedField {

    private static final int LENGTH = 1;

    public ChannelGroupVisibility() {
        super(LENGTH);
    }

    public int getChannelGroupVisibilitySelection() {
        return Integer.parseInt(getText());
    }

    public void setChannelGroupVisibilitySelection(int blockCount) {
        setText(Integer.toString(blockCount));
    }
}