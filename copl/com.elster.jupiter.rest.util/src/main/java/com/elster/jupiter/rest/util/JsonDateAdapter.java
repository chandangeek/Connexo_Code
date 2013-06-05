package com.elster.jupiter.rest.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

public class JsonDateAdapter extends XmlAdapter<Long, Date> {

    @Override
    public Date unmarshal(Long v) throws Exception {
        return v == null ? null : new Date(v);
    }

    @Override
    public Long marshal(Date v) throws Exception {
        return v == null ? null : v.getTime();
    }

}
