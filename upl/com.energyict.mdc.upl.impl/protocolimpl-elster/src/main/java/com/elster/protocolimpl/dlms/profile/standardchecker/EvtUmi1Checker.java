package com.elster.protocolimpl.dlms.profile.standardchecker;

import com.elster.dlms.types.data.AbstractDlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.protocolimpl.dlms.profile.UmiEvent;
import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;
import com.elster.protocolimpl.dlms.profile.entrymgmt.CapturedObjects;
import com.elster.protocolimpl.dlms.profile.entrymgmt.CheckingArchiveEntry;

/**
 * User: heuckeg
 * Date: 17.04.13
 * Time: 13:24
 */
public class EvtUmi1Checker implements IArchiveLineChecker
{
    private int index1 = -1;
    private int index2 = -1;

    public void prepareChecker(CheckingArchiveEntry entry, CapturedObjects archiveObjects)
    {
        index1 = archiveObjects.indexOf(entry.getObisCode(), entry.getAttribute());
        index2 = archiveObjects.indexOf(entry.getObisCode(), 4);
    }

    public CheckResult check(Object[] archiveLine)
    {
        int umiEventCode = 0;
        byte[] umiData = new byte[8];
        if (archiveLine[index1] instanceof AbstractDlmsDataInteger)
        {
            umiEventCode = ((AbstractDlmsDataInteger)archiveLine[index1]).getValue();
        }
        if (archiveLine[index2] instanceof DlmsDataOctetString)
        {
            umiData = ((DlmsDataOctetString)archiveLine[index2]).getValue();
        }
        UmiEvent umiEvent = UmiEvent.getUmiEvent(umiEventCode, umiData);
        return new CheckResult(true, umiEvent.getEisEventCode(), umiEvent.getUmiEventCode(), umiEvent.getMsg());
    }
}
