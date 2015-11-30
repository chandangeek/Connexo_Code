package com.energyict.mdc.protocol.pluggable.mocks;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A standard set of properties
 * <p>
 * Copyrights EnergyICT
 * Date: 5/02/13
 * Time: 15:02
 */
public class SDKStandardDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    private final PropertySpecService propertySpecService;

    /**
     * This value holds the name of the Property that will do something
     */
    public final String doSomeThingPropertyName = "DoSomeThing";

    public SDKStandardDeviceProtocolDialect(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return "SDKStandardDialect";
    }

    @Override
    public String getDisplayName() {
        return "SDK dialect (default)";
    }

    private PropertySpec getDoSomeThingPropertySpec() {
        return this.propertySpecService.basicPropertySpec(doSomeThingPropertyName, false, new BooleanFactory());
    }

    @Override
    public Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet() {
        return Optional.of(new SDKStandardDialectCustomPropertySet(this.propertySpecService));
    }

}