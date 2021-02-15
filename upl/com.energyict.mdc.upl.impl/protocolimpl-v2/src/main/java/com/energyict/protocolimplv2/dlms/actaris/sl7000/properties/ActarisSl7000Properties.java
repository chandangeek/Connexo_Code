package com.energyict.protocolimplv2.dlms.actaris.sl7000.properties;

import com.energyict.dlms.cosem.FrameCounterProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.common.properties.DlmsPropertiesFrameCounterSupport;

import java.math.BigDecimal;

import static com.energyict.dlms.common.DlmsProtocolProperties.ADDRESSING_MODE;

public class ActarisSl7000Properties extends DlmsPropertiesFrameCounterSupport {

    public int getLimitMaxNrOfDays() {
        return getProperties().getTypedProperty(
                ActarisSl7000ConfigurationSupport.LIMIT_MAX_NR_OF_DAYS_PROPERTY,
                BigDecimal.valueOf(30)
        ).intValue();
    }

    public boolean useRegisterProfile() {
        return getProperties().getTypedProperty(ActarisSl7000ConfigurationSupport.USE_REGISTER_PROFILE, false);
    }

    public boolean useCachedFrameCounter() {
        return getProperties().getTypedProperty(ActarisSl7000ConfigurationSupport.USE_CACHED_FRAME_COUNTER, false);
    }

    public ObisCode frameCounterObisCode() {
        return FrameCounterProvider.getDefaultObisCode();
    }

    @Override
    public int getAddressingMode() {
        return parseBigDecimalProperty(ADDRESSING_MODE, new BigDecimal(4));
    }

}
