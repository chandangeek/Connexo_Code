package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.types.basic.ObisCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: heuckeg
 * Date: 05.09.12
 * Time: 12:03
 */
public class TestArchiveStructure {

    @Test
    public void testArchiveStructureEntryConstructing() {
        ArchiveStructure.ArchiveStructureEntry entry;

        entry = new ArchiveStructure.ArchiveStructureEntry(-1, false, 0, new ObisCode("1.2.3.4.5.6"), 4);
        assertEquals("TST=1.2.3.4.5.6A4", entry.toString());

        entry = new ArchiveStructure.ArchiveStructureEntry(-2, false, 0, new ObisCode("2.3.4.5.6.7"), 2);
        assertEquals("EVT_L2=2.3.4.5.6.7A2", entry.toString());

        entry = new ArchiveStructure.ArchiveStructureEntry(-3, false, 0, new ObisCode("3.4.5.6.7.8"), 6);
        assertEquals("SYS=3.4.5.6.7.8A6", entry.toString());

        entry = new ArchiveStructure.ArchiveStructureEntry(-4, false, 0, new ObisCode("4.5.6.7.8.9"), 8);
        assertEquals("SP-4=4.5.6.7.8.9A8", entry.toString());

        entry = new ArchiveStructure.ArchiveStructureEntry(0, false, 0, new ObisCode("1.2.3.4.5.255"), 2);
        assertEquals("CHN0=1.2.3.4.5.255A2", entry.toString());

        entry = new ArchiveStructure.ArchiveStructureEntry(1, true, 8, new ObisCode("1.2.3.4.5.255"), 2);
        assertEquals("CHN1[C]=1.2.3.4.5.255A2", entry.toString());

        entry = new ArchiveStructure.ArchiveStructureEntry(2, true, 9, new ObisCode("1.2.3.4.5.255"), 2);
        assertEquals("CHN2[C9]=1.2.3.4.5.255A2", entry.toString());
    }

    @Test
    public void testArchiveStructureEntryParsing() {
        ArchiveStructure.ArchiveStructureEntry entry;

        entry = ArchiveStructure.ArchiveStructureEntry.parse("TST=1.2.3.4.5.6A4");
        assertEquals("TST=1.2.3.4.5.6A4", entry.toString());

        entry = ArchiveStructure.ArchiveStructureEntry.parse("CHN0=1.2.3.4.5.255");
        assertEquals("CHN0=1.2.3.4.5.255A2", entry.toString());

        entry = ArchiveStructure.ArchiveStructureEntry.parse("CHN1[C]=1.2.3.4.5.255A2");
        assertEquals("CHN1[C]=1.2.3.4.5.255A2", entry.toString());

        entry = ArchiveStructure.ArchiveStructureEntry.parse("CHN2[C9]=1.2.3.4.5.255A2");
        assertEquals("CHN2[C9]=1.2.3.4.5.255A2", entry.toString());

        entry = ArchiveStructure.ArchiveStructureEntry.parse("EVT=2.3.4.5.6.7A2");
        assertEquals("EVT_L2=2.3.4.5.6.7A2", entry.toString());

        entry = ArchiveStructure.ArchiveStructureEntry.parse("SYS=3.4.5.6.7.8A6");
        assertEquals("SYS=3.4.5.6.7.8A6", entry.toString());

        entry = ArchiveStructure.ArchiveStructureEntry.parse("SYS=4.5.6.7.8.9");
        assertEquals("SYS=4.5.6.7.8.9A2", entry.toString());

        entry = ArchiveStructure.ArchiveStructureEntry.parse("EVT_L2=4.5.6.7.8.9");
        assertEquals("EVT_L2=4.5.6.7.8.9A2", entry.toString());

        entry = ArchiveStructure.ArchiveStructureEntry.parse("EVT_DLMS=4.5.6.7.8.9");
        assertEquals("EVT_DLMS=4.5.6.7.8.9A2", entry.toString());
    }

