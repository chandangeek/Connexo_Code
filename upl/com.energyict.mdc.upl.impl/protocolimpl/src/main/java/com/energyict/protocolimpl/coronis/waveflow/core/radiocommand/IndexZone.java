package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

public class IndexZone {

    private Date lastDailyLoggedIndex;
    private int dailyIndexOnA;
    private int dailyIndexOnB;
    private int dailyIndexOnC;
    private int dailyIndexOnD;
    private int currentIndexOnA;
    private int expectedIndexOnB;
    private int expectedIndexOnC;
    private int expectedIndexOnD;
    private WaveFlow waveFlow;

    public IndexZone(WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    public IndexZone(WaveFlow waveFlow, int currentIndexOnA, int dailyIndexOnA, int dailyIndexOnB, int dailyIndexOnC, int dailyIndexOnD, int expectedIndexOnB, int expectedIndexOnC, int expectedIndexOnD, Date lastDailyLoggedIndex) {
        this.waveFlow = waveFlow;
        this.currentIndexOnA = currentIndexOnA;
        this.dailyIndexOnA = dailyIndexOnA;
        this.dailyIndexOnB = dailyIndexOnB;
        this.dailyIndexOnC = dailyIndexOnC;
        this.dailyIndexOnD = dailyIndexOnD;
        this.expectedIndexOnB = expectedIndexOnB;
        this.expectedIndexOnC = expectedIndexOnC;
        this.expectedIndexOnD = expectedIndexOnD;
        this.lastDailyLoggedIndex = lastDailyLoggedIndex;
    }

    public WaveFlow getWaveFlow() {
        return waveFlow;
    }

    public void parse(byte[] data, int offset) throws IOException {
        
        lastDailyLoggedIndex = TimeDateRTCParser.parse(data, offset, 7, getWaveFlow().getTimeZone()).getTime();
        offset += 7;
        dailyIndexOnA = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;
        dailyIndexOnB = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;
        dailyIndexOnC = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;
        dailyIndexOnD = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;
        currentIndexOnA = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;
        expectedIndexOnB = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;
        expectedIndexOnC = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;
        expectedIndexOnD = ProtocolTools.getIntFromBytes(data, offset, 4);
        offset += 4;
    }

    public int getDailyIndexOnD() {
        return dailyIndexOnD;
    }

    public int getCurrentIndexOnA() {
        return currentIndexOnA;
    }

    public int getDailyIndexOnA() {
        return dailyIndexOnA;
    }

    public int getDailyIndexOnB() {
        return dailyIndexOnB;
    }

    public int getDailyIndexOnC() {
        return dailyIndexOnC;
    }

    public int getExpectedIndexOnB() {
        return expectedIndexOnB;
    }

    public int getExpectedIndexOnC() {
        return expectedIndexOnC;
    }

    public int getExpectedIndexOnD() {
        return expectedIndexOnD;
    }

    public Date getLastDailyLoggedIndex() {
        return lastDailyLoggedIndex;
    }

    public int getDailyIndex(int channel) {
        if (channel == 0) {
            return getDailyIndexOnA();
        }
        if (channel == 1) {
            return getDailyIndexOnB();
        }
        if (channel == 2) {
            return getDailyIndexOnC();
        }
        if (channel == 3) {
            return getDailyIndexOnD();
        }
        return 0;
    }
}