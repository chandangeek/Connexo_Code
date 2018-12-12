package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

/**
 * Created by cisac on 12/12/2016.
 */
public class MultiAPNConfigMapping extends G3Mapping{

    public MultiAPNConfigMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        final Data data = cosemObjectFactory.getData(getObisCode());
        return parse(data.getValueAttr());
    }

    /**
     * "Configured APNs ::= STRUCTURE {
     * active_configuration ::= long-unsiged (-1 for automatic scan),
     * apn_configurations ::= ARRAY {
     *          apn_configuration ::= STRUCTURE {
     *                  active_selection:           boolean,
     *                  apn_name:                   octet-string,
     *                  pdp_user_name:              octet-string,
     *                  pdp_password:               octet-string,
     *                  authentication_protocol     enum (ignored),
     *                  chap_algorithm              unsigned (ignored),
     *                  ip_comp_protocol            long-unsigned (ignored),													i
     *                  p_version                  enum (ignored)									}	}"

     * @param abstractDataType
     * @param unit
     * @param captureTime
     * @return
     * @throws IOException
     */
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        Structure structure = abstractDataType.getStructure();

        Array apnConfigs = structure.getDataType(1).getArray();
        JSONObject json = new JSONObject();

        try {
            json.put("activeAPN", structure.getDataType(0).longValue());

            JSONArray jsonArray = new JSONArray();
            int index = 1;
            for(AbstractDataType config: apnConfigs){
                Structure struct = config.getStructure();
                JSONObject j = new JSONObject();
                j.put("index", index);
                j.put("active", struct.getDataType(0).getBooleanObject().getState());
                j.put("APN", getStringValueFromStructureEntry(config, 1));
                j.put("userName", getStringValueFromStructureEntry(config, 2));
                j.put("password", getStringValueFromStructureEntry(config, 3));
                jsonArray.put(j);
            }

            json.put("configurations", jsonArray);

            return new RegisterValue(getObisCode(), json.toString());

        } catch (Exception e) {
            // swallow and return default
        }

        return new RegisterValue(getObisCode(), abstractDataType.toString());
    }

    private String getStringValueFromStructureEntry(AbstractDataType config, int entryIndex) {
        return config.getStructure().getDataType(entryIndex).getOctetString().stringValue();
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DATA.getClassId();
    }
}
