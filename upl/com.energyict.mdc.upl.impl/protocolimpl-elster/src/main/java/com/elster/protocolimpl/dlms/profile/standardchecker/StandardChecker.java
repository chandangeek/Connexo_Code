package com.elster.protocolimpl.dlms.profile.standardchecker;

import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;

import java.util.HashMap;

/**
 * User: heuckeg
 * Date: 17.04.13
 * Time: 10:52
 */
public class StandardChecker
{
    public static HashMap<String, IArchiveLineChecker> getDefault()
    {
        HashMap<String, IArchiveLineChecker> result = new HashMap<String, IArchiveLineChecker>();
        result.put("EVT_DLMS", new EvtDlmsChecker());
        result.put("EVT_DLMSV3", new EvtDlmsCheckerV3());
        result.put("EVT_DLMSV4", new EvtDlmsCheckerV4());
        result.put("EVT_UMI1", new EvtUmi1Checker());
        result.put("EVT_UNITS1", new EvtUnits1Checker());
        return result;
    }
}
