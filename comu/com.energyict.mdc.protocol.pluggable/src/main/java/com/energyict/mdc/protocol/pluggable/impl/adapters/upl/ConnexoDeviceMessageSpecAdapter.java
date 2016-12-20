package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts a given CXO DeviceMessageSpec to the UPL DeviceMessageSpec interface.
 * This is straight forward for all fields except for the PropertySpecs, they need adaptation.
 * <p>
 * This is used when given pending messages (in offline form) from the framework to the protocols that need to execute them.
 * The framework uses CXO interfaces, the protocols use UPL interfaces.
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 14/12/2016 - 11:40
 */
public class ConnexoDeviceMessageSpecAdapter implements com.energyict.mdc.upl.messages.DeviceMessageSpec {

    private final DeviceMessageSpec cxoDeviceMessageSpec;

    public ConnexoDeviceMessageSpecAdapter(DeviceMessageSpec cxoDeviceMessageSpec) {
        this.cxoDeviceMessageSpec = cxoDeviceMessageSpec;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return new ConnexoDeviceMessageCategoryAdapter(cxoDeviceMessageSpec.getCategory());
    }

    @Override
    public String getName() {
        return cxoDeviceMessageSpec.getName();
    }

    @Override
    public long getMessageId() {
        return cxoDeviceMessageSpec.getId().dbValue();
    }

    @Override
    public TranslationKey getNameTranslationKey() {
        return new TranslationKey() {
            @Override
            public String getKey() {
                return cxoDeviceMessageSpec.getName();
            }

            @Override
            public String getDefaultFormat() {
                return cxoDeviceMessageSpec.getName();
            }
        };
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return cxoDeviceMessageSpec::getName;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return cxoDeviceMessageSpec.getPropertySpecs().stream()
                .map(ConnexoToUPLPropertSpecAdapter::new)
                .collect(Collectors.toList());
    }
}