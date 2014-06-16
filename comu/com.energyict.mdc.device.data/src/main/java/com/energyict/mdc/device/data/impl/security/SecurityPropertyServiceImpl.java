package com.energyict.mdc.device.data.impl.security;

import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.dynamic.PropertySpec;
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
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationTypeSupport;
import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides an implementation for the {@link SecurityPropertyService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (16:25)
 */
@Component(name = "com.energyict.mdc.device.data.security", service = SecurityPropertyService.class, property = "name=SecurityPropertyService")
public class SecurityPropertyServiceImpl implements SecurityPropertyService {

    private volatile ProtocolPluggableService protocolPluggableService;

    public SecurityPropertyServiceImpl() {
    }

    @Inject
    public SecurityPropertyServiceImpl(ProtocolPluggableService protocolPluggableService) {
        super();
        this.setProtocolPluggableService(protocolPluggableService);
    }

    @Override
    public List<SecurityProperty> getSecurityProperties(Device device, Date when, SecurityPropertySet securityPropertySet) {
        List<SecurityProperty> defaultResult = new ArrayList<>(0);
        if (securityPropertySet.currentUserIsAllowedToViewDeviceProperties()) {
            Optional<Relation> relation = this.findActiveProperties(device, securityPropertySet, when);
            if (relation.isPresent()) {
                return this.toSecurityProperties(relation.get(), device, securityPropertySet);
            }
        }
        return defaultResult;
    }

    public Optional<Relation> findActiveProperties(Device device, SecurityPropertySet securityPropertySet, Date activeDate) {
        RelationType relationType = this.findSecurityPropertyRelationType(device);
        if (relationType != null) {
            FilterAspect deviceAspect = new RelationDynamicAspect(relationType.getAttributeType(SecurityPropertySetRelationTypeSupport.DEVICE_ATTRIBUTE_NAME));
            FilterAspect securityPropertySetAspect = new RelationDynamicAspect(relationType.getAttributeType(SecurityPropertySetRelationTypeSupport.SECURITY_PROPERTY_SET_ATTRIBUTE_NAME));

            RelationSearchFilter searchFilter =
                    new RelationSearchFilter(
                            CompositeFilterCriterium.matchAll(
                                    new SimpleFilterCriterium(deviceAspect, SimpleFilterCriterium.Operator.EQUALS, device),
                                    new SimpleFilterCriterium(securityPropertySetAspect, SimpleFilterCriterium.Operator.EQUALS, securityPropertySet)));
            List<Relation> relations = relationType.findByFilter(searchFilter);
            if (relations.isEmpty()) {
                return Optional.absent();
            }
            else {
                for (Relation relation : relations) {
                    if (relation.getPeriod().isEffective(activeDate)) {
                        return Optional.of(relation);
                    }
                }
                return null;
            }
        }
        else {
            // No RelationType means there are not security properties
            return Optional.absent();
        }
    }

    private RelationType findSecurityPropertyRelationType(Device device) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = device.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
        return this.protocolPluggableService.findSecurityPropertyRelationType(deviceProtocolPluggableClass);
    }

    private List<SecurityProperty> toSecurityProperties(Relation relation, Device device, SecurityPropertySet securityPropertySet) {
        Set<PropertySpec> propertySpecs = securityPropertySet.getPropertySpecs();
        List<SecurityProperty> properties = new ArrayList<>(propertySpecs.size());  // The maximum number of properties is defined by the number of specs
        for (PropertySpec propertySpec : propertySpecs) {
            Object propertyValue = relation.get(propertySpec.getName());
            if (propertyValue != null) {
                properties.add(new SecurityPropertyImpl(device, securityPropertySet, propertySpec, propertyValue, relation.getPeriod()));
            }
        }
        return properties;
    }

    @Override
    public boolean hasSecurityProperties(Device device, Date when, SecurityPropertySet securityPropertySet) {
        return this.findActiveProperties(device, securityPropertySet, when).isPresent();
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

}