/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.check;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.time.Instant;
import java.util.StringJoiner;

/**
 * Implements the parse used to test the connectivity.
 *
 * 43	length
 * --
 * 1	hard coded value
 * --
 * 42	Device ID
 * 4C
 * 4F
 * 54
 * 30
 * 30
 * 30
 * 31
 * 30
 * 32
 * 30
 * 33
 * 30
 * 30
 * --
 * 42	Mechanical serial
 * 4C
 * 4F
 * 54
 * 30
 * 30
 * 30
 * 31
 * 30
 * 32
 * 30
 * 33
 * 30
 * 30
 * --
 * 0	Configuration number
 * 0
 * 0
 * 0
 * --
 * 0	Device status
 * 0
 * --
 * 3A	TX number
 * --
 * 0	meter index
 * 0
 * 0
 * 0
 * --
 * AB	CRC
 * --
 * CD	CRC
 * --
 * 2	operator iD
 * 6
 * 2
 * 0
 * 1
 * 0
 * --
 * 76	Cell ID
 * 1
 * --
 * A0	RSSI
 * --
 * FA	RSRQ
 * --
 * 12	TX power
 * --
 * 0	ECL
 * --
 * 78	Latitude
 * 56
 * 34
 * 12
 * --
 * 78	Longitude
 * 56
 * 34
 * 12
 * --
 * 18	UTC
 * 96
 * DB
 * 63
 */
public class CheckFrameParser {
    private final String frame;

    public CheckFrameParser(byte[] payload) {
        this(new String(payload));
    }

    private enum Indexes {
        PKT_LENGTH          ( 0 ),  // is it really length?
        PKT_MAGIC           ( 1 ),
        TEST_NBIOT_DEVID    ( 2 ),
        TEST_NBIOT_MECID    ( 16 ),
        CONFIG_NUMBER       ( 30 ),
        DEVICE_STATUS       ( 34 ),  //! index
        TEST_TX_NUMBER      ( 36 ),  // Hourly Log
        CUR_MET_INDEX       ( 37 ),
        CRC                 ( 41 ),
        OPERATER_ID         ( 43 ),
        CELL_ID             ( 49 ),
        SIGNAL_STRENGTH     ( 51 ),
        SIGNAL_QUALITY      ( 52 ),
        TX_POWER            ( 53 ),
        EXTERNAL_COVERAGE   ( 54 ),
        LATTITUDE_POS       ( 55 ),
        LONGITUDE_POS       ( 59 ),
        DATETIME            ( 63 );

        private final int idx;

        Indexes(int i) {
            this.idx = i;
        }

        public int getIdx() {
            return idx;
        }

        public boolean isLast() {
            return this.ordinal() == values().length - 1;
        }

        public Indexes next() {
            return values()[(this.ordinal() + 1) % values().length];
        }

        static Indexes find(int idx) {
            for (Indexes i : Indexes.values()) {
                if (i.getIdx() == idx) {
                    return i;
                }
            }
            return null;
        }
    }


    public CheckFrameParser(String frame) {
        if (frame != null) {
            this.frame = frame.replace(" ", "");
        } else {
            this.frame = "";
        }
    }

    public boolean isCheckFrame() {
        return frame != null
                && frame.length() == 134
                && extractInt(Indexes.PKT_LENGTH) == 0x43
                && extractInt(Indexes.PKT_MAGIC) == 0x01;
    }

    @Override
    public String toString() {
        StringJoiner json = new StringJoiner(",");
        json.add(jsonElement("deviceId", getDeviceId()));
        json.add(jsonElement("mechanicalId", getMechanicalId()));
        json.add(jsonElement("configNr", getConfigNumber()));
        json.add(jsonElement("deviceStatus", getDeviceStatus()));
        json.add(jsonElement("txNr", getTextTxNumber()));
        json.add(jsonElement("meterIndex", getMeterIndex()));
        json.add(jsonElement("crc", getCRC()));
        json.add(jsonElement("operatorId", getOperatorId()));
        json.add(jsonElement("cellId", getCellId()));
        json.add(jsonElement("signalStrength", getSignalStrength()));
        json.add(jsonElement("signalQuality", getSignalQuality()));
        json.add(jsonElement("txPower", getTxPower()));
        json.add(jsonElement("ecl", getECL()));
        json.add(jsonElement("lat", getLatitude()));
        json.add(jsonElement("lng", getLongitude()));
        json.add(jsonElement("utc", getDateTimeUtc().toString()));

        return "{" + json + "}";
    }

    private String jsonElement(String name, String value) {
        return "\"" + name + "\":\"" + value + "\"";
    }

    private String jsonElement(String name, long value) {
        return "\"" + name + "\":" + value ;
    }

    private String extract(Indexes begin) {
        int startIdx = begin.getIdx();
        int endIdx;
        if (begin.isLast()) {
            endIdx = frame.length() / 2;
        } else {
            Indexes end = begin.next();
            endIdx = end.getIdx();
        }
        String s = frame.substring(startIdx * 2, endIdx * 2);
        return s;
    }

    private int extractInt(Indexes begin) {
        return Integer.parseInt(extract(begin), 16);
    }

    private String extractBCD(Indexes start) {
        StringJoiner bcd = new StringJoiner("");
        String hexString = extract(start);
        for (int i=0; i< hexString.length() / 2; i++) {

            int v = Integer.parseInt(hexString.substring(i * 2, i * 2 + 2));
            bcd.add(String.format("%d",v));
        }
        return bcd.toString();
    }

    public int getLength() {
        return extractInt(Indexes.PKT_LENGTH);
    }

    public int getMagicFixed() {
        return extractInt(Indexes.PKT_MAGIC);
    }

    public String getDeviceId() {
        String zeroPadded = new String(ProtocolTools.getBCDFromHexString(extract(Indexes.TEST_NBIOT_DEVID),2));
        if (zeroPadded.length() > 1) {
            if (zeroPadded.getBytes()[0] == '0') {
                return zeroPadded.substring(1);
            }
        }
        return zeroPadded;
    }

    public String getMechanicalId() {
        return new String(ProtocolTools.getBCDFromHexString(extract(Indexes.TEST_NBIOT_MECID),2));
    }

    public String getConfigNumber() {
        return extract(Indexes.CONFIG_NUMBER);
    }

    public String getDeviceStatus() {
        return extract(Indexes.DEVICE_STATUS);
    }

    public int getTextTxNumber() {
        return extractInt(Indexes.TEST_TX_NUMBER);
    }


    public long getMeterIndex() {
        return longFromReversedHex(extract(Indexes.CUR_MET_INDEX));
    }

    public String getCRC() {
        return extract(Indexes.CRC);
    }

    public String getOperatorId(){
        return extractBCD(Indexes.OPERATER_ID);
    }

    public int getCellId() {
        String v = extract(Indexes.CELL_ID);
        return Integer.parseInt(v.substring(0, 2), 16) + Integer.parseInt(v.substring(2, 4), 16) * 0x100;
    }

    public int getRSSI() {
        return extractInt(Indexes.SIGNAL_STRENGTH);
    }

    public int getSignalStrength() {
        return getRSSI();
    }

    public int getRSRQ() {
        return extractInt(Indexes.SIGNAL_QUALITY);
    }

    public int getSignalQuality() {
        return getRSRQ();
    }

    public int getTxPower() {
        return extractInt(Indexes.TX_POWER);
    }

    public int getECL() {
        return extractInt(Indexes.EXTERNAL_COVERAGE);
    }

    public String getLatitude() {
        return extract(Indexes.LATTITUDE_POS);
    }

    public String getLongitude() {
        return extract(Indexes.LONGITUDE_POS);
    }

    private String reverseHex(String reversedHex) {
        byte[] u = ProtocolTools.getBytesFromHexString(reversedHex, 2);
        for (int i=0; i < u.length / 2; i++) {
            byte b = u[i];
            u[i] = u[u.length - i - 1];
            u[u.length - i -1] = b;
        }
        return ProtocolTools.getHexStringFromBytes(u, "");
    }

    private long longFromReversedHex(String reversedHex) {
        return Long.parseLong(reverseHex(reversedHex), 16);
    }

    public Instant getDateTimeUtc() {
        String utcReversed = extract(Indexes.DATETIME);

        return Instant.ofEpochSecond(longFromReversedHex(utcReversed));
    }
}
