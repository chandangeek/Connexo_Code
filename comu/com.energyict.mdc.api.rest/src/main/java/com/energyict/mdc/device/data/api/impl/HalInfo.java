package com.energyict.mdc.device.data.api.impl;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/29/15.
 */
public class HalInfo {

    public Links _links;
    public Embedded _embedded;
    public Integer total;
    @JsonUnwrapped
    public Object wrappedInfo;

    public static HalInfo wrap(Object info, String href) {
        HalInfo halInfo = new HalInfo();
        halInfo._links= new Links(href);
        halInfo.wrappedInfo = info;
        return halInfo;
    }


    public static HalInfo wrap(List<DeviceInfo> deviceInfos) {
        HalInfo halInfo = new HalInfo();
        halInfo._links= new Links("http://localhost:8085/public/api/dda/v1.0/devices");
        halInfo._links.first= new Links.Reference("http://localhost:8085/public/api/dda/v1.0/devices");
        halInfo._embedded=new HalInfo.Embedded();
        halInfo._embedded.data=new ArrayList<>();
        halInfo._embedded.data.addAll(deviceInfos.stream().map(info->HalInfo.wrap(info, "http://localhost:8085/public/api/dda/v1.0/devices/"+info.mRID)).collect(toList()));
        return halInfo;
    }

    static class Embedded {
        public List<HalInfo> data;
    }
}
