package com.elster.jupiter.pki.impl.wrappers.symmetric;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpecService;

import java.time.Clock;

public class HsmReversibleKey extends HsmKeyImpl {

    HsmReversibleKey(DataVaultService dataVaultService, PropertySpecService propertySpecService, DataModel dataModel, Clock clock, Thesaurus thesaurus, HsmEnergyService hsmEnergyService, HsmEncryptionService hsmEncryptionService) {
        super(dataVaultService, propertySpecService, dataModel, clock, thesaurus, hsmEnergyService, hsmEncryptionService);
    }

    /**
     *
     * @return label in plain text, decrypted using HSM and label stored
     */
    @Override
    public byte[] getKey() {
        try {
            return hsmEncryptionService.decrypt(super.getKey(), super.getLabel());
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
            super.setKey(this.hsmEncryptionService.encrypt(key, label), label);
        } catch (HsmBaseException e) {
            throw new RuntimeException(e);
        }
    }


}
