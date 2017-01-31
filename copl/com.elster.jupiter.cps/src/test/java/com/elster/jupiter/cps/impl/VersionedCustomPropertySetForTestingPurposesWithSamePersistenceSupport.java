/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.properties.PropertySpecService;

/**
 * A versioned {@link CustomPropertySet} for the {@link TestDomain}
 * that will be used in the integration test classes of this bundle.
 * Should this be a real implementation, the class would need
 * a @Component annotation so that the OSGi container
 * would automatically pick it up and add it to the
 * {@link CustomPropertySet} whiteboard.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (14:28)
 */
public class VersionedCustomPropertySetForTestingPurposesWithSamePersistenceSupport extends VersionedCustomPropertySetForTestingPurposes {

    public VersionedCustomPropertySetForTestingPurposesWithSamePersistenceSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

}