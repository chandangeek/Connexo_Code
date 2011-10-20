/*
 * Data.java
 *
 * Created on 30 augustus 2004, 13:52
 */

package com.energyict.dlms.cosem;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;

import java.io.IOException;
import java.util.Date;
/**
 *
 * @author  Koen
 * Changes:
 * GNA |03022009| Added method to get an attributes abstractDataType
 */
public class Data extends AbstractCosemObject implements CosemObject {

	public final int DEBUG=0;
	public static final int CLASSID=1;

    /** Creates a new instance of Data */
    public Data(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

    public String toString() {
        try {
           return "value="+getDataContainer().toString();
        }
        catch(IOException e) {
           return "data retrieving error!";
        }
    }

    public String getText() throws IOException {
        return getDataContainer().getText(",");
    }

    public Date getBillingDate() throws IOException {
        return getDataContainer().getRoot().getOctetString(0).toDate(protocolLink.getTimeZone());

    }

    public Date getCaptureTime() throws IOException {
        return null;
    }

    public Quantity getQuantityValue() throws IOException {
        return new Quantity(getDataContainer().getRoot().convert2Long(0), Unit.get(""));
    }

    public int getResetCounter() {
        return -1;
    }

    public ScalerUnit getScalerUnit() throws IOException {
        return new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
    }

    public long getValue() throws IOException {
        DataContainer dataContainer=getDataContainer();
        if (dataContainer.getRoot().isInteger(0)) {
           return (long)((Integer)dataContainer.getRoot().getElement(0)).intValue();
        } else if (dataContainer.getRoot().isOctetString(0)) {
            String value = ((OctetString)dataContainer.getRoot().getElement(0)).toString().trim();
            try {
                return Long.valueOf(value).longValue();
            } catch (NumberFormatException e) {
                throw new IOException("Data, getValue(), invalid data value type. " + e.getMessage());
        }
        } else {
        throw new IOException("Data, getValue(), invalid data value type...");
    }
    }

    public String getString() throws IOException {
        DataContainer dataContainer=getDataContainer();
        if (dataContainer.getRoot().isOctetString(0)) {
           return ((OctetString)dataContainer.getRoot().getElement(0)).toString().trim();
        }
        else if (dataContainer.getRoot().isString(0)) {
           return ((String)dataContainer.getRoot().getElement(0)).trim();
        }
        throw new IOException("Data, getString(), invalid data value type...");
    }

    public AbstractDataType getValueAttr() throws IOException {
        return AXDRDecoder.decode(getLNResponseData(2));
    }

    public void setValueAttr(AbstractDataType val) throws IOException {
        write(2, val.getBEREncodedByteArray());
    }

    /**
     * Getter for property object.
     * @return Value of property object.
     */
    public DataContainer getDataContainer() throws IOException {
        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(getResponseData(DATA_VALUE),protocolLink.getLogger());
        if (DEBUG >= 1) {
			dataContainer.printDataContainer();
		}
        return dataContainer;
    }

    public byte[] getData() throws IOException {
        return getResponseData(DATA_VALUE);
    }

    protected int getClassId() {
        return CLASSID;
    }

}
