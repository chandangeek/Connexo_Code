package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.protocolimpl.dlms.profile.entrymgmt.ArchiveStructure2;
import com.elster.protocolimpl.dlms.profile.standardchecker.StandardChecker;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * User: heuckeg
 * Date: 17.04.13
 * Time: 12:11
 */
@SuppressWarnings("unused")
public class GeneralDlmsLogProcessor implements ILogProcessor
{
    // Initialized via constructor parameter
    private final TimeZone timeZone;
    private final Logger logger;

    private SimpleProfileObject profileObject;
    private ArchiveStructure2 profileStructure;

    public GeneralDlmsLogProcessor(TimeZone timeZone, Logger logger)
    {
        this.timeZone = timeZone;
        this.logger = logger;
    }

    public void prepare(SimpleProfileObject profileObject, Object archiveStructure) throws IOException
    {
        if (!(archiveStructure instanceof String))
        {
            throw new IllegalArgumentException("GeneralDlmsLogProcessor.prepare: wrong argument for archiveStructure. Should be String. Is " + archiveStructure.getClass().getName());
        }
        this.profileObject = profileObject;

        profileStructure = new ArchiveStructure2((String)archiveStructure, StandardChecker.getDefault());

        profileStructure.prepareForProcessing(profileObject);
    }

    public List<MeterEvent> getMeterEvents(Date from, Date to) throws IOException
    {
        List<MeterEvent> events = new ArrayList<MeterEvent>();

        logger.info(String.format("GeneralDlmsLogProcessor.getMeterEvents: try to get data from %s to %s", from.toString(), to.toString()));

        long readLines = 0;
        if (profileObject.getEntriesInUse() > 0)
        {
            readLines = profileObject.readProfileData(from, to);
        }
        if (readLines == 0)
        {
            logger.info("GeneralDlmsLogProcessor.getMeterEvents(" + from + "," + to + "): no data read out");
            return events;
        }

        logger.info("GeneralDlmsLogProcessor.getMeterEvents(" + from + "," + to + "): lines to parse: " + profileObject.getRowCount());

        Object[] data = new Object[profileObject.getColumnCount()];
        /* for every line we have... */
        for (int i = 0; i < profileObject.getRowCount(); i++)
        {
            for (int j = 0; j < profileObject.getColumnCount(); j++)
            {
                data[j] = profileObject.getValue(i, j);
            }
            MeterEvent me = profileStructure.processLogLine(data, TimeZone.getTimeZone("GMT+2"));
            if (me != null)
            {
                events.add(me);
            }
        }

        logger.info("GeneralDlmsLogProcessor.getMeterEvents: generated list with " + events.size() + " of MeterEvents.");

        return events;
    }
}
