package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.messages.CreditDeviceMessage;

// Support CreditTypeLogic for AcudMessageExecutor and AcudRegisterFactory
public class AcudCreditUtils {

    private static final ObisCode IMPORT_CREDIT = ObisCode.fromString("0.0.19.10.0.255");
    private static final ObisCode EMERGENCY_CREDIT = ObisCode.fromString("0.0.19.10.1.255");

    // return ObisCode of associated credit type;
    static public ObisCode getCreditTypeObiscode(CreditDeviceMessage.CreditType credit_t) {
        switch (credit_t) {
            case Import_credit:
                return IMPORT_CREDIT;
            case Emergency_credit:
                return EMERGENCY_CREDIT;
            default:
                throw new RuntimeException("Can't eval credit type by description: " + credit_t.getDescription());
        }
    }
};
