/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.BaseReadingRecord;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ReadingInfos {
    public int total;
    public List<ReadingInfo> readingInfos = new ArrayList<>();

    ReadingInfos() {
    }

    ReadingInfos(BaseReadingRecord reading) {
        add(reading);
    }

    ReadingInfos(Iterable<? extends BaseReadingRecord> readings) {
        addAll(readings);
    }

    ReadingInfo add(BaseReadingRecord reading) {
    	if (reading.getQuantity(0) != null) {
    		ReadingInfo result = new ReadingInfo(reading);
    		readingInfos.add(result);
    		total++;
    		return result;
    	} else {
    		return null;
    	}
    }

    void addAll(Iterable<? extends BaseReadingRecord> readings) {
        for (BaseReadingRecord each : readings) {
            add(each);
        }
    }

}
