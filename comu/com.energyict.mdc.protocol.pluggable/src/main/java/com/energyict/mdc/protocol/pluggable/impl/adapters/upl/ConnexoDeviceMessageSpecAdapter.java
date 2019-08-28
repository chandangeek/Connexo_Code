package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLDeviceMessageSpecAdapter;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
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
 *
 *
 * @author khe
 * @since 14/12/2016 - 11:40
 */
public class ConnexoDeviceMessageSpecAdapter implements com.energyict.mdc.upl.messages.DeviceMessageSpec {

    private final DeviceMessageSpec cxoDeviceMessageSpec;

    public static com.energyict.mdc.upl.messages.DeviceMessageSpec adaptTo(DeviceMessageSpec cxoDeviceMessageSpec) {
        if (cxoDeviceMessageSpec instanceof UPLDeviceMessageSpecAdapter) {
            return ((UPLDeviceMessageSpecAdapter) cxoDeviceMessageSpec).getUplDeviceMessageSpec();
        } else {
            return new ConnexoDeviceMessageSpecAdapter(cxoDeviceMessageSpec);
        }
    }

    private ConnexoDeviceMessageSpecAdapter(DeviceMessageSpec cxoDeviceMessageSpec) {
        this.cxoDeviceMessageSpec = cxoDeviceMessageSpec;
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return ConnexoDeviceMessageCategoryAdapter.adaptTo(cxoDeviceMessageSpec.getCategory());
    }

    public DeviceMessageSpec getConnexoDeviceMessageSpec() {
        return cxoDeviceMessageSpec;
    }

    @Override
    public String getName() {
        return cxoDeviceMessageSpec.getName();
    }

    @Override
    public long getId() {
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
    public List<PropertySpec> getPropertySpecs() {
        return cxoDeviceMessageSpec.getPropertySpecs().stream()
                .map(ConnexoToUPLPropertSpecAdapter::adaptTo)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnexoDeviceMessageSpecAdapter) {
            return cxoDeviceMessageSpec.equals(((ConnexoDeviceMessageSpecAdapter) obj).cxoDeviceMessageSpec);
        } else {
            return cxoDeviceMessageSpec.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return cxoDeviceMessageSpec != null ? cxoDeviceMessageSpec.hashCode() : 0;
    }
}