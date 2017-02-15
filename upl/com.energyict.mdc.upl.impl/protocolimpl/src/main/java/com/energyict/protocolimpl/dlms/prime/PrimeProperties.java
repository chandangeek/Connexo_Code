package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.DLMSReference;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ProtocolProperty;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY;
import static com.energyict.protocolimpl.dlms.common.NTASecurityProvider.DATATRANSPORT_ENCRYPTIONKEY;

/**
 * Copyrights EnergyICT
 * Date: 21/02/12
 * Time: 14:47
 */
public class PrimeProperties extends DlmsProtocolProperties {

    /**
     * Name of the property containing the load profile OBIS code to fetch.
     */
    private static final String PROPNAME_LOAD_PROFILE_OBIS_CODE = "LoadProfileObisCode";
    private static final String PROPNAME_EVENT_LOGBOOK_OBIS_CODE = "EventLogBookObisCode";

    private static final String FW_UPGRADE_POLLING_DELAY = "FWUpgradePollingDelay";
    private static final String FW_UPGRADE_POLLING_RETRIES = "FWUpgradePollingRetries";
    private static final String FW_IMAGE_NAME = "FirmwareImageName";
    private static final String READ_SERIAL_NUMBER = "ReadSerialNumber";
    public static final String EVENTS_ONLY = "EventsOnly";

    private static final String DEFAULT_EVENTS_ONLY = "0";
    private static final int FIRMWARE_CLIENT_ADDRESS = 3;
    private static final String DOT = ".";

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;

    PrimeProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        this(new Properties(), propertySpecService, nlsService);
    }

    private PrimeProperties(Properties properties, PropertySpecService propertySpecService, NlsService nlsService) {
        super(properties);
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    @Override
    public DLMSReference getReference() {
        return DLMSReference.LN;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.integerSpec(CLIENT_MAC_ADDRESS, PropertyTranslationKeys.DLMS_CLIENT_MAC_ADDRESS),
                this.stringSpec(SERVER_MAC_ADDRESS, PropertyTranslationKeys.DLMS_SERVER_MAC_ADDRESS),
                this.integerSpec(SECURITY_LEVEL, PropertyTranslationKeys.DLMS_SECURITYLEVEL),
                this.integerSpec(CONNECTION, PropertyTranslationKeys.DLMS_CONNECTION),
                this.hexStringSpec(DATATRANSPORT_AUTHENTICATIONKEY, PropertyTranslationKeys.DLMS_DATATRANSPORT_AUTHENTICATIONKEY),
                this.hexStringSpec(DATATRANSPORT_ENCRYPTIONKEY, PropertyTranslationKeys.DLMS_DATATRANSPORT_ENCRYPTIONKEY),
                new ObisCodePropertySpec(PROPNAME_LOAD_PROFILE_OBIS_CODE,this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DLMS_LOAD_PROFILE_OBIS_CODE).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DLMS_LOAD_PROFILE_OBIS_CODE_DESCRIPTION).format()),
                new ObisCodePropertySpec(PROPNAME_EVENT_LOGBOOK_OBIS_CODE,this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DLMS_EVENT_LOGBOOK_OBIS_CODE).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.DLMS_EVENT_LOGBOOK_OBIS_CODE_DESCRIPTION).format()),
                this.stringSpec(FW_IMAGE_NAME, PropertyTranslationKeys.DLMS_FW_IMAGE_NAME),
                this.integerSpec(FW_UPGRADE_POLLING_DELAY, PropertyTranslationKeys.DLMS_FW_UPGRADE_POLLING_DELAY),
                this.integerSpec(FW_UPGRADE_POLLING_RETRIES, PropertyTranslationKeys.DLMS_FW_UPGRADE_POLLING_RETRIES),
                this.integerSpec(READ_SERIAL_NUMBER, PropertyTranslationKeys.DLMS_READ_SERIAL_NUMBER));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    private PropertySpec hexStringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::hexStringSpec);
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey, Integer... validValues) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.propertySpecService::integerSpec)
                .addValues(validValues)
                .markExhaustive()
                .finish();
    }

    @Override
    public int getClientMacAddress() {
        return getIntProperty(CLIENT_MAC_ADDRESS, "1");
    }

    @Override
    public String getServerMacAddress() {
        final String oldMacAddress = getStringValue(SERVER_MAC_ADDRESS, "1:16");
        return oldMacAddress.replaceAll("x", getNodeAddress());
    }

    @Override
    public String getSecurityLevel() {
        return getStringValue(SECURITY_LEVEL, "1:0");
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        return new NTASecurityProvider(getProtocolProperties());
    }

    @ProtocolProperty
    public final ObisCode getLoadProfileObiscode() {
        final String obisString = getStringValue(PROPNAME_LOAD_PROFILE_OBIS_CODE, "");
        try {
            return ObisCode.fromString(obisString);
        } catch (IllegalArgumentException e) {
            return PrimeProfile.BASIC_PROFILE;
        }
    }

    public ObisCode getEventLogBookObiscode() {
        String obiscodeString = getStringValue(PROPNAME_EVENT_LOGBOOK_OBIS_CODE, null);
        if (obiscodeString != null) {
            return ObisCode.fromString(obiscodeString);
        } else {
            return null;    //Read out all logbooks if no specific logbook obiscode has been configured
        }
    }

    @ProtocolProperty
    public final boolean isEventsOnly() {
        return getBooleanProperty(EVENTS_ONLY, DEFAULT_EVENTS_ONLY);
    }

    public final boolean isFirmwareClient() {
        return getClientMacAddress() == FIRMWARE_CLIENT_ADDRESS;
    }

    @ProtocolProperty
    public final int getPollingDelay() {
        return getIntProperty(FW_UPGRADE_POLLING_DELAY, "10000");
    }

    @ProtocolProperty
    public final int getPollingRetries() {
        return getIntProperty(FW_UPGRADE_POLLING_RETRIES, "20");
    }

    @ProtocolProperty
    public final String getFWImageName() {
        return getStringValue(FW_IMAGE_NAME, "NewImage");
    }

    @ProtocolProperty
    public final boolean isReadSerialNumber() {
        return getBooleanProperty(READ_SERIAL_NUMBER, "0");
    }

    public final String getFWImageNameWithoutExtension() {
        String fullFileName = getFWImageName();
        if (!fullFileName.contains(DOT)) {
            return fullFileName;
        }
        int extensionIndex = fullFileName.lastIndexOf(".");
        return fullFileName.substring(0, extensionIndex);
    }

}