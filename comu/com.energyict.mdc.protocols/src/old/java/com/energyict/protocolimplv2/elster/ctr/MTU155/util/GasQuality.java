package com.energyict.protocolimplv2.elster.ctr.MTU155.util;

import com.energyict.protocolimplv2.elster.ctr.MTU155.RequestFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 24/02/11
 * Time: 9:54
 */

/**
 * @deprecated  Previous this object was used to launch a 'query for multiple registers'.
 *              However, the concept of 'query for multiple registers' is now fully integrated in the requestFactory - making this object no longer useful.
 * @see com.energyict.genericprotocolimpl.elster.ctr.RequestFactory#getObjects(java.lang.String...)
 *
 */
public class GasQuality extends AbstractUtilObject {

    private static final String Z_COMP_METHOD_OBJ_ID = "A.B.2";
    private static final String VOL_COMP_METHOD_OBJ_ID = "A.B.4";
    private static final String P_REF_OBJECT_ID = "4.9.1";
    private static final String T_REF_OBJECT_ID = "7.B.1";
    private static final String INST_C_COEFFICIENT = "A.0.0";
    private static final String INST_Z_COEFFICIENT = "A.2.0";
    private static final String INST_GAS_DENSITY_COEFFICIENT = "A.3.0";
    private static final String INST_AIR_DENSITY_COEFFICIENT = "A.4.0";
    private static final String INST_REL_DENSITY_COEFFICIENT = "A.5.0";
    private static final String INST_GAS_N2_PERCENTAGE = "A.6.0";
    private static final String INST_GAS_CO2_PERCENTAGE = "A.7.0";
    private static final String INST_GAS_H2_PERCENTAGE = "A.8.0";
    private static final String INST_HCV = "B.1.0";

    private static final String[] OBJECTS_TO_REQUEST = new String[]{
            Z_COMP_METHOD_OBJ_ID,
            VOL_COMP_METHOD_OBJ_ID,
            P_REF_OBJECT_ID,
            T_REF_OBJECT_ID,
            INST_C_COEFFICIENT,
            INST_Z_COEFFICIENT,
            INST_GAS_DENSITY_COEFFICIENT,
            INST_AIR_DENSITY_COEFFICIENT,
            INST_REL_DENSITY_COEFFICIENT,
            INST_GAS_N2_PERCENTAGE,
            INST_GAS_CO2_PERCENTAGE,
            INST_GAS_H2_PERCENTAGE,
            INST_HCV
    };

    private List<AbstractCTRObject> ctrObjectList;

    public GasQuality(RequestFactory requestFactory, Logger logger) {
        super(requestFactory, logger);
    }

    /**
     * Check if the GasQuality object contains an object with the given ID
     *
     * @param id
     * @return
     */
    public static boolean containsObjectId(CTRObjectID id) {
        for (String capturedId : OBJECTS_TO_REQUEST) {
            if (id.is(capturedId)) {
                return true;
            }
        }
        return false;
    }

    public List<AbstractCTRObject> getObjects() throws CTRException {
        if (ctrObjectList == null) {
            AttributeType attributeType = new AttributeType(0x03);
            ctrObjectList = getRequestFactory().queryRegisters(attributeType, OBJECTS_TO_REQUEST);
        }
        return ctrObjectList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GasQuality {\n");
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
