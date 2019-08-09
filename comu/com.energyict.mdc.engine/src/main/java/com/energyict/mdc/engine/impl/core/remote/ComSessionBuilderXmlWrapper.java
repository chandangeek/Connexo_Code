package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.device.data.impl.tasks.history.ComSessionBuilderImpl;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

/**
 * Wraps a {@link DeviceProtocolCache} for the purpose of
 * using it as a top level xml document
 * in methods that are part of the remote query api.
 */
@XmlRootElement
public class ComSessionBuilderXmlWrapper {

    @XmlAttribute
    public Instant stopDate;

    @XmlElement(type = ComSessionBuilderImpl.class)
    public ComSessionBuilder builder;

    @XmlAttribute
    public ComSession.SuccessIndicator successIndicator;


    public ComSessionBuilderXmlWrapper() {
        super();
    }

    public ComSessionBuilderXmlWrapper(ComSessionBuilder builder, Instant stopDate, ComSession.SuccessIndicator successIndicator) {
        this.stopDate = stopDate;
        this.builder = builder;
        this.successIndicator = successIndicator;
    }

    public Instant getStopDate() {
        return stopDate;
    }

    public ComSessionBuilder getBuilder() {
        return builder;
    }

    public ComSession.SuccessIndicator getSuccessIndicator() {
        return successIndicator;
    }
}