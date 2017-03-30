/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.GprsRequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.CIA;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.VF;

import java.util.Calendar;

public class InitialisationCode {

    private Calendar activationDate;
    private CIA cia;
    private VF vf;
    private int firmwareSize;
    private int numberOfTotalSegments;

    public InitialisationCode(Calendar activationDate, CIA cia, VF vf, int firmwareSize, boolean useLongFrameFormat) {
        this.activationDate = activationDate;
        this.cia = cia;
        this.vf = vf;
        this.firmwareSize = firmwareSize;
        this.numberOfTotalSegments = (int) Math.ceil((double) firmwareSize / (double) (useLongFrameFormat ? GprsRequestFactory.LENGTH_CODE_PER_REQUEST_LONG_FRAMES : GprsRequestFactory.LENGTH_CODE_PER_REQUEST_SHORT_FRAMES));
    }

    public byte[] getBytes() {
        byte[] Year = new byte[]{(byte) (activationDate.get(Calendar.YEAR) - 2000)};
        byte[] Month = new byte[]{(byte) (activationDate.get(Calendar.MONTH) + 1)}; // Java Calendar months are 0-based - send 1-based to device.
        byte[] Day = new byte[]{(byte) activationDate.get(Calendar.DAY_OF_MONTH)};
        byte[] bCIA = cia.getBytes();
        byte[] bVF = vf.getBytes();
        byte[] LS = ProtocolTools.getBytesFromInt(firmwareSize, 4);
        byte[] NS = ProtocolTools.getBytesFromInt(numberOfTotalSegments, 2);

        return ProtocolTools.concatByteArrays(
                Year,
                Month,
                Day,
                bCIA,
                bVF,
                LS,
                NS
        );
    }
}
