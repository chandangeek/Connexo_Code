package com.energyict.mdc.multisense.api;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.net.URI;
import java.util.List;

/**
 * Created by bvn on 4/29/15.
 */
public class HalInfo {

    public Links _links;
    public Embedded _embedded;
    public Integer total;
    @JsonUnwrapped
    public Object wrappedInfo;

    public static HalInfo wrap(Object info, URI href) {
        HalInfo halInfo = new HalInfo();
        halInfo._links= new Links(href.toString());
        halInfo.wrappedInfo = info;
        return halInfo;
    }

    static class Embedded {
        public List<HalInfo> data;
    }
}
