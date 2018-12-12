package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.util.ProtocolLink;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: heuckeg
 * Date: 19.07.11
 * Time: 09:41
 *
 * read a profile with log entries
 */
public class DlmsLogProfile {
    /**
     * The used {@link com.elster.protocolimpl.dsfg.ProtocolLink}
     */
    private final ProtocolLink link;
    /* Obis code of profile */
    private final ObisCode obisCodeOfProfile;
    /* Obis code of event object */
    private final ObisCode obisCodeOfEvent;
    /* profile object to get the profile data */
    private SimpleProfileObject profile = null;

    /*
    Logbuch / metro events
ONO             1	0.128.96.8.74.255	2	0
TST             8	0.0.1.0.0.255	2	0
VB              3	7.0.11.2.0.255	2	0
EVENT_COUNTER   4	0.0.96.15.2.255	5	0
EVENT_COUNTER   4	0.0.96.15.2.255	2	0
STATUS_REG      3	0.6.96.10.1.255	2	0
EVENT           1	7.128.96.5.74.255	2	0
*/

    /**
     * Default constructor
     *
     * @param link - reference to ProtocolLink
     * @param obisCodeOfProfile - obis code of profile object
     * @param obisCodeOfEvent - obiss code of captured event object
     */
    public DlmsLogProfile(ProtocolLink link, ObisCode obisCodeOfProfile, ObisCode obisCodeOfEvent) {
        this.link = link;
        this.obisCodeOfProfile = obisCodeOfProfile;
        this.obisCodeOfEvent = obisCodeOfEvent;
    }

    /**
     * Get interval data within the request period
     *
     * @param from - the initial date for the interval data
     * @param to   - the end date for the interval data
     * @return the requested meter events
     * @throws java.io.IOException when reading of the data failed
     */
    @SuppressWarnings({"unused"})
    public List<MeterEvent> getMeterEvents(Date from, Date to)
            throws IOException {

        List<MeterEvent> events = new ArrayList<MeterEvent>();

        long readLines = getProfileObject().readProfileData(from, to);
        if (readLines == 0) {
            System.out.println("getMeterEvents: no data to readout");
            return events;
        }

        int tstIndex = profile.indexOfCapturedObject(Ek280Defs.CLOCK_OBJECT);
        if (tstIndex < 0) {
            throw new IOException("getMeterEvents: row timestamp not found in profile");
        }
        int eventIndex = profile.indexOfCapturedObject(obisCodeOfEvent);
        if (eventIndex < 0) {
            throw new IOException("getMeterEvents: row event not found in profile");
        }

        System.out.println("getMeterEvents: lines to parse: " + getProfileObject().getRowCount());

        MeterEvent me;

        /* for every line we have... */
        for (int i = 0; i < profile.getRowCount(); i++) {

            /* get time stamp */
            DlmsDateTime tst = (DlmsDateTime) profile.getValue(i, tstIndex);

            int event = (Integer)profile.getValue(i, eventIndex);

            DlmsEvent dlmsEvent = DlmsEvent.findEvent(event);
            if (dlmsEvent != null) {
                me = new MeterEvent(tst.getUtcDate(), dlmsEvent.getEisEventCode(), dlmsEvent.getLisEventCode(), dlmsEvent.getMsg());
            } else {
                me = new MeterEvent(tst.getUtcDate(), MeterEvent.OTHER, event, "unknown event");
            }
            events.add(me);
            //System.out.println("(" + Integer.toHexString(tst.getClockStatus()) + ")  " + me);
        }

        return events;
    }

    /**
     * returns an object to get profile data.
     * If it still doesn't exist, it will be created
     *
     * @return a profile object
     * @throws IOException - in case of error
     */
    private SimpleProfileObject getProfileObject() throws IOException {
        if (profile == null) {
            profile = (SimpleProfileObject) link.getObjectManager().getSimpleCosemObject(obisCodeOfProfile);
        }
        return profile;
    }
}
