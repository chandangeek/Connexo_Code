package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.HsmNotConfiguredException;
import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.hsm.model.keys.HsmKeyType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.SecretFactory;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.impl.MessageSeeds;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HsmReversibleKey extends HsmKeyImpl {

    private final HsmEncryptionService hsmEncryptionService;

    @Inject
    HsmReversibleKey(PropertySpecService propertySpecService, DataModel dataModel, Clock clock, Thesaurus thesaurus, HsmEnergyService hsmEnergyService, HsmEncryptionService hsmEncryptionService) {
        super(propertySpecService, dataModel, clock, thesaurus, hsmEnergyService);
        this.hsmEncryptionService = hsmEncryptionService;
    }

    /**
     *
     * @return label in plain text, decrypted using HSM and label stored
     */
    @Override
    public byte[] getKey() {
        try {
            if (super.getKey() == null) {
                return null;
            }
            return hsmEncryptionService.symmetricDecrypt(super.getKey(), super.getLabel());
        } catch (HsmBaseException|HsmNotConfiguredException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void generateValue(SecurityAccessorType securityAccessorType, Optional<HsmKey> masterKey) {
        try {
            HsmKeyType hsmKeyType = securityAccessorType.getHsmKeyType();

            byte[] newKey;

            SecretFactory passwordGenerator = new SecretFactory();

            if (HsmJssKeyType.AUTHENTICATION.equals(hsmKeyType.getHsmJssKeyType())){
                // here the size is zero, but we'll leave it, maybe some good samaritan will fix in gui
                String randomPassword =  passwordGenerator.generatePassword(hsmKeyType.getKeySize());
                newKey = randomPassword.getBytes();
            } else {
                // for HLS-Secrets and AES keys generate random bytes
                newKey = passwordGenerator.generateHexByteArray(hsmKeyType.getKeySize());
            }

            setKey(newKey, super.getLabel());
            save();
        } catch (Exception e) {
            throw new PkiLocalizedException(thesaurus, MessageSeeds.CANNOT_RENEW_REVERSIBLE_KEY, e);
        }
    }

    /**
     *
     * @param key in plain text while it will be encrypted using label received
     * @param label
     */
    @Override
    public void setKey(byte[] key, String label) {
        try {
            super.validateSetKey(key, label);
            byte[] encryptedValue = this.hsmEncryptionService.symmetricEncrypt(key, label);
            super.setKey(encryptedValue, label);
            // we don't actually need setSmartMeterKey, just that the Com-Server engine will try to process it,
            // so to not have crashes in various places. better set it with the encrypted value here
            super.setSmartMeterKey(encryptedValue);
        } catch (HsmBaseException|HsmNotConfiguredException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        HsmReversibleKeyPropertyValidator hsmPropertyValidator = HsmReversibleKeyPropertyValidator.build(properties);
        hsmPropertyValidator.validate(getDataModel());
        this.setKey(hsmPropertyValidator.getKey(super.getHsmJssKeyType()), hsmPropertyValidator.getLabel());
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        byte[] key = getKey();
        if (key != null){
            properties.put(HsmProperties.DECRYPTED_KEY.getPropertyName(), getDisplayValue(key));
        }
        properties.put(HsmProperties.LABEL.getPropertyName(), getLabel());
        return properties;
    }

    private String getDisplayValue(byte[] key) {

        HsmJssKeyType hsmJssKeyType = super.getHsmJssKeyType();
        switch (hsmJssKeyType) {
            case AUTHENTICATION: return new String(key);
        }
        return DatatypeConverter.printHexBinary(key);
    }


}
