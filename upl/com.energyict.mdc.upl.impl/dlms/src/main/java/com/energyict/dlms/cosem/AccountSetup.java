package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.AccountSetupAttributes;
import com.energyict.dlms.cosem.methods.CreditSetupMethods;

import java.io.IOException;

/**
 * Created by H245796 on 18.12.2017.
 */
public class AccountSetup extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public AccountSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.ACCOUNT_SETUP.getClassId();
    }

    public AbstractDataType readLogicalName() throws IOException {
        return readDataType(AccountSetupAttributes.LOGICAL_NAME, OctetString.class);
    }

    public Structure readModeAndStatus() throws IOException {
        return readDataType(AccountSetupAttributes.MODE_AND_STATUS, Structure.class);
    }

    public AbstractDataType readCurrentCreditInUse() throws IOException {
        return readDataType(AccountSetupAttributes.CURRENT_CREDIT_IN_USE, Unsigned8.class);
    }

    public AbstractDataType readCurrentCreditStatus() throws IOException {
        return readDataType(AccountSetupAttributes.CURRENT_CREDIT_STATUS, BitString.class);
    }

    public AbstractDataType readAvailableCredit() throws IOException {
        return readDataType(AccountSetupAttributes.AVAILABLE_CREDIT, Integer64.class);
    }

    public AbstractDataType readAmountToClear() throws IOException {
        return readDataType(AccountSetupAttributes.AMOUNT_TO_CLEAR, Integer64.class);
    }

    public AbstractDataType readCreditReferenceList() throws IOException {
        return readDataType(AccountSetupAttributes.CREDIT_REFERENCE_LIST, Array.class);
    }

    public AbstractDataType readCurrency() throws IOException {
        return readDataType(AccountSetupAttributes.CURRENCY, Structure.class);
    }

    public AbstractDataType readNextCreditAvailableThreshold() throws IOException {
        return readDataType(AccountSetupAttributes.NEXT_CREDIT_AVAILABLE_THRESHOLD, Integer64.class);
    }

    public TypeEnum readPaymentMode() throws IOException {
        return readModeAndStatus().getDataType(0).getTypeEnum();
    }

    public TypeEnum readAccountStatus() throws IOException {
        return readModeAndStatus().getDataType(1).getTypeEnum();
    }
}