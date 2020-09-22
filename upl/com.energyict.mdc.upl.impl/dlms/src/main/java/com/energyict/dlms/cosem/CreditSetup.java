package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.CreditSetupAttributes;
import com.energyict.dlms.cosem.methods.CreditSetupMethods;

import java.io.IOException;

/**
 * Created by H245796 on 18.12.2017.
 */
public class CreditSetup extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public CreditSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.CREDIT_SETUP.getClassId();
    }

    public AbstractDataType readLogicalName() throws IOException {
        return readDataType(CreditSetupAttributes.LOGICAL_NAME, OctetString.class);
    }

    public AbstractDataType readCurrentCreditAmount() throws IOException {
        return readDataType(CreditSetupAttributes.CURRENT_CREDIT_AMOUNT, Integer32.class);
    }

    public AbstractDataType readCreditType() throws IOException {
        return readDataType(CreditSetupAttributes.CREDIT_TYPE, TypeEnum.class);
    }

    public AbstractDataType readPriority() throws IOException {
        return readDataType(CreditSetupAttributes.PRIORITY, Unsigned8.class);
    }

    public AbstractDataType readWarningTreshold() throws IOException {
        return readDataType(CreditSetupAttributes.WARNING_THRESHOLD, Structure.class);
    }

    public AbstractDataType readLimit() throws IOException {
        return readDataType(CreditSetupAttributes.LIMIT, Structure.class);
    }

    public AbstractDataType readCreditConfiguration() throws IOException {
        return readDataType(CreditSetupAttributes.CREDIT_CONFIGURATION, OctetString.class);
    }

    public AbstractDataType readCreditStatus() throws IOException {
        return readDataType(CreditSetupAttributes.CREDIT_STATUS, Unsigned64.class);
    }

    public AbstractDataType readPresetCreditAmount() throws IOException {
        return readDataType(CreditSetupAttributes.PRESET_CREDIT_AMOUNT, BitString.class);
    }

    public AbstractDataType readCreditAvailableThreshold() throws IOException {
        return readDataType(CreditSetupAttributes.CREDIT_AVAILABLE_THRESHOLD, DateTime.class);
    }

    public AbstractDataType readPeriod() throws IOException {
        return readDataType(CreditSetupAttributes.PERIOD, Integer32.class);
    }

    public void writeCreditAttribute(CreditSetupAttributes attribute, AbstractDataType data) throws IOException {
        write(attribute, data);
    }

    public void invokeCreditMethod(CreditSetupMethods chargeSetupMethod) throws IOException {
        methodInvoke(chargeSetupMethod);
    }

    public void invokeCreditMethod(CreditSetupMethods chargeSetupMethod, AbstractDataType data) throws IOException {
        methodInvoke(chargeSetupMethod, data);
    }

}