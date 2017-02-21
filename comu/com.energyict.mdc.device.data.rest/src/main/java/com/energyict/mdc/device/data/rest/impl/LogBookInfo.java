/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.data.LogBook;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LogBookInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public Instant lastEventDate;
    public EndDeviceEventTypeInfo lastEventType;
    public Instant lastReading;

    public static LogBookInfo from(LogBook logBook, Thesaurus thesaurus) {
        LogBookInfo info = new LogBookInfo();
        info.id = logBook.getId();
        info.name = logBook.getLogBookType().getName();
        info.obisCode = logBook.getLogBookType().getObisCode();
        info.overruledObisCode = logBook.getDeviceObisCode();
        info.lastReading = logBook.getLatestEventAdditionDate().orElse(null);

        Optional<Instant> lastLogBookDate = logBook.getLastLogBook();
        if (lastLogBookDate.isPresent()) {
            info.lastEventDate = lastLogBookDate.get();
            List<EndDeviceEventRecord> endDeviceEvents = logBook.getEndDeviceEvents(Ranges.closedOpen(info.lastEventDate, info.lastEventDate
                    .plusSeconds(1)));
            if (!endDeviceEvents.isEmpty()) {
                EndDeviceEventRecord lastRecord = endDeviceEvents.get(0);
                info.lastEventType = EndDeviceEventTypeInfo.from(lastRecord.getEventType(), thesaurus);
            }
        }
        return info;
    }

    public static List<LogBookInfo> from(List<LogBook> logBooks, Thesaurus thesaurus) {
        List<LogBookInfo> infos = new ArrayList<>(logBooks.size());
        for (LogBook logBook : logBooks) {
            infos.add(from(logBook, thesaurus));
        }
        return infos;
    }
}
