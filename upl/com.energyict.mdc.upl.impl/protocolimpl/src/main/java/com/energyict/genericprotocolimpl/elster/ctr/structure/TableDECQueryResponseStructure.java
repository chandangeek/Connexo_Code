package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28-okt-2010
 * Time: 13:26:00
 */
public class TableDECQueryResponseStructure extends AbstractTableQueryResponseStructure {

    private static final List<String> CAPTURED_OBJECTS;

    static {
        CAPTURED_OBJECTS = new ArrayList();
        CAPTURED_OBJECTS.add("8.0.1");
        CAPTURED_OBJECTS.add("12.2.0");
        CAPTURED_OBJECTS.add("2.0.0");
        CAPTURED_OBJECTS.add("2.1.0");
        CAPTURED_OBJECTS.add("2.3.0");
        CAPTURED_OBJECTS.add("1.0.0");
        CAPTURED_OBJECTS.add("1.2.0");
        CAPTURED_OBJECTS.add("13.0.0");
        CAPTURED_OBJECTS.add("13.0.1");
        CAPTURED_OBJECTS.add("4.0.0");
        CAPTURED_OBJECTS.add("7.0.0");
        CAPTURED_OBJECTS.add("A.0.0");
        CAPTURED_OBJECTS.add("A.1.6");
        CAPTURED_OBJECTS.add("8.1.2");
        CAPTURED_OBJECTS.add("10.1.0");
        CAPTURED_OBJECTS.add("12.0.0");
        CAPTURED_OBJECTS.add("D.9.0");
        CAPTURED_OBJECTS.add("E.C.0");
    }

    //See documentation p. 62, TABLE DEC structure
    private CTRAbstractValue<String> pdr;
    private AbstractCTRObject dataAndOraS;
    private AbstractCTRObject diagnR;
    private AbstractCTRObject tot_Vm;
    private AbstractCTRObject tot_Vb;
    private AbstractCTRObject tot_Vme;
    private AbstractCTRObject Qm;
    private AbstractCTRObject Qb;
    private AbstractCTRObject sInput;
    private AbstractCTRObject sAlarmInput;
    private AbstractCTRObject pressure;
    private AbstractCTRObject temperature;
    private AbstractCTRObject conversionFactor;
    private AbstractCTRObject compressibility;
    private AbstractCTRObject shift;
    private AbstractCTRObject numberOfElements;
    private AbstractCTRObject deviceStatus;
    private AbstractCTRObject sealStatus;
    private AbstractCTRObject gsmField;

    public CTRAbstractValue<String> getPdr() {
        return pdr;
    }

    public List<AbstractCTRObject> getObjects() {
        List<AbstractCTRObject> list = new ArrayList();
        list.add(dataAndOraS);
        list.add(diagnR);
        list.add(tot_Vm);
        list.add(tot_Vb);
        list.add(tot_Vme);
        list.add(Qm);
        list.add(Qb);
        list.add(sInput);
        list.add(sAlarmInput);
        list.add(pressure);
        list.add(temperature);
        list.add(conversionFactor);
        list.add(compressibility);
        list.add(shift);
        list.add(numberOfElements);
        list.add(deviceStatus);
        list.add(sealStatus);
        list.add(gsmField);
        return list;
    }

    public TableDECQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    /**
     * Check if the DECF table contains an object with the given ID
     *
     * @param id
     * @return
     */
    public static boolean containsObjectId(CTRObjectID id) {
        for (String capturedId : CAPTURED_OBJECTS) {
            if (id.is(capturedId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public byte[] getBytes() {

        return padData(ProtocolTools.concatByteArrays(
                pdr.getBytes(),
                dataAndOraS.getBytes(),
                diagnR.getBytes(),
                tot_Vm.getBytes(),
                tot_Vb.getBytes(),
                tot_Vme.getBytes(),
                Qm.getBytes(),
                Qb.getBytes(),
                sInput.getBytes(),
                sAlarmInput.getBytes(),
                pressure.getBytes(),
                temperature.getBytes(),
                conversionFactor.getBytes(),
                compressibility.getBytes(),
                shift.getBytes(),
                numberOfElements.getBytes(),
                deviceStatus.getBytes(),
                sealStatus.getBytes(),
                gsmField.getBytes()
        ));
    }

    @Override
    public TableDECQueryResponseStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        CTRObjectFactory factory = new CTRObjectFactory();
        AttributeType type = new AttributeType(0x00);
        type.setHasIdentifier(false);
        type.setHasValueFields(true);

        int ptr = offset;

        pdr = factory.parse(rawData, ptr, type, "C.0.0").getValue()[0];
        ptr += pdr.getValueLength();

        dataAndOraS = factory.parse(rawData, ptr, type, "8.0.1");
        ptr += dataAndOraS.getLength();

        diagnR = factory.parse(rawData, ptr, type, "12.2.0");
        ptr += diagnR.getLength();

        type.setHasQualifier(true);

        tot_Vm = factory.parse(rawData, ptr, type, "2.0.0");
        ptr += tot_Vm.getLength();

        tot_Vb = factory.parse(rawData, ptr, type, "2.1.0");
        ptr += tot_Vb.getLength();

        tot_Vme = factory.parse(rawData, ptr, type, "2.3.0");
        ptr += tot_Vme.getLength();

        Qm = factory.parse(rawData, ptr, type, "1.0.0");
        ptr += Qm.getLength();

        Qb = factory.parse(rawData, ptr, type, "1.2.0");
        ptr += Qb.getLength();

        type.setHasQualifier(false);

        sInput = factory.parse(rawData, ptr, type, "13.0.0");
        ptr += sInput.getLength();

        sAlarmInput = factory.parse(rawData, ptr, type, "13.0.1");
        ptr += sAlarmInput.getLength();

        type.setHasQualifier(true);

        pressure = factory.parse(rawData, ptr, type, "4.0.0");
        ptr += pressure.getLength();

        temperature = factory.parse(rawData, ptr, type, "7.0.0");
        ptr += temperature.getLength();

        conversionFactor = factory.parse(rawData, ptr, type, "A.0.0");
        ptr += conversionFactor.getLength();

        compressibility = factory.parse(rawData, ptr, type, "A.1.6");
        ptr += compressibility.getLength();

        type.setHasQualifier(false);

        shift = factory.parse(rawData, ptr, type, "8.1.2");
        ptr += shift.getLength();

        numberOfElements = factory.parse(rawData, ptr, type, "10.1.0");
        ptr += numberOfElements.getLength();

        deviceStatus = factory.parse(rawData, ptr, type, "12.0.0");
        ptr += deviceStatus.getLength();

        sealStatus = factory.parse(rawData, ptr, type, "D.9.0");
        ptr += sealStatus.getLength();

        gsmField = factory.parse(rawData, ptr, type, "E.C.0");
        ptr += gsmField.getLength();

        return this;
    }
}