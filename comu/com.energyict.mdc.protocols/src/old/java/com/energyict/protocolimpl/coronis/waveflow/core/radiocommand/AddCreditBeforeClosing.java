/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class AddCreditBeforeClosing extends AbstractRadioCommand {

    private boolean success = false;
    private int quantity;
    private int add;                 //indicates whether to add (0) the quantity or to replace (1) the current quantity.
    private int close;               //indicates whether to close (0) or to limit (1) the amount when the credits = 0.

    protected AddCreditBeforeClosing(WaveFlow waveFlow, int quantity, int add, int close) {
        super(waveFlow);
        this.quantity = quantity;
        this.add = add;
        this.close = close;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        success = (data[0] == 0x00);
    }

    @Override
    protected byte[] prepare() throws IOException {
        byte out = 0x00;
        out += 0x01 * close;    //Set the flags in b0 and b1
        out += 0x02 * add;
        byte[] quantityBytes = ProtocolTools.getBytesFromInt(quantity, 4);
        return ProtocolTools.concatByteArrays(new byte[]{out}, quantityBytes);
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.AddCreditBeforeClosing;
    }
}