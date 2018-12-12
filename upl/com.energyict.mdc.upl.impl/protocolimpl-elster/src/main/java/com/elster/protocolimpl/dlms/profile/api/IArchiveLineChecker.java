package com.elster.protocolimpl.dlms.profile.api;

import com.elster.protocolimpl.dlms.profile.entrymgmt.CapturedObjects;
import com.elster.protocolimpl.dlms.profile.entrymgmt.CheckingArchiveEntry;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 17:23
 */
public interface IArchiveLineChecker
{

    /**
     * Initialization for checker...
     *
     * @param archiveObjects - array of objects in archive
     */
    public void prepareChecker(CheckingArchiveEntry entry, CapturedObjects archiveObjects);

    /**
     * Checks status/events of a line
     *
     * @param archiveLine - archive line data
     * @return eisStatus - result is true if data of line can be used
     *                   - eisStatus contains ei server status
     */
    public CheckResult check(Object[] archiveLine);

    /**
     * function "result" class
     */
    @SuppressWarnings({"unused"})
    public static class CheckResult
    {
        private final boolean result;
        private final int eisStatus;
        private final int code;
        private final String msg;

        public CheckResult(final boolean result, final int eisStatus, final int code, final String msg)
        {
            this.result = result;
            this.eisStatus = eisStatus;
            this.code = code;
            this.msg = msg;
        }

        public CheckResult(final boolean result, final int eisStatus)
        {
            this(result, eisStatus, 0, "");
        }

        public boolean getResult()
        {
            return result;
        }

        public int getEisStatus()
        {
            return eisStatus;
        }

        public int getCode()
        {
            return code;
        }

        public String getMsg()
        {
            return msg;
        }
    }
}
