package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.cosem.classes.class03.Unit;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;
import com.elster.protocolimpl.dlms.profile.standardchecker.EvtDlmsChecker;
import org.junit.Test;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 13:36
 */
public class TestArchiveEntries
{
    @Test
    public void testEntryCreation()
    {
        AbstractArchiveEntry entry = new TimeStampEntry(new ObisCode(1,2,3,4,5,6), 2);
        assertEquals("TST=1.2.3.4.5.6A2", entry.toString());

        entry = new ChannelArchiveEntry(new ObisCode(1,2,3,4,5,6), 2, 2, false, 8);
        assertEquals("CHN2=1.2.3.4.5.6A2", entry.toString());

        entry = new ChannelArchiveEntry(new ObisCode(1,2,3,4,5,6), 3, 3, true, 8);
        assertEquals("CHN3[C]=1.2.3.4.5.6A3", entry.toString());

        entry = new ChannelArchiveEntry(new ObisCode(1,2,3,4,5,6), 4, 4, true, 9);
        assertEquals("CHN4[C9]=1.2.3.4.5.6A4", entry.toString());
    }

    @Test
    public void testExtendedEntryCreation()
    {
        ChannelArchiveEntry entry = new ChannelArchiveEntry(new ObisCode(1,2,3,4,5,6), 2, 2, false, 8);
        entry.setScaler(-3);
        assertEquals("CHN2[S:-3]=1.2.3.4.5.6A2", entry.toString());

        entry = new ChannelArchiveEntry(new ObisCode(1,2,3,4,5,6), 2, 2, false, 8);
        entry.setUnit(Unit.CUBIC_METRE_VOLUME);
        assertEquals("CHN2[U:m3]=1.2.3.4.5.6A2", entry.toString());

        entry = new ChannelArchiveEntry(new ObisCode(1,2,3,4,5,6), 2, 2, false, 8);
        entry.setScaler(-2);
        entry.setUnit(Unit.CUBIC_METRE_VOLUME);
        assertEquals("CHN2[S:-2U:m3]=1.2.3.4.5.6A2", entry.toString());

        entry = new ChannelArchiveEntry(new ObisCode(1,2,3,4,5,6), 4, 4, true, 9);
        entry.setScaler(-2);
        entry.setUnit(Unit.CUBIC_METRE_VOLUME);
        assertEquals("CHN4[C9S:-2U:m3]=1.2.3.4.5.6A4", entry.toString());
    }

    @Test
    public void testArchiveStructureFactory()
    {
        AbstractArchiveEntry entry;

        HashMap<String, IArchiveLineChecker> checkerList = new HashMap<String, IArchiveLineChecker>();

        checkerList.put("EVT_DLMS", new EvtDlmsChecker());

        entry = ArchiveStructureFactory.parseArchiveStructureDefinition("TST=1.2.3.4.5.6A2", checkerList);
        assertTrue(entry instanceof TimeStampEntry);
        assertEquals("TST=1.2.3.4.5.6A2", entry.toString());

        entry = ArchiveStructureFactory.parseArchiveStructureDefinition("CHN4[C9]=1.2.3.4.5.6A4", checkerList);
        assertTrue(entry instanceof ChannelArchiveEntry);
        assertEquals("CHN4[C9]=1.2.3.4.5.6A4", entry.toString());

        entry = ArchiveStructureFactory.parseArchiveStructureDefinition("EVT_DLMS=1.2.3.4.5.6A4", checkerList);
        assertTrue(entry instanceof CheckingArchiveEntry);
        assertEquals("EVT_DLMS=1.2.3.4.5.6A4", entry.toString());

        entry = ArchiveStructureFactory.parseArchiveStructureDefinition("CHN2[C9s:-3u:m3]=1.2.3.4.5.6A2", checkerList);
        assertTrue(entry instanceof ChannelArchiveEntry);
        assertEquals("CHN2[C9S:-3U:m3]=1.2.3.4.5.6A2", entry.toString());

        entry = ArchiveStructureFactory.parseArchiveStructureDefinition("CHN2[C9s:-3u:14]=1.2.3.4.5.6A2", checkerList);
        assertTrue(entry instanceof ChannelArchiveEntry);
        assertEquals("CHN2[C9S:-3U:m3]=1.2.3.4.5.6A2", entry.toString());
    }
}
