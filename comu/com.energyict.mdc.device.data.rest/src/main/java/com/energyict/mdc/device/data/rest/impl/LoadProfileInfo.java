package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 7/28/14.
 */
public class LoadProfileInfo {
    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public String interval; // the interval definition of the load profile
    public Date lastReading;
    public List<IntervalDataInfo> intervalData;
}

class IntervalDataInfo {
    public Date interval;
    public Map<String, BigDecimal> channelData;
}
