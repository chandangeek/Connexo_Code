package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Calendar;

public class InitialisationCode {

    private Calendar activationDate;
    private String CIA;
    private String VF;
    private int firmwareSize;
    private int numberOfTotalSegments;

    public InitialisationCode(Calendar activationDate, String CIA, String VF, int firmwareSize) {
        this.activationDate = activationDate;
        this.CIA = CIA;
        this.VF = VF;
        this.firmwareSize = firmwareSize;
        this.numberOfTotalSegments = (int) Math.ceil((double) firmwareSize/ (double) GprsRequestFactory.LENGTH_CODE_PER_REQUEST);
    }

    public byte[] getBytes() {
        byte[] Year = new byte[]{(byte) (activationDate.get(Calendar.YEAR) - 2000)};
        byte[] Month = new byte[]{(byte) (activationDate.get(Calendar.MONTH) + 1)}; // Java Calendar months are 0-based - send 1-based to device.
        byte[] Day = new byte[]{(byte) activationDate.get(Calendar.DAY_OF_MONTH)};
        byte[] bCIA = CIA.getBytes();
        byte[] bVF = VF.getBytes();
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