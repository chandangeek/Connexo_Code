/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.common;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import com.energyict.dlms.cosem.CapturedObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileCache implements Serializable {

    private Map<ObisCodeAndAttribute, Unit> units = new HashMap<ObisCodeAndAttribute, Unit>();
    private Map<ObisCode, List<ChannelInfo>> channelInfos = new HashMap<ObisCode, List<ChannelInfo>>();

    public ProfileCache() {
    }

    public void cache(final ObisCodeAndAttribute obisCodeAndAttribute, final Unit unit) {
        this.units.put(obisCodeAndAttribute, unit);
    }

    public void cache(final CapturedObject capturedObject, final Unit unit) {
        ObisCodeAndAttribute obisCodeAndAttribute = new ObisCodeAndAttribute(capturedObject.getAttributeIndex(), capturedObject.getObisCode());
        this.cache(obisCodeAndAttribute, unit);
    }

    public void cache(final ObisCode obis, final List<ChannelInfo> channelInfo) {
        this.channelInfos.put(obis, channelInfo);
    }

    public Unit getUnit(ObisCodeAndAttribute obisCodeAndAttribute) {
        return this.units.get(obisCodeAndAttribute);
    }

    public Unit getUnit(CapturedObject capturedObject) {
        ObisCodeAndAttribute obisCodeAndAttribute = new ObisCodeAndAttribute(capturedObject.getAttributeIndex(), capturedObject.getObisCode());
        return this.getUnit(obisCodeAndAttribute);
    }

    public List<ChannelInfo> getChannelInfo(final ObisCode obis) {
        return this.channelInfos.get(obis);
    }
}