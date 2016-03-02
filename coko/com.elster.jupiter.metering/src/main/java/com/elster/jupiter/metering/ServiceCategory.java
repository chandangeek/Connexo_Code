package com.elster.jupiter.metering;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.config.MeterRole;
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

    UsagePointBuilder newUsagePoint(String mRID);

    UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Instant start);

    List<RegisteredCustomPropertySet> getCustomPropertySets();

    void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet);

    String getDisplayName();

    void setActive(boolean active);

    boolean isActive();

    List<MeterRole> getMeterRoles();

    void addMeterRole(MeterRole meterRole);

    void removeMeterRole(MeterRole meterRole);
}
