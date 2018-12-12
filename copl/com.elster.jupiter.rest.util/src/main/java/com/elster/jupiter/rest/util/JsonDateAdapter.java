/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

// Deprecated as ObjectMapper is already configured to (de)serialize Dates as ms since epoch 
@Deprecated 
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
