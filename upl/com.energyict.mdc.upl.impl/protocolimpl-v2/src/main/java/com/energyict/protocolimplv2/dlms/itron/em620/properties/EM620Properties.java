package com.energyict.protocolimplv2.dlms.itron.em620.properties;

import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.READCACHE_PROPERTY;
import static com.energyict.protocolimplv2.dlms.itron.em620.properties.EM620ConfigurationSupport.AARQ_RETRIES_PROPERTY;
import static com.energyict.protocolimplv2.dlms.itron.em620.properties.EM620ConfigurationSupport.AARQ_TIMEOUT_PROPERTY;
import static com.energyict.protocolimplv2.dlms.itron.em620.properties.EM620ConfigurationSupport.FRAME_COUNTER_RECOVERY_RETRIES;
import static com.energyict.protocolimplv2.dlms.itron.em620.properties.EM620ConfigurationSupport.FRAME_COUNTER_RECOVERY_STEP;
import static com.energyict.protocolimplv2.dlms.itron.em620.properties.EM620ConfigurationSupport.USE_CACHED_FRAME_COUNTER;
import static com.energyict.protocolimplv2.dlms.itron.em620.properties.EM620ConfigurationSupport.VALIDATE_CACHED_FRAMECOUNTER;

public class EM620Properties extends DlmsProperties {

    private static final int PUBLIC_CLIENT_MAC_ADDRESS = 16;

    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler = null;

    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(USE_CACHED_FRAME_COUNTER, false);
    }

    public boolean validateCachedFrameCounter() {
        return getProperties().getTypedProperty(VALIDATE_CACHED_FRAMECOUNTER, true);
    }

    public int getFrameCounterRecoveryRetries() {
        return getProperties().getTypedProperty(FRAME_COUNTER_RECOVERY_RETRIES, BigDecimal.valueOf(100)).intValue();
    }

    public int getFrameCounterRecoveryStep() {
        return getProperties().getTypedProperty(FRAME_COUNTER_RECOVERY_STEP, BigDecimal.ONE).intValue();
    }

    public boolean usesPublicClient() {
        return getClientMacAddress() == PUBLIC_CLIENT_MAC_ADDRESS;
    }

    public int getAARQRetries() {
        return getProperties().getTypedProperty(AARQ_RETRIES_PROPERTY, BigDecimal.valueOf(2)).intValue();
    }

    public long getAARQTimeout() {
        return getProperties().getTypedProperty(AARQ_TIMEOUT_PROPERTY, HS3300ConfigurationSupport.DEFAULT_NOT_USED_AARQ_TIMEOUT).toMillis();
    }

    public int getStatusFlagChannel() {
        return getProperties().getTypedProperty(FRAME_COUNTER_RECOVERY_STEP, 0);
    }

    @Override
    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        if (invokeIdAndPriorityHandler == null) {
            invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler();
        }
        return invokeIdAndPriorityHandler;
    }

}
