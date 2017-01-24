package com.energyict.protocolimplv2.elster.ctr.MTU155.util;

import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 24/02/11
 * Time: 9:54
 */
public class DstSettings extends AbstractUtilObject {

    private static final String GAS_DAY_TIME_OBJ_ID = "8.1.3";
    private static final String DAYLIGHT_SAVING_TIME_OBJ_ID = "8.2.0";
    private static final String EK155_GAS_DAY_TIME_OBJECT_ID = "8.1.F";

    private String[] objectsToRequest;
    private List<AbstractCTRObject> ctrObjectList;

    public DstSettings(RequestFactory requestFactory) {
        super(requestFactory, requestFactory.getLogger());

        if (requestFactory.isEK155Protocol()) {
            objectsToRequest = new String[]{
                    GAS_DAY_TIME_OBJ_ID,
                    DAYLIGHT_SAVING_TIME_OBJ_ID,
                    EK155_GAS_DAY_TIME_OBJECT_ID
            };
        } else {
            objectsToRequest = new String[]{
                    GAS_DAY_TIME_OBJ_ID,
                    DAYLIGHT_SAVING_TIME_OBJ_ID,
            };
        }
    }

    public List<AbstractCTRObject> getObjects() throws CTRException {
        if (ctrObjectList == null) {
            AttributeType attributeType = new AttributeType(0x03);
            ctrObjectList = getRequestFactory().queryRegisters(attributeType, objectsToRequest);
        }
        return ctrObjectList;
    }

    public int getGasDayStartEndTime() throws CTRException {
        for (AbstractCTRObject object : getObjects()) {
            if (object.getId().toString().equals(GAS_DAY_TIME_OBJ_ID)) {
                try {
                    return object.getValue(0).getIntValue();
                } catch (IndexOutOfBoundsException e) {
                    // Absorb the exception
                }
            }
        }
        throw new CTRException("Failed to readout Gas day time object 8.1.3");
    }

    public int getDayLightSavingEnabledValue() throws CTRException {
        for (AbstractCTRObject object : getObjects()) {
            if (object.getId().toString().equals(DAYLIGHT_SAVING_TIME_OBJ_ID)) {
                try {
                    return object.getValue(0).getIntValue();
                } catch (IndexOutOfBoundsException e) {
                    // Absorb the exception
                }
            }
        }
        throw new CTRException("Failed to readout object 8.2.0");
    }

    public int getEK155GasDayStartEndTime() throws CTRException {
        for (AbstractCTRObject object : getObjects()) {
            if (object.getId().toString().equals(EK155_GAS_DAY_TIME_OBJECT_ID)) {
                try {
                    return object.getValue(1).getIntValue();
                } catch (IndexOutOfBoundsException e) {
                    // Absorb the exception
                }
            }
        }
        throw new CTRException("Failed to readout Gas day time object 8.1.F");
    }

    public int getEK155TimeInUCTValue() throws CTRException {
        for (AbstractCTRObject object : getObjects()) {
            if (object.getId().toString().equals(EK155_GAS_DAY_TIME_OBJECT_ID)) {
                try {
                    return object.getValue(1).getIntValue();
                } catch (IndexOutOfBoundsException e) {
                    // Absorb the exception
                }
            }
        }
        throw new CTRException("Failed to readout Gas day time object 8.1.F");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DstSettings {\n");
        if (ctrObjectList == null) {
            sb.append(" > ").append("ctrObjectList=null\n");
        } else {
            for (AbstractCTRObject ctrObject : ctrObjectList) {
                sb.append(" > ").append(ctrObject.toString()).append("  ").append(ctrObject.getClass().getSimpleName()).append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
