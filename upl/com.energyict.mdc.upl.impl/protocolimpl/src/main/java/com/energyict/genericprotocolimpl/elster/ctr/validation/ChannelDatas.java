package com.energyict.genericprotocolimpl.elster.ctr.validation;

import com.energyict.interval.IntervalRecord;
import com.energyict.mdw.core.Channel;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 15/03/11
 * Time: 11:20
 */
public class ChannelDatas {

    List<List<IntervalRecord>> channelData = new ArrayList<List<IntervalRecord>>();

    public ChannelDatas(List<Channel> channels, Date installationDate) {
        for (Channel channel : channels) {
            channelData.add(channel.getIntervalData(installationDate, new Date()));
        }
    }

    public String getIntervalInfo(Date intervalDate) {
        StringBuilder sb = new StringBuilder();
        for (List<IntervalRecord> intervalRecords : channelData) {
            IntervalRecord record = findIntervalRecord(intervalDate, intervalRecords);
            if ((record != null) && (record.getValue() != null)) {
                sb.append(record.getValue()).append(";");
            } else {
                sb.append("-;");
            }
        }
        return sb.toString();
    }

    private IntervalRecord findIntervalRecord(Date intervalDate, List<IntervalRecord> intervalRecords) {
        for (IntervalRecord intervalRecord : intervalRecords) {
            if (intervalRecord.getDate().getTime() == intervalDate.getTime()) {
                return intervalRecord;
            }
        }
        return null;
    }

}
