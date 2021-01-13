package com.energyict.protocolimpl.dlms;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DLMSProfileIntervalsValueFilter {

    private int SUMMER_FLAG = 8;
    private int TEST_FLAG = 524288;
    private int STATUS_ZERO = 0;

    private long VALUE_ZERO = 0l;

    public List<IntervalData> filter(List<IntervalData> intervalDataList, List<ChannelInfo> channelInfos) {
        List<IntervalData> result = new ArrayList<>();
        Map<Date, List<IntervalData>> dateDataMap = buildDateDataMap(intervalDataList);

        for (Date endDate : dateDataMap.keySet()) {
            List<IntervalData> dataList = dateDataMap.get(endDate);
            result.add(combineIntervalDatas(dataList, channelInfos));
        }
        return result;
    }

    private IntervalData combineIntervalDatas(List<IntervalData> intervalDataList, List<ChannelInfo> channelInfos) {
        IntervalData result = buildEmptyIntervalData(intervalDataList);

        for (IntervalData data : intervalDataList) {
            if (includeValues(data)) {
                result.setIntervalValues(combine(result.getIntervalValues(), data.getIntervalValues(), channelInfos));
            }
            result.addEiStatus(data.getEiStatus());
            result.addProtocolStatus(data.getEiStatus());
        }
        return result;
    }

    private Map<Date, List<IntervalData>> buildDateDataMap(List<IntervalData> intervalDataList) {
        Map<Date, List<IntervalData>> map = new TreeMap<>();
        for (IntervalData data : intervalDataList) {
            Date endTime = data.getEndTime();
            List<IntervalData> dataList = map.get(endTime);
            if (dataList == null) {
                dataList = new ArrayList<>();
            }
            dataList.add(data);
            map.put(endTime, dataList);
        }
        return map;
    }

    private IntervalData buildEmptyIntervalData(List<IntervalData> intervalDataList) {
        IntervalData first = intervalDataList.get(0);

        IntervalData result = new IntervalData(first.getEndTime(), STATUS_ZERO, STATUS_ZERO, first.getTariffCode());
        for (Object each : first.getIntervalValues()) {
            result.addValue(VALUE_ZERO, STATUS_ZERO, STATUS_ZERO);
        }
        return result;
    }

    private List<IntervalValue> combine(List<IntervalValue> intervalValues1, List<IntervalValue> intervalValues2, List<ChannelInfo> channelInfos) {
        List<IntervalValue> results = new ArrayList<>();
        for (int i=0; i<intervalValues1.size(); i++) {
            Number value;
            if (isCummulative(i, channelInfos)) {
                value = Math.max(intervalValues1.get(i).getNumber().longValue(), intervalValues2.get(i).getNumber().longValue());
            }
            else {
                value = intervalValues1.get(i).getNumber().longValue() + intervalValues2.get(i).getNumber().longValue();
            }
            int eiStatus = intervalValues1.get(i).getEiStatus() | intervalValues2.get(i).getEiStatus();
            int protcolStatus = intervalValues1.get(i).getProtocolStatus() | intervalValues2.get(i).getProtocolStatus();
            results.add(new IntervalValue(value, protcolStatus, eiStatus));
        }
        return results;
    }

    private boolean isCummulative(int index, List<ChannelInfo> channelInfos) {
        if (channelInfos==null || channelInfos.size()<index+1) {
            return false;
        }
        return channelInfos.get(index).isCumulative();
    }

    private boolean includeValues(IntervalData intervalData) {
        return !hasTestFlag(intervalData);
    }

    private boolean hasTestFlag(IntervalData intervalData) {
        return (intervalData.getProtocolStatus() & TEST_FLAG) == TEST_FLAG;
    }

}
