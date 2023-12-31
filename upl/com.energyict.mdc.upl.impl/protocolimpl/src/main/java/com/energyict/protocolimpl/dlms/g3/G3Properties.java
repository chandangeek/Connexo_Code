package com.energyict.protocolimpl.dlms.g3;

import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.dlms.DLMSReference;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.DescriptionTranslationKey;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.MASTERKEY;
import static com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties.DSMR_40_HEX_PASSWORD;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 10:34
 */
public class G3Properties extends DlmsProtocolProperties {

    private static final String PROFILE_TYPE = "ProfileType";
    private static final int DEFAULT_G3_MAX_REC_PDU_SIZE = 512;
    private static final String HLS_SECRET = "HlsSecret";
    public static final String AARQ_RETRIES = "AARQRetries";
    public static final int DEFAULT_AARQ_RETRIES = 2;
    public static final String AARQ_TIMEOUT = "AARQTimeout";
    public static final String PSK = "PSK";
    public static final int DEFAULT_AARQ_TIMEOUT = 0;      //Means: not used
    public static final boolean DEFAULT_VALIDATE_INVOKE_ID = true;
    public static final String PROP_LASTSEENDATE = "LastSeenDate";

    private G3SecurityProvider g3SecurityProvider;
    private final PropertySpecService propertySpecService;

    public G3Properties(PropertySpecService propertySpecService) {
        this(com.energyict.mdc.upl.TypedProperties.empty(), propertySpecService);
    }

    public G3Properties(TypedProperties properties, PropertySpecService propertySpecService) {
        super(properties);
        this.propertySpecService = propertySpecService;
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @ProtocolProperty
    public String getPassword() {
        String hlsSecret = getStringValue(HLS_SECRET, "");
        if (hlsSecret == null || hlsSecret.isEmpty() || "".equals(hlsSecret)) {
            return getStringValue(PK_PASSWORD, "");     //Is limited to 20 chars in EiServer!
        } else {
            return hlsSecret;
        }
    }

    public int getAARQRetries() {
        return getIntProperty(AARQ_RETRIES, DEFAULT_AARQ_RETRIES);
    }

    public int getAARQTimeout() {
        return getIntProperty(AARQ_TIMEOUT, DEFAULT_AARQ_TIMEOUT);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(SERVER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_MAC_ADDRESS),
                this.integerSpec(CONNECTION, PropertyTranslationKeys.DLMS_CONNECTION),
                this.stringSpec(HLS_SECRET, PropertyTranslationKeys.DLMS_HLS_SECRET),
                this.stringSpec(DSMR_40_HEX_PASSWORD, PropertyTranslationKeys.DLMS_DSMR_40_HEX_PASSWORD),
                this.integerSpec(PROFILE_TYPE, PropertyTranslationKeys.DLMS_PROFILE_TYPE),
                this.stringSpec(PROP_LASTSEENDATE, PropertyTranslationKeys.DLMS_LAST_SEEN_DATE),
                this.integerSpec(AARQ_RETRIES, PropertyTranslationKeys.DLMS_AARQ_RETRIES),
                this.integerSpec(AARQ_TIMEOUT, PropertyTranslationKeys.DLMS_AARQ_TIMEOUT),
                this.integerSpec(VALIDATE_INVOKE_ID, PropertyTranslationKeys.DLMS_VALIDATE_INVOKE_ID),
                this.stringSpec(PSK, PropertyTranslationKeys.DLMS_PSK),
                this.keyAccessorTypeReferencePropertySpec(MASTERKEY, PropertyTranslationKeys.DLMS_MASTERKEY));
    }

    protected <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    protected PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    protected PropertySpec hexStringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::hexStringSpec);
    }

    protected PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    private PropertySpec keyAccessorTypeReferencePropertySpec(String name, TranslationKey translationKey) {
        return this.propertySpecService
                .referenceSpec(KeyAccessorType.class.getName())
                .named(name, translationKey)
                .describedAs(new DescriptionTranslationKey(translationKey))
                .finish();
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, 2);
    }

    @Override
    public String getServerMacAddress() {
        return getNodeAddress();
    }

    @Override
    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, "1:0");
    }

    @Override
    public G3SecurityProvider getSecurityProvider() {
        if (g3SecurityProvider == null) {
            g3SecurityProvider = new G3SecurityProvider(this.propertySpecService, this.getProtocolProperties());
        }
        return g3SecurityProvider;
    }

    @ProtocolProperty
    public G3ProfileType getProfileType() {
        final int profileTypeId = getIntProperty(PROFILE_TYPE, 1);
        return G3ProfileType.fromProfileId(profileTypeId);
    }

    @Override
    public byte[] getSystemIdentifier() {
        if (getSerialNumber() == null) {
            return new byte[6];
        }
        final String serial = ProtocolTools.addPaddingAndClip(getSerialNumber(), '0', 12, true);
        return ProtocolTools.getBytesFromHexString(serial, "");
    }

    @Override
    public int getMaxRecPDUSize() {
        return getIntProperty(MAX_REC_PDU_SIZE, DEFAULT_G3_MAX_REC_PDU_SIZE);
    }

    @Override
    protected boolean validateInvokeId() {
        return getBooleanProperty(VALIDATE_INVOKE_ID, DEFAULT_VALIDATE_INVOKE_ID);
    }

}