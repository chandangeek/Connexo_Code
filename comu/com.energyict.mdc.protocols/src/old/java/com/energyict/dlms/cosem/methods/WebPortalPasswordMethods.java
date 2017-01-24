package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

/**
 * @author khe
 */
public enum WebPortalPasswordMethods implements DLMSClassMethods {

    CHANGE_USER_1_PASSWORD(1, 80),
    CHANGE_USER_2_PASSWORD(2, 88);

    /**
     * The method number.
     */
    private final int methodNumber;

    /**
     * The short address.
     */
    private final int shortAddress;

    /**
     * Create a new instance.
     *
     * @param methodNumber The method number.
     * @param shortAddress The short address.
     */
    private WebPortalPasswordMethods(final int methodNumber, final int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.WEB_PORTAL_PASSWORDS;
    }

    /**
     * {@inheritDoc}
     */
    public final int getShortName() {
        return this.shortAddress;
    }

    /**
     * {@inheritDoc}
     */
    public final int getMethodNumber() {
        return this.methodNumber;
    }
}