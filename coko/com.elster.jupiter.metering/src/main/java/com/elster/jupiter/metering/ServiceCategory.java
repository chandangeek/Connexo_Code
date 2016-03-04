package com.elster.jupiter.metering;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.HasTranslatableName;
import com.elster.jupiter.orm.HasAuditInfo;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface ServiceCategory extends HasTranslatableName, HasAuditInfo, HasId {
	String getAliasName();
	String getDescription();
	ServiceKind getKind();

    /**
     * Creates a new UsagePoint for this ServiceCategory.
     *
     * @param mRID The master resource identifier for the new UsagePoint
     * @return The builder that allows you to specify optional information
     * @deprecated Use #newUsagePoint(String, Instant) instead
     */
    @Deprecated
	UsagePointBuilder newUsagePoint(String mRID);

    /**
     * Creates a new UsagePoint for this ServiceCategory.
     *
     * @param mRID The master resource identifier for the new UsagePoint
     * @param installationTime The time of installation of the new UsagePoint
     * @return The builder that allows you to specify optional information
     */
	UsagePointBuilder newUsagePoint(String mRID, Instant installationTime);

    UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Instant start);

	List<RegisteredCustomPropertySet> getCustomPropertySets();

	void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

	void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

	String getDisplayName();

	void setActive(boolean active);
	boolean isActive();
}
