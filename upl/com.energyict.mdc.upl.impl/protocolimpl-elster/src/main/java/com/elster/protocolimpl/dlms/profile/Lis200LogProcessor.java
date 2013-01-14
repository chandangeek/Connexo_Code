package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * User: heuckeg
 * Date: 07.09.12
 * Time: 14:50
 */
public class Lis200LogProcessor
        implements ILogProcessor
{
    private final Logger logger;

    private SimpleProfileObject profile;
    private ArchiveStructure archiveStructure;

    public Lis200LogProcessor(Logger logger)
    {
        this.logger = logger;
    }

    public void prepare(SimpleProfileObject profileObject, ArchiveStructure archiveStructure)
            throws IOException
    {
        this.profile = profileObject;
        this.archiveStructure = archiveStructure;
    }

    public List<MeterEvent> getMeterEvents(Date from, Date to)
            throws IOException
    {

        List<MeterEvent> events = new ArrayList<MeterEvent>();

        logger.info(String.format("Lis200LogProcessor.getMeterEvents: try to get data from %s to %s", from.toString(), to.toString()));

        long readLines = profile.readProfileData(from, to);
        if (readLines == 0)
        {
            logger.info("Lis200LogProcessor.getMeterEvents(" + from + "," + to + "): no data read out");
            return events;
        }

        int tstIndex = profile.indexOfCapturedObject(archiveStructure.getTSTEntry().getObisCode());
        int eventIndex = profile.indexOfCapturedObject(archiveStructure.getEventEntry().getObisCode());

        logger.info("Lis200LogProcessor.getMeterEvents(" + from + "," + to + "): lines to parse: " + profile.getRowCount());

        MeterEvent me;

        /* for every line we have... */
        for (int i = 0; i < profile.getRowCount(); i++)
        {
            /* get time stamp */
            DlmsDateTime tst = (DlmsDateTime) profile.getValue(i, tstIndex);

            int event = (Integer) profile.getValue(i, eventIndex);

//            DlmsEvent dlmsEvent = DlmsEvent.findEvent(event);
//            if (dlmsEvent != null) {
//                me = new MeterEvent(tst.getUtcDate(), dlmsEvent.getEisEventCode(), dlmsEvent.getLisEventCode(), dlmsEvent.getMsg());
//            } else {
//            me = new MeterEvent(tst.getUtcDate(), MeterEvent.OTHER, event, "Lis 200 event" + Integer.toHexString(event));
//            }
//            events.add(me);
        }

        logger.info("Lis200LogProcessor.getMeterEvents: generated list with " + events.size() + " of MeterEvents.");

        return events;
    }

}
