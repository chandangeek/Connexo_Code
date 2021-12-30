package com.energyict.protocolimpl.iec1107.abba230;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.TimeZone;

enum Marker {
    NEWDAY(0xE4),
    POWERUP(0xE5),
    POWERDOWN(0xE6),
    CONFIGURATIONCHANGE(0xE8),
    FORCEDENDOFDEMAND(0xE9),
    TIMECHANGE(0xEA),
    PROFILECLEARED(0xEB),
    DAYLIGHTSAVING(0xED),
    ENDOFDATA(0xFF);  // when last packet does not contain 64 (normal mode) or 256 (DS mode) bytes

    private int marker;

    Marker(int levelCode) {
        this.marker = levelCode;
    }

    public int getMarker() {
        return marker;
    }

    public static Marker fromInt(int val) {
        for (Marker type : values()) {
            if (type.getMarker() == val) {
                return type;
            }
        }
        return null;
    }

    public static boolean isMarker( int val ) {
        return fromInt( val ) != null;
    }
}


/**
 * Interface for classes ABBA230LoadProfileEntry and ABBA230InstrumentationProfileEntry.
 * The layout of the byte stream for lad/instrumentation entries has some major differences, so they are kept apart & handled in apart class.
 *
 */
public interface ABBA230ProfileEntry {
    /** */
    void start(ABBA230RegisterFactory registerFactory, ByteArrayInputStream bai, int nrOfChannels) throws IOException;

    int getProfileEntryType();
    /** */
    ProfileConfigRegister getProfileConfig();
    /** */
    int getIntegrationPeriod();
    /** */
    boolean isDST();
    /** */
    long getTime();
    /**
     * @return */
    int getDataStatus();
    /** */
    double[] getValues();
    /** */
    int getNumberOfChannels();
    /** */
    String toString(TimeZone timeZone, boolean dst);
    /** */
    byte[] getChannelMask();
}
