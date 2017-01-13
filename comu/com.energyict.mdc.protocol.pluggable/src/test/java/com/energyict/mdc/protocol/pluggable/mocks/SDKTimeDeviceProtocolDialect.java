package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import java.util.Optional;

/**
 * A set of properties related to the TimeSetting.
 * <p>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:51
 */
public class SDKTimeDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public static final String clockOffsetToWritePropertyName = "ClockOffsetWhenReading";
    public static final String clockOffsetToReadPropertyName = "ClockOffsetWhenWriting";

    private final PropertySpecService propertySpecService;

    public SDKTimeDeviceProtocolDialect(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return "SDKTimeDialect";
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return "SDK dialect for time testing";
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new SDKTimeDialectCustomPropertySet(this.propertySpecService));
    }

}