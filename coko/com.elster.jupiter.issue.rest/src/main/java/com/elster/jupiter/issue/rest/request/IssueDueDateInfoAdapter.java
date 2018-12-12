/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.request;

import com.elster.jupiter.util.Checks;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class IssueDueDateInfoAdapter extends XmlAdapter<String, IssueDueDateInfo> {
    @Override
    public IssueDueDateInfo unmarshal(String jsonValue) throws Exception {
        IssueDueDateInfo empty =  new IssueDueDateInfo(0L, 0L);
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return empty;
        }
        String[] values = jsonValue.split(":");  // "startTime:endTime" is the string format received from front-end
        return values.length < 2 ? empty : new IssueDueDateInfo(Long.parseLong(values[0]), Long.parseLong(values[1]));
    }

    @Override
    public String marshal(IssueDueDateInfo dueDateInfo) throws Exception {
        if (dueDateInfo == null) {
            return null;
        }
        return dueDateInfo.startTime + ":" + dueDateInfo.endTime;
    }
}
