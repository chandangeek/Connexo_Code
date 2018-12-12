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

    NO_REQUIRED_PROTECTION(0),
    AUTHENTICATED_REQUEST(4),
    ENCRYPTED_REQUEST(8),
    AUTHENTICATED_AND_ENCRYPTED_REQUEST(12),
    DIGITALLY_SIGNED_REQUEST(16),
    AUTHENTICATED_RESPONSE(32),
    AUTHENTICATED_REQUEST_AND_RESPONSE(36),
    ENCRYPTED_RESPONSE(64),
    ENCRYPTED_REQUEST_AND_RESPONSE(72),
    AUTHENTICATED_AND_ENCRYPTED_RESPONSE(96),
    AUTHENTICATED_AND_ENCRYPTED_REQUEST_AND_RESPONSE(108),
    DIGITALLY_SIGNED_RESPONSE(128),
    DIGITALLY_SIGNED_REQUEST_AND_RESPONSE(144);

    private int id;

    private RequiredProtection(int id) {
        this.id = id;
    }

    public final int getId() {
        return id;
    }

    public TypeEnum getTypeEnum() {
        return new TypeEnum(id);
    }

}
