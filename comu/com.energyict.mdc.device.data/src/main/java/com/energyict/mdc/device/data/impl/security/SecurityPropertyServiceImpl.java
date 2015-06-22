package com.energyict.mdc.device.data.impl.security;

import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.dynamic.relation.CompositeFilterCriterium;
import com.energyict.mdc.dynamic.relation.FilterAspect;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationDynamicAspect;
import com.energyict.mdc.dynamic.relation.RelationSearchFilter;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.SimpleFilterCriterium;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.Predicates;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Provides an implementation for the {@link SecurityPropertyService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (16:25)
 */
@Component(name = "com.energyict.mdc.device.data.security", service = SecurityPropertyService.class, property = "name=SecurityPropertyService")
public class SecurityPropertyServiceImpl implements SecurityPropertyService {

    private volatile Clock clock;
    private volatile ProtocolPluggableService protocolPluggableService;

    // For OSGi framework
    @SuppressWarnings("unused")
    public SecurityPropertyServiceImpl() {
    }

    // For unit testing purposes
    @Inject
    public SecurityPropertyServiceImpl(Clock clock, ProtocolPluggableService protocolPluggableService) {
        super();
        this.setClock(clock);
        this.setProtocolPluggableService(protocolPluggableService);
    }

    @Override
    public List<SecurityProperty> getSecurityProperties(Device device, Instant when, SecurityPropertySet securityPropertySet) {
        if (securityPropertySet.currentUserIsAllowedToViewDeviceProperties()) {
            return this.getSecurityPropertiesIgnoringPrivileges(device, when, securityPropertySet);
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<SecurityProperty> getSecurityPropertiesIgnoringPrivileges(Device device, Instant when, SecurityPropertySet securityPropertySet) {
        return this.findActiveProperties(device, securityPropertySet, when)
                    .map(r -> this.toSecurityProperties(r, device, securityPropertySet))
                    .orElse(Collections.emptyList());
    }

    public Optional<Relation> findActiveProperties(Device device, SecurityPropertySet securityPropertySet, Instant activeDate) {
        RelationType relationType = this.findSecurityPropertyRelationType(device);
        if (relationType != null) {
            FilterAspect deviceAspect = new RelationDynamicAspect(relationType.getAttributeType(SecurityPropertySetRelationAttributeTypeNames.DEVICE_ATTRIBUTE_NAME));
            FilterAspect securityPropertySetAspect = new RelationDynamicAspect(relationType.getAttributeType(SecurityPropertySetRelationAttributeTypeNames.SECURITY_PROPERTY_SET_ATTRIBUTE_NAME));

            RelationSearchFilter searchFilter =
                    new RelationSearchFilter(
                            CompositeFilterCriterium.matchAll(
                                    new SimpleFilterCriterium(deviceAspect, SimpleFilterCriterium.Operator.EQUALS, device),
                                    new SimpleFilterCriterium(securityPropertySetAspect, SimpleFilterCriterium.Operator.EQUALS, securityPropertySet)));
            List<Relation> relations = relationType.findByFilter(searchFilter);
            if (relations.isEmpty()) {
                return Optional.empty();
            } else {
                for (Relation relation : relations) {
                    if (relation.getPeriod().contains(activeDate)) {
                        return Optional.of(relation);
                    }
                }
                return null;
            }
        } else {
            // No RelationType means there are not security properties
            return Optional.empty();
        }
    }

    private RelationType findSecurityPropertyRelationType(Device device) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = device.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
        return this.protocolPluggableService.findSecurityPropertyRelationType(deviceProtocolPluggableClass);
    }

    private List<SecurityProperty> toSecurityProperties(Relation relation, Device device, SecurityPropertySet securityPropertySet) {
        return securityPropertySet
                    .getPropertySpecs()
                    .stream()
                    .map(each -> this.toSecurityProperty(each, relation, device, securityPropertySet))
                    .flatMap(Functions.asStream())
                    .collect(Collectors.toList());
    }

    private Optional<SecurityProperty> toSecurityProperty(PropertySpec propertySpec, Relation relation, Device device, SecurityPropertySet securityPropertySet) {
        Boolean status = (Boolean) relation.get(SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME);
        Object propertyValue = relation.get(propertySpec.getName());
        if (propertyValue != null) {
            return Optional.of(new SecurityPropertyImpl(device, securityPropertySet, propertySpec, propertyValue, relation.getPeriod(), status));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasSecurityProperties(Device device, Instant when, SecurityPropertySet securityPropertySet) {
        return this.findActiveProperties(device, securityPropertySet, when).isPresent();
    }

    @Override
    public boolean securityPropertiesAreValid(Device device) {
        return device.getDeviceConfiguration().getComTaskEnablements().stream().noneMatch(enablement -> isMissingOrIncomplete(device, enablement.getSecurityPropertySet()));
    }

    private boolean isMissingOrIncomplete(Device device, SecurityPropertySet securityPropertySet) {
        List<SecurityProperty> securityProperties = this.getSecurityPropertiesIgnoringPrivileges(device, this.clock.instant(), securityPropertySet);
        return securityProperties.isEmpty()
            || securityProperties
                    .stream()
                    .anyMatch(Predicates.not(SecurityProperty::isComplete));
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

}