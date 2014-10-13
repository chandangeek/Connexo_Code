package com.elster.jupiter.license.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.security.Privileges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import java.util.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 28/03/2014
 * Time: 16:28
 */
@Component(name = "com.elster.jupiter.license", service = {InstallService.class, LicenseService.class},
        property = {"name=" + LicenseService.COMPONENTNAME}, immediate = true)
public class LicenseServiceImpl implements LicenseService, InstallService {

    private volatile DataModel dataModel;
    private volatile OrmService ormService;
    private volatile UserService userService;
    private volatile EventService eventService;

    public LicenseServiceImpl() {
    }

    @Inject
    public LicenseServiceImpl(OrmService ormService, UserService userService, EventService eventService) {
        setOrmService(ormService);
        setUserService(userService);
        setEventService(eventService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    public void install() {
        dataModel.install(true, true);
        createPrivileges();
        assignPrivilegesToDefaultRoles();
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

    private void createPrivileges() {
        this.userService.createResourceWithPrivileges("SYS", "license.license", "license.license.description", new String[]{Privileges.VIEW_LICENSE, Privileges.UPLOAD_LICENSE});
    }

    private void assignPrivilegesToDefaultRoles() {
        this.userService.grantGroupWithPrivilege(UserService.DEFAULT_ADMIN_ROLE, new String[]{Privileges.UPLOAD_LICENSE, Privileges.VIEW_LICENSE});
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
    public void activate() {
        dataModel.register(getModule());
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(UserService.class).toInstance(userService);
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
            return licensedApps.keySet();
        }

    }

    public void clearCache() {
        ormService.invalidateCache(COMPONENTNAME, TableSpecs.LIC_LICENSE.name());
    }
}
