package com.elster.jupiter.license.impl;

import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.License;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;

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
import java.util.Date;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 2/04/2014
 * Time: 11:17
 */
public class LicenseImpl implements License {
    private static final String LICENSE_APP_KEY = "license.application.key";
    private static final String LICENSE_CREATION_DATE_KEY = "license.creation.date";
    private static final String LICENSE_EXPIRATION_DATE_KEY = "license.expiration.date";
    private static final String LICENSE_DESCRIPTION_KEY = "license.description";
    private static final String LICENSE_GRACE_PERIOD_KEY = "license.grace.period";
    private static final String LICENSE_TYPE_KEY = "license.type";

    @NotNull
    @Size(min = 1, max = 3)
    private String appKey;
    private byte[] signedObject;
    private transient Properties properties;
    private String info;
    @SuppressWarnings("unused")
    private UtcInstant createTime;
    @SuppressWarnings("unused")
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;

    private SignedObject getSignedObject() throws IOException, ClassNotFoundException {
        try (InputStream baseStream = new ByteArrayInputStream(signedObject)) {
            ObjectInputStream stream = new ObjectInputStream(baseStream);
            return (SignedObject) stream.readObject();
        }
    }

    void setSignedObject(SignedObject signedObject) throws IOException {
        if (signedObject == null) {
            throw InvalidLicenseException.invalidLicense();
        }
        if (this.signedObject != null) {
            try {
                Properties newProperties = extractProperties(signedObject);
                if (!appKey.equals(newProperties.getProperty(LICENSE_APP_KEY))) {
                    throw InvalidLicenseException.licenseForOtherApp();
                }
                if (getUtcInstant(LICENSE_CREATION_DATE_KEY, newProperties).before(getUtcInstant(LICENSE_CREATION_DATE_KEY, getProperties()))) {
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

    private Properties getProperties() {
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

    static LicenseImpl from(DataModel dataModel, String applicationKey, SignedObject signedObject) {
        try {
            return dataModel.getInstance(LicenseImpl.class).init(applicationKey, signedObject);
        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException | SignatureException | InvalidKeyException | InvalidKeySpecException e) {
            throw new InvalidLicenseException(e);
        }
    }

    private LicenseImpl init(String applicationKey, SignedObject signedObject) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        appKey = applicationKey;
        new LicenseVerifier().extract(signedObject);
        setSignedObject(signedObject);
        return this;
    }


    @Override
    public String getApplicationKey() {
        return appKey;
    }

    @Override
    public Status getStatus() {
        return getExpiration().before(new Date()) ? Status.EXPIRED : Status.ACTIVE;
    }

    @Override
    public String getDescription() {
        return getProperties().getProperty(LICENSE_DESCRIPTION_KEY);
    }

    @Override
    public UtcInstant getExpiration() {
        return getUtcInstant(LICENSE_EXPIRATION_DATE_KEY, getProperties());
    }

    @Override
    public UtcInstant getActivation() {
        return modTime;
    }

    @Override
    public int getGracePeriodInDays() {
        int gracePeriod = getInt(LICENSE_GRACE_PERIOD_KEY, getProperties());
        if (Status.EXPIRED.equals(getStatus())) {
            DateTime endOfGracePeriod = new DateTime(getExpiration().getTime(), DateTimeZone.UTC).plusDays(gracePeriod);
            gracePeriod = Math.max(0, Days.daysBetween(LocalDate.now(), endOfGracePeriod.toLocalDate()).getDays());
        }
        return gracePeriod;
    }

    @Override
    public Type getType() {
        return "full".equals(getProperties().getProperty(LICENSE_TYPE_KEY, "evaluation")) ? Type.FULL : Type.EVALUATION;
    }

    @Override
    public Properties getLicensedValues() {
        Properties props = new Properties();
        if (!Status.EXPIRED.equals(getStatus()) || (0 != getGracePeriodInDays())) {
            props.putAll(getProperties());
        }
        return filterStandardProperties(props);
    }

    private Properties filterStandardProperties(Properties props) {
        String[] defaultKeys = new String[]{LICENSE_CREATION_DATE_KEY, LICENSE_DESCRIPTION_KEY,
                LICENSE_EXPIRATION_DATE_KEY, LICENSE_APP_KEY, LICENSE_GRACE_PERIOD_KEY, LICENSE_TYPE_KEY};
        for (String defaultKey : defaultKeys) {
            props.remove(defaultKey);
        }
        return props;
    }

    private UtcInstant getUtcInstant(String key, Properties properties) {
        try {
            long creationTimeStamp = Long.parseLong(properties.getProperty(key, "not present"));
            return new UtcInstant(creationTimeStamp);
        } catch (NumberFormatException e) {
            throw new InvalidLicenseException(e);
        }
    }

    private int getInt(String key, Properties properties) {
        try {
            return Integer.parseInt(properties.getProperty(key, "not present"));
        } catch (NumberFormatException e) {
            throw new InvalidLicenseException(e);
        }
    }
}
