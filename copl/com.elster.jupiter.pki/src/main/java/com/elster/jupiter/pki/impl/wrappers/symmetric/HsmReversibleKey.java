package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;
import javax.xml.bind.DatatypeConverter;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

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
            return hsmEncryptionService.symmetricDecrypt(super.getKey(), super.getLabel());
        } catch (HsmBaseException e) {
            throw new RuntimeException(e);
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
            super.setKey(this.hsmEncryptionService.symmetricEncrypt(key, label), label);
        } catch (HsmBaseException e) {
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
