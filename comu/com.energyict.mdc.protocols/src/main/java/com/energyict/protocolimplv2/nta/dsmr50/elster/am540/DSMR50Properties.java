package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;


import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.dynamic.HexStringFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocolimplv2.g3.common.G3Properties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension of the standard DLMS properties, adding DSMR50 stuff
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/12/2014 - 17:27
 */
public class DSMR50Properties extends G3Properties {

    public static final String AARQ_TIMEOUT_PROPERTY = "AARQTimeout";
    public static final String AARQ_RETRIES_PROPERTY = "AARQRetries";
    public static final String READCACHE_PROPERTY = "ReadCache";
    public static final String CumulativeCaptureTimeChannel = "CumulativeCaptureTimeChannel";
    public static final String PSK_PROPERTY = "PSK";
    public static final String CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = "CheckNumberOfBlocksDuringFirmwareResume";
    private TimeDuration DEFAULT_AARQ_TIMEOUT_PROPERTY = TimeDuration.NONE;
    private BigDecimal DEFAULT_AARQ_RETRIES_PROPERTY = BigDecimal.valueOf(2);
    private boolean DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME = true;

    public DSMR50Properties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    /**
     * Property indicating to read the cache out (useful because there's no config change state)
     */
    public boolean isReadCache() {
        return getProperties().<Boolean>getTypedProperty(READCACHE_PROPERTY, false);
    }

    public boolean isCumulativeCaptureTimeChannel() {
        return getProperties().<Boolean>getTypedProperty(CumulativeCaptureTimeChannel, false);
    }

    public HexString getPSK() { //TODO use this for the push event notification mechanism
        return getProperties().<HexString>getTypedProperty(PSK_PROPERTY);
    }

    public long getAARQTimeout() {
        return getProperties().getTypedProperty(AARQ_TIMEOUT_PROPERTY, DEFAULT_AARQ_TIMEOUT_PROPERTY).getMilliSeconds();
    }

    public long getAARQRetries() {
        return getProperties().getTypedProperty(AARQ_RETRIES_PROPERTY, DEFAULT_AARQ_RETRIES_PROPERTY).longValue();
    }

    private PropertySpec readCachePropertySpec() {
        return getPropertySpecService().booleanPropertySpec(READCACHE_PROPERTY, false, false);
    }

    private PropertySpec aarqTimeoutPropertySPec(){
        return getPropertySpecService().timeDurationPropertySpec(AARQ_TIMEOUT_PROPERTY, false, DEFAULT_AARQ_TIMEOUT_PROPERTY);
    }

    private PropertySpec aarqRetriesPropertySPec(){
        return getPropertySpecService().bigDecimalPropertySpec(AARQ_RETRIES_PROPERTY, false, DEFAULT_AARQ_RETRIES_PROPERTY);
    }

    private PropertySpec pskPropertySPec(){
        return getPropertySpecService().basicPropertySpec(PSK_PROPERTY, false, HexStringFactory.class);
    }

    private PropertySpec cumulativeCaptureTimePropertySPec(){
        return getPropertySpecService().booleanPropertySpec(CumulativeCaptureTimeChannel, false, false);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(aarqTimeoutPropertySPec());
        propertySpecs.add(aarqRetriesPropertySPec());
        propertySpecs.add(pskPropertySPec());
        propertySpecs.add(cumulativeCaptureTimePropertySPec());
        propertySpecs.add(readCachePropertySpec());
        return propertySpecs;
    }

    public boolean getCheckNumberOfBlocksDuringFirmwareResume() {
        return getProperties().getTypedProperty(CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME, DEFAULT_CHECK_NUMBER_OF_BLOCKS_DURING_FIRMWARE_RESUME);
    }
}