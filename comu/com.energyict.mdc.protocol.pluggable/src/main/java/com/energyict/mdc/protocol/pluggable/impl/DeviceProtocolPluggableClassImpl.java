package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolNotAllowedByLicenseException;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol.MeterProtocolAdapterImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.SmartMeterProtocolAdapterImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLDeviceProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLMeterProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLSmartMeterProtocolAdapter;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.Device;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

/**
 * Defines a PluggableClass based on a {@link DeviceProtocol}.
 * <p>
 * We are responsible for wrapping the given Pluggable with a correct Adapter
 * ({@link com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter} or
 * {@link SmartMeterProtocolAdapterImpl})
 * or a correct cast to {@link DeviceProtocol}.
 * <p>
 * Copyrights EnergyICT
 * Date: 3/07/12
 * Time: 9:00
 */
@HasValidProperties(groups = {Save.Update.class, Save.Create.class})
public final class DeviceProtocolPluggableClassImpl extends PluggableClassWrapper<DeviceProtocol> implements DeviceProtocolPluggableClass {

    private final CustomPropertySetService customPropertySetService;
    private final PropertySpecService propertySpecService;
    private final ProtocolPluggableService protocolPluggableService;
    private final SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;
    private final CapabilityAdapterMappingFactory capabilityAdapterMappingFactory;
    private final MessageAdapterMappingFactory messageAdapterMappingFactory;
    private final DataModel dataModel;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;
    private final IdentificationService identificationService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    public DeviceProtocolPluggableClassImpl(EventService eventService, PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, DataModel dataModel, Thesaurus thesaurus, CustomPropertySetService customPropertySetService, CapabilityAdapterMappingFactory capabilityAdapterMappingFactory, MessageAdapterMappingFactory messageAdapterMappingFactory, IssueService issueService, CollectedDataFactory collectedDataFactory, MeteringService meteringService, IdentificationService identificationService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        super(eventService, thesaurus);
        this.propertySpecService = propertySpecService;
        this.protocolPluggableService = protocolPluggableService;
        this.securitySupportAdapterMappingFactory = securitySupportAdapterMappingFactory;
        this.dataModel = dataModel;
        this.customPropertySetService = customPropertySetService;
        this.capabilityAdapterMappingFactory = capabilityAdapterMappingFactory;
        this.messageAdapterMappingFactory = messageAdapterMappingFactory;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
        this.identificationService = identificationService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    static DeviceProtocolPluggableClassImpl from(DataModel dataModel, PluggableClass pluggableClass) {
        return dataModel.getInstance(DeviceProtocolPluggableClassImpl.class).initializeFrom(pluggableClass);
    }

    DeviceProtocolPluggableClassImpl initializeFrom(PluggableClass pluggableClass) {
        this.setPluggableClass(pluggableClass);
        return this;
    }

    @Override
    protected Discriminator discriminator() {
        return Discriminator.DEVICEPROTOCOL;
    }

    @Override
    public TypedProperties getProperties() {
        return super.getProperties();
    }

    @Override
    protected DeviceProtocol newInstance(PluggableClass pluggableClass) {
        Object protocol = this.protocolPluggableService.createProtocol(pluggableClass.getJavaClassName());

        DeviceProtocol deviceProtocol;
        if (protocol instanceof DeviceProtocol) {
            deviceProtocol = (DeviceProtocol) protocol;
        } else if (protocol instanceof com.energyict.mdc.upl.DeviceProtocol) {
            //Adapt it from UPL to CXO DeviceProtocol if necessary
            deviceProtocol = UPLDeviceProtocolAdapter.adapt((com.energyict.mdc.upl.DeviceProtocol) protocol).with(getThesaurus(), propertySpecService);
        } else {
            if (protocol instanceof com.energyict.mdc.upl.MeterProtocol) {
                protocol = new UPLMeterProtocolAdapter((com.energyict.mdc.upl.MeterProtocol) protocol);
            } else if (protocol instanceof com.energyict.mdc.upl.SmartMeterProtocol) {
                protocol = new UPLSmartMeterProtocolAdapter((com.energyict.mdc.upl.SmartMeterProtocol) protocol);
            }

            // Must be a lecagy pluggable class
            deviceProtocol = this.checkForProtocolWrappers(protocol);
        }
        return deviceProtocol;
    }

    /**
     * Check if the specified protocol needs a Protocol adapter
     * and return the appropriate adapter to create a {@link DeviceProtocol}.
     *
     * @param protocol the instantiated protocol
     * @throws ProtocolCreationException if and only if the given protocol does not implement: <ul>
     *                                   <li>{@link SmartMeterProtocol}</li>
     *                                   <li>{@link MeterProtocol}</li>
     *                                   </ul>
     */
    private DeviceProtocol checkForProtocolWrappers(Object protocol) {
        if (protocol instanceof SmartMeterProtocol) {
            return new SmartMeterProtocolAdapterImpl((SmartMeterProtocol) protocol, this.propertySpecService, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.capabilityAdapterMappingFactory, messageAdapterMappingFactory, this.dataModel, issueService, collectedDataFactory, meteringService, identificationService, this.getThesaurus(), deviceMessageSpecificationService);
        } else if (protocol instanceof MeterProtocol) {
            return new MeterProtocolAdapterImpl((MeterProtocol) protocol, this.propertySpecService, this.protocolPluggableService, this.securitySupportAdapterMappingFactory, this.capabilityAdapterMappingFactory, messageAdapterMappingFactory, this.dataModel, issueService, collectedDataFactory, identificationService, this.getThesaurus(), deviceMessageSpecificationService);
        } else {
            throw new ProtocolCreationException(MessageSeeds.UNSUPPORTED_LEGACY_PROTOCOL_TYPE, protocol.getClass());
        }
    }

    @Override
    protected void validateLicense() {
        if (!this.protocolPluggableService.isLicensedProtocolClassName(this.getJavaClassName())) {
            throw new ProtocolNotAllowedByLicenseException(this.getThesaurus(), this.getJavaClassName());
        }
    }

    @Override
    public void save() {
        Save.action(this.getId()).validate(dataModel, this);
        super.save();
        this.registerCustomPropertySets();
    }

    public void registerCustomPropertySets() {
        this.registerSecurityCustomPropertySet();
        this.registerDialectCustomPropertySets();
    }

    private void registerSecurityCustomPropertySet() {
        this.newInstance().getCustomPropertySet().ifPresent(this::registerSecuritySet);
    }

    private void registerSecuritySet(CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>> customPropertySet) {
        this.customPropertySetService.addSystemCustomPropertySet(customPropertySet);
    }

    private void registerDialectCustomPropertySets() {
        this.getDialectCustomPropertySets().forEach(this::registerDialect);
    }

    private Stream<CustomPropertySet> getDialectCustomPropertySets() {
        return this.newInstance()
                .getDeviceProtocolDialects()
                .stream()
                .map(DeviceProtocolDialect::getCustomPropertySet)
                .flatMap(Functions.asStream());
    }

    private void registerDialect(CustomPropertySet customPropertySet) {
        this.customPropertySetService.addSystemCustomPropertySet(customPropertySet);
    }

    @Override
    public void delete() {
        super.delete();
        this.unregisterSecurityCustomPropertySet();
        this.unregisterDialectCustomPropertySet();
    }

    private void unregisterSecurityCustomPropertySet() {
        this.newInstance().getCustomPropertySet().ifPresent(this::unregisterSecuritySet);
    }

    private void unregisterDialectCustomPropertySet() {
        this.getDialectCustomPropertySets().forEach(this::unregisterDialect);
    }

    private void unregisterSecuritySet(CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>> customPropertySet) {
        this.customPropertySetService.removeSystemCustomPropertySet(customPropertySet);
    }

    private void unregisterDialect(CustomPropertySet customPropertySet) {
        this.customPropertySetService.removeSystemCustomPropertySet(customPropertySet);
    }

    @Override
    public PluggableClassType getPluggableClassType() {
        return PluggableClassType.DeviceProtocol;
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return super.getProperties(propertySpecs);
    }

    @Override
    public String getVersion() {
        return super.getVersion();
    }

    @Override
    public DeviceProtocol getDeviceProtocol() {
        DeviceProtocol deviceProtocol = this.newInstance();
        deviceProtocol.addDeviceProtocolDialectProperties(this.getProperties(deviceProtocol.getPropertySpecs()));
        return deviceProtocol;
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICEPROTOCOL;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICEPROTOCOL;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICEPROTOCOL;
    }
}