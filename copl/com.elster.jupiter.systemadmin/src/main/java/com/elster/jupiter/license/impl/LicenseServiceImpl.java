package com.elster.jupiter.license.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyrights EnergyICT
 * Date: 28/03/2014
 * Time: 16:28
 */
@Component(
        name = "com.elster.jupiter.license",
        service = {InstallService.class, LicenseService.class, PrivilegesProvider.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = {"name=" + LicenseService.COMPONENTNAME},
        immediate = true)
public class LicenseServiceImpl implements LicenseService, InstallService, PrivilegesProvider, MessageSeedProvider, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile OrmService ormService;
    private volatile UserService userService;
    private volatile EventService eventService;

    private BundleContext context;
    private Timer dailyCheck = new Timer("License check");
    List<ServiceRegistration<License>> licenseServices = new CopyOnWriteArrayList<>();

    public LicenseServiceImpl() {
    }

    @Inject
    public LicenseServiceImpl(OrmService ormService, UserService userService, EventService eventService, NlsService nlsService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setUserService(userService);
        setEventService(eventService);
        activate(null);
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    public void install() {
        dataModel.install(true, true);
        //createPrivileges();
        createEventTypes();
    }

    private void createEventTypes() {
        List<com.elster.jupiter.events.EventType> eventTypesForComponent = eventService.getEventTypesForComponent(LicenseService.COMPONENTNAME);
        for (EventType eventType : EventType.values()) {
            if (!eventTypesForComponent.stream().anyMatch(et -> et.getName().equals(eventType.name()))) {
                eventType.install(eventService);
            }
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "EVT", "NLS" +
                "");
    }

    @Override
    public String getModuleName() {
        return LicenseService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(LicenseService.COMPONENTNAME, Privileges.RESOURCE_LICENSE.getKey(), Privileges.RESOURCE_LICENSE_DESCRIPTION.getKey(),
                Arrays.asList(
                        Privileges.Constants.VIEW_LICENSE, Privileges.Constants.UPLOAD_LICENSE)));
        return resources;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel(COMPONENTNAME, "License management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(this.getComponentName(), Layer.DOMAIN);
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Activate
    public void activate(BundleContext context) {
        dataModel.register(getModule());

        this.context = context;
        dailyCheck.scheduleAtFixedRate(new LicenseCheckTask(), 0, 24 * 60 * 60 * 1000);
    }

    @Deactivate
    public void deactivate() {
        dailyCheck.cancel();
        dailyCheck.purge();
        unregisterApps();
    }

    private void registerApps() {
        if (dataModel.isInstalled()) {
            List<License> licenses = dataModel.mapper(License.class).find();
            for (License license : licenses) {
                Dictionary<String, String> props = new Hashtable<>();
                props.put("com.elster.jupiter.license.application.key", license.getApplicationKey());
                if (license.getStatus().equals(License.Status.ACTIVE) || license.getGracePeriodInDays() > 0) {
                    props.put("com.elster.jupiter.license.rest.key", license.getApplicationKey());
                }

                if (context != null) {
                    licenseServices.add(context.registerService(License.class, license, props));
                }
            }
        }
    }

    private void unregisterApps() {
        for (ServiceRegistration<License> service : licenseServices) {
            service.unregister();
        }

        licenseServices.clear();
    }

    private synchronized void reloadApps() {
        unregisterApps();
        registerApps();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(UserService.class).toInstance(userService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(LicenseService.class).toInstance(LicenseServiceImpl.this);
            }
        };
    }

    @Override
    public List<String> getLicensedApplicationKeys() {
        List<License> licenses = dataModel.mapper(License.class).find();
        List<String> keys = new ArrayList<>(licenses.size());
        for (License license : licenses) {
            keys.add(license.getApplicationKey());
        }
        return keys;
    }

    @Override
    public Optional<License> getLicenseForApplication(String applicationKey) {
        return dataModel.mapper(License.class).getOptional(applicationKey);
    }

    @Override
    public Optional<Properties> getLicensedValuesForApplication(String applicationKey) {
        Optional<License> license = dataModel.mapper(License.class).getOptional(applicationKey);
        if (license.isPresent()) {
            Properties properties = new Properties();
            properties.putAll(license.get().getLicensedValues());
            return Optional.of(properties);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getLicensedValue(String applicationKey, String licensedKey) {
        Optional<Properties> licensedValuesForApplication = getLicensedValuesForApplication(applicationKey);
        if (licensedValuesForApplication.isPresent()) {
            return Optional.ofNullable(licensedValuesForApplication.get().getProperty(licensedKey));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Set<String> addLicense(SignedObject licensedObjects) throws InvalidLicenseException, IOException {
        LicenseVerifier licenseVerifier = new LicenseVerifier();
        Object licensedMap;
        try {
            licensedMap = licenseVerifier.extract(licensedObjects);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IOException | SignatureException | ClassNotFoundException e) {
            throw new InvalidLicenseException(thesaurus, e);
        }
        if (!(licensedMap instanceof Hashtable)) {
            throw new InvalidLicenseException(thesaurus, MessageSeeds.ALREADY_ACTIVE);
        } else {
            Hashtable<String, SignedObject> licensedApps = (Hashtable<String, SignedObject>) licensedMap;
            for (String applicationKey : licensedApps.keySet()) {
                Optional<License> licenseOption = dataModel.mapper(License.class).getOptional(applicationKey);
                if (licenseOption.isPresent()) {
                    LicenseImpl license = (LicenseImpl) licenseOption.get();
                    license.setSignedObject(licensedApps.get(applicationKey));
                    dataModel.update(license, "signedObject");
                    eventService.postEvent(EventType.LICENSE_UPDATED.topic(), license);
                } else {
                    LicenseImpl license = LicenseImpl.from(dataModel, applicationKey, licensedApps.get(applicationKey), thesaurus);
                    dataModel.persist(license);
                    eventService.postEvent(EventType.LICENSE_UPDATED.topic(), license);
                }
            }

            reloadApps();
            return licensedApps.keySet();
        }

    }

    public void clearCache() {
        ormService.invalidateCache(COMPONENTNAME, TableSpecs.LIC_LICENSE.name());
    }

    @Override
    public String getComponentName() {
        return LicenseService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    class LicenseCheckTask extends TimerTask {

        @Override
        public void run() {
            reloadApps();
        }
    }
}