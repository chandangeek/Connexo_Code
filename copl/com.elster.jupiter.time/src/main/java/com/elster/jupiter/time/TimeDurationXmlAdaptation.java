/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import javax.xml.bind.annotation.XmlElement;

/**
 * Adapter class for {@link TimeDuration} to enable xml marshalling.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-10 (14:07)
 */
public class TimeDurationXmlAdaptation {

    @XmlElement
    public int seconds;

    public TimeDurationXmlAdaptation () {
        super();
    }

    public TimeDurationXmlAdaptation (TimeDuration timeDuration) {
        this();
        this.seconds = timeDuration.getSeconds();
    }

}