package com.energyict.protocolimplv2.dlms.g3.cache;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.dlms.common.ObisCodeAndAttribute;
import com.energyict.protocolimpl.dlms.common.ProfileCache;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540Cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2015 - 10:46
 */
public class G3Cache extends AM540Cache implements ProfileCache {

    private Map<ObisCodeAndAttribute, Unit> units = new HashMap<>();
    private Map<ObisCode, List<ChannelInfo>> channelInfos = new HashMap<>();

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