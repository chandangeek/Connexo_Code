package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.ChargeSetupAttributes;
import com.energyict.dlms.cosem.methods.ChargeSetupMethods;

import java.io.IOException;

/**
 * Created by H245796 on 18.12.2017.
 */
public class ChargeSetup extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public ChargeSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.CHARGE_SETUP.getClassId();
    }

    public AbstractDataType readLogicalName() throws IOException {
        return readDataType(ChargeSetupAttributes.LOGICAL_NAME, OctetString.class);
    }

    public AbstractDataType readTotalAmountPaid() throws IOException {
        return readDataType(ChargeSetupAttributes.TOTAL_AMOUNT_PAID, Integer32.class);
    }

    public AbstractDataType readChargeType() throws IOException {
        return readDataType(ChargeSetupAttributes.CHARGE_TYPE, TypeEnum.class);
    }

    public AbstractDataType readPriority() throws IOException {
        return readDataType(ChargeSetupAttributes.PRIORITY, Unsigned8.class);
    }

    public AbstractDataType readUnitChargeActive() throws IOException {
        return readDataType(ChargeSetupAttributes.UNIT_CHARGE_ACTIVE, Structure.class);
    }

    public AbstractDataType readUnitChargePasive() throws IOException {
        return readDataType(ChargeSetupAttributes.UNIT_CHARGE_PASSIVE, Structure.class);
    }

    public AbstractDataType readUnitChargeActivationTime() throws IOException {
        return readDataType(ChargeSetupAttributes.UNIT_CHARGE_ACTIVATION_TIME, OctetString.class);
    }

    public AbstractDataType readPeriod() throws IOException {
        return readDataType(ChargeSetupAttributes.PERIOD, Unsigned64.class);
    }

    public AbstractDataType readChargeConfiguration() throws IOException {
        return readDataType(ChargeSetupAttributes.CHARGE_CONFIGURATION, BitString.class);
    }

    public AbstractDataType readLastCollectionTime() throws IOException {
        return readDataType(ChargeSetupAttributes.LAST_COLLECTION_TIME, DateTime.class);
    }

    public AbstractDataType readLastCollectionAmount() throws IOException {
        return readDataType(ChargeSetupAttributes.LAST_COLLECTION_AMOUNT, Integer32.class);
    }

    public AbstractDataType readTotalAmmountRemaining() throws IOException {
        return readDataType(ChargeSetupAttributes.TOTAL_AMOUNT_REMAINING, Integer32.class);
    }

    public AbstractDataType readProportion() throws IOException {
        return readDataType(ChargeSetupAttributes.PROPORTION, Unsigned16.class);
    }

    public void writeChargeAttribute(ChargeSetupAttributes attribute, AbstractDataType data) throws IOException {
        write(attribute, data);
    }

    public void invokeChargeMethod(ChargeSetupMethods chargeSetupMethod) throws IOException {
        methodInvoke(chargeSetupMethod);
    }

    public void invokeChargeMethod(ChargeSetupMethods chargeSetupMethod, AbstractDataType data) throws IOException {
        methodInvoke(chargeSetupMethod, data);
    }

}