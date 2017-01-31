/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProfileReadByDate.java
 *
 * Created on 21 February 2006, 10:00
 *
 */

package com.energyict.protocolimpl.iec1107.abba230;

import java.util.Date;

/**@author fbo */

/** 18 oct 2011
 * ProfileReadByDate can be used for both Load Profiles and Instrumentation Profiles.
 */

public class ProfileReadByDate {

    Date from;
    Date to;
    String name;        // LoadProfileReadByDate or InstrumentationProfileReadByDate

    /** Creates new ProfileReadByDate */
    public ProfileReadByDate(String name, Date from, Date to) {
        this.name = name;
        this.from = from;
        this.to = to;
    }

    /** @return start of period */
    public Date getFrom() {
        return from;
    }

    /** @return end of period */
    public Date getTo() {
        return to;
    }

    public String toString() {
        return name + " [from=" + from + ", to=" + to + "]";
    }

}
