package com.energyict.dlms.cosem;

import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static com.energyict.dlms.DLMSCOSEMGlobals.REGISTER_SCALER_UNIT;
import static com.energyict.dlms.DLMSCOSEMGlobals.REGISTER_VALUE;

/**
 *
 * @author  Koen
 * Changes:
 * GNA |03022009| Added method to get an attributes abstractDataType
 */
public class Register extends AbstractCosemObject implements CosemObject {
    public final int DEBUG=0;
    public static final int CLASSID = DLMSClassId.REGISTER.getClassId();

    long value; // instance specific value converted to long
    ScalerUnit scalerUnit=null;
    boolean valueCached=false;

    /** Creates a new instance of Register */
    public Register(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

    public String toString() {
        try {
           return "value="+getValue()+", scalerUnit="+getScalerUnit().getEisUnit().toString();
        }
        catch(IOException e) {
           return "register retrieving error!";
        }
    }
    /**
     * Getter for property value.
     * @return Value of property value.
     */
    public long getValue() throws IOException {
        if (valueCached) {
			return value;
		} else {
			return (getValue(getResponseData(REGISTER_VALUE)));
		}
    }
    public long getValue(byte[] responseData) throws ProtocolException {
        value = DLMSUtils.parseValue2long(responseData);
        return value;
    }

    public void setValue(Long value) {
        valueCached=true;
        this.value=value.longValue();
    }

    public void setScalerUnit(ScalerUnit scalerUnit) {
        this.scalerUnit=scalerUnit;
    }

    /**
     * Getter for property scalerUnit.
     * @return Value of property scalerUnit.
     */
    public ScalerUnit getScalerUnit() throws IOException {
        try {
            if (scalerUnit == null) {
                byte[] responseData = getResponseData(REGISTER_SCALER_UNIT);
                scalerUnit = new ScalerUnit(responseData);
            }
            return scalerUnit;
        }
        catch(IOException e) {
            if (getObjectReference().isAbstract() && (e.toString().indexOf("R/W denied")>=0)) {
				return new ScalerUnit(0, 255); // no scaler and unitless
			} else {
				throw e;
			}
        }
    }

    public AbstractDataType getValueAttr() throws IOException {
        return AXDRDecoder.decode(getLNResponseData(2));
    }

    public Quantity getQuantityValue() throws IOException {
       return new Quantity(new BigDecimal(getValue()),getScalerUnit().getEisUnit());
    }

    public Date getCaptureTime() throws IOException {
        return null;
    }

    public Date getBillingDate() {
        return null;
    }

    public int getResetCounter() {
        return -1;
    }

    protected int getClassId() {
        return CLASSID;
    }

    public String getText() throws IOException {
        return null;
    }

    public void setScalerUnitAttr(Unit unit) throws IOException {
    	final ScalerUnit scalerUnit = new ScalerUnit(unit);
        final Structure structure = scalerUnit.getScalerUnitStructure();
        write(RegisterAttributes.SCALER_UNIT, structure);
    }

    public void setValueAttr(AbstractDataType value) throws IOException {
        write(RegisterAttributes.VALUE, value);
    }

}
