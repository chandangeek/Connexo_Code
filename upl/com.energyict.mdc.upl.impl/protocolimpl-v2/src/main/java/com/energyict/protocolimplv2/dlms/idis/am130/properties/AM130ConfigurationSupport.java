package com.energyict.protocolimplv2.dlms.idis.am130.properties;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.idis.IDIS;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.FORCED_DELAY;
import static com.energyict.dlms.common.DlmsProtocolProperties.GBT_WINDOW_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.MAX_REC_PDU_SIZE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.USE_GBT;
import static com.energyict.dlms.common.DlmsProtocolProperties.VALIDATE_INVOKE_ID;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 15:53
 */
public class AM130ConfigurationSupport implements HasDynamicProperties {

    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "LimitMaxNrOfDays";

    public static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final BigDecimal DEFAULT_GBT_WINDOW_SIZE = BigDecimal.valueOf(5);
    public static final boolean USE_GBT_DEFAULT_VALUE = true;
    public static final CipheringType DEFAULT_CIPHERING_TYPE = CipheringType.GENERAL_GLOBAL;


    @Override
    public List<PropertySpec> getPropertySpecs() {
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
                this.callHomeIdPropertySpec());
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        // currently I don't hold any properties
    }

    protected PropertySpec serverUpperMacAddressPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, false, BigDecimal.ONE);
    }

    protected PropertySpec useGeneralBlockTransferPropertySpec() {
        return UPLPropertySpecFactory.booleanValue(USE_GBT, false, USE_GBT_DEFAULT_VALUE);
    }

    protected PropertySpec generalBlockTransferWindowSizePropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(GBT_WINDOW_SIZE, false, DEFAULT_GBT_WINDOW_SIZE);
    }

    protected PropertySpec cipheringTypePropertySpec() {
        return UPLPropertySpecFactory.stringWithDefault(DlmsProtocolProperties.CIPHERING_TYPE, false, DEFAULT_CIPHERING_TYPE.getDescription(),
                CipheringType.GLOBAL.getDescription(),
                CipheringType.DEDICATED.getDescription(),
                CipheringType.GENERAL_GLOBAL.getDescription(),
                CipheringType.GENERAL_DEDICATED.getDescription());
    }

    public PropertySpec callingAPTitlePropertySpec() {
        return UPLPropertySpecFactory.stringWithDefault(IDIS.CALLING_AP_TITLE, false, IDIS.CALLING_AP_TITLE_DEFAULT);
    }

    protected PropertySpec limitMaxNrOfDaysPropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(LIMIT_MAX_NR_OF_DAYS_PROPERTY, false, BigDecimal.ZERO);
    }

    protected PropertySpec timeZonePropertySpec() {
        return UPLPropertySpecFactory.timeZone(TIMEZONE, false);
    }

    protected PropertySpec validateInvokeIdPropertySpec() {
        return UPLPropertySpecFactory.booleanValue(VALIDATE_INVOKE_ID, false, DEFAULT_VALIDATE_INVOKE_ID);
    }

    protected PropertySpec readCachePropertySpec() {
        return UPLPropertySpecFactory.booleanValue(Dsmr50Properties.READCACHE_PROPERTY, false, false);
    }

    protected PropertySpec forcedDelayPropertySpec() {
        return UPLPropertySpecFactory.duration(FORCED_DELAY, false, DEFAULT_FORCED_DELAY);
    }

    protected PropertySpec maxRecPduSizePropertySpec() {
        return UPLPropertySpecFactory.bigDecimal(MAX_REC_PDU_SIZE, false, DEFAULT_MAX_REC_PDU_SIZE);
    }

    protected PropertySpec callHomeIdPropertySpec() {
        return UPLPropertySpecFactory.string(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, false);
    }

}