/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.elster.jupiter.time.TimeDuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wraps a {@link TimeDuration} for the purpose of
 * returning it as a top level xml document
 * in methods that are part of the remote query api.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-29 (15:15)
 */
@XmlRootElement
public class TimeDurationXmlWrapper {

    @XmlElement
    public TimeDuration timeDuration;

    public TimeDurationXmlWrapper () {
        super();
    }

    public TimeDurationXmlWrapper (TimeDuration timeDuration) {
        this();
        this.timeDuration = timeDuration;
    }

}