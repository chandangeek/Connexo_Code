package com.energyict.protocolimplv2.dlms.idis.am130.properties;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.CipheringType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 15:53
 */
public class AM130ConfigurationSupport implements ConfigurationSupport {

    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "LimitMaxNrOfDays";

    public static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(5);
    public static final boolean USE_GBT_DEFAULT_VALUE = true;
    public static final CipheringType DEFAULT_CIPHERING_TYPE = CipheringType.GENERAL_GLOBAL;

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return Collections.emptyList();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return Arrays.asList(
                this.forcedDelayPropertySpec(),
                this.maxRecPduSizePropertySpec(),
                this.timeZonePropertySpec(),
                this.validateInvokeIdPropertySpec(),
                this.limitMaxNrOfDaysPropertySpec(),
                this.readCachePropertySpec(),
                this.useGeneralBlockTransferPropertySpec(),
                this.generalBlockTransferWindowSizePropertySpec(),
                this.cipheringTypePropertySpec(),
                this.callingAPTitlePropertySpec(),
                this.serverUpperMacAddressPropertySpec(),
                this.callHomeIdPropertySpec()
        );
    }

    private PropertySpec serverUpperMacAddressPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, BigDecimal.ONE);
    }

    protected PropertySpec useGeneralBlockTransferPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(USE_GBT, USE_GBT_DEFAULT_VALUE);
    }

    protected PropertySpec generalBlockTransferWindowSizePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(GBT_WINDOW_SIZE, DEFAULT_GBT_WINDOW_SIZE);
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(
                DlmsProtocolProperties.CIPHERING_TYPE,
                DEFAULT_CIPHERING_TYPE.getDescription(),
                CipheringType.GLOBAL.getDescription(),
                CipheringType.DEDICATED.getDescription(),
                CipheringType.GENERAL_GLOBAL.getDescription(),
                CipheringType.GENERAL_DEDICATED.getDescription()
        );
    }

    public PropertySpec callingAPTitlePropertySpec() {
        return PropertySpecFactory.stringPropertySpec(IDIS.CALLING_AP_TITLE, IDIS.CALLING_AP_TITLE_DEFAULT);
    }

    protected PropertySpec limitMaxNrOfDaysPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(LIMIT_MAX_NR_OF_DAYS_PROPERTY, BigDecimal.ZERO);
    }

    protected PropertySpec timeZonePropertySpec() {
        return PropertySpecFactory.timeZoneInUseReferencePropertySpec(TIMEZONE);
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

    protected PropertySpec readCachePropertySpec() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(Dsmr50Properties.READCACHE_PROPERTY, false);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(
                FORCED_DELAY,
                new TimeDuration(DEFAULT_FORCED_DELAY.intValue() / 1000));
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(MAX_REC_PDU_SIZE, DEFAULT_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec callHomeIdPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME);
    }

}