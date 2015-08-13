package com.elster.protocolimpl.dlms.registers;

import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.applicationlayer.CosemDataAccessException;
import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleClockObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleDataObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleRegisterObject;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.DlmsTime;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataVisibleString;
import com.elster.protocolimpl.dlms.util.DlmsUtils;
import com.elster.protocolimpl.dlms.util.ProtocolLink;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * User: heuckeg
 * Date: 24.03.11
 * Time: 13:50
 */
@SuppressWarnings({"unused"})
public class DlmsRegisterReader {

    private CosemApplicationLayer layer;

    private ProtocolLink protocolLink;

    private RegisterMap readableRegisters;

    private SimpleCosemObjectManager objectManager = null;

    private HashMap<String, SimpleProfileObject> monthlyProfiles = new HashMap<String, SimpleProfileObject>();

    private int begOfDay = 6;

    public DlmsRegisterReader(final CosemApplicationLayer layer, final ProtocolLink link, final RegisterMap map, final SimpleCosemObjectManager objectManager) {
        this.layer = layer;
        this.protocolLink = link;
        this.readableRegisters = map;
        this.objectManager = objectManager;
    }

    public RegisterValue getRegisterValue(ObisCode obisCode, Date date)
            throws IOException {
        IReadableRegister registerToRead = readableRegisters.forObisCode(obisCode);

        if (registerToRead != null) {
            if (registerToRead instanceof DlmsSimpleRegisterDefinition) {
                if ((((DlmsSimpleRegisterDefinition) registerToRead).getClassId() == 0) &&
                        (((DlmsSimpleRegisterDefinition) registerToRead).getAttributeNo() == 0)) {
                    return getSimpleRegister((DlmsSimpleRegisterDefinition) registerToRead, date);
                } else {
                    return getRemappedRegister((DlmsSimpleRegisterDefinition) registerToRead, date);
                }
            }
            if (registerToRead instanceof DlmsHistoricalRegisterDefinition) {
                return getHistoricalRegister((DlmsHistoricalRegisterDefinition) registerToRead, obisCode, date);
            }
        }
        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported by the protocol");
    }

    private RegisterValue getRemappedRegister(DlmsSimpleRegisterDefinition register, Date date)
            throws IOException {
        ObisCode obisCode = ObisCode.fromString(register.getObisCode());
        GetDataResult dataResult;
        try {
            CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(
                    register.getElsObisCode(), register.getClassId(), register.getAttributeNo());

            //--- Read ---
            dataResult = layer.getAttribute(attributeDescriptor, null);
        } catch (CosemDataAccessException cdaException) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                    + " is not supported by the protocol");
        }

        DlmsData dd = dataResult.getData();
        if (dd.getType() == DlmsData.DataType.VISIBLE_STRING) {
            String r = ((DlmsDataVisibleString) dd).getValue();
            return new RegisterValue(obisCode, r);
        }
        if (dd.getType() == DlmsData.DataType.OCTET_STRING) {
            String r = new String(((DlmsDataOctetString) dd).getValue());
            return new RegisterValue(obisCode, r);
        }
        return new RegisterValue(obisCode, dd.toString());
    }

    public RegisterValue getSimpleRegister(DlmsSimpleRegisterDefinition register, Date date)
            throws IOException {
        ObisCode obisCode = ObisCode.fromString(register.getObisCode());
        try {
            // special handling of "DST enabled"
            if (register.getObisCode().equals("0.0.96.53.0.255")) {
                // read clock object...
                SimpleClockObject clockObject = (SimpleClockObject) objectManager.getSimpleCosemObject(Ek280Defs.CLOCK_OBJECT);

                String v = clockObject.isDaylightSavingsEnabled() ? "1" : "0";
                Quantity val = new Quantity(v, Unit.getUndefined());
                return new RegisterValue(obisCode, val, null, date);
            }

            // special handling of meter location (should be later in ElsterDlmsLibrary)
            if (register.getObisCode().equals("7.128.0.0.6.255")) {
                CosemAttributeDescriptor attributeDescriptor = new CosemAttributeDescriptor(
                        new com.elster.dlms.types.basic.ObisCode("7.128.0.0.6.255"), 1, 2);

                //--- Read ---
                GetDataResult dataResult1 = layer.getAttribute(attributeDescriptor, null);

                DlmsData dd = dataResult1.getData();
                if (dd.getType() == DlmsData.DataType.VISIBLE_STRING) {
                    String r = ((DlmsDataVisibleString) dd).getValue();
                    return new RegisterValue(obisCode, r);
                }
                return null;
            }

            /* get object */
            SimpleCosemObject vm = objectManager.getSimpleCosemObject(register.getElsObisCode());

            /* test read object... */
            if (vm instanceof SimpleRegisterObject) {
                SimpleRegisterObject sro = (SimpleRegisterObject) vm;
                if (sro.isNumber()) {
                    String u = sro.getScalerUnit().getUnit().getDisplayName();
                    Quantity val = new Quantity(sro.getScaledValue(), DlmsUtils.getUnitFromString(u));
                    //System.out.println("OBIS(sro):" + obisCode.toString() + "=" + ((SimpleRegisterObject) vm).getValueAsString() + " <" + u + ">");
                    return new RegisterValue(obisCode, val, null, date);
                } else {
                    //System.out.println("OBIS(sro):" + obisCode.toString() + "=" + ((SimpleRegisterObject) vm).getValueAsString());
                    return new RegisterValue(obisCode, sro.getValueAsString());
                }
            }
            if (vm instanceof SimpleDataObject) {
                //System.out.println("OBIS(sdo):" + obisCode.toString() + "=" + ((SimpleDataObject) vm).getValueAsString());

                //correct data from A1...
                String data = ((SimpleDataObject) vm).getValueAsString();
                if ((data == null) || (data.length() == 0)) {
                    data = "";
                }
                while ((data.length() > 0) && (data.charAt(0) < 32)) //while (data.startsWith(nullStr))
                {
                    data = data.substring(1);
                }

                return new RegisterValue(obisCode, data);
            }
            throw new NoSuchRegisterException("Class of object for ObisCode " + obisCode.toString()
                    + " is not supported by the protocol");
        } catch (CosemDataAccessException cdaException) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                    + " is not supported by the protocol");
        }
    }

    private RegisterValue getHistoricalRegister(final DlmsHistoricalRegisterDefinition register, final ObisCode obisCode, final Date date)
            throws NoSuchRegisterException {
        RegisterValue result = null;
        try {
            SimpleProfileObject monthlyArchive = getArchive(register.getProfile());

            Calendar currDate = Calendar.getInstance();
            currDate.setTime(date);
            currDate.add(Calendar.MONTH, -obisCode.getF() + 1);
            int year = currDate.get(Calendar.YEAR);
            int month = currDate.get(Calendar.MONTH);

            Calendar fromDate = buildFromDate(year, month, begOfDay);
            Calendar toDate = (Calendar) fromDate.clone();
            toDate.add(Calendar.MONTH, 1);
            long readLines = monthlyArchive.readProfileData(fromDate.getTime(), toDate.getTime(), false, true);

            if (readLines > 0) {
                int tstIndex = monthlyArchive.indexOfCapturedObject(register.getOcDate(), register.getAtDate());
                int valIndex = monthlyArchive.indexOfCapturedObject(register.getOcValue(), register.getAtValue());

                if ((tstIndex < 0) && (valIndex < 0)) {
                    throw new IOException("");
                }

                DlmsDateTime tst = (DlmsDateTime) monthlyArchive.getValue(0, tstIndex);

                Object v = monthlyArchive.getValue(0, valIndex);
                com.elster.dlms.cosem.classes.class03.Unit unit = monthlyArchive.getUnit(valIndex);
                Unit valUnit = DlmsUtils.getUnitFromString(unit.getDisplayName());
                Quantity q = new Quantity((Number) v, valUnit);

                Calendar billingFrom = (Calendar) fromDate.clone();
                billingFrom.add(Calendar.MONTH, -1);

                result = new RegisterValue(obisCode,
                        q,
                        tst.getUtcDate(),
                        billingFrom.getTime(),
                        fromDate.getTime(),
                        date,
                        0,
                        null);
            }

        } catch (IOException e) {
            throw new NoSuchRegisterException("ObisCode " + register.getObisCode() + " is not supported by the protocol");
        } catch (Exception e) {
            protocolLink.getLogger().severe("getHistoricalRegister: " + e.getMessage());
        }

        return result;
    }

    @SuppressWarnings("MagicConstant")
    private Calendar buildFromDate(final int year, final int month, final int hour) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, hour, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private SimpleProfileObject getArchive(com.elster.dlms.types.basic.ObisCode ObisCode)
            throws IOException {
        SimpleProfileObject archive = monthlyProfiles.get(ObisCode.toString());
        if (archive == null) {
            archive = (SimpleProfileObject) objectManager.getSimpleCosemObject(ObisCode);
            monthlyProfiles.put(ObisCode.toString(), archive);

            SimpleRegisterObject bodObject = (SimpleRegisterObject) objectManager.getSimpleCosemObject(
                    new com.elster.dlms.types.basic.ObisCode("7.0.0.9.3.255"));

            Object o = bodObject.getValue();
            if (o instanceof DlmsDataOctetString) {
                DlmsTime t = new DlmsTime(((DlmsDataOctetString) o).getValue());
                begOfDay = t.getHour();
            }
        }
        return archive;
    }


    public String getDescriptionFor(String s) {
        IReadableRegister registerToRead = readableRegisters.forObisCode(s);

        return registerToRead == null ? null : registerToRead.getDescriptor();
    }
}
