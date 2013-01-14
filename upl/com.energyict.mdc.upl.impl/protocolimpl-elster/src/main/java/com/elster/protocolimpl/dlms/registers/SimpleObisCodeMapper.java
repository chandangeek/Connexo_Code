package com.elster.protocolimpl.dlms.registers;

import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.applicationlayer.CosemDataAccessException;
import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleClockObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleDataObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleRegisterObject;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataVisibleString;
import com.elster.protocolimpl.dlms.util.DlmsUtils;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * User: heuckeg
 * Date: 24.03.11
 * Time: 13:50
 */
@SuppressWarnings({"unused"})
public class SimpleObisCodeMapper {

    private CosemApplicationLayer layer;
    private RegisterMap map;
    private SimpleCosemObjectManager objectManager = null;

    public SimpleObisCodeMapper(CosemApplicationLayer layer, RegisterMap map) {
        this.layer = layer;
        this.map = map;
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

        if (!map.contains(obisCode)) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                    + " is not supported by the protocol");
        }

        try {
            Date tst = new Date();

            // special handling of "DST enabled"
            if (obisCode.toString().equals("0.0.96.53.0.255")) {

                SimpleClockObject clockObject = (SimpleClockObject) getObjectManager().getSimpleCosemObject(Ek280Defs.CLOCK_OBJECT);

                String v = clockObject.isDaylightSavingsEnabled() ? "1" : "0";
                Quantity val = new Quantity(v, Unit.getUndefined());
                //System.out.println("OBIS(sro):" + obisCode.toString() + "=" + ((SimpleRegisterObject) vm).getValueAsString() + " <" + u + ">");
                return new RegisterValue(obisCode, val, null, tst);
            }

            // special handling of meter location (should be later in ElsterDlmsLibrary)
            if (obisCode.toString().equals("7.128.0.0.6.255")) {

                CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(
                        new com.elster.dlms.types.basic.ObisCode("7.128.0.0.6.255"), 1, 2);

                //--- Read ---
                GetDataResult dataResult1 =
                        layer.getAttribute(attributeDescriptor, null);

                DlmsData dd = dataResult1.getData();
                if (dd.getType() == DlmsData.DataType.VISIBLE_STRING) {
                    String r = ((DlmsDataVisibleString) dd).getValue();
                    return new RegisterValue(obisCode, r);
                }
                return null;
            }

            /* "translate" ei code to elster code */
            com.elster.dlms.types.basic.ObisCode elObisCode = map.forObisCode(obisCode);

            /* get object */
            SimpleCosemObject vm = getObjectManager().getSimpleCosemObject(elObisCode);

            /* test read object... */
            if (vm instanceof SimpleRegisterObject) {
                SimpleRegisterObject sro = (SimpleRegisterObject) vm;
                if (sro.isNumber()) {
                    String u = sro.getScalerUnit().getUnit().getDisplayName();
                    Quantity val = new Quantity(sro.getScaledValue(), DlmsUtils.getUnitFromString(u));
                    //System.out.println("OBIS(sro):" + obisCode.toString() + "=" + ((SimpleRegisterObject) vm).getValueAsString() + " <" + u + ">");
                    return new RegisterValue(obisCode, val, null, tst);
                } else {
                    //System.out.println("OBIS(sro):" + obisCode.toString() + "=" + ((SimpleRegisterObject) vm).getValueAsString());
                    return new RegisterValue(obisCode, sro.getValueAsString());
                }
            }
            if (vm instanceof SimpleDataObject) {
                //System.out.println("OBIS(sdo):" + obisCode.toString() + "=" + ((SimpleDataObject) vm).getValueAsString());
                return new RegisterValue(obisCode, ((SimpleDataObject) vm).getValueAsString());
            }
            throw new NoSuchRegisterException("Class of object for ObisCode " + obisCode.toString()
                    + " is not supported by the protocol");
        } catch (CosemDataAccessException cdaException) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                    + " is not supported by the protocol");
        }
    }

    public String getDescriptionFor(String s) {
        for (Map.Entry<ObisCode, com.elster.dlms.types.basic.ObisCode> entry : map.getAsMap().entrySet()) {
            if (entry.getKey().toString().equals(s)) {
                return entry.getKey().toString();
            }
        }
        return null;
    }

    private SimpleCosemObjectManager getObjectManager() {
        if (objectManager == null) {
            objectManager = new SimpleCosemObjectManager(layer, Ek280Defs.DEFINITIONS);
        }
        return objectManager;
    }
}