    @Test
    public void testArchiveStructureCreation() {
        /* EK280 V2
   classId=1, logicalName=0.128.96.8.67.255, attributeIndex=2, dataIndex=0 = ONO
   classId=8, logicalName=0.0.1.0.0.255, attributeIndex=2, dataIndex=0     = TST
   classId=3, logicalName=7.0.11.2.0.255, attributeIndex=2, dataIndex=0    = Forward undisturbed converter volume, index, current value at base conditions, Total
   classId=3, logicalName=7.0.13.2.0.255, attributeIndex=2, dataIndex=0    = Vb total quantity
   classId=3, logicalName=7.0.11.0.0.255, attributeIndex=2, dataIndex=0    = Forward undisturbed converter volume, index, current value at measuring conditions
   classId=3, logicalName=7.0.13.0.0.255, attributeIndex=2, dataIndex=0    = Vm total quantity
   classId=3, logicalName=7.0.42.42.0.255, attributeIndex=2, dataIndex=0   = Absolute pressure, average, last interval, process interval 2 (default = 1 hour)
   classId=3, logicalName=7.0.41.42.0.255, attributeIndex=2, dataIndex=0   = Absolute temperature, average, last interval, process interval 2 (default = 1 hour)
   classId=3, logicalName=7.0.53.0.16.255, attributeIndex=2, dataIndex=0   = Compressibility factor, current value at measuring conditions, average, current interval ( default 1 hour)
   classId=3, logicalName=7.0.52.0.16.255, attributeIndex=2, dataIndex=0   = k factor, current value at measuring conditions, average, current interval ( default 1 hour)
   classId=1, logicalName=7.128.96.5.2.255, attributeIndex=2, dataIndex=0  = Momentary status 2
   classId=1, logicalName=7.128.96.5.4.255, attributeIndex=2, dataIndex=0  = Momentary status 4
   classId=1, logicalName=7.128.96.5.7.255, attributeIndex=2, dataIndex=0  = Momentary status 7
   classId=1, logicalName=7.128.96.5.6.255, attributeIndex=2, dataIndex=0  = Momentary status 6
   classId=1, logicalName=7.129.96.5.2.255, attributeIndex=2, dataIndex=0  = system status
   classId=3, logicalName=7.0.97.97.2.255, attributeIndex=2, dataIndex=0   = error register interval profile
   classId=1, logicalName=7.128.96.5.67.255, attributeIndex=2, dataIndex=0 = event
   classId=1, logicalName=7.0.129.10.33.3, attributeIndex=2, dataIndex=0   = globl. ONO
        */
        String definition = "TST=0.0.1.0.0.255" + "," +
                "CHN0[C9]=7.0.11.2.0.255" + "," +
                "CHN1[C9]=7.0.13.2.0.255" + "," +
                "CHN2[C9]=7.0.11.0.0.255" + "," +
                "CHN3[C9]=7.0.13.0.0.255" + "," +
                "CHN4=7.0.42.42.255.255" + "," +
                "CHN5=7.0.41.42.255.255" + "," +
                "CHN6=7.0.53.0.16.255" + "," +
                "CHN7=7.0.52.0.16.255" + "," +
                "SYS=7.129.96.5.2.255" + "," +
                "EVT_L2=7.128.96.5.67.255";
        ArchiveStructure structure = new ArchiveStructure(definition);

        assertTrue(structure.hasTSTEntry());
        assertTrue(structure.hasEventEntry());
        assertTrue(structure.hasSystemStateEntry());
        assertEquals(8, structure.channelCount());
        assertEquals("TST=0.0.1.0.0.255A2", structure.getTSTEntry().toString());
        assertEquals("SYS=7.129.96.5.2.255A2", structure.getSystemStateEntry().toString());
        assertEquals("EVT_L2=7.128.96.5.67.255A2", structure.getEventEntry().toString());
        assertEquals("CHN0[C9]=7.0.11.2.0.255A2", structure.getChannelEntry(0).toString());
        assertEquals("CHN1[C9]=7.0.13.2.0.255A2", structure.getChannelEntry(1).toString());
        assertEquals("CHN2[C9]=7.0.11.0.0.255A2", structure.getChannelEntry(2).toString());
        assertEquals("CHN3[C9]=7.0.13.0.0.255A2", structure.getChannelEntry(3).toString());
        assertEquals("CHN4=7.0.42.42.255.255A2", structure.getChannelEntry(4).toString());
        assertEquals("CHN5=7.0.41.42.255.255A2", structure.getChannelEntry(5).toString());
        assertEquals("CHN6=7.0.53.0.16.255A2", structure.getChannelEntry(6).toString());
        assertEquals("CHN7=7.0.52.0.16.255A2", structure.getChannelEntry(7).toString());

    }
}
