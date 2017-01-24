package com.energyict.protocolimpl.iec1107.abba230;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.TimeZone;

/**
 * Interface for classes ABBA230LoadProfileEntry and ABBA230InstrumentationProfileEntry.
 * The layout of the byte stream for lad/instrumentation entries has some major differences, so they are kept apart & handled in apart class.
 *
 */

public interface ABBA230ProfileEntry {

    // type attribute:
    // markers
    static public final int POWERUP = 0xE5;
    static public final int CONFIGURATIONCHANGE = 0xE8;
    static public final int POWERDOWN = 0xE6;
    static public final int NEWDAY = 0xE4;
    static public final int TIMECHANGE = 0xEA;
    static public final int DAYLIGHTSAVING = 0xED;
    static public final int PROFILECLEARED = 0xEB;
    static public final int FORCEDENDOFDEMAND = 0xE9;
    // when last packet does not contain 64 (normal mode) or 256 (DS mode) bytes
    static public final int ENDOFDATA = 0xFF;

    void start(ABBA230RegisterFactory registerFactory, ByteArrayInputStream bai, int nrOfChannels) throws IOException;

    void init() throws IOException;

    int getType();

    ProfileConfigRegister getProfileConfig();

    int getIntegrationPeriod();

    boolean isDST();

    long getTime();

    boolean isMarker();

    int getStatus();

    double[] getValues();

    int getNumberOfChannels();

    String toString(TimeZone timeZone, boolean dst);

    byte[] getChannelmask();
}
