package com.elster.jupiter.rest.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.sun.org.glassfish.gmbal.DescriptorFields;

import java.time.Instant;
import java.util.Date;

// Deprecated as support for Instant (de)serialization has been added to the Objectmapper in com.elster.jupiter.rest.whiteboard
@Deprecated
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
