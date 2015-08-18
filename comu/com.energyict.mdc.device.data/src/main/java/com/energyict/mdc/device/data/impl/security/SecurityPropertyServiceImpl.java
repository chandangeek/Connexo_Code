package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.exceptions.NestedRelationTransactionException;
import com.energyict.mdc.device.data.exceptions.SecurityPropertyException;
import com.energyict.mdc.dynamic.relation.CompositeFilterCriterium;
import com.energyict.mdc.dynamic.relation.FilterAspect;
import com.energyict.mdc.dynamic.relation.Relation;
import com.energyict.mdc.dynamic.relation.RelationDynamicAspect;
import com.energyict.mdc.dynamic.relation.RelationSearchFilter;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.SimpleFilterCriterium;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.streams.Predicates;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames.DEVICE_ATTRIBUTE_NAME;
import static com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames.SECURITY_PROPERTY_SET_ATTRIBUTE_NAME;
import static com.energyict.mdc.protocol.pluggable.SecurityPropertySetRelationAttributeTypeNames.STATUS_ATTRIBUTE_NAME;


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
    private volatile Thesaurus thesaurus;

    // For OSGi framework
    public SecurityPropertyServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public SecurityPropertyServiceImpl(Clock clock, ProtocolPluggableService protocolPluggableService, NlsService nlsService) {
        this();
        this.setClock(clock);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setNlsService(nlsService);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
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

    private List<SecurityProperty> getSecurityPropertiesIgnoringPrivileges(Device device, Instant when, SecurityPropertySet securityPropertySet) {
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
            }
            else {
                for (Relation relation : relations) {
                    if (relation.getPeriod().contains(activeDate)) {
                        return Optional.of(relation);
                    }
                }
                return Optional.empty();
            }
        }
        else {
            // No RelationType means there are no security properties
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
            return Optional.of(
                    new SecurityPropertyImpl(
                            device,
                            securityPropertySet,
                            propertySpec,
                            propertyValue,
                            relation.getPeriod(),
                            // Status is a required attribute on the relation should it cannot be null
                            status));
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
    public boolean securityPropertiesAreValid(Device device, SecurityPropertySet securityPropertySet) {
        if (this.hasRequiredProperties(securityPropertySet)) {
            return !this.isMissingOrIncomplete(device, securityPropertySet);
        }
        else {
            return true;
        }
    }

    private boolean hasRequiredProperties(SecurityPropertySet securityPropertySet) {
        return securityPropertySet
                .getPropertySpecs()
                .stream()
                .anyMatch(PropertySpec::isRequired);
    }

    @Override
    public boolean securityPropertiesAreValid(Device device) {
        return device.getDeviceConfiguration()
                .getComTaskEnablements()
                .stream()
                .noneMatch(comTaskEnablement -> isMissingOrIncomplete(device, comTaskEnablement.getSecurityPropertySet()));
    }

    private boolean isMissingOrIncomplete(Device device, SecurityPropertySet securityPropertySet) {
        List<SecurityProperty> securityProperties = this.getSecurityPropertiesIgnoringPrivileges(device, this.clock.instant(), securityPropertySet);
        return securityProperties.isEmpty()
            || securityProperties
                    .stream()
                    .anyMatch(Predicates.not(SecurityProperty::isComplete));
    }

    @Override
    public void setSecurityProperties(Device device, SecurityPropertySet securityPropertySet, TypedProperties properties) {
        if (securityPropertySet.currentUserIsAllowedToEditDeviceProperties()) {
            this.doSetSecurityProperties(device, securityPropertySet, properties);
        }
        else {
            throw new SecurityPropertyException(this.thesaurus, securityPropertySet);
        }
    }

    private void doSetSecurityProperties(Device device, SecurityPropertySet securityPropertySet, TypedProperties properties) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = device.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
        RelationType relationType = this.protocolPluggableService.findSecurityPropertyRelationType(deviceProtocolPluggableClass);
        RelationTransaction transaction = relationType.newRelationTransaction();
        transaction.setFrom(this.clock.instant());
        transaction.setTo(null);
        transaction.set(DEVICE_ATTRIBUTE_NAME, device);
        transaction.set(SECURITY_PROPERTY_SET_ATTRIBUTE_NAME, securityPropertySet);
        properties.propertyNames().stream().forEach(propertyName -> transaction.set(propertyName, properties.getLocalValue(propertyName)));
        transaction.set(STATUS_ATTRIBUTE_NAME, isSecurityPropertySetComplete(securityPropertySet, properties));
        try {
            transaction.execute();
        }
        catch (BusinessException e) {
            throw new NestedRelationTransactionException(this.thesaurus, e, transaction.getRelationType().getName());
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private boolean isSecurityPropertySetComplete(SecurityPropertySet securityPropertySet, TypedProperties typedProperties) {
        return !securityPropertySet.getPropertySpecs()
                .stream()
                .anyMatch(p -> p.isRequired() && !typedProperties.hasLocalValueFor(p.getName()));
    }

    @Override
    public void deleteSecurityPropertiesFor(Device device) {
        RelationType relationType = this.findSecurityPropertyRelationType(device);
        if (relationType != null) {
            device.getDeviceConfiguration()
                    .getSecurityPropertySets()
                    .stream()
                    .forEach(securityPropertySet -> this.deleteSecurityPropertiesFor(device, relationType, securityPropertySet));
        }
    }

    private void deleteSecurityPropertiesFor(Device device, RelationType relationType, SecurityPropertySet securityPropertySet) {
        FilterAspect deviceAspect = new RelationDynamicAspect(relationType.getAttributeType(SecurityPropertySetRelationAttributeTypeNames.DEVICE_ATTRIBUTE_NAME));
        FilterAspect securityPropertySetAspect = new RelationDynamicAspect(relationType.getAttributeType(SecurityPropertySetRelationAttributeTypeNames.SECURITY_PROPERTY_SET_ATTRIBUTE_NAME));

        RelationSearchFilter searchFilter =
                new RelationSearchFilter(
                        CompositeFilterCriterium.matchAll(
                                new SimpleFilterCriterium(deviceAspect, SimpleFilterCriterium.Operator.EQUALS, device),
                                new SimpleFilterCriterium(securityPropertySetAspect, SimpleFilterCriterium.Operator.EQUALS, securityPropertySet)));
        List<Relation> relations = relationType.findByFilter(searchFilter);
        relations.stream().forEach(relation -> {
            try {
                relation.delete();
            }
            catch (BusinessException e) {
                throw new NestedRelationTransactionException(this.thesaurus, e, relationType.getName());
            }
            catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        });
    }

}