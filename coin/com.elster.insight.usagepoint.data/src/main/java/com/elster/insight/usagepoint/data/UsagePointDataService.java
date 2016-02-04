package com.elster.insight.usagepoint.data;


import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface UsagePointDataService {
    String COMPONENT_NAME = "UDC";

    /**
     * Returns an utility class which allows to manage custom property sets values on usage point.
     *
     * @param mrid usage point mrid
     * @return The utility extension or <code>Optional.empty()</code> if there is no usage point with the given mrid.
     */
    Optional<UsagePointPropertySetValuesExtension> findUsagePointExtensionByMrid(String mrid);
}