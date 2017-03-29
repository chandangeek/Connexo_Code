package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 21/03/12
 * Time: 15:56
 */
public abstract class G3Mapping {

    private final ObisCode obis;

    protected G3Mapping(ObisCode obis) {
        this.obis = obis;
    }

    public RegisterValue readRegister(AS330D as330D) throws IOException {
        return readRegister(as330D.getSession().getCosemObjectFactory());
    }

    public abstract RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException;

    public final ObisCode getObisCode() {
        return obis;
    }

    public ObisCode getBaseObisCode() {
        return getObisCode();
    }

    /**
     * meterTimeZone, Unit and CaptureTime are optional (they can be null), in this case they will not be used to construct the RegisterValue.
     * They are only relevant for e.g. extended register.
     */
    public abstract RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException;

    public RegisterValue parse(AbstractDataType abstractDataType) throws IOException {
        return parse(abstractDataType, null, null);
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit) throws IOException {
        return parse(abstractDataType, unit, null);
    }

    public abstract int getDLMSClassId();

    /**
     * Sometimes it's necessary to read out multiple attributes (e.g. value, unit and capture time in case of extended register)
     * The proper subclasses can specify this by overriding this method
     */
    public int[] getAttributeNumbers() {
        return new int[]{getAttributeNumber()};
    }

    /**
     * Usually the value attribute (2) is requested, subclasses can override this
     */
    protected int getAttributeNumber() {
        return 2;
    }

    /**
     * Returns the index of the "value" attribute, i.e. the actual attribute to read-out
     * This will be overwritten in most mappings.
     *
     * @return index of value attribute
     */
    public int getValueAttribute() {
        return getAttributeNumber(); // careful, this is overwritten
    }

    /**
     * Return the index of the unit attribute, if present.
     * 0 if not applicable (no unit attribute to be read-out);
     *
     * @return index of unit attribute or 0 if n/a
     */
    public int getUnitAttribute() {
        return  0; // no unit attribute to read
    }

    /**
     * Return the index of the capture time attribute, if present.
     * 0 if not applicable (no capture time attribute to read-out)
     * @return index of the capture time attribute
     */
    public int getCaptureTimeAttribute(){
        return 0;
    }
}