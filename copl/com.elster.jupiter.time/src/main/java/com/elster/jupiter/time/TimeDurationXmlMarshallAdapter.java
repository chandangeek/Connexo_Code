/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Provides mapping services for the JAXB marshalling mechanism
 * for the {@link TimeDuration} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-10 (14:16)
 */
public class TimeDurationXmlMarshallAdapter extends XmlAdapter<TimeDurationXmlAdaptation, TimeDuration> {

    @Override
    public TimeDuration unmarshal (TimeDurationXmlAdaptation v) throws Exception {
        return new TimeDuration(v.seconds);
    }

    @Override
    public TimeDurationXmlAdaptation marshal (TimeDuration v) throws Exception {
        return new TimeDurationXmlAdaptation(v);
    }

}