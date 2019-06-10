/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.generalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointTypeInfo;
import com.elster.jupiter.metering.impl.audit.AbstractUsagePointAuditDecoder;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.SimplePropertyType;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuditTrailGeneralAttributesDecoder extends AbstractUsagePointAuditDecoder {

    AuditTrailGeneralAttributesDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService) {
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.setThesaurus(thesaurus);
    }

    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("name", this.getThesaurus().getFormat(PropertyTranslationKeys.USAGEPOINT_GENERAL_INFORMATION).format());
        return builder.build();
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        try {
            if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
                return getAuditLogChangesForUpdate();
            } else if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) {
                return getAuditLogChangesForNew();
            }
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogChangesForUpdate() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            usagePoint.ifPresent(upEntry -> {
                DataMapper<UsagePoint> dataMapper = ormService.getDataModel(MeteringService.COMPONENTNAME).get().mapper(UsagePoint.class);

                List<UsagePoint> allEntries = getChangedObjects(dataMapper, upEntry.getId())
                        .stream()
                        .filter(distinctByKey(p -> ((UsagePoint)p).getVersion()))
                        .map(p-> (UsagePoint)p)
                        .sorted(Comparator.comparing(UsagePoint::getVersion))
                        .collect(Collectors.toList());

                UsagePoint from = allEntries.get(0);
                UsagePoint to = allEntries.get(allEntries.size() - 1);
                getAuditLogChangeForString(from.getName(), to.getName(), PropertyTranslationKeys.USAGEPOINT_NAME).ifPresent(auditLogChanges::add);
                getAuditLogChangeForLocation(from, to).ifPresent(auditLogChanges::add);
                getAuditLogChangeForCoordinates(from, to).ifPresent(auditLogChanges::add);
                getAuditLogChangeForString(getThesaurus().getString(from.getLifeCycle().getName(), from.getLifeCycle().getName()), getThesaurus().getString(to.getLifeCycle().getName(), to.getLifeCycle().getName()), PropertyTranslationKeys.USAGEPOINT_LIFE_CYCLE_NAME).ifPresent(auditLogChanges::add);
                getAuditLogChangeForString(from.getReadRoute(), to.getReadRoute(), PropertyTranslationKeys.USAGEPOINT_READROUTE).ifPresent(auditLogChanges::add);
                getAuditLogChangeForString(from.getServiceDeliveryRemark(), to.getServiceDeliveryRemark(), PropertyTranslationKeys.USAGEPOINT_SERVICE_DELIVERY_REMARK).ifPresent(auditLogChanges::add);
            });
            return auditLogChanges;
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogChangesForNew() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            usagePoint.ifPresent(upEntry -> {
                getAuditLogChangeForString(upEntry.getName(), PropertyTranslationKeys.USAGEPOINT_NAME).ifPresent(auditLogChanges::add);
                getAuditLogChangeForLocation(upEntry).ifPresent(auditLogChanges::add);
                getAuditLogChangeForCoordinates(upEntry).ifPresent(auditLogChanges::add);
                getAuditLogChangeForString(upEntry.getReadRoute(), PropertyTranslationKeys.USAGEPOINT_READROUTE).ifPresent(auditLogChanges::add);
                getAuditLogChangeForString(upEntry.getServiceDeliveryRemark(), PropertyTranslationKeys.USAGEPOINT_SERVICE_DELIVERY_REMARK).ifPresent(auditLogChanges::add);
                getAuditLogChangeForString(upEntry.getServiceCategory().getDisplayName(), PropertyTranslationKeys.USAGEPOINT_SERVICEPRIORITY).ifPresent(auditLogChanges::add);
                getAuditLogChangeForString(getThesaurus().getString(upEntry.getLifeCycle().getName(), upEntry.getLifeCycle().getName()), PropertyTranslationKeys.USAGEPOINT_LIFE_CYCLE_NAME).ifPresent(auditLogChanges::add);
                getAuditLogChangeForOptional(Optional.of(upEntry.getCreateDate()), PropertyTranslationKeys.USAGEPOINT_CREATETIME, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add);
                getAuditLogChangeForUpType(upEntry).ifPresent(auditLogChanges::add);
                getAuditLogChangeForString(getThesaurus().getString(upEntry.getLifeCycle().getName(), upEntry.getLifeCycle().getName()), PropertyTranslationKeys.USAGEPOINT_LIFE_CYCLE_NAME).ifPresent(auditLogChanges::add);
            });
            return auditLogChanges;
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    protected Map<String, Object> getActualClauses(long id) {
        return ImmutableMap.of("id", id);
    }

    private Optional<AuditLogChange> getAuditLogChangeForLocation(UsagePoint from, UsagePoint to) {
        if (!to.getLocation().equals(from.getLocation())) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(PropertyTranslationKeys.USAGEPOINT_LOCATION));
            auditLogChange.setType("LOCATION");
            to.getLocation().ifPresent(location -> auditLogChange.setValue(formatLocation(location)));
            from.getLocation().ifPresent(location -> auditLogChange.setPreviousValue(formatLocation(location)));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForCoordinates(UsagePoint from, UsagePoint to) {
        if (!to.getSpatialCoordinates().equals(from.getSpatialCoordinates())) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(PropertyTranslationKeys.USAGEPOINT_COORDINATES));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            to.getSpatialCoordinates().ifPresent(coordinates -> auditLogChange.setValue(coordinates.toString()));
            from.getSpatialCoordinates().ifPresent(coordinates -> auditLogChange.setPreviousValue(coordinates.toString()));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForLocation(UsagePoint to) {
        if (to.getLocation().isPresent()) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(PropertyTranslationKeys.USAGEPOINT_LOCATION));
            auditLogChange.setType("LOCATION");
            to.getLocation().ifPresent(location -> auditLogChange.setValue(formatLocation(location)));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForCoordinates(UsagePoint to) {
        if (to.getSpatialCoordinates().isPresent()) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(PropertyTranslationKeys.USAGEPOINT_COORDINATES));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            to.getSpatialCoordinates().ifPresent(coordinates -> auditLogChange.setValue(coordinates.toString()));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChangeForUpType(UsagePoint to) {
        String type = "";
        if (to.isSdp() && to.isVirtual()) {
            type = UsagePointTypeInfo.UsagePointType.UNMEASURED_SDP.getDisplayName(getThesaurus());
        }
        if (!to.isSdp() && to.isVirtual()) {
            type = UsagePointTypeInfo.UsagePointType.UNMEASURED_NON_SDP.getDisplayName(getThesaurus());
        }
        if (to.isSdp() && !to.isVirtual()) {
            type = UsagePointTypeInfo.UsagePointType.MEASURED_SDP.getDisplayName(getThesaurus());
        }
        if (!to.isSdp() && !to.isVirtual()) {
            type = UsagePointTypeInfo.UsagePointType.MEASURED_NON_SDP.getDisplayName(getThesaurus());
        }
        AuditLogChange auditLogChange = new AuditLogChangeBuilder();
        auditLogChange.setName(getDisplayName(PropertyTranslationKeys.USAGEPOINT_TYPE_OF_USAGE_POINT));
        auditLogChange.setType(SimplePropertyType.TEXT.name());
        auditLogChange.setValue(type);
        return Optional.of(auditLogChange);
    }

    private String formatLocation(Location location) {
        List<List<String>> formattedLocationMembers = location.format();
        formattedLocationMembers.stream().skip(1).forEach(list ->
                list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(list.indexOf(member), "\\r\\n" + member)));
        return formattedLocationMembers.stream()
                .flatMap(List::stream).filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }


}
