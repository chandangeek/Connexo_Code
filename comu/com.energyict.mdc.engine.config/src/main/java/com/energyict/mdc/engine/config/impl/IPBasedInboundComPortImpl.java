/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.IPBasedInboundComPort;
import com.energyict.mdc.engine.config.InboundComPort;

import javax.validation.Payload;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlElement;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.config.IPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
@UniquePortNumber(groups = {Save.Create.class, Save.Update.class})
public abstract class IPBasedInboundComPortImpl extends InboundComPortImpl implements IPBasedInboundComPort, Payload {

    @Min(value = 1, groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}")
    @Max(value = 65536, groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.PORT_NUMBER_MAX_VALUE+"}")
    private int portNumber;
    @Min(value = 1, groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}")
    private int numberOfSimultaneousConnections;

    protected IPBasedInboundComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    @XmlElement
    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    @Override
    @XmlElement
    public int getNumberOfSimultaneousConnections() {
        return numberOfSimultaneousConnections;
    }

    @Override
    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {
        this.numberOfSimultaneousConnections = numberOfSimultaneousConnections;
    }

    protected void validateCreate() {
        super.validateCreate();
    }

    @Override
    protected void copyFrom(ComPort source) {
        super.copyFrom(source);
        IPBasedInboundComPort mySource = (IPBasedInboundComPort) source;
        this.setPortNumber(mySource.getPortNumber());
        this.setNumberOfSimultaneousConnections(mySource.getNumberOfSimultaneousConnections());
    }

    static class IpBasedInboundComPortBuilderImpl<B extends IpBasedInboundComPortBuilder<B,C>, C extends IPBasedInboundComPort & InboundComPort>
            extends InboundComPortBuilderImpl<B, C>
            implements IpBasedInboundComPortBuilder<B,C> {

        protected IpBasedInboundComPortBuilderImpl(Class<B> clazz, C comPort, String name, int numberOfSimultaneousConnections, int portNumber) {
            super(clazz, comPort, name);
            this.comPort.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
            this.comPort.setPortNumber(portNumber);
        }

    }


}