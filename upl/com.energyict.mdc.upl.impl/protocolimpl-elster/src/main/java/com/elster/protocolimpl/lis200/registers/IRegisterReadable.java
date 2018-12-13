package com.elster.protocolimpl.lis200.registers;

import com.elster.protocolimpl.lis200.utils.RawArchiveLineInfo;

import java.io.IOException;
import java.util.TimeZone;

/**
 * interface for devices with the capability to get historical data
 *
 * User: heuckeg
 * Date: 19.04.11
 * Time: 09:13
 */
public interface IRegisterReadable {

    /* get an array with register definitions */
    public RegisterDefinition[] getRegisterDefinition();
    /* get begin of day for a specific archive range */
    public int getBeginOfDay() throws IOException;
    /* timezone of device */
    public TimeZone getTimeZone();
    /* get access data for an historical archive */
    public HistoricalArchive getHistoricalArchive(int archive);
    /* get parse information for a value in an historical archive */
    public RawArchiveLineInfo getArchiveLineInfo(int archive, String value);
}
