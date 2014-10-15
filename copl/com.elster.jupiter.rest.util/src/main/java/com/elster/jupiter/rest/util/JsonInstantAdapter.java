package com.elster.jupiter.rest.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import java.time.Instant;
import java.util.Date;

public class JsonInstantAdapter extends XmlAdapter<Long, Instant> {

    @Override
    public Instant unmarshal(Long v) throws Exception {
        return v == null ? null : Instant.ofEpochMilli(v.longValue());
    }

    @Override
    public Long marshal(Instant v) throws Exception {
        return v == null ? null : v.toEpochMilli();
    }

}
