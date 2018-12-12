/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.license.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Properties;

public class LicenseImpl implements License {
    private static final String LICENSE_APP_KEY = "license.application.key";
    private static final String LICENSE_CREATION_DATE_KEY = "license.creation.date";
    private static final String LICENSE_EXPIRATION_DATE_KEY = "license.expiration.date";
    private static final String LICENSE_DESCRIPTION_KEY = "license.description";
    private static final String LICENSE_GRACE_PERIOD_KEY = "license.grace.period";
    private static final String LICENSE_TYPE_KEY = "license.type";

    private final Thesaurus thesaurus;
    private final Clock clock;

    @Inject
    public LicenseImpl(Thesaurus thesaurus, Clock clock) {
        super();
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    @NotNull
    @Size(min = 1, max = 3)
    private String appKey;
    private byte[] signedObject;
    private transient Properties properties;
    private String info;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
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
            throw new InvalidLicenseException(thesaurus, MessageSeeds.INVALID_LICENSE);
        }
        if (this.signedObject != null) {
            try {
                Properties newProperties = extractProperties(signedObject);
                if (!appKey.equals(newProperties.getProperty(LICENSE_APP_KEY))) {
                    throw new InvalidLicenseException(thesaurus, MessageSeeds.LICENSE_FOR_OTHER_APP);
                }
                if (getInstant(LICENSE_CREATION_DATE_KEY, newProperties).isBefore(getInstant(LICENSE_CREATION_DATE_KEY, getProperties()))) {
                    throw new InvalidLicenseException(thesaurus, MessageSeeds.NEWER_LICENSE_EXISTS);
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException | ClassNotFoundException e) {
                throw new InvalidLicenseException(thesaurus, e);
            }
        }

        try (ByteArrayOutputStream baseStream = new ByteArrayOutputStream()) {
            ObjectOutputStream stream = new ObjectOutputStream(baseStream);
            stream.writeObject(signedObject);
            stream.flush();
            byte[] bytes = baseStream.toByteArray();
            if (Arrays.equals(bytes, this.signedObject)) {
                throw new InvalidLicenseException(thesaurus, MessageSeeds.NEWER_LICENSE_EXISTS);
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
                throw new InvalidLicenseException(thesaurus, e);
            }
        }
        return properties;
    }

    private Properties extractProperties(SignedObject object) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IOException, SignatureException, ClassNotFoundException {
        return (Properties) new LicenseVerifier().extract(object);
    }

    static LicenseImpl from(DataModel dataModel, String applicationKey, SignedObject signedObject, Thesaurus thesaurus) {
        try {
            return dataModel.getInstance(LicenseImpl.class).init(applicationKey, signedObject);
        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException | SignatureException | InvalidKeyException | InvalidKeySpecException e) {
            throw new InvalidLicenseException(thesaurus, e);
        }
    }

    private LicenseImpl init(String applicationKey, SignedObject signedObject) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        appKey = applicationKey;
        new LicenseVerifier().extract(signedObject);
        setSignedObject(signedObject);
        return this;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String getApplicationKey() {
        return getProperties().getProperty(LICENSE_APP_KEY);
    }

    @Override
    public Status getStatus() {
        return getExpiration().isBefore(Instant.now(clock)) ? Status.EXPIRED : Status.ACTIVE;
    }

    @Override
    public String getDescription() {
        return getProperties().getProperty(LICENSE_DESCRIPTION_KEY);
    }

    @Override
    public Instant getExpiration() {
        return getInstant(LICENSE_EXPIRATION_DATE_KEY, getProperties());
    }

    @Override
    public Instant getActivation() {
        return modTime;
    }

    @Override
    public int getGracePeriodInDays() {
        int gracePeriod = getInt(LICENSE_GRACE_PERIOD_KEY, getProperties());
        if (Status.EXPIRED.equals(getStatus())) {
            LocalDate expirationDay = getExpiration().atOffset(ZoneOffset.UTC).toLocalDate();
            LocalDate endOfGracePeriod = expirationDay.plusDays(gracePeriod);
            gracePeriod = (int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), endOfGracePeriod));
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

    private Instant getInstant(String key, Properties properties) {
        try {
            long creationTimeStamp = Long.parseLong(properties.getProperty(key, "not present"));
            return Instant.ofEpochMilli(creationTimeStamp);
        } catch (NumberFormatException e) {
            throw new InvalidLicenseException(thesaurus, e);
        }
    }

    private int getInt(String key, Properties properties) {
        try {
            return Integer.parseInt(properties.getProperty(key, "not present"));
        } catch (NumberFormatException e) {
            throw new InvalidLicenseException(thesaurus, e);
        }
    }
}