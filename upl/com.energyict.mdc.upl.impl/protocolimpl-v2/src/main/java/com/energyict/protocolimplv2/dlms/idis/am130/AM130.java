package com.energyict.protocolimplv2.dlms.idis.am130;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.dlms.DLMSCache;
import com.energyict.mdc.channels.ip.InboundIpConnectionType;
import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130LogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130Messaging;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130Properties;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.AM500;
import com.energyict.protocolimplv2.dlms.idis.am500.events.IDISLogBookFactory;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540Cache;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of the old AM500 protocol (IDIS package 1), adding extra features (IDIS package 2)
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/02/2015 - 13:43
 */
public class AM130 extends AM500 {

    private AM130RegisterFactory registerFactory = null;
    private long initialFrameCounter = -1;

    /**
     * The version date
     */
    @Override
    public String getVersion() {
        return "$Date$";
    }

    /**
     * A collection of general AM130 properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (idisConfigurationSupport == null) {
            idisConfigurationSupport = new AM130ConfigurationSupport();
        }
        return idisConfigurationSupport;
    }

    public AM130Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new AM130Properties();
        }
        return (AM130Properties) dlmsProperties;
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType());
        result.add(new InboundIpConnectionType());
        return result;
    }

    @Override
    public String getProtocolDescription() {
        return "AM130 DLMS (IDIS P2)";
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof AM540Cache)) {
            AM540Cache am540Cache = (AM540Cache) deviceProtocolCache;
            super.setDeviceCache(am540Cache);
            if (initialFrameCounter == -1) {
                initialFrameCounter = am540Cache.getFrameCounter();
            }
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((((DLMSCache) getDeviceCache()).getObjectList() == null) || (readCache)) {
            getLogger().info("ReadCache property is true, reading cache!");
            readObjectList();
            ((DLMSCache) getDeviceCache()).saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            getLogger().info("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(((DLMSCache) getDeviceCache()).getObjectList());
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        super.setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(initialFrameCounter == -1 ? 1 : initialFrameCounter);    //Set the frameCounter from last session (which has been loaded from cache)
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        DeviceProtocolCache deviceCache = super.getDeviceCache();
        if (deviceCache == null || !(deviceCache instanceof AM540Cache)) {
            deviceCache = new AM540Cache();
        }
        ((AM540Cache) deviceCache).setFrameCounter(getDlmsSession().getAso().getSecurityContext().getFrameCounter() + 1);     //Save this for the next session
        setDeviceCache(deviceCache);
        return deviceCache;
    }

    protected IDISLogBookFactory getIDISLogBookFactory() {
        if (idisLogBookFactory == null) {
            idisLogBookFactory = new AM130LogBookFactory(this);
        }
        return idisLogBookFactory;
    }

    protected IDISMessaging getIdisMessaging() {
        if (idisMessaging == null) {
            idisMessaging = new AM130Messaging(this);
        }
        return idisMessaging;
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getAM130RegisterFactory().readRegisters(registers);
    }

    private AM130RegisterFactory getAM130RegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new AM130RegisterFactory(this);
        }
        return registerFactory;
    }
}