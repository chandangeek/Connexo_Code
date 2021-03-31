package test.com.energyict.protocolimplv2.sdksample;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;
import com.energyict.protocolimplv2.dialects.AbstractDeviceProtocolDialect;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class SDKCreditTaskProtocolDialectProperties extends AbstractDeviceProtocolDialect {

    public static final String creditType = "creditType";
    public static final String creditAmount = "creditAmount";

    public SDKCreditTaskProtocolDialectProperties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    public SDKCreditTaskProtocolDialectProperties(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_CREDIT.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.SDK_SAMPLE_CREDIT).format();
    }

    @Override
    public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory.specBuilder(creditType, false, PropertyTranslationKeys.SDKSAMPLE_CREDIT_TYPE, getPropertySpecService()::stringSpec).finish(),
                UPLPropertySpecFactory
                        .specBuilder(creditAmount, false, PropertyTranslationKeys.SDKSAMPLE_CREDIT_AMOUNT, getPropertySpecService()::bigDecimalSpec)
                        .markExhaustive()
                        .setDefaultValue(new BigDecimal(0))
                        .finish()
        );
    }
}