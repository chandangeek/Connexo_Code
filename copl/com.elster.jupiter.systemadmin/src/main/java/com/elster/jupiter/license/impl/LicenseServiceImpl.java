package com.elster.jupiter.license.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.security.Privileges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.Resource;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
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

/**
 * Copyrights EnergyICT
 * Date: 28/03/2014
 * Time: 16:28
 */
@Component(name = "com.elster.jupiter.license", service = {InstallService.class, LicenseService.class, PrivilegesProvider.class},
        property = {"name=" + LicenseService.COMPONENTNAME}, immediate = true)
public class LicenseServiceImpl implements LicenseService, InstallService, PrivilegesProvider {

    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile UserService userService;
    private volatile EventService eventService;

    private BundleContext context;
    private Timer dailyCheck = new Timer("License check");
    List<ServiceRegistration<License>> licenseServices = new CopyOnWriteArrayList<>();

    public LicenseServiceImpl() {
    }

    @Inject
    public LicenseServiceImpl(OrmService ormService, UserService userService, EventService eventService) {
        setOrmService(ormService);
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
        return Arrays.asList("ORM", "USR", "EVT");
    }

    @Override
    public String getModuleName() {
        return LicenseService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(LicenseService.COMPONENTNAME, "license.license", "license.license.description",
                Arrays.asList(
                        Privileges.VIEW_LICENSE, Privileges.UPLOAD_LICENSE)));
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
                Dictionary<String, String> props = new Hashtable<String, String>();
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
                bind(UserService.class).toInstance(userService);
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
        Object licensedMap = null;
        try {
            licensedMap = licenseVerifier.extract(licensedObjects);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IOException | SignatureException | ClassNotFoundException e) {
            throw new InvalidLicenseException(e);
        }
        if (!(licensedMap instanceof Hashtable)) {
            throw InvalidLicenseException.invalidLicense();
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
                    LicenseImpl license = LicenseImpl.from(dataModel, applicationKey, licensedApps.get(applicationKey));
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

    class LicenseCheckTask extends TimerTask {

        @Override
        public void run() {
            reloadApps();
        }
    }
}
