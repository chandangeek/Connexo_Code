package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 9:59
 */
public class SDKCalendarTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String activeCalendarName = "activeCalendarName";
    public static final String passiveCalendarName = "passiveCalendarName";

    public SDKCalendarTaskProtocolDialectProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public SDKCalendarTaskProtocolDialectProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_CALENDAR.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_CALENDAR).format();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(activeCalendarName, false, PropertyTranslationKeys.SDKSAMPLE_ACTIVE_CALENDAR_NAME, getPropertySpecService()::stringSpec).finish(),
                UPLPropertySpecFactory.specBuilder(passiveCalendarName, false, PropertyTranslationKeys.SDKSAMPLE_PASSIVE_CALENDAR_NAME, getPropertySpecService()::stringSpec).finish()
        );
    }
}