package com.elster.jupiter.pki.impl;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.ExtendedKeyUsage;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeyUsage;

import org.bouncycastle.asn1.x509.KeyPurposeId;

import javax.inject.Inject;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import java.util.EnumSet;

// TODO split up in sub classes...
@ValidatePassphraseType(groups = {Save.Create.class, Save.Update.class})
public class KeyTypeImpl implements KeyType {
    private final DataModel dataModel;

    private long id;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @Size(max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;
    private CryptographicType cryptographicType;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String keyAlgorithm;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String signatureAlgorithm;
    private Integer keySize;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String curve;
    private long keyUsages;
    private long extendedKeyUsages;
    @Max(value = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private Integer passwordLength;
    private Boolean useLowerCaseCharacters;
    private Boolean useUpperCaseCharacters;
    private Boolean useNumbers;
    private Boolean useSpecialCharacters;

    enum Fields {
        NAME("name"),
        CRYPTOGRAPHIC_TYPE("cryptographicType"),
        KEY_ALGORITHM("keyAlgorithm"),
        SIGNATURE_ALGORITHM("signatureAlgorithm"),
        KEY_SIZE("keySize"),
        DESCRIPTION("description"),
        CURVE("curve"),
        KEY_USAGES("keyUsages"),
        EXTENDED_KEY_USAGES("extendedKeyUsages"),
        ID("id"),
        LENGTH("passwordLength"),
        LOWERCASE("useLowerCaseCharacters"),
        UPPERCASE("useUpperCaseCharacters"),
        NUMBERS("useNumbers"),
        SPECIAL_CHARS("useSpecialCharacters")
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public KeyTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public CryptographicType getCryptographicType() {
        return cryptographicType;
    }

    public void setCryptographicType(CryptographicType cryptographicType) {
        this.cryptographicType = cryptographicType;
    }

    @Override
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String algorithm) {
        this.keyAlgorithm = algorithm;
    }

    @Override
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    @Override
    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    public void setKeyUsages(EnumSet<KeyUsage> keyUsages) {
        this.keyUsages = keyUsages2Long(keyUsages);
    }

    @Override
    public EnumSet<KeyUsage> getKeyUsages() {
        return long2keyUsages(this.keyUsages);
    }

    @Override
    public org.bouncycastle.asn1.x509.KeyUsage getKeyUsage() {
        return new org.bouncycastle.asn1.x509.KeyUsage(getKeyUsages().stream().mapToInt(usage->usage.bouncyCastleBitPosition).sum());
    }

    public void setExtendedKeyUsages(EnumSet<ExtendedKeyUsage> extendedKeyUsages) {
        this.extendedKeyUsages = extendedKeyUsages2Long(extendedKeyUsages);
    }

    @Override
    public EnumSet<ExtendedKeyUsage> getExtendedKeyUsages() {
        return long2extendedKeyUsages(this.extendedKeyUsages);
    }

    @Override
    public org.bouncycastle.asn1.x509.ExtendedKeyUsage getExtendedKeyUsage() {
        KeyPurposeId[] keyPurposeIds = getExtendedKeyUsages().stream().map(extendedKeyUsage -> extendedKeyUsage.keyPurposeId).toArray(KeyPurposeId[]::new);
        return new org.bouncycastle.asn1.x509.ExtendedKeyUsage(keyPurposeIds);
    }

    @Override
    public String getCurve() {
        return curve;
    }

    @Override
    public Integer getPasswordLength() {
        return passwordLength;
    }

    @Override
    public Boolean useLowerCaseCharacters() {
        return useLowerCaseCharacters;
    }

    @Override
    public Boolean useUpperCaseCharacters() {
        return useUpperCaseCharacters;
    }

    @Override
    public Boolean useNumbers() {
        return useNumbers;
    }

    @Override
    public Boolean useSpecialCharacters() {
        return useSpecialCharacters;
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
    }

    public void setUseLowerCaseCharacters(boolean useLowerCaseCharacters) {
        this.useLowerCaseCharacters = useLowerCaseCharacters;
    }

    public void setUseUpperCaseCharacters(boolean useUpperCaseCharacters) {
        this.useUpperCaseCharacters = useUpperCaseCharacters;
    }

    public void setUseNumbers(boolean useNumbers) {
        this.useNumbers = useNumbers;
    }

    public void setUseSpecialCharacters(boolean useSpecialCharacters) {
        this.useSpecialCharacters = useSpecialCharacters;
    }

    public void setCurve(String curve) {
        this.curve = curve;
    }

    public void save() {
        Save.action(id).save(dataModel, this);
    }

    private long keyUsages2Long(EnumSet<KeyUsage> keyUsages) {
        return keyUsages.stream().mapToInt(usage -> 1 << usage.bitPosition).sum();
    }

    private long extendedKeyUsages2Long(EnumSet<ExtendedKeyUsage> extendedKeyUsages) {
        return extendedKeyUsages.stream().mapToInt(usage -> 1 << usage.bitPosition).sum();
    }

    private EnumSet<KeyUsage> long2keyUsages(long bits) {
        EnumSet<KeyUsage> usages = EnumSet.noneOf(KeyUsage.class);
        for (int bitPosition=0; bitPosition<64; bitPosition++) {
            if ((bits & 1L) == 1) {
                KeyUsage.byBitPosition(bitPosition).ifPresent(usages::add);
            }
            bits = bits >>> 1;
        }
        return usages;
    }

    private EnumSet<ExtendedKeyUsage> long2extendedKeyUsages(long bits) {
        EnumSet<ExtendedKeyUsage> usages = EnumSet.noneOf(ExtendedKeyUsage.class);
        for (int bitPosition=0; bitPosition<64; bitPosition++) {
            if ((bits & 1L) == 1) {
                ExtendedKeyUsage.byBitPosition(bitPosition).ifPresent(usages::add);
            }
            bits = bits >>> 1;
        }
        return usages;
    }

}
