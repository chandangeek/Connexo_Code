package com.elster.protocolimpl.dlms.profile.standardchecker;

import com.elster.dlms.types.data.AbstractDlmsDataInteger;
import com.elster.protocolimpl.dlms.profile.UnitsEvent;
import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;
import com.elster.protocolimpl.dlms.profile.entrymgmt.CapturedObjects;
import com.elster.protocolimpl.dlms.profile.entrymgmt.CheckingArchiveEntry;

/**
 * User: heuckeg
 * Date: 30.08.13
 * Time: 13:47
 */
public class EvtUnits1Checker implements IArchiveLineChecker
{

    private int index = -1;

    public void prepareChecker(CheckingArchiveEntry entry, CapturedObjects archiveObjects)
    {
        index = archiveObjects.indexOf(entry.getObisCode(), entry.getAttribute());
    }

    public CheckResult check(Object[] archiveLine)
    {
        int unitsEventCode = 0;
        if (archiveLine[index] instanceof AbstractDlmsDataInteger)
        {
            unitsEventCode = ((AbstractDlmsDataInteger)archiveLine[index]).getValue();
        }
        if (archiveLine[index] instanceof Integer)
        {
            unitsEventCode = (Integer)archiveLine[index];
        }

        UnitsEvent event = UnitsEvent.findEvent(unitsEventCode);
        return new CheckResult(true, event.getEisEventCode(), event.getUnitsEventCode(), event.getMsg());
    }
}
