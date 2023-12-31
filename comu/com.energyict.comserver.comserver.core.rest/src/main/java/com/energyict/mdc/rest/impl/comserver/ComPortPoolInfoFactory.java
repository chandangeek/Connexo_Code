/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;


public class ComPortPoolInfoFactory {
    private final Thesaurus thesaurus;
    private final ComPortInfoFactory comPortInfoFactory;

    @Inject
    public  ComPortPoolInfoFactory(ComPortInfoFactory comPortInfoFactory, Thesaurus thesaurus) {
        this.comPortInfoFactory = comPortInfoFactory;
        this.thesaurus = thesaurus;
    }

    public ComPortPoolInfo<? extends ComPortPool> asInfo(ComPortPool comPortPool, EngineConfigurationService engineConfigurationService, MdcPropertyUtils mdcPropertyUtils) {
        if (InboundComPortPool.class.isAssignableFrom(comPortPool.getClass())) {
            return setLocalisedValue(new InboundComPortPoolInfo((InboundComPortPool) comPortPool, comPortInfoFactory, mdcPropertyUtils));
        } else {
            return setLocalisedValue(new OutboundComPortPoolInfo((OutboundComPortPool) comPortPool, engineConfigurationService, comPortInfoFactory));
        }
    }

    private ComPortPoolInfo<? extends ComPortPool> setLocalisedValue(ComPortPoolInfo info) {
        if (info != null && info.comPortType != null && info.comPortType.id != null) {
            ComPortTypeAdapter comPortTypeAdapter = new ComPortTypeAdapter();
            info.comPortType.localizedValue = thesaurus.getString(comPortTypeAdapter.marshal(info.comPortType.id), comPortTypeAdapter.marshal(info.comPortType.id));
        }
        return info;
    }
}
