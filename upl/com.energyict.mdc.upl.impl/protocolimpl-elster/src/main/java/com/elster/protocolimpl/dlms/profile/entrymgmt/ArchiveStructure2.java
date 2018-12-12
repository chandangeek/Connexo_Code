package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.class03.Unit;
import com.elster.dlms.cosem.classes.class07.CaptureObjectDefinition;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleRegisterObject;
import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 15:28
 */
public class ArchiveStructure2
{
    private final ArrayList<AbstractArchiveEntry> entries;
    private TimeStampEntry tst;
    private final ArrayList<ChannelArchiveEntry> channels;
    private final ArrayList<CheckingArchiveEntry> stats;

    public ArchiveStructure2(final String structureString, HashMap<String, IArchiveLineChecker> checkerList) throws IOException
    {
        Integer channelScaler = null;

        entries = new ArrayList<AbstractArchiveEntry>();
        channels = new ArrayList<ChannelArchiveEntry>();
        stats = new ArrayList<CheckingArchiveEntry>();
        tst = null;

        // optional scaler for all channels set?
        String[] structureDefs = structureString.split("\\|");
        if (structureDefs.length > 1)
        {
            String s = structureDefs[1];
            if (s.startsWith("S:"))
            {
                try
                {
                    channelScaler = Integer.parseInt(s.substring(2));
                }
                catch (NumberFormatException ignore)
                {
                }
            }
        }

        String[] entryDefs = structureDefs[0].split(",");
        for (String entryDef : entryDefs)
        {
            AbstractArchiveEntry entry = ArchiveStructureFactory.parseArchiveStructureDefinition(entryDef, checkerList);
            entries.add(entry);
            if (entry instanceof TimeStampEntry)
            {
                if (tst == null)
                {
                    tst = (TimeStampEntry) entry;
                }
                continue;
            }
            if (entry instanceof ChannelArchiveEntry)
            {
                ChannelArchiveEntry channelEntry = (ChannelArchiveEntry) entry;
                if ((channelScaler != null) && !channelEntry.isScalerSet())
                {
                    channelEntry.setScaler(channelScaler);
                }
                channels.add(channelEntry);
                continue;
            }
            if (entry instanceof CheckingArchiveEntry)
            {
                stats.add((CheckingArchiveEntry) entry);
            }
        }

        if (tst == null)
        {
            throw new IOException("No time stamp entry in archive structure");
        }
    }

    public List<AbstractArchiveEntry> getEntries()
    {
        return entries;
    }

    public int getChannelCount()
    {
        return channels.size();
    }

    public void prepareForProcessing(SimpleProfileObject profileObject) throws IOException
    {
        CapturedObjects archiveObjects = getCapturedObjects(profileObject);

        for (AbstractArchiveEntry entry : entries)
        {
            int index = archiveObjects.indexOf(entry.getObisCode(), entry.getAttribute());
            if (index < 0)
            {
                throw new IOException("Missing object in archive:" + entry.toString());
            }
            entry.setIndex(index);
            if (entry instanceof ChannelArchiveEntry)
            {
                ChannelArchiveEntry channelEntry = (ChannelArchiveEntry) entry;
                if (!channelEntry.isScalerSet() || !channelEntry.isUnitSet())
                {
                    CaptureObjectDefinition co = archiveObjects.get(index);
                    if ((co.getClassId() == 3) && (co.getAttributeIndex() == 2))
                    {
                        checkScalerAndUnit(profileObject, (ChannelArchiveEntry) entry);
                    }
                }
            }
            if (entry instanceof CheckingArchiveEntry)
            {
                ((CheckingArchiveEntry) entry).prepareChecker(archiveObjects);
            }
        }
    }

    private void checkScalerAndUnit(SimpleProfileObject profileObject, ChannelArchiveEntry entry)
    {
        try
        {
            ScalerUnit su = getScalerUnitFromRelatedObject(profileObject, entry.getIndex());
            if (!entry.isScalerSet())
            {
                entry.setScaler(su.getScaler());
            }
            if (!entry.isUnitSet())
            {
                entry.setUnit(su.getUnit());
            }
        }
        catch (IOException e)
        {
            String oc = entry.getObisCode().toString();
            if ((oc.equals("7.0.13.83.0.255") || (oc.equals("7.0.12.82.0.255"))))
            {
                if (!entry.isScalerSet())
                {
                    entry.setScaler(-3);
                }
                if (!entry.isUnitSet())
                {
                    entry.setUnit(Unit.CUBIC_METRE_CORRECTED_VOLUME);
                }
            }
        }
    }

    protected CapturedObjects getCapturedObjects(SimpleProfileObject profileObject) throws IOException
    {
        return new CapturedObjects(profileObject.getCaptureObjects());
    }

    protected ScalerUnit getScalerUnitFromRelatedObject(SimpleProfileObject profileObject, int index) throws IOException
    {
        final SimpleCosemObject relatedObject = profileObject.getRelatedObject(index);
        return ((SimpleRegisterObject) relatedObject).getScalerUnit();
    }

    public List<ChannelInfo> buildChannelInfo()
    {
        List<ChannelInfo> channelInfo = new ArrayList<ChannelInfo>();

        for (ChannelArchiveEntry entry : channels)
        {
            channelInfo.add(entry.toChannelInfo());
        }
        return channelInfo;
    }

    public IntervalData processProfileLine(Object[] data, TimeZone timezone)
    {
        int tariffCode = 0;
        int eisStatus = 0;

        // time stamp evaluation
        Date date = tst.toDate(data, timezone, eisStatus);

        // status valuation
        for (CheckingArchiveEntry entry : stats)
        {
            IArchiveLineChecker.CheckResult result = entry.check(data);
            if (!result.getResult())
            {
                return null;
            }
            eisStatus |= result.getEisStatus();
            tariffCode = result.getCode();
        }

        // channel evaluation
        List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
        for (ChannelArchiveEntry entry : channels)
        {

            BigDecimal value = entry.scaleValue(data[entry.getIndex()]);
            if (value != null)
            {
                intervalValues.add(new IntervalValue(value, 0, eisStatus));
            } else
            {
                intervalValues.add(new IntervalValue(BigDecimal.ZERO, 0, IntervalStateBits.MISSING));
            }
        }

        return new IntervalData(date, eisStatus, 0, tariffCode, intervalValues);

    }

    public MeterEvent processLogLine(Object[] data, TimeZone timezone)
    {
        int eisStatus = 0;
        int deviceCode = 0;
        String msg = "";
        // time stamp evaluation
        Date date = tst.toDate(data, timezone, eisStatus);

        // status valuation
        for (CheckingArchiveEntry entry : stats)
        {
            IArchiveLineChecker.CheckResult result = entry.check(data);
            if (!result.getResult())
            {
                return null;
            }
            eisStatus |= result.getEisStatus();
            deviceCode = result.getCode();
            msg = result.getMsg();
        }
        return new MeterEvent(date, eisStatus, deviceCode, msg);
    }
}
