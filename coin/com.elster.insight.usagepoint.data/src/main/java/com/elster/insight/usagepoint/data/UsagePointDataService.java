package com.elster.insight.usagepoint.data;


import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface UsagePointDataService {
    String COMPONENT_NAME = "UDC";

    /**
     * Returns an utility class which allows to manage custom property sets and their values on usage point.
     *
     * @param mrid usage point mrid.
     * @return The utility extension or <code>Optional.empty()</code> if there is no usage point with the given mrid.
     */
    Optional<UsagePointCustomPropertySetExtension> findUsagePointExtensionByMrid(String mrid);

    /**
     * Returns an utility class which allows to manage custom property sets and their values on usage point.
     * The underlying usage point instance is locked.
     *
     * @param id      usage point id.
     * @param version current usage point version.
     * @return The utility extension or <code>Optional.empty()</code> if there is no usage point with the given mrid
     * or version doesn't match.
     */
    Optional<UsagePointCustomPropertySetExtension> findAndLockUsagePointExtensionByIdAndVersion(long id, long version);
}