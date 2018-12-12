package com.elster.protocolimpl.lis200.profile;

import com.elster.utils.lis200.events.Dl220EventInterpreter;
import com.elster.utils.lis200.events.Ek260EventInterpreter;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Test cases to test profile processing
 * <p/>
 * User: heuckeg
 * Date: 30.03.11
 * Time: 11:14
 */
public class Lis200ProfileTest implements ProtocolLink {

    private TimeZone timeZone = TimeZone.getTimeZone("GMT+1");

    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return null;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isIEC1107Compatible() {
        return false;
    }

    public int getNumberOfChannels() throws IOException {
        return 0;
    }

    public String getPassword() {
        return null;
    }

    public byte[] getDataReadout() {
        return new byte[0];
    }

    public int getProfileInterval() throws IOException {
        return 0;
    }

    public ChannelMap getChannelMap() {
        return null;
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }

    public Logger getLogger() {
        return null;
    }

    public int getNrOfRetries() {
        return 0;
    }

    public boolean isRequestHeader() {
        return false;
    }

    @Test
    public void dl220processingTest() throws IOException {

        System.out.println("testDl220processing()");

        String rawData = getResourceAsString("DL220Profile.txt");

        timeZone = TimeZone.getTimeZone("GMT+1");
        Lis200Profile profile = new Lis200Profile(this,
                2, ",,TST,CHN00[C],CHN01[C],ST.1,ST.SY,EVNT,CHKSUM", null, 0, 0,
                new Dl220EventInterpreter());

        profile.setInterval(5); // min
        List<IntervalData> id = profile.buildIntervalData(rawData);

        StringBuilder result = new StringBuilder();
        for (IntervalData i : id) {
            result.append(i);
            result.append("\n");
        }

        String compareData = getResourceAsString("dl220comp.txt");
        assertEquals(result.toString(), compareData);
    }

    @Test
    @Ignore("This test only seems to be working locally. It keeps failing on the build server (TimeZone issue)")
    public void ek260ProcessingTest() throws IOException {

        System.out.println("testEk260processing()");

        String intervalRawData = getResourceAsString("4011220_A3.txt");
        String eventRawData = getResourceAsString("4011220_A4.txt");

        timeZone = TimeZone.getTimeZone("EUROPE/BERLIN");
        Lis200Profile profile = new Lis200Profile(this,
                2, ",,TST,CHN00[C],CHN01[C],CHN02[C],CHN03[C],CHN04,CHN05,CHN06,CHN07,ST1,ST2,ST3,ST4,STSY,EVNT,CKSUM",
                null, 0, 0, new Ek260EventInterpreter());

        profile.setInterval(60);
        List<IntervalData> id = profile.buildIntervalData(intervalRawData);

        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));

        StringBuilder result = new StringBuilder();
        for (IntervalData i : id) {
            result.append(i);
            result.append("\n");
        }

        List<MeterEvent> me = profile.buildEventData(eventRawData);
        for (MeterEvent m : me) {
            result.append(m);
            result.append("\n");
        }

        String compareData = getResourceAsString("4011220_A34comp.txt");
        assertEquals(result.toString(), compareData);
    }

    private String getResourceAsString(String resourceName) {

        StringBuilder stringBuilder = new StringBuilder();

        InputStream stream = Lis200ProfileTest.class.getResourceAsStream(resourceName);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            bufferedReader.close();
        } catch (IOException ignored) {

        }

        return stringBuilder.toString();
    }

}
