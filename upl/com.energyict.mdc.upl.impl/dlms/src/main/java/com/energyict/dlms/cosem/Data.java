package com.energyict.dlms.cosem;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.protocolimpl.base.IOExceptionWithCause;

import java.io.IOException;
import java.util.Date;

/**
 * This class represents the dlms Data object (class_id: 1, version: 0)
 * This object allows modelling various data, such as configuration data and parameters.
 * <p/>
 * Copyrights EnergyICT
 * User: jme
 * Date: 06/01/2012
 * Time: 11:35
 *
 * @author Koen
 */
public class Data extends AbstractCosemObject implements CosemObject {

    /**
     * The dlms Data class ID is 1. This is the same value as {@link DLMSClassId} returns for DATA.
     * Because ALL the dlms class id's can be found centralized in one enum, {@link DLMSClassId},
     * it's better to use that enum class.
     *
     * @deprecated The class id of every Dlms object is available using the {@link DLMSClassId} enum.
     */
    public static final int CLASSID = DLMSClassId.DATA.getClassId();

    /**
     * Creates a new instance of Data given the {@link com.energyict.dlms.ProtocolLink} and the {@link ObjectReference}
     *
     * @param protocolLink    The protocol link that will be used to get the object from the device
     * @param objectReference The object reference containing the unique LN or the SN of the object in the device
     */
    public Data(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * Get the dlms class id for this data object, as defined in the dlms bluebook.
     *
     * @return The dlms class id of a data object, as int value (DATA = 1)
     */
    protected int getClassId() {
        return DLMSClassId.DATA.getClassId();
    }

    /**
     * Get the value attribute as text value. The text is actually the value represented as a {@link DataContainer}.
     *
     * @return The attribute value as text.
     * @throws IOException If there was an error during the readout of the value attribute
     * @see com.energyict.dlms.DataContainer#getText(java.lang.String)
     */
    public String getText() throws IOException {
        return getDataContainer().getText(",");
    }

    /**
     * Try to read the billing date from the value attribute. This method assumes that
     * the value attribute contains a date, represented as an {@link OctetString}. If this
     * is not the case, the method will throw an exception.
     *
     * @return The billing date
     * @throws IOException If there was an error while reading the value or the value did not
     *                     contain an date encoded as {@link OctetString}
     */
    public Date getBillingDate() throws IOException {
        return getDataContainer().getRoot().getOctetString(0).toDate(protocolLink.getTimeZone());

    }

    /**
     * The Data object has no capture time. This will always return null
     *
     * @return null, because the data object has no capture time
     * @throws IOException This method will never throw an IOException because there is no capture time to read from the device
     */
    public Date getCaptureTime() throws IOException {
        return null;
    }

    public Quantity getQuantityValue() throws IOException {
        return new Quantity(getDataContainer().getRoot().convert2Long(0), Unit.get(""));
    }

    /**
     * The Data object has no reset counter. This will always return -1
     *
     * @return -1 because there is no reset counter available
     */
    public int getResetCounter() {
        return -1;
    }

    /**
     * The Data object has no scaler or unit. This will always return a unit less value with a scaler 0
     *
     * @return
     * @throws IOException
     */
    public ScalerUnit getScalerUnit() throws IOException {
        return new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
    }

    /**
     * Read the Data object's value attribute and try to return it as a long. This method assumes that the value
     * attribute contains an integer, long or a string that represents a long, and will throw an IOException if this is not the case.
     *
     * @return The long value of the value attribute
     * @throws IOException if there was an error reading the data or if the value was not convertible to a long value
     */
    public long getValue() throws IOException {
        DataStructure root = getDataContainer().getRoot();
        if (root.isInteger(0)) {
            return (Integer) root.getElement(0);
        } else if (root.isLong(0)) {
            return (Long) root.getElement(0);
        } else if (root.isOctetString(0) || root.isString(0)) {
            try {
                return Long.valueOf(root.getElement(0).toString().trim()).longValue();
            } catch (NumberFormatException e) {
                throw new IOExceptionWithCause("Data, getValue(), invalid data value type. ", e);
            }
        } else {
            throw new IOException("Data, getValue(), invalid data value type...");
        }
    }

    /**
     * Read the Data object's value attribute and try to return it as a String. This method assumes that
     * the value attribute contains a string value, and will throw an IOException if this is not the case.
     *
     * @return The string value of the value attribute
     * @throws IOException if there was an error reading the data or if the value was not a string value
     */
    public String getString() throws IOException {
        DataContainer dataContainer = getDataContainer();
        if (dataContainer.getRoot().isOctetString(0)) {
            return dataContainer.getRoot().getElement(0).toString().trim();
        } else if (dataContainer.getRoot().isString(0)) {
            return ((String) dataContainer.getRoot().getElement(0)).trim();
        }
        throw new IOException("Data, getString(), invalid data value type...");
    }

    /**
     * Read the Data object's value attribute and return it as a raw byte array. This method should be used rarely.
     * A more common method to read the value attribute is {@link Data#getValueAttr()}.
     *
     * @return the raw AXDR encoded binary data
     * @throws IOException if there was an error reading the data
     * @see Data#getValueAttr()
     * @see Data#getDataContainer()
     */
    public byte[] getRawValueAttr() throws IOException {
        return getResponseData(DataAttributes.VALUE);
    }

    /**
     * Read the Data object's value attribute, parse it and return it as an {@link com.energyict.dlms.axrdencoding.AbstractDataType}
     * This should be the most used method to retrieve the contents of the value attribute
     *
     * @return the raw AXDR encoded binary data
     * @throws IOException if there was an error reading the data
     * @see Data#getRawValueAttr()
     * @see Data#getDataContainer()
     */
    public AbstractDataType getValueAttr() throws IOException {
        return AXDRDecoder.decode(getResponseData(DataAttributes.VALUE));
    }

    /**
     * Read the Data object's value attribute and return it as a DataContainer object
     *
     * @return DataContainer object, containing the value of this Data object
     * @throws IOException if there was an error reading the data
     * @see Data#getValueAttr()
     */
    public DataContainer getDataContainer() throws IOException {
        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(getRawValueAttr(), getLogger());
        return dataContainer;
    }

    /**
     * Write a new value to the vallue attribute of this {@link Data} object.
     *
     * @param value The new dlms {@link com.energyict.dlms.axrdencoding.AbstractDataType} to write to the value attribute. The value cannot be null.
     * @throws IOException if there was an error writing the data
     * @see Data#getValueAttr()
     */
    public void setValueAttr(AbstractDataType value) throws IOException {
        write(DataAttributes.VALUE, value.getBEREncodedByteArray());
    }

    public String toString() {
        try {
            return "value=" + getDataContainer().toString();
        } catch (IOException e) {
            return "data retrieving error! " + e.getMessage();
        }
    }

}
