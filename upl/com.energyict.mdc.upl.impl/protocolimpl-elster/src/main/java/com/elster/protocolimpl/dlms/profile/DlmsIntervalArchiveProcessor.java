package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.protocolimpl.dlms.util.DlmsUtils;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: heuckeg
 * Date: 06.09.12
 * Time: 12:19
 */
@SuppressWarnings({"unused"})
public class DlmsIntervalArchiveProcessor implements IArchiveProcessor
{
    private final Logger logger;
    private SimpleProfileObject profile;
    private ArchiveStructure archiveStructure;

    private int tstIndex;
    private int evtIndex;
    private int[] chnIndex;

    public DlmsIntervalArchiveProcessor(final Logger logger) {
        this.logger = logger;
    }

    public void prepare(final SimpleProfileObject profile, final Object archiveStructure)
            throws IOException {
        if (!(archiveStructure instanceof ArchiveStructure)) {
            throw new IllegalArgumentException("GeneralArchiveProcessor.prepare: wrong argument for archiveStructure. Should be ArchiveStructure. Is " + archiveStructure.getClass().getName());
        }

        this.profile = profile;
        this.archiveStructure = (ArchiveStructure) archiveStructure;

        tstIndex = findInCapturedObjects(this.archiveStructure.getTSTEntry());

        chnIndex = new int[this.archiveStructure.channelCount()];
        for (int i = 0; i < this.archiveStructure.channelCount(); i++) {
            chnIndex[i] = findInCapturedObjects(this.archiveStructure.getChannelEntry(i));
        }
    }

    private int findInCapturedObjects(ArchiveStructure.ArchiveStructureEntry entry)
            throws IOException
    {
        return profile.indexOfCapturedObject(entry.getObisCode(), entry.getAttribute());
    }

    public int getNumberOfChannels()
            throws IOException
    {
        return archiveStructure.channelCount();
    }

    public int getInterval()
            throws IOException
    {
        return (int) profile.getCapturePeriod();
    }

    @SuppressWarnings("deprecation")
    public List<ChannelInfo> buildChannelInfo()
            throws IOException
    {
        List<ChannelInfo> channelInfo = new ArrayList<ChannelInfo>();

        // create a ChannelInfo object for all captured objects
        for (int i = 0; i < archiveStructure.channelCount(); i++)
        {
            ArchiveStructure.ArchiveStructureEntry entry = archiveStructure.getChannelEntry(i);

            String channelName = "Channel " + i;

            String unitString = "";
            if (chnIndex[i] >= 0)
            {
                // get the unit of the object
                com.elster.dlms.cosem.classes.class03.Unit u = profile.getUnit(chnIndex[i]);
                unitString = u.getDisplayName();
            }

            Unit channelUnit = DlmsUtils.getUnitFromString(unitString);
            ChannelInfo ci = new ChannelInfo(i, channelName, channelUnit);

            if (entry.isAdvance())
            {
                ci.setCumulative();
                // We also use the deprecated method for 8.3 versions
                int ov = 1;
                for (int j = 0; j < entry.getOverflow(); j++)
                {
                    ov *= 10;
                }
                ci.setCumulativeWrapValue(new BigDecimal(ov));
            }

            channelInfo.add(ci);
            System.out.println(String.format("%d: %s [%s] %s %g", ci.getChannelId(), ci.getName(), ci.getUnit().toString(), ci.isCumulative(), ci.getCumulativeWrapValue()));
        }

        return channelInfo;
    }

    public List<IntervalData> getIntervalData(Date from, Date to)
            throws IOException
    {
        List<IntervalData> intervalList = new ArrayList<IntervalData>();

        logger.info(String.format("DlmsIntervalArchiveProcessor.getIntervalData: try to get data from %s to %s", from.toString(), to.toString()));

        long readLines = profile.readProfileData(from, to);
        logger.info("DlmsIntervalArchiveProcessor.getIntervalData: " + readLines + " lines read.");
        if (readLines == 0)
        {
            logger.info("DlmsIntervalArchiveProcessor.getIntervalData: no data to readout");
            return intervalList;
        }

        logger.info("DlmsIntervalArchiveProcessor.getIntervalData: lines to parse: " + profile.getRowCount());

        Object v;
        List<IntervalValue> intervalValues;
        IntervalData id;

        /* for every line we have... */
        for (int i = 0; i < profile.getRowCount(); i++)
        {

            /* get time stamp */
            DlmsDateTime tst = (DlmsDateTime) profile.getValue(i, tstIndex);
            System.out.print(tst.toString());

            /* determine status */
            int eiStatus = 0;
//            if ((tst.getClockStatus() & 0xF) > 0)
//            {
                //eiStatus |= IntervalStateBits.BADTIME;
//            }

            /* build list of values of line */
            intervalValues = new ArrayList<IntervalValue>();
            for (int index : chnIndex)
            {
                if (index >= 0)
                {
                    v = profile.getValue(i, index);
                    if (v instanceof Number)
                    {
                        intervalValues.add(new IntervalValue((Number) v, 0, eiStatus));
                    }
                } else
                {
                    intervalValues.add(new IntervalValue(BigDecimal.ZERO, 0, IntervalStateBits.MISSING));
                }
            }

            Date d = tst.getUtcDate();
            if (d == null) {
                d = tst.getLocalDate();
            }
            id = new IntervalData(d, eiStatus, 0, 0, intervalValues);
            intervalList.add(id);
            System.out.println(id);
        }

        logger.info("DlmsIntervalArchiveProcessor.getIntervalData: generated interval list with " + intervalList.size() + " of IntervalData.");
        return intervalList;
    }
}
