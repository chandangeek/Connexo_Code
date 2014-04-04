package com.elster.jupiter.license.impl;

import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.common.base.Optional;
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

    public LicenseServiceImpl() {
    }

    @Inject
    public LicenseServiceImpl(OrmService ormService) {
        setOrmService(ormService);
        activate();
        if (!dataModel.isInstalled()) {
            install();
        }
    }

    public void install() {
        dataModel.install(true, true);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel(COMPONENTNAME, "License management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
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
            }
        };
    }

    @Override
    public List<String> getLicensedApplicationKeys() {
        List<License> licenses = dataModel.mapper(License.class).find();
        List<String> keys = new ArrayList<>(licenses.size());
        for (License license : licenses) {
            keys.add(license.getApplicationName());
        }
        return keys;
    }

    @Override
    public Optional<Properties> getLicensedValuesForApplication(String applicationKey) {
        Optional<License> license = dataModel.mapper(License.class).getOptional(applicationKey);
        if (license.isPresent()) {
            Properties properties = new Properties();
            properties.putAll(license.get().getProperties());
            return Optional.of(properties);
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<String> getLicensedValue(String applicationKey, String licensedKey) {
        Optional<Properties> licensedValuesForApplication = getLicensedValuesForApplication(applicationKey);
        if (licensedValuesForApplication.isPresent()) {
            return Optional.fromNullable(licensedValuesForApplication.get().getProperty(licensedKey));
        } else {
            return Optional.absent();
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
            throw new InvalidLicenseException();
        } else {
            Hashtable<String, SignedObject> licensedApps = (Hashtable<String, SignedObject>) licensedMap;
            for (String applicationKey : licensedApps.keySet()) {
                Optional<License> licenseOption = dataModel.mapper(License.class).getOptional(applicationKey);
                if (licenseOption.isPresent()) {
                    License license = licenseOption.get();
                    license.setSignedObject(licensedApps.get(applicationKey));
                    dataModel.update(license, "signedObject");
                } else {
                    License license = License.from(dataModel, applicationKey, licensedApps.get(applicationKey));
                    dataModel.persist(license);
                }
            }
            return licensedApps.keySet();
        }

    }

    public void clearCache() {
        ormService.invalidateCache(COMPONENTNAME, TableSpecs.LIC_LICENSE.name());
    }
}
