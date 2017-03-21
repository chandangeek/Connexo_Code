package com.elster.us.protocolimplv2.sel.profiles;

import com.elster.us.protocolimplv2.sel.SELProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import org.junit.Test;
import org.mockito.Mock;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LoadProfileEIServerFormatterTest {

    @Mock
    private PropertySpecService propertySpecService;

    @Test
    public void testLPStatus() throws IOException {
        SELProperties properties = new SELProperties(propertySpecService);
        File lpFile = new File("src/test/files/LDP1_DATA.BIN");
        LDPParser ldpParser = new LDPParser();
        LDPData results = ldpParser.parseYModemFile(new DataInputStream(new FileInputStream(lpFile)));
        LoadProfileEIServerFormatter formatter = new LoadProfileEIServerFormatter(results, properties);
        List<IntervalData> data = new ArrayList<IntervalData>();
        List<Integer> channelIndexes = new ArrayList<Integer>();
        channelIndexes.add(0);
        channelIndexes.add(1);
        data = formatter.getIntervalData(channelIndexes);
        int allStatuses = IntervalStateBits.POWERDOWN + IntervalStateBits.BADTIME + IntervalStateBits.OTHER + IntervalStateBits.CORRUPTED + IntervalStateBits.TEST;
        assertEquals(data.get(1).getEiStatus(), allStatuses);
        assertEquals(data.get(2).getEiStatus(), IntervalStateBits.TEST);
        assertEquals(data.get(3).getEiStatus(), 0);
    }

}
