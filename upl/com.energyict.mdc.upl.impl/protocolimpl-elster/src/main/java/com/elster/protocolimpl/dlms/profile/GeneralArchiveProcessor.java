package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.protocolimpl.dlms.profile.entrymgmt.ArchiveStructure2;
import com.elster.protocolimpl.dlms.profile.standardchecker.StandardChecker;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * User: heuckeg
 * Date: 17.04.13
 * Time: 10:49
 */
public class GeneralArchiveProcessor implements IArchiveProcessor
{
    // initialized by constructor parameter
    private final TimeZone timeZone;
    private final Logger logger;

    // initialized by prepare
    private SimpleProfileObject profileObject;
    private ArchiveStructure2 profileStructure = null;

    public GeneralArchiveProcessor(final TimeZone timeZone, final Logger logger)
    {
        this.timeZone = timeZone;
        this.logger = logger;
    }

    public void prepare(final SimpleProfileObject profileObject, final Object archiveStructure) throws IOException
    {
        if (!(archiveStructure instanceof String))
        {
            throw new IllegalArgumentException("GeneralArchiveProcessor.prepare: wrong argument for archiveStructure. Should be String. Is " + archiveStructure.getClass().getName());
        }
        this.profileObject = profileObject;

        profileStructure = new ArchiveStructure2((String) archiveStructure, StandardChecker.getDefault());

        profileStructure.prepareForProcessing(profileObject);
    }

    public int getNumberOfChannels() throws IOException
    {
        return profileStructure.getChannelCount();
    }

    public int getInterval() throws IOException
    {
        return (int) profileObject.getCapturePeriod();
    }

    public List<ChannelInfo> buildChannelInfo() throws IOException
    {
        return profileStructure.buildChannelInfo();
    }

    public List<IntervalData> getIntervalData(Date from, Date to) throws IOException
    {
        List<IntervalData> intervalList = new ArrayList<IntervalData>();

        logger.info(String.format("GeneralArchiveProcessor.getIntervalData: try to get data from %s to %s", from.toString(), to.toString()));

        long readLines = profileObject.readProfileData(from, to);
        logger.info("GeneralArchiveProcessor.getIntervalData: " + readLines + " lines read.");
        if (readLines == 0)
        {
            logger.info("GeneralArchiveProcessor.getIntervalData: no data to readout");
            return intervalList;
        }

        logger.info("GeneralArchiveProcessor.getIntervalData: lines to parse: " + profileObject.getRowCount());

        Object[] data = new Object[profileObject.getColumnCount()];
        /* for every line we have... */
        for (int i = 0; i < profileObject.getRowCount(); i++)
        {
            for (int j = 0; j < profileObject.getColumnCount(); j++)
            {
                data[j] = profileObject.getRawValue(i, j);
            }

            IntervalData ivd = profileStructure.processProfileLine(data, timeZone);
            if (ivd != null)
            {
                intervalList.add(ivd);
            }
        }
        logger.info("GeneralArchiveProcessor.getIntervalData: generated interval list with " + intervalList.size() + " of IntervalData.");
        return intervalList;
    }
}
