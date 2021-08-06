package com.energyict.protocolimplv2.umi.ei4.structures;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UmiHelper {
    public static final Date UMI_ZERO_DATE = Date.from(Instant.from(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)));

    public static char[] convertBytesToChars(byte[] source) {
        char[] destination = new char[source.length];
        for (int i = 0; i < source.length; i++) {
            destination[i] = ((char) source[i]);
        }
        return destination;
    }

    public static byte[] convertCharsToBytes(char[] source) {
        byte[] destination = new byte[32];
        for (int i = 0; i < 32; i++) {
            if (i < source.length) {
                destination[i] = ((byte) source[i]);
            } else {
                destination[i] = 0x00;
            }
        }
        return destination;
    }

    public static Date convertToDateFromUmiFormat(long secondsFrom2000) {
        OffsetDateTime utc = OffsetDateTime.of(2000, 01, 01, 00, 00, 00, 00, ZoneOffset.UTC);
        long utcReferentSeconds = utc.toEpochSecond();
        return new Date(TimeUnit.SECONDS.toMillis(utcReferentSeconds + secondsFrom2000));
    }

    public static long convertToUmiFormatFromDate(Date date) {
        OffsetDateTime utc = OffsetDateTime.of(2000, 01, 01, 00, 00, 00, 00, ZoneOffset.UTC);
        return (TimeUnit.MILLISECONDS.toSeconds(date.getTime()) - utc.toEpochSecond());
    }

    public static long convertToUmiFormatFromInstant(Instant lastErrorTime) {
        Instant instant = Instant.from(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        return lastErrorTime.getEpochSecond() - instant.getEpochSecond();
    }

    public static Instant convertToInstantFromUmiFormat(long toUnsignedLong) {
        Instant instant = Instant.from(ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        return instant.plusSeconds(toUnsignedLong);
    }
}
