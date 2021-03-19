/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.pluggable.PluggableClassType;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.InboundDeviceProtocol;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.common.protocol.security.RequestSecurityLevel;
import com.energyict.mdc.common.protocol.security.ResponseSecurityLevel;
import com.energyict.mdc.common.protocol.security.SecuritySuite;
import com.energyict.mdc.common.services.WrappingFinder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.LicensedProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingException;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.api.services.NotAppropriateDeviceCacheMarshallingTargetException;
import com.energyict.mdc.protocol.api.services.UnableToCreateProtocolInstance;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListener;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListenerRegistration;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.UnknownPluggableClassPropertiesException;
import com.energyict.mdc.protocol.pluggable.adapters.upl.ConnexoToUPLPropertSpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLEncryptionLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLRequestSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLResponseSecurityLevelAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.accesslevel.UPLSecuritySuiteLevelAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.TranslationKeys;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnectionFunctionTranslationKeys;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnectionTypePluggableClassTranslationKeys;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoDeviceMessageCategoryAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.ConnexoDeviceMessageSpecAdapter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.DeviceAccessLevelTranslationKeys;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLOfflineDeviceAdapter;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.crypto.HsmProtocolService;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedCertificateWrapper;
import com.energyict.mdc.upl.meterdata.CollectedConfigurationInformation;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedDeviceCache;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageAcknowledgement;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.security.CertificateWrapper;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;

/**
 * Provides an interface for the {@link ProtocolPluggableService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (13:47)
 */
@Component(name = "com.energyict.mdc.protocol.pluggable", service = {ProtocolPluggableService.class, ServerProtocolPluggableService.class, MessageSeedProvider.class, TranslationKeyProvider.class}, property = "name=" + ProtocolPluggableService.COMPONENTNAME)
public class ProtocolPluggableServiceImpl implements ServerProtocolPluggableService, MessageSeedProvider, TranslationKeyProvider {

    private static final Logger LOGGER = Logger.getLogger(ProtocolPluggableServiceImpl.class.getName());
    private static final String MDC_APPLICATION_KEY = "MDC";

    private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile EventService eventService;
    private Thesaurus messagesThesaurus;
    private Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile PluggableService pluggableService;
    private volatile IdentificationService identificationService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile CustomPropertySetInstantiatorService customPropertySetInstantiatorService;
    private volatile List<DeviceProtocolService> deviceProtocolServices = new CopyOnWriteArrayList<>();
    private volatile List<InboundDeviceProtocolService> inboundDeviceProtocolServices = new CopyOnWriteArrayList<>();
    private volatile List<ConnectionTypeService> connectionTypeServices = new CopyOnWriteArrayList<>();
    private volatile List<DeviceProtocolMessageService> deviceProtocolMessageServices = new CopyOnWriteArrayList<>();
    private volatile List<DeviceProtocolSecurityService> deviceProtocolSecurityServices = new CopyOnWriteArrayList<>();
    private volatile List<LicensedProtocolService> licensedProtocolServices = new CopyOnWriteArrayList<>();
    private volatile List<DeviceCacheMarshallingService> deviceCacheMarshallingServices = new CopyOnWriteArrayList<>();
    private volatile List<CollectedDataFactory> collectedDataFactories = new CopyOnWriteArrayList<>();
    private volatile IssueService issueService;
    private volatile LicenseService licenseService;
    private volatile TransactionService transactionService;
    private volatile UserService userService;
    private volatile MeteringService meteringService;
    private volatile DataVaultService dataVaultService;
    private volatile UpgradeService upgradeService;
    private volatile HsmProtocolService hsmProtocolService;

    private volatile boolean installed = false;
    private volatile List<ProtocolDeploymentListenerRegistrationImpl> registrations = new CopyOnWriteArrayList<>();

    // For OSGi purposes
    public ProtocolPluggableServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public ProtocolPluggableServiceImpl(
            OrmService ormService,
            ThreadPrincipalService threadPrincipalService,
            EventService eventService,
            NlsService nlsService,
            IssueService issueService,
            UserService userService,
            MeteringService meteringService,
            PropertySpecService propertySpecService,
            PluggableService pluggableService,
            IdentificationService identificationService,
            DeviceMessageSpecificationService deviceMessageSpecificationService,
            CustomPropertySetInstantiatorService customPropertySetInstantiatorService,
            CustomPropertySetService customPropertySetService,
            LicenseService licenseService,
            DataVaultService dataVaultService,
            TransactionService transactionService,
            UpgradeService upgradeService,
            HsmProtocolService hsmProtocolService
    ) {
        this();
        setOrmService(ormService);
        setThreadPrincipalService(threadPrincipalService);
        setEventService(eventService);
        setNlsService(nlsService);
        setMeteringService(meteringService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setIdentificationService(identificationService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setCustomPropertySetInstantiatorService(customPropertySetInstantiatorService);
        setCustomPropertySetService(customPropertySetService);
        setPluggableService(pluggableService);
        setUserService(userService);
        setLicenseService(licenseService);
        setDataVaultService(dataVaultService);
        setTransactionService(transactionService);
        setUpgradeService(upgradeService);
        setHsmProtocolService(hsmProtocolService);
        activate();
    }

    @Override
    public ProtocolDeploymentListenerRegistration register(ProtocolDeploymentListener listener) {
        ProtocolDeploymentListenerRegistrationImpl registration = new ProtocolDeploymentListenerRegistrationImpl(listener);
        this.registrations.add(registration);
        registration.notifyAlreadyDeployedServices();
        return registration;
    }

    private void unregister(ProtocolDeploymentListenerRegistrationImpl registration) {
        this.registrations.remove(registration);
    }

    @Override
    public Object createProtocol(String javaClassName) {
        for (DeviceProtocolService service : this.deviceProtocolServices) {
            try {
                return service.createProtocol(javaClassName);
            } catch (ProtocolCreationException e) {
                // Try the next DeviceProtocolService
            }
        }
        if (this.deviceProtocolServices.isEmpty()) {
            LOGGER.fine("No registered device protocol services");
        } else {
            LOGGER.log(Level.FINE, this::allDeviceProtocolServiceClassNames);
        }
        throw new NoServiceFoundThatCanLoadTheJavaClass(javaClassName);
    }

    private String allDeviceProtocolServiceClassNames() {
        return this.deviceProtocolServices
                .stream()
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public Object createDeviceProtocolMessagesFor(String javaClassName) {
        for (DeviceProtocolMessageService service : this.deviceProtocolMessageServices) {
            try {
                return service.createDeviceProtocolMessagesFor(javaClassName);
            } catch (ProtocolCreationException e) {
                // Try the next DeviceProtocolMessageService
            }
        }
        throw new NoServiceFoundThatCanLoadTheJavaClass(javaClassName);
    }

    @Override
    public Object createDeviceProtocolSecurityFor(String javaClassName) {
        for (DeviceProtocolSecurityService service : this.deviceProtocolSecurityServices) {
            try {
                return service.createDeviceProtocolSecurityFor(javaClassName);
            } catch (DeviceProtocolAdapterCodingExceptions e) {
                // Try the next DeviceProtocolSecurityService
            }
        }
        throw new NoServiceFoundThatCanLoadTheJavaClass(javaClassName);
    }

    @Override
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className) {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.DeviceProtocol, name, className);
        final DeviceProtocolPluggableClassImpl deviceProtocolPluggableClass = DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass);
        deviceProtocolPluggableClass.save();
        pluggableClass.save();
        return deviceProtocolPluggableClass;
    }

    @Override
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className, TypedProperties properties) {
        boolean dirty = false;
        Set<String> unsupportedPropertyNames = properties.propertyNames();
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.newDeviceProtocolPluggableClass(name, className);
        for (PropertySpec propertySpec : deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs()) {
            unsupportedPropertyNames.remove(propertySpec.getName());
            if (properties.hasValueFor(propertySpec.getName())) {
                deviceProtocolPluggableClass
                        .setProperty(
                                propertySpec,
                                properties.getProperty(propertySpec.getName()));
                dirty = true;
            }
        }
        if (!unsupportedPropertyNames.isEmpty()) {
            throw new UnknownPluggableClassPropertiesException(this.thesaurus, unsupportedPropertyNames, className);
        }
        if (dirty) {
            deviceProtocolPluggableClass.save();
        }
        return deviceProtocolPluggableClass;
    }

    @Override
    public void registerDeviceProtocolPluggableClassAsCustomPropertySet(String javaClassName) {
        List<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndClassName(PluggableClassType.DeviceProtocol, javaClassName);
        if (!pluggableClasses.isEmpty()) {
            DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClasses.get(0)).registerCustomPropertySets();
        }
    }

    private List<LicensedProtocol> getAllKnownProtocols() {
        List<LicensedProtocol> protocols = new ArrayList<>();
        for (LicensedProtocolService licensedProtocolService : this.licensedProtocolServices) {
            protocols.addAll(licensedProtocolService.getAllKnownProtocols());
        }
        return protocols;
    }

    @Override
    public List<LicensedProtocol> getAllLicensedProtocols() {
        Optional<License> mdcLicense = this.getMdcLicense();
        if (mdcLicense.isPresent()) {
            List<LicensedProtocol> licensedProtocols = new ArrayList<>();
            for (LicensedProtocolService licensedProtocolService : this.licensedProtocolServices) {
                licensedProtocols.addAll(licensedProtocolService.getAllLicensedProtocols(mdcLicense.get()));
            }
            return licensedProtocols;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isLicensedProtocolClassName(String javaClassName) {
        Optional<License> mdcLicense = this.getMdcLicense();
        if (mdcLicense.isPresent()) {
            for (LicensedProtocolService licensedProtocolService : this.licensedProtocolServices) {
                if (licensedProtocolService.isValidJavaClassName(javaClassName, mdcLicense.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<License> getMdcLicense() {
        return this.licenseService.getLicenseForApplication(MDC_APPLICATION_KEY);
    }

    @Override
    public Finder<DeviceProtocolPluggableClass> findAllDeviceProtocolPluggableClasses() {
        return new WrappingFinder<DeviceProtocolPluggableClass, PluggableClass>(this.pluggableService.findAllByType(PluggableClassType.DeviceProtocol)) {
            @Override
            public List<DeviceProtocolPluggableClass> convert(List<PluggableClass> pluggableClasses) {
                return pluggableClasses
                        .stream()
                        .map(pluggableClass -> DeviceProtocolPluggableClassImpl.from(ProtocolPluggableServiceImpl.this.dataModel, pluggableClass))
                        .collect(Collectors.toList());
            }
        };
    }

    @Override
    public LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        for (LicensedProtocolService licensedProtocolService : this.licensedProtocolServices) {
            LicensedProtocol licensedProtocol = licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            if (licensedProtocol != null) {
                return licensedProtocol;
            }
        }
        return null;
    }

    @Override
    public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long id) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findByTypeAndId(PluggableClassType.DeviceProtocol, id);
        return pluggableClass.map(pc -> DeviceProtocolPluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public Optional<DeviceProtocolPluggableClass> findAndLockDeviceProtocolPluggableClassByIdAndVersion(long id, long version) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findAndLockPluggableClassByIdAndVersion(PluggableClassType.DeviceProtocol, id, version);
        return pluggableClass.map(pc -> DeviceProtocolPluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassByName(String name) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findByTypeAndName(PluggableClassType.DeviceProtocol, name);
        if (pluggableClass.isPresent()) {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass.get());
            return Optional.of(deviceProtocolPluggableClass);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassesByClassName(String className) {
        List<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndClassName(PluggableClassType.DeviceProtocol, className);
        List<DeviceProtocolPluggableClass> deviceProtocolPluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            deviceProtocolPluggableClasses.add(DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return deviceProtocolPluggableClasses;
    }

    @Override
    public void deleteDeviceProtocolPluggableClass(long id) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = this.findDeviceProtocolPluggableClass(id);
        deviceProtocolPluggableClass
                .orElseThrow(() -> new NotFoundException("DeviceProtocolPluggableClass with id " + id + " cannot be deleted because it does not exist"))
                .delete();
    }

    @Override
    public DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass(DeviceProtocolPluggableClass pluggableClass, String dialectName) {
        DeviceProtocolDialect deviceProtocolDialect = this.getDeviceProtocolDialectFor(pluggableClass.getDeviceProtocol(), dialectName);
        return new DeviceProtocolDialectUsagePluggableClassImpl(pluggableClass, deviceProtocolDialect);
    }

    private DeviceProtocolDialect getDeviceProtocolDialectFor(DeviceProtocol deviceProtocol, String name) {
        for (DeviceProtocolDialect deviceProtocolDialect : deviceProtocol.getDeviceProtocolDialects()) {
            if (deviceProtocolDialect.getDeviceProtocolDialectName().equals(name)) {
                return deviceProtocolDialect;
            }
        }
        return null;
    }

    @Override
    public List<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClassByClassName(String javaClassName) {
        List<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndClassName(PluggableClassType.DiscoveryProtocol, javaClassName);
        List<InboundDeviceProtocolPluggableClass> inboundDeviceProtocolPluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            inboundDeviceProtocolPluggableClasses.add(InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return inboundDeviceProtocolPluggableClasses;
    }

    @Override
    public Optional<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClass(long id) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findByTypeAndId(PluggableClassType.DiscoveryProtocol, id);
        return pluggableClass.map(pc -> InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public Optional<InboundDeviceProtocolPluggableClass> findAndLockInboundDeviceProtocolPluggableClassByIdAndVersion(long id, long version) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findAndLockPluggableClassByIdAndVersion(PluggableClassType.DiscoveryProtocol, id, version);
        return pluggableClass.map(pc -> InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public void deleteInboundDeviceProtocolPluggableClass(long id) {
        Optional<InboundDeviceProtocolPluggableClass> inboundDeviceProtocolPluggableClass = this.findInboundDeviceProtocolPluggableClass(id);
        if (inboundDeviceProtocolPluggableClass.isPresent()) {
            inboundDeviceProtocolPluggableClass.get().delete();
        }
    }

    @Override
    public List<InboundDeviceProtocolPluggableClass> findAllInboundDeviceProtocolPluggableClass() {
        List<PluggableClass> pluggableClasses = this.pluggableService.findAllByType(PluggableClassType.DiscoveryProtocol).find();
        List<InboundDeviceProtocolPluggableClass> inboundDeviceProtocolPluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            inboundDeviceProtocolPluggableClasses.add(InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return inboundDeviceProtocolPluggableClasses;
    }

    @Override
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName) {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.DiscoveryProtocol, name, javaClassName);
        final InboundDeviceProtocolPluggableClassImpl inboundDeviceProtocolPluggableClass = InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass);
        pluggableClass.save();
        inboundDeviceProtocolPluggableClass.save();
        return inboundDeviceProtocolPluggableClass;
    }

    @Override
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName, TypedProperties properties) {
        InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass = this.newInboundDeviceProtocolPluggableClass(name, javaClassName);
        for (PropertySpec propertySpec : inboundDeviceProtocolPluggableClass.getInboundDeviceProtocol().getPropertySpecs()) {
            if (properties.hasValueFor(propertySpec.getName())) {
                inboundDeviceProtocolPluggableClass.setProperty(propertySpec, properties.getProperty(propertySpec.getName()));
            }
        }
        inboundDeviceProtocolPluggableClass.save();
        return inboundDeviceProtocolPluggableClass;
    }

    @Override
    public List<ConnectionTypePluggableClass> findConnectionTypePluggableClassByClassName(String javaClassName) {
        List<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndClassName(PluggableClassType.ConnectionType, javaClassName);
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            connectionTypePluggableClasses.add(ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return connectionTypePluggableClasses;
    }

    @Override
    public Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClassByNameTranslationKey(String name) {
        Optional<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndName(PluggableClassType.ConnectionType, name);
        return pluggableClasses.map(pc -> ConnectionTypePluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClass(long id) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findByTypeAndId(PluggableClassType.ConnectionType, id);
        return pluggableClass.map(pc -> ConnectionTypePluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public List<ConnectionTypePluggableClass> findAllConnectionTypePluggableClasses() {
        List<PluggableClass> pluggableClasses = this.pluggableService.findAllByType(PluggableClassType.ConnectionType).find();
        return pluggableClasses
                .stream()
                .map(pluggableClass -> ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass))
                .collect(Collectors.toList());
    }

    @Override
    public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName) {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.ConnectionType, name, javaClassName);
        final ConnectionTypePluggableClassImpl connectionTypePluggableClass = ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass);
        pluggableClass.save();
        connectionTypePluggableClass.save();
        return connectionTypePluggableClass;
    }

    @Override
    public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName, TypedProperties properties) {
        ConnectionTypePluggableClass connectionTypePluggableClass = this.newConnectionTypePluggableClass(name, javaClassName);
        connectionTypePluggableClass
                .getConnectionType()
                .getPropertySpecs()
                .stream()
                .filter(propertySpec -> properties.hasValueFor(propertySpec.getName()))
                .forEach(propertySpec -> connectionTypePluggableClass.setProperty(propertySpec, properties.getProperty(propertySpec.getName())));
        connectionTypePluggableClass.save();
        return connectionTypePluggableClass;
    }

    @Override
    public void registerConnectionTypePluggableClassAsCustomPropertySet(String javaClassName) {
        List<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndClassName(PluggableClassType.ConnectionType, javaClassName);
        if (!pluggableClasses.isEmpty()) {
            ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClasses.get(0)).registerCustomPropertySet();
        }
    }

    @Override
    public Optional<Object> unMarshallDeviceProtocolCache(String jsonCache) {
        try {
            for (DeviceCacheMarshallingService service : this.deviceCacheMarshallingServices) {
                try {
                    return service.unMarshallCache(jsonCache);
                } catch (NotAppropriateDeviceCacheMarshallingTargetException e) {
                    // Try the next service
                }
            }
        } catch (DeviceCacheMarshallingException e) {
            // A service accepted the
            LOGGER.severe(e.getMessage());
            LOGGER.log(Level.FINE, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public String marshallDeviceProtocolCache(Object legacyCache) {
        if (legacyCache != null) {
            for (DeviceCacheMarshallingService service : this.deviceCacheMarshallingServices) {
                try {
                    return service.marshall(legacyCache);
                } catch (NotAppropriateDeviceCacheMarshallingTargetException e) {
                    // Try the next service
                } catch (DeviceCacheMarshallingException e) {
                    /* Some services don't distinguish between DeviceCacheMarshallingException and NotAppropriateDeviceCacheMarshallingTargetException.
                     * Log the failure and try the next service. */
                    LOGGER.severe(e.getMessage());
                    LOGGER.log(Level.FINE, e.getMessage(), e);
                }
            }
        }
        return "";
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Protocol Pluggable Classes");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.messagesThesaurus = nlsService.getThesaurus(DeviceMessageSpecificationService.COMPONENT_NAME, Layer.DOMAIN);
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN).join(messagesThesaurus);
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setPluggableService(PluggableService pluggableService) {
        this.pluggableService = pluggableService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setHsmProtocolService(HsmProtocolService hsmProtocolService) {
        this.hsmProtocolService = hsmProtocolService;
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolServices.add(deviceProtocolService);
        if (installed) {
            registerDeviceProtocolPluggableClasses();
            this.registrations.forEach(each -> each.notifyAdded(deviceProtocolService));
        }
    }

    @SuppressWarnings("unused")
    public void removeDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolServices.remove(deviceProtocolService);
        this.registrations.forEach(each -> each.notifyRemove(deviceProtocolService));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolMessageService(DeviceProtocolMessageService deviceProtocolMessageService) {
        this.deviceProtocolMessageServices.add(deviceProtocolMessageService);
    }

    @SuppressWarnings("unused")
    public void removeDeviceProtocolMessageService(DeviceProtocolMessageService deviceProtocolMessageService) {
        this.deviceProtocolMessageServices.remove(deviceProtocolMessageService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolSecurityService(DeviceProtocolSecurityService deviceProtocolSecurityService) {
        this.deviceProtocolSecurityServices.add(deviceProtocolSecurityService);
    }

    @SuppressWarnings("unused")
    public void removeDeviceProtocolSecurityService(DeviceProtocolSecurityService deviceProtocolSecurityService) {
        this.deviceProtocolSecurityServices.remove(deviceProtocolSecurityService);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolServices.add(licensedProtocolService);
        if (this.installed) {
            this.registerDeviceProtocolPluggableClasses();
        }
    }

    @SuppressWarnings("unused")
    public void removeLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolServices.remove(licensedProtocolService);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.add(inboundDeviceProtocolService);
        if (installed) {
            registerInboundDeviceProtocolPluggableClasses();
            this.registrations.forEach(each -> each.notifyAdded(inboundDeviceProtocolService));
        }
    }

    @SuppressWarnings("unused")
    public void removeInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.remove(inboundDeviceProtocolService);
        this.registrations.forEach(each -> each.notifyRemove(inboundDeviceProtocolService));
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setIdentificationService(IdentificationService identificationService) {
        this.identificationService = identificationService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setCustomPropertySetInstantiatorService(CustomPropertySetInstantiatorService customPropertySetInstantiatorService) {
        this.customPropertySetInstantiatorService = customPropertySetInstantiatorService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.add(connectionTypeService);
        if (installed) {
            registerConnectionTypePluggableClasses();
            this.registrations.forEach(each -> each.notifyAdded(connectionTypeService));
        }
    }

    @SuppressWarnings("unused")
    public void removeConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.remove(connectionTypeService);
        this.registrations.forEach(each -> each.notifyRemove(connectionTypeService));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceCacheMarshallingService(DeviceCacheMarshallingService deviceCacheMarshallingService) {
        this.deviceCacheMarshallingServices.add(deviceCacheMarshallingService);
    }

    @SuppressWarnings("unused")
    public void removeDeviceCacheMarshallingService(DeviceCacheMarshallingService deviceCacheMarshallingService) {
        this.deviceCacheMarshallingServices.remove(deviceCacheMarshallingService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCollectedDataFactory(CollectedDataFactory collectedDataFactory) {
        this.collectedDataFactories.add(collectedDataFactory);
    }

    @SuppressWarnings("unused")
    public void removeCollectedDataFactory(CollectedDataFactory collectedDataFactory) {
        this.collectedDataFactories.remove(collectedDataFactory);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @Override
    public ConnectionType createConnectionType(String javaClassName) {
        ConnectionType connectionType = null;
        Throwable throwable = null;
        for (ConnectionTypeService connectionTypeService : this.connectionTypeServices) {
            try {
                connectionType = connectionTypeService.createConnectionType(javaClassName);
            } catch (Exception e) {
                throwable = e;
                // silently ignore, will try other service
            }
        }
        if (connectionType != null) {
            return connectionType;
        } else {
            if (this.connectionTypeServices.isEmpty()) {
                LOGGER.fine("No registered connection type services");
            } else {
                LOGGER.log(Level.FINE, this::allConnectionTypeServiceClassNames);
            }
            throw new UnableToCreateProtocolInstance(throwable, javaClassName);
        }
    }

    private String allConnectionTypeServiceClassNames() {
        return this.connectionTypeServices
                .stream()
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
        InboundDeviceProtocol inboundDeviceProtocol;
        Exception throwable = null;
        for (InboundDeviceProtocolService inboundDeviceProtocolService : this.inboundDeviceProtocolServices) {
            try {
                inboundDeviceProtocol = inboundDeviceProtocolService.createInboundDeviceProtocolFor(pluggableClass);
                if (inboundDeviceProtocol != null) {
                    return inboundDeviceProtocol;
                }
            } catch (Exception e) {
                throwable = e;
                // silently ignore, will try other service
            }
        }
        if (this.inboundDeviceProtocolServices.isEmpty()) {
            LOGGER.fine("No registered inbound device protocol services");
        } else {
            LOGGER.log(Level.FINE, this::allInboundDeviceProtocolServiceClassNames);
        }
        throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, throwable, pluggableClass.getJavaClassName());
    }

    private String allInboundDeviceProtocolServiceClassNames() {
        return this.inboundDeviceProtocolServices
                .stream()
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public PropertySpec adapt(com.energyict.mdc.upl.properties.PropertySpec uplPropertySpec) {
        return UPLToConnexoPropertySpecAdapter.adaptTo(uplPropertySpec);
    }

    @Override
    public com.energyict.mdc.upl.properties.PropertySpec adapt(PropertySpec propertySpec) {
        return ConnexoToUPLPropertSpecAdapter.adaptTo(propertySpec);
    }

    @Override
    public AuthenticationDeviceAccessLevel adapt(com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel uplLevel) {
        return UPLAuthenticationLevelAdapter.adaptTo(uplLevel, thesaurus);
    }

    @Override
    public EncryptionDeviceAccessLevel adapt(com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel uplLevel) {
        return UPLEncryptionLevelAdapter.adaptTo(uplLevel, thesaurus);
    }

    @Override
    public SecuritySuite adapt(com.energyict.mdc.upl.security.SecuritySuite uplLevel) {
        return UPLSecuritySuiteLevelAdapter.adaptTo(uplLevel, thesaurus);
    }

    @Override
    public RequestSecurityLevel adapt(com.energyict.mdc.upl.security.RequestSecurityLevel uplLevel) {
        return UPLRequestSecurityLevelAdapter.adaptTo(uplLevel, thesaurus);
    }

    @Override
    public ResponseSecurityLevel adapt(com.energyict.mdc.upl.security.ResponseSecurityLevel uplLevel) {
        return UPLResponseSecurityLevelAdapter.adaptTo(uplLevel, thesaurus);
    }

    @Override
    public com.energyict.mdc.upl.messages.DeviceMessageCategory adapt(DeviceMessageCategory connexoCategory) {
        return ConnexoDeviceMessageCategoryAdapter.adaptTo(connexoCategory);
    }

    @Override
    public com.energyict.mdc.upl.messages.DeviceMessageSpec adapt(DeviceMessageSpec connexoSpec) {
        return ConnexoDeviceMessageSpecAdapter.adaptTo(connexoSpec);
    }

    @Override
    public OfflineDevice adapt(com.energyict.mdc.upl.offline.OfflineDevice offlineDevice) {
        return new UPLOfflineDeviceAdapter(offlineDevice);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(TransactionService.class).toInstance(transactionService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(PluggableService.class).toInstance(pluggableService);
                bind(IdentificationService.class).toInstance(identificationService);
                bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                bind(CustomPropertySetInstantiatorService.class).toInstance(customPropertySetInstantiatorService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(IssueService.class).toInstance(issueService);
                bind(LicenseService.class).toInstance(licenseService);
                bind(UserService.class).toInstance(userService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(DataVaultService.class).toInstance(dataVaultService);
                bind(HsmProtocolService.class).toInstance(hsmProtocolService);
                bind(CollectedDataFactory.class).toInstance(new CompositeCollectedDataFactory());
                bind(MessageAdapterMappingFactory.class).to(MessageAdapterMappingFactoryImpl.class).in(Singleton.class);
                bind(SecuritySupportAdapterMappingFactory.class).to(SecuritySupportAdapterMappingFactoryImpl.class).in(Singleton.class);
                bind(CapabilityAdapterMappingFactory.class).to(CapabilityAdapterMappingFactoryImpl.class).in(Singleton.class);
                bind(ProtocolPluggableService.class).toInstance(ProtocolPluggableServiceImpl.this);
                bind(ServerProtocolPluggableService.class).toInstance(ProtocolPluggableServiceImpl.this);
            }
        };
    }

    @Activate
    public void activate() {
        //TODO need a proper implementation of the DataVault!
        this.dataModel.register(this.getModule());

        upgradeService.register(InstallIdentifier.identifier("MultiSense", ProtocolPluggableService.COMPONENTNAME), dataModel, Installer.class, ImmutableMap.of(version(10, 3), UpgraderV10_3.class));

        this.installed = true;
        this.registerAllPluggableClasses();
    }

    private void registerAllPluggableClasses() {
        if (installed) {
            this.registerInboundDeviceProtocolPluggableClasses();
            this.registerDeviceProtocolPluggableClasses();
            this.registerConnectionTypePluggableClasses();
        }
    }

    private void setPrincipal() {
        if (threadPrincipalService.getPrincipal() == null) {
            this.threadPrincipalService.set(getPrincipal());
        }
        this.threadPrincipalService.set(COMPONENTNAME, "PluggableClassRegistration");
    }

    private Principal getPrincipal() {
        return () -> "Jupiter Installer";
    }

    private void registerDeviceProtocolPluggableClasses() {
        if (!this.deviceProtocolServices.isEmpty()) {
            this.setPrincipal();
            DeviceProtocolPluggableClassRegistrar registrar = new DeviceProtocolPluggableClassRegistrar(this, this.transactionService, this.meteringService);
            registrar.registerAll(this.getAllKnownProtocols());
        } else {
            LOGGER.fine("No device protocol services have registered yet, makes no sense to attempt to register all device protocol pluggable classes");
        }
    }

    private void registerInboundDeviceProtocolPluggableClasses() {
        if (!this.inboundDeviceProtocolServices.isEmpty()) {
            this.setPrincipal();
            InboundDeviceProtocolPluggableClassRegistrar registrar = new InboundDeviceProtocolPluggableClassRegistrar(this, this.transactionService);
            registrar.registerAll(Collections.unmodifiableList(this.inboundDeviceProtocolServices));
        } else {
            LOGGER.fine("No inbound protocol services have registered yet, makes no sense to attempt to register all inbound device protocol pluggable classes");
        }
    }

    private void registerConnectionTypePluggableClasses() {
        if (!this.connectionTypeServices.isEmpty()) {
            this.setPrincipal();
            ConnectionTypePluggableClassRegistrar registrar = new ConnectionTypePluggableClassRegistrar(this, this.transactionService);
            registrar.registerAll(Collections.unmodifiableList(this.connectionTypeServices));
        } else {
            LOGGER.fine("No connection type services have registered yet, makes no sense to attempt to register all connection type pluggable classes");
        }
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public String getComponentName() {
        return ProtocolPluggableService.COMPONENTNAME;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Stream.of(TranslationKeys.values()),
                Stream.of(ConnectionTypePluggableClassTranslationKeys.values()),
                Stream.of(ConnectionFunctionTranslationKeys.values()),
                Stream.of(DeviceAccessLevelTranslationKeys.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public Thesaurus protocolsThesaurus() {
        return this.messagesThesaurus;
    }

    private class CompositeCollectedDataFactory implements CollectedDataFactory {
        private CollectedDataFactory getCollectedDataFactory() {
            return collectedDataFactories.stream().findFirst().orElseThrow(() -> new IllegalStateException("No suitable CollectedDataFactory found"));
        }

        @Override
        public CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
            return this.getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
        }

        @Override
        public CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createCollectedTopology(deviceIdentifier);
        }

        @Override
        public CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier) {
            return this.getCollectedDataFactory().createCollectedLogBook(logBookIdentifier);
        }

        @Override
        public CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier) {
            return this.getCollectedDataFactory().createMaximumDemandCollectedRegister(registerIdentifier);
        }

        @Override
        public CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier) {
            return this.getCollectedDataFactory().createCollectedRegisterForAdapter(registerIdentifier);
        }

        @Override
        public CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier) {
            return this.getCollectedDataFactory().createBillingCollectedRegister(registerIdentifier);
        }

        @Override
        public CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier) {
            return this.getCollectedDataFactory().createDefaultCollectedRegister(registerIdentifier);
        }

        @Override
        public CollectedLogBook createNoLogBookCollectedData(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createNoLogBookCollectedData(deviceIdentifier);
        }

        @Override
        public CollectedMessage createCollectedMessage(MessageIdentifier messageIdentifier) {
            return this.getCollectedDataFactory().createCollectedMessage(messageIdentifier);
        }

        @Override
        public CollectedDeviceInfo createDeviceConnectionProperties(DeviceIdentifier deviceIdentifier, Map<String, Object> connectionPropertyNameAndValue) {
            return this.getCollectedDataFactory().createDeviceConnectionProperties(deviceIdentifier, connectionPropertyNameAndValue);
        }

        @Override
        public CollectedMessage createCollectedMessageWithLogbookData(MessageIdentifier messageIdentifier, CollectedLogBook collectedLoadProfile) {
            return this.getCollectedDataFactory().createCollectedMessageWithLogbookData(messageIdentifier, collectedLoadProfile);
        }

        @Override
        public CollectedMessage createCollectedMessageWithUpdateSecurityProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue) {
            return this.getCollectedDataFactory().createCollectedMessageWithUpdateSecurityProperty(deviceIdentifier, messageIdentifier, propertyName, propertyValue);
        }

        @Override
        public CollectedMessage createCollectedMessageWithUpdateGeneralProperty(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName, Object propertyValue) {
            return this.getCollectedDataFactory().createCollectedMessageWithUpdateGeneralProperty(deviceIdentifier, messageIdentifier, propertyName, propertyValue);
        }

        @Override
        public CollectedMessage createCollectedMessageWithFile(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String fileName, String fileExtension, byte[] contents) {
            return this.getCollectedDataFactory().createCollectedMessageWithFile(deviceIdentifier, messageIdentifier, fileName, fileExtension, contents);
        }

        @Override
        public CollectedCertificateWrapper createCollectedCertificateWrapper(X509Certificate x509Certificate) {
            return this.getCollectedDataFactory().createCollectedCertificateWrapper(x509Certificate);
        }

        @Override
        public CollectedMessage createCollectedMessageWithCertificates(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CertificateWrapper> certificateWrappers) {
            return this.getCollectedDataFactory().createCollectedMessageWithCertificates(deviceIdentifier, messageIdentifier, certificateWrappers);
        }

        @Override
        public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, String meterSerialNumber) {
            return this.getCollectedDataFactory().createCollectedLoadProfileConfiguration(profileObisCode, meterSerialNumber);
        }

        @Override
        public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier deviceIdentifier, String meterSerialNumber) {
            return this.getCollectedDataFactory().createCollectedLoadProfileConfiguration(profileObisCode, deviceIdentifier, meterSerialNumber);
        }

        @Override
        public CollectedDeviceInfo createCollectedDeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
            return this.getCollectedDataFactory().createCollectedDeviceDialectProperty(deviceIdentifier, propertyName, propertyValue);
        }

        @Override
        public CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgement(MessageIdentifier messageIdentifier) {
            return this.getCollectedDataFactory().createDeviceProtocolMessageAcknowledgement(messageIdentifier);
        }

        @Override
        public CollectedMessageAcknowledgement createDeviceProtocolMessageAcknowledgementFromSms(MessageIdentifier messageIdentifier) {
            return this.getCollectedDataFactory().createDeviceProtocolMessageAcknowledgementFromSms(messageIdentifier);
        }

        @Override
        public CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile) {
            return this.getCollectedDataFactory().createCollectedMessageWithLoadProfileData(messageIdentifier, collectedLoadProfile);
        }

        @Override
        public CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile, LoadProfileReader loadProfileReader) {
            return this.getCollectedDataFactory().createCollectedMessageWithLoadProfileData(messageIdentifier, collectedLoadProfile, loadProfileReader);
        }

        @Override
        public CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters) {
            return this.getCollectedDataFactory().createCollectedMessageWithRegisterData(deviceIdentifier, messageIdentifier, collectedRegisters);
        }

        @Override
        public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createCollectedDeviceCache(deviceIdentifier);
        }

        @Override
        public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier, DeviceProtocolCache deviceCache) {
            return this.getCollectedDataFactory().createCollectedDeviceCache(deviceIdentifier, deviceCache);
        }

        @Override
        public CollectedMessage createCollectedMessageForSwappingSecurityAccessorKeys(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, String propertyName) {
            return this.getCollectedDataFactory().createCollectedMessageForSwappingSecurityAccessorKeys(deviceIdentifier, messageIdentifier, propertyName);
        }

        @Override
        public CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages) {
            return this.getCollectedDataFactory().createCollectedMessageList(offlineDeviceMessages);
        }

        @Override
        public CollectedMessageList createEmptyCollectedMessageList() {
            return this.getCollectedDataFactory().createEmptyCollectedMessageList();
        }

        @Override
        public CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createCollectedRegisterList(deviceIdentifier);
        }

        @Override
        public CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileName, String fileExtension, byte[] contents) {
            return this.getCollectedDataFactory().createCollectedConfigurationInformation(deviceIdentifier, fileName, fileExtension, contents);
        }

        @Override
        public CollectedDeviceInfo createDeviceConnectionProperty(DeviceIdentifier deviceIdentifier, Object propertyValue, String propertyName) {
            return this.getCollectedDataFactory().createDeviceConnectionProperty(deviceIdentifier, propertyValue, propertyName);
        }

        @Override
        public CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
            return this.getCollectedDataFactory().createCollectedDeviceProtocolProperty(deviceIdentifier, propertyName, propertyValue);
        }

        @Override
        public CollectedFirmwareVersion createFirmwareVersionsCollectedData(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createFirmwareVersionsCollectedData(deviceIdentifier);
        }

        @Override
        public CollectedBreakerStatus createBreakerStatusCollectedData(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createBreakerStatusCollectedData(deviceIdentifier);
        }

        @Override
        public CollectedCalendar createCalendarCollectedData(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createCalendarCollectedData(deviceIdentifier);
        }
    }

    private class ProtocolDeploymentListenerRegistrationImpl implements ProtocolDeploymentListenerRegistration {
        private final ProtocolDeploymentListener listener;

        private ProtocolDeploymentListenerRegistrationImpl(ProtocolDeploymentListener listener) {
            super();
            this.listener = listener;
        }

        private void notifyAlreadyDeployedServices() {
            deviceProtocolServices.forEach(this::notifyAdded);
            inboundDeviceProtocolServices.forEach(this::notifyAdded);
            connectionTypeServices.forEach(this::notifyAdded);
        }

        private void notifyAdded(DeviceProtocolService service) {
            this.listener.deviceProtocolServiceDeployed(service);
        }

        private void notifyRemove(DeviceProtocolService service) {
            this.listener.deviceProtocolServiceUndeployed(service);
        }

        private void notifyAdded(InboundDeviceProtocolService service) {
            this.listener.inboundDeviceProtocolServiceDeployed(service);
        }

        private void notifyRemove(InboundDeviceProtocolService service) {
            this.listener.inboundDeviceProtocolServiceUndeployed(service);
        }

        private void notifyAdded(ConnectionTypeService service) {
            this.listener.connectionTypeServiceDeployed(service);
        }

        private void notifyRemove(ConnectionTypeService service) {
            this.listener.connectionTypeServiceUndeployed(service);
        }

        @Override
        public void unregister() {
            ProtocolPluggableServiceImpl.this.unregister(this);
        }
    }

}