package com.elster.protocolimpl.dlms.profile.standardchecker;

import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;
import com.elster.protocolimpl.dlms.profile.entrymgmt.CapturedObjects;
import com.elster.protocolimpl.dlms.profile.entrymgmt.CheckingArchiveEntry;
import com.energyict.protocol.IntervalStateBits;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 17:29
 */
public class EvtDlmsCheckerV4 implements IArchiveLineChecker
{
    private int index = 0;

    public void prepareChecker(CheckingArchiveEntry entry, CapturedObjects archiveObjects)
    {
        index = archiveObjects.indexOf(entry.getObisCode(), entry.getAttribute());
    }

    public CheckResult check(Object[] archiveLine)
    {
        int code = 0;
        int eisStatus = 0;
        BitString state = getAsBitString(archiveLine[index]);
        if (state != null) {
            if (state.isBitSet(0) || state.isBitSet(1)) {
                eisStatus |= IntervalStateBits.DEVICE_ERROR;
            }
            if (state.isBitSet(2)) {
                eisStatus |= IntervalStateBits.BADTIME;
            }
            // revision 4: ignore all UNI-TS status bits

            if (state.isBitSet(5)) {
                eisStatus |= IntervalStateBits.CORRUPTED;
            }

            if (state.isBitSet(6))
            {
                code = 1;
            }
            if (state.isBitSet(7))
            {
                code += 2;
            }
        }

        return new CheckResult(true, eisStatus, code, "");
    }

    private BitString getAsBitString(Object value)
    {
        if (value instanceof BitString)
        {
            return (BitString)value;
        }
        if (value instanceof DlmsDataUnsigned)
        {
            int i = ((DlmsDataUnsigned)value).getValue();
            int j = Integer.reverse(i);
            int k = Integer.reverseBytes(j);
            return new BitString(8, new byte[] {(byte)(k & 0xFF)});
        }
        return null;
    }
}
