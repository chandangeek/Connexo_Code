package com.energyict.mdc.device.data.rest.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.masterdata.rest.EndDeviceEventTypeInfo;

public class LogBookInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public Date lastEventDate;
    public EndDeviceEventTypeInfo lastEventType;
    public Date lastReading;

    public static LogBookInfo from(LogBook logBook, NlsService nlsService) {
        LogBookInfo info = new LogBookInfo();
        info.id = logBook.getId();
        info.name = logBook.getLogBookType().getName();
        info.obisCode = logBook.getLogBookType().getObisCode();
        info.overruledObisCode = logBook.getDeviceObisCode();
        info.lastReading = logBook.getLatestEventAdditionDate();

        Date lastLogBookDate = logBook.getLastLogBook();
        if (lastLogBookDate != null) {
            info.lastEventDate = lastLogBookDate;
            List<EndDeviceEventRecord> endDeviceEvents = logBook.getEndDeviceEvents(new Interval(lastLogBookDate, lastLogBookDate));
            if (endDeviceEvents.size() > 0) {
                EndDeviceEventRecord lastRecord = endDeviceEvents.get(0);
                info.lastEventType = EndDeviceEventTypeInfo.from(lastRecord.getEventType(), nlsService);
            }
        }
        return info;
    }

    public static List<LogBookInfo> from(List<LogBook> logBooks, NlsService nlsService) {
        List<LogBookInfo> infos = new ArrayList<>(logBooks.size());
        for (LogBook logBook : logBooks) {
            infos.add(from(logBook, nlsService));
        }
        return infos;
    }
}
