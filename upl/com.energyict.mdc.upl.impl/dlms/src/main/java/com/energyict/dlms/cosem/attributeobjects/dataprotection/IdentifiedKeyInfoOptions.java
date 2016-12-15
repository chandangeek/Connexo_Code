package com.energyict.dlms.cosem.attributeobjects.dataprotection;

import com.energyict.dlms.axrdencoding.TypeEnum;

/**
 * Created by cisac on 12/15/2016.
 */
public enum IdentifiedKeyInfoOptions {

    GLOBAL_UNICAST_EK(0),
    GLOBAL_BROADCAST_EK(1);

    private int id;

    IdentifiedKeyInfoOptions(int id) {
        this.id = id;
    }

    public TypeEnum getIdentifiedKeyInfo() {
        return new TypeEnum(id);
    }
}
