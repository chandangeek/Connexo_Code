package com.elster.jupiter.license.impl;

import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 2/04/2014
 * Time: 11:17
 */
public class License {
    @NotNull
    @Size(min = 1, max = 3)
    private String appKey;
    private byte[] signedObject;
    private transient Properties properties;
    @SuppressWarnings("unused")
    private UtcInstant createTime;
    @SuppressWarnings("unused")
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;

    String getApplicationName() {
        return appKey;
    }

    private SignedObject getSignedObject() throws IOException, ClassNotFoundException {
        try (InputStream baseStream = new ByteArrayInputStream(signedObject)) {
            ObjectInputStream stream = new ObjectInputStream(baseStream);
            return (SignedObject) stream.readObject();
        }
    }

    void setSignedObject(SignedObject signedObject) throws IOException {
        if (signedObject == null) {
            throw new InvalidLicenseException();
        }
        if (this.signedObject != null) {
            try {
                Properties newProperties = extractProperties(signedObject);
                if (getUtcInstant(LicenseService.LICENSE_CREATION_DATE_KEY, newProperties).before(getUtcInstant(LicenseService.LICENSE_CREATION_DATE_KEY, getProperties()))) {
                    throw InvalidLicenseException.newerLicenseAlreadyExists();
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException | ClassNotFoundException e) {
                throw new InvalidLicenseException(e);
            }
        }

        try (ByteArrayOutputStream baseStream = new ByteArrayOutputStream()) {
            ObjectOutputStream stream = new ObjectOutputStream(baseStream);
            stream.writeObject(signedObject);
            stream.flush();
            byte[] bytes = baseStream.toByteArray();
            if (Arrays.equals(bytes, this.signedObject)) {
                throw InvalidLicenseException.licenseAlreadyActive();
            }
            this.signedObject = bytes;
            this.properties = null;
        }
    }

    private UtcInstant getUtcInstant(String key, Properties newProperties) {
        try {
            long creationTimeStamp = Long.parseLong(newProperties.getProperty(key, "not present"));
            return new UtcInstant(creationTimeStamp);
        } catch (NumberFormatException e) {
            throw new InvalidLicenseException(e);
        }

    }

    Properties getProperties() {
        if (properties == null) {
            try {
                properties = extractProperties(getSignedObject());
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IOException | SignatureException | ClassNotFoundException e) {
                throw new InvalidLicenseException(e);
            }
        }
        return properties;
    }

    private Properties extractProperties(SignedObject object) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IOException, SignatureException, ClassNotFoundException {
        return (Properties) new LicenseVerifier().extract(object);
    }

    static License from(DataModel dataModel, String applicationKey, SignedObject signedObject) {
        try {
            return dataModel.getInstance(License.class).init(applicationKey, signedObject);
        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException | SignatureException | InvalidKeyException | InvalidKeySpecException e) {
            throw new InvalidLicenseException(e);
        }
    }

    private License init(String applicationKey, SignedObject signedObject) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        appKey = applicationKey;
        new LicenseVerifier().extract(signedObject);
        setSignedObject(signedObject);
        return this;
    }


}
