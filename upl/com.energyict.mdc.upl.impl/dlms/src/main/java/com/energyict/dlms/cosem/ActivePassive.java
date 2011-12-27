package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.attributes.SupplierIdAttributes;
import com.energyict.dlms.cosem.methods.SupplierIdMethods;


import java.io.IOException;

/**
 * Contains functionality to adjust/handle the ActivePassive object
 */
public class ActivePassive extends AbstractCosemObject {

    private AbstractDataType value;
    private ScalerUnit scalerUnit;

    private AbstractDataType passiveValue;
    private ScalerUnit passiveScalerUnit;

    private DateTime activationDate;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     */
    public ActivePassive(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * Getter for the dlms class id
     *
     * @return the id of the dlms class
     */
    @Override
    protected int getClassId() {
        return DLMSClassId.ACTIVE_PASSIVE.getClassId();
    }

    /**
     * Read the value attribute from the Device
     *
     * @return the value
     * @throws java.io.IOException if for some reason you could not read the attribute
     */
    public AbstractDataType readValue() throws IOException {
        this.value = AXDRDecoder.decode(getResponseData(SupplierIdAttributes.VALUE), 0);
        return this.value;
    }

    /**
     * Get the value attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readValue()} method
     *
     * @return the 'cached' value attribute
     * @throws java.io.IOException if for some reason the attribute could not be read from the device
     */
    public AbstractDataType getValue() throws IOException {
        if (this.value == null) {
            readValue();
        }
        return value;
    }

    /**
     * Write the value to the device. <b>Note:</b> it may not be allowed to set it directly, you can try it
     * with the {@link #writePassiveValue(com.energyict.dlms.axrdencoding.AbstractDataType)} and {@link #writeActivationDate(com.energyict.dlms.axrdencoding.util.DateTime)}
     *
     * @param value the value to write
     * @throws java.io.IOException if for some reason you could not write the attribute
     */
    public void writeValue(final AbstractDataType value) throws IOException {
        write(SupplierIdAttributes.VALUE, value.getBEREncodedByteArray());
        this.value = value;
    }


    /**
     * Read the scalerUnit attribute from the Device
     *
     * @return the scalerUnit
     * @throws java.io.IOException if for some reason you could not read the attribute
     */
    public ScalerUnit readScalerUnit() throws IOException {
        this.scalerUnit = new ScalerUnit(getResponseData(SupplierIdAttributes.SCALER_UNIT), 0);
        return scalerUnit;
    }

    /**
     * Get the ScalerUnit attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readScalerUnit()} method
     *
     * @return the 'cached' ScalerUnit attribute
     * @throws java.io.IOException if for some reason the attribute could not be read from the device
     */
    public ScalerUnit getScalerUnit() throws IOException {
        if (this.scalerUnit == null) {
            readScalerUnit();
        }
        return this.scalerUnit;
    }

    /**
     * Write the ScalerUnit to the device. <b>Note:</b> it may not be allowed to set it directly, you can try it
     * with the {@link #writePassiveScalerUnit(com.energyict.dlms.ScalerUnit)} and {@link #writeActivationDate(com.energyict.dlms.axrdencoding.util.DateTime)}
     *
     * @param scalerUnit the ScalerUnit to write
     * @throws java.io.IOException if for some reason you could not write the attribute
     */
    public void writeScalerUnit(final ScalerUnit scalerUnit) throws IOException {
        write(SupplierIdAttributes.SCALER_UNIT, scalerUnit.getScalerUnitStructure().getBEREncodedByteArray());
        this.scalerUnit = scalerUnit;
    }


    /**
     * Read the PASSIVE value attribute from the Device
     *
     * @return the PASSIVE value
     * @throws java.io.IOException if for some reason you could not read the attribute
     */
    public AbstractDataType readPassiveValue() throws IOException {
        this.passiveValue = AXDRDecoder.decode(getResponseData(SupplierIdAttributes.PASSIVE_VALUE), 0);
        return passiveValue;
    }

    /**
     * Get the PASSIVE value attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readPassiveValue()} method
     *
     * @return the 'cached' PASSIVE value attribute
     * @throws java.io.IOException if for some reason the attribute could not be read from the device
     */
    public AbstractDataType getPassiveValue() throws IOException {
        if (this.passiveValue == null) {
            readPassiveValue();
        }
        return this.passiveValue;
    }

    /**
     * Write the passiveValue to the device
     *
     * @param passiveValue the passiveValue to write
     * @throws java.io.IOException if for some reason you could not write the attribute
     */
    public void writePassiveValue(final AbstractDataType passiveValue) throws IOException {
        write(SupplierIdAttributes.PASSIVE_VALUE, passiveValue.getBEREncodedByteArray());
        this.passiveValue = passiveValue;
    }

    /**
     * Read the PASSIVE ScalerUnit attribute from the Device
     *
     * @return the PASSIVE ScalerUnit
     * @throws java.io.IOException if for some reason you could not read the attribute
     */
    public ScalerUnit readPassiveScalerUnit() throws IOException {
        this.passiveScalerUnit = new ScalerUnit(getResponseData(SupplierIdAttributes.PASSIVE_SCALER_UNIT), 0);
        return this.passiveScalerUnit;
    }

    /**
     * Get the PASSIVE ScalerUnit attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readPassiveScalerUnit()} method
     *
     * @return the 'cached' PASSIVE ScalerUnit attribute
     * @throws java.io.IOException if for some reason the attribute could not be read from the device
     */
    public ScalerUnit getPassiveScalerUnit() throws IOException {
        if (this.passiveScalerUnit == null) {
            readPassiveScalerUnit();
        }
        return passiveScalerUnit;
    }

    /**
     * Write the passiveScalerUnit to the device
     *
     * @param passiveScalerUnit the passiveScalerUnit to write
     * @throws java.io.IOException if for some reason you could not write the attribute
     */
    public void writePassiveScalerUnit(final ScalerUnit passiveScalerUnit) throws IOException {
        write(SupplierIdAttributes.PASSIVE_SCALER_UNIT, passiveScalerUnit.getScalerUnitStructure().getBEREncodedByteArray());
        this.passiveScalerUnit = passiveScalerUnit;
    }

    /**
     * Read the ActivationDate attribute from the Device
     *
     * @return the ActivationDate
     * @throws java.io.IOException if for some reason you could not read the attribute
     */
    public DateTime readActivationDate() throws IOException {
        this.activationDate = new DateTime(getResponseData(SupplierIdAttributes.ACTIVATION_TIME));
        return this.activationDate;
    }

    /**
     * Get the ActivationDate attribute. If the attribute is cached, then this is returned, otherwise it is fetched from the device using the {@link #readActivationDate()} method
     *
     * @return the 'cached' ActivationDate attribute
     * @throws java.io.IOException if for some reason the attribute could not be read from the device
     */
    public DateTime getActivationDate() throws IOException {
        if (this.activationDate == null) {
            readActivationDate();
        }
        return activationDate;
    }

    /**
     * Write the activationDate to the device
     *
     * @param activationDate the activationDate to write
     * @throws java.io.IOException if for some reason you could not write the attribute
     */
    public void writeActivationDate(final DateTime activationDate) throws IOException {
        write(SupplierIdAttributes.ACTIVATION_TIME, activationDate.getBEREncodedByteArray());
        this.activationDate = activationDate;
    }

    /**
     * Indicate that the tenant information may be reset.
     *
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] reset() throws IOException {
        return methodInvoke(SupplierIdMethods.RESET, new Integer8(0));
    }

    /**
     * Indicate the meter to activate his passive information.
     *
     * @return raw data returned from the method invocation
     * @throws java.io.IOException if for some reason the invocation did not succeed
     */
    public byte[] activate() throws IOException {
        return methodInvoke(SupplierIdMethods.ACTIVATE, new Integer8(0));
    }
}
