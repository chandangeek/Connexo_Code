package com.energyict.dlms.cosem.attributeobjects.dataprotection;

import com.energyict.dlms.axrdencoding.TypeEnum;

/**
 * Created by cisac on 12/15/2016.
 *
 * When the enum value is interpreted as an unsigned, the meaning of each bit is as shown below:
     Bit Required protection
     0 unused, shall be set to 0,
     1 unused, shall be set to 0,
     2 authenticated request,
     3 encrypted request,
     4 digitally signed request,
     5 authenticated response,
     6 encrypted response,
     7 digitally signed response
 */
public enum RequiredProtection {

    AUTHENTICATED_REQUEST(4),
    ENCRYPTED_REQUEST(8),
    DIGITALLY_SIGNED_REQUEST(16),
    AUTHENTICATED_RESPONSE(32),
    ENCRYPTED_RESPONSE(64),
    DIGITALLY_SIGNED_RESPONSE(128);

    private int id;

    RequiredProtection(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public TypeEnum getTypeEnum() {
        return new TypeEnum(id);
    }

}
