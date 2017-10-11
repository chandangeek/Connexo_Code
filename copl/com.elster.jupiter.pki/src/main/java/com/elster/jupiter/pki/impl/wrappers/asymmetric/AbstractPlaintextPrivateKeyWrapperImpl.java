/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.wrappers.asymmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.pki.PrivateKeyWrapper;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Checks;

import com.google.common.collect.ImmutableMap;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;

import javax.validation.constraints.Size;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Implements storage of a PrivateKey in the DataVault.
 */
abstract public class AbstractPlaintextPrivateKeyWrapperImpl implements PlaintextPrivateKeyWrapper {

    protected final DataVaultService dataVaultService;
    protected final PropertySpecService propertySpecService;
    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    private long id;
    @Size(max = Table.MAX_STRING_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String encryptedPrivateKey;
    private Reference<KeyType> keyTypeReference = Reference.empty();
    private Instant expirationTime;

    public enum Fields {
        ENCRYPTED_KEY("encryptedPrivateKey"),
        KEY_TYPE("keyTypeReference"),
        EXPIRATION("expirationTime"),;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    public static final Map<String, Class<? extends PrivateKeyWrapper>> IMPLEMENTERS =
            ImmutableMap.of(
                    "R", PlaintextRsaPrivateKey.class,
                    "D", PlaintextDsaPrivateKey.class,
                    "E", PlaintextEcdsaPrivateKey.class);

    AbstractPlaintextPrivateKeyWrapperImpl(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Thesaurus thesaurus) {
        this.dataVaultService = dataVaultService;
        this.propertySpecService = propertySpecService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    public AbstractPlaintextPrivateKeyWrapperImpl init(KeyType keyType) {
        keyTypeReference.set(keyType);
        return this;
    }

    protected String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public KeyType getKeyType() {
        return keyTypeReference.get();
    }

    @Override
    public Optional<Instant> getExpirationTime() {
        return Optional.ofNullable(expirationTime);
    }

    @Override
    public String getKeyEncryptionMethod() {
        return DataVaultPrivateKeyFactory.KEY_ENCRYPTION_METHOD;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        EnumSet.allOf(Properties.class).forEach(p -> p.copyFromMap(properties, this));
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        EnumSet.allOf(Properties.class).forEach(p -> p.copyToMap(properties, this));
        return properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return EnumSet.allOf(Properties.class)
                .stream().map(properties -> properties.asPropertySpec(propertySpecService)).collect(toList());
    }

    @Override
    public void save() {
        Save.action(id).save(dataModel, this);
    }

    @Override
    public Optional<PrivateKey> getPrivateKey() {
        try {
            if (Checks.is(this.encryptedPrivateKey).emptyOrOnlyWhiteSpace()) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(doGetPrivateKey());
            }
        } catch (InvalidKeyException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY, e);
        } catch (NoSuchAlgorithmException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        } catch (InvalidKeySpecException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY_SPECIFICATION, e);
        } catch (NoSuchProviderException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.UNKNOWN_PROVIDER, e);
        }
    }

    protected abstract PrivateKey doGetPrivateKey() throws InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchProviderException;

    @Override
    public void setPrivateKey(PrivateKey privateKey) {
        this.encryptedPrivateKey = dataVaultService.encrypt(privateKey.getEncoded());
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public PublicKey generateValue() {
        try {
            PublicKey publicKey = doGenerateValue();
            save();
            return publicKey;
        } catch (InvalidKeyException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY, e);
        } catch (NoSuchAlgorithmException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        } catch (InvalidKeySpecException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY_SPECIFICATION, e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_ALGORITHM_PARAMETERS, e);
        } catch (NoSuchProviderException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.UNKNOWN_PROVIDER, e);
        }
    }

    protected abstract PublicKey doGenerateValue() throws
            InvalidKeyException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchProviderException, InvalidAlgorithmParameterException;

    @Override
    public PublicKey getPublicKey() {
        try {
            return doGetPublicKey();
        } catch (NoSuchAlgorithmException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.ALGORITHM_NOT_SUPPORTED, e);
        } catch (InvalidKeySpecException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY_SPECIFICATION, e);
        } catch (NoSuchProviderException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.UNKNOWN_PROVIDER, e);
        }
    }


    protected abstract PublicKey doGetPublicKey() throws
            NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchProviderException;

    public PKCS10CertificationRequest generateCSR(X500Name subjectDN, String signatureAlgorithm) {
        try {
            return doGenerateCSR(subjectDN, signatureAlgorithm);
        } catch (OperatorCreationException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.INVALID_KEY_SPECIFICATION, e);
        } catch (IOException e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.FAILED_TO_GENERATE_CSR, e);
        }
    }

    private PKCS10CertificationRequest doGenerateCSR(X500Name subjectDN, String signatureAlgorithm) throws
            OperatorCreationException, IOException {
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(ASN1Sequence.fromByteArray(getPublicKey().getEncoded()));
        PKCS10CertificationRequestBuilder csrBuilder = new PKCS10CertificationRequestBuilder(subjectDN, subjectPublicKeyInfo);
        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
        if (!getKeyType().getKeyUsages().isEmpty()) {
            extensionsGenerator.addExtension(Extension.keyUsage, true, getKeyType().getKeyUsage());
        }
        if (!getKeyType().getExtendedKeyUsages().isEmpty()) {
            extensionsGenerator.addExtension(Extension.extendedKeyUsage, true, getKeyType().getExtendedKeyUsage());
        }
        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extensionsGenerator.generate());
        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(getPrivateKey().get());
        return csrBuilder.build(contentSigner);
    }


    public enum Properties {
        ENCRYPTED_PRIVATE_KEY("privateKey") {
            public PropertySpec asPropertySpec(PropertySpecService propertySpecService) {
                return propertySpecService.stringSpec()
                        .named(getPropertyName(), "Private key")
                        .describedAs("Plaintext view of private key")
                        .finish();
            }

            @Override
            void copyFromMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyWrapperImpl privateKey) {
                if (properties.containsKey(getPropertyName())) {
                    byte[] decode = DatatypeConverter.parseHexBinary((String) properties.get(getPropertyName()));
                    privateKey.encryptedPrivateKey = privateKey.dataVaultService.encrypt(decode);
                }
            }

            @Override
            void copyToMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyWrapperImpl privateKey) {
                byte[] decrypt = privateKey.dataVaultService.decrypt(privateKey.encryptedPrivateKey);
                properties.put(getPropertyName(), DatatypeConverter.printHexBinary(decrypt));
            }
        },;

        private final String propertyName;

        Properties(String propertyName) {
            this.propertyName = propertyName;
        }

        abstract PropertySpec asPropertySpec(PropertySpecService propertySpecService);

        abstract void copyFromMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyWrapperImpl privateKey);

        abstract void copyToMap(Map<String, Object> properties, AbstractPlaintextPrivateKeyWrapperImpl privateKey);

        String getPropertyName() {
            return propertyName;
        }
    }

}
