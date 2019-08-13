package com.energyict.dlms.cosem.methods;

import com.energyict.dlms.cosem.DLMSClassId;

public enum CommunicationPortProtectionMethods implements DLMSClassMethods {

    /**
     * Resets the variables of the lockout mechanism:
     * - the number of failed_attempts is reset to 0;
     * - the current_lockout_time is reset to 0;
     * - the protection_status is set to unlocked if the protection_mode is (2) locked_on_failed_attempts, and is not affected otherwise.
     * data ::= integer (0)
     */
    RESET(1, 0x50);

    private final int methodNumber;
    private final int shortAddress;

    CommunicationPortProtectionMethods(int methodNumber, int shortAddress) {
        this.methodNumber = methodNumber;
        this.shortAddress = shortAddress;
    }

    @Override
    public int getMethodNumber() {
        return this.methodNumber;
    }

    @Override
    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.COMMUNICATION_PORT_PROTECTION;
    }

    @Override
    public int getShortName() {
        return this.shortAddress;
    }
}
