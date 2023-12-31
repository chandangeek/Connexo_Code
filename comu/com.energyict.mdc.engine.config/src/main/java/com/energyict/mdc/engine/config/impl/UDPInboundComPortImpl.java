/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.IPBasedInboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.UDPBasedInboundComPort;
import com.energyict.mdc.common.comserver.UDPInboundComPort;
import com.energyict.mdc.ports.ComPortType;

import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

/**
 * Provides an implementation for the {@link UDPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class UDPInboundComPortImpl extends IPBasedInboundComPortImpl implements UDPInboundComPort, OutboundComPort {

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY + "}")
    @Min(value = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.MDC_VALUE_TOO_SMALL + "}")
    private Integer bufferSize;

    @Inject
    protected UDPInboundComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public boolean isUDPBased() {
        return true;
    }

    @Override
    protected void copyFrom(ComPort source) {
        super.copyFrom(source);
        this.setBufferSize(((UDPInboundComPort) source).getBufferSize());
    }

    @Override
    @XmlElement
    public Integer getBufferSize() {
        return bufferSize;
    }

    @Override
    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    static class UDPInboundComPortBuilderImpl<B extends UDPInboundComPortBuilder<B, C>, C extends UDPInboundComPort & IPBasedInboundComPort>
            extends IpBasedInboundComPortBuilderImpl<B, C>
            implements UDPInboundComPortBuilder<B, C> {

        protected UDPInboundComPortBuilderImpl(Class<B> clazz, C comPort, String name, int numberOfSimultaneousConnections, int portNumber) {
            super(clazz, comPort, name, numberOfSimultaneousConnections, portNumber);
            comPort.setComPortType(ComPortType.UDP);
        }

        @Override
        public B bufferSize(Integer bufferSize) {
            comPort.setBufferSize(bufferSize);
            return self;
        }
    }

}