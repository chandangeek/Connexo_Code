package com.energyict.protocolimpl.dlms.common;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;

import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 11/06/2015 - 11:13
 */
public interface ProfileCache {

    public void cache(final ObisCodeAndAttribute obisCodeAndAttribute, final Unit unit);

    public void cache(final CapturedObject capturedObject, final Unit unit);

    public void cache(final ObisCode obis, final List<ChannelInfo> channelInfo);

    public Unit getUnit(ObisCodeAndAttribute obisCodeAndAttribute);

    public Unit getUnit(CapturedObject capturedObject);

    public List<ChannelInfo> getChannelInfo(final ObisCode obis);

}