package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecBuilder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ValueFactory;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link PropertySpecService} interface
 * and registers as an OSGi component to be used by other dependent modules.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-17 (10:59)
 */
@Component(name = "com.energyict.mdc.dynamic.propertyspecservice", service = PropertySpecService.class)
public class PropertySpecServiceImpl implements PropertySpecService {

    @Override
    public <T> PropertySpec<T> basicPropertySpec(String name, boolean required, ValueFactory<T> valueFactory) {
        return new BasicPropertySpec<>(name, required, valueFactory);
    }

    @Override
    public <T extends IdBusinessObject> PropertySpec<T> referencePropertySpec(String name, boolean required, IdBusinessObjectFactory<T> valueFactory) {
        return new ReferencePropertySpec<>(name, required, valueFactory);
    }

    @Override
    public <T> PropertySpecBuilder<T> newPropertySpecBuilder(ValueFactory<T> valueFactory) {
        return PropertySpecBuilderImpl.forClass(valueFactory);
    }

}