package com.elster.protocolimpl.dsfg.register;

import com.elster.protocolimpl.dsfg.Dsfg;
import com.elster.protocolimpl.dsfg.DsfgUtils;
import com.elster.protocolimpl.dsfg.objects.ClockObject;
import com.elster.protocolimpl.dsfg.objects.SimpleObject;
import com.elster.protocolimpl.dsfg.telegram.DataBlock;
import com.elster.protocolimpl.dsfg.telegram.DataElement;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * User: Gunter
 * Date: 17.11.11
 * Time: 10:15
 */
public class DsfgRegisterReader {

    private Dsfg dsfg;

    /**
     * Constructor for RegisterReader
     *
     * @param dsfg - reference to owner class
     */
    public DsfgRegisterReader(Dsfg dsfg) {
        this.dsfg = dsfg;
    }

    /**
     * function to retrieve a "register" value
     *
     * @param obisCode - "address" for value (coded)
     * @param tst      - read date of register value
     * @return read value if successful, null if not
     */
    public RegisterValue getRegisterValue(ObisCode obisCode, Date tst) {

        RegisterValue result = null;

        String instance = getInstance(obisCode);

        String address = buildRegisterAddress(obisCode, "d");

        try {
            Date date;
            if (isHistoricalValue(obisCode)) {
                // calculate date of value to retrieve
                date = calculateValueDate(obisCode, dsfg.getTimeZone());
                Long from = ClockObject.calendarToRaw(date, dsfg.getTimeZone());

                // get value from archive
                DataElement de = new DataElement(address,
                        null,
                        from,
                        null,
                        null);

                DataBlock db = new DataBlock(instance, 'A', 'J', 'Z',
                        new DataElement[]{de, de});
                DataBlock in = dsfg.getDsfgConnection().sendRequest(db);

                Object o = in.getElementAt(0).getValue();

                BigDecimal v = null;
                if (o != null) {
                    if (o instanceof Double) {
                        v = new BigDecimal((Double) o);
                    }
                    if (o instanceof Long) {
                        v = new BigDecimal((Long) o);
                    }
                }
                if (v == null) {
                    return null;
                }

                // no error so far, get unit of value
                SimpleObject unitReader = new SimpleObject(dsfg, buildRegisterAddress(obisCode, "f"));
                String unitString = unitReader.getValue();
                Unit unit = DsfgUtils.getUnitFromString(unitString);

                // combine value and unit to a register value
                BigDecimal v2 = v.setScale(6, BigDecimal.ROUND_HALF_UP);
                Quantity val = new Quantity(v2, unit);
                result = new RegisterValue(obisCode,
                        val,
                        date,
                        null,
                        null,
                        tst, 0, null);
            } else {
                DataBlock db = new DataBlock(instance, 'A', 'J', 'M', address);
                DataBlock in = dsfg.getDsfgConnection().sendRequest(db);

                Object value = in.getElementAt(0).getValue();
                if (value instanceof Number) {
                    Quantity val = new Quantity((Number) value, Unit.getUndefined());
                    result = new RegisterValue(obisCode, val, null, tst);
                } else {
                    String v = value.toString();
                    result = new RegisterValue(obisCode, null, null, null, null,
                            tst, 0, v);
                }
            }
        } catch (IOException ignore) {
        }
        return result;
    }

    private String getInstance(ObisCode obisCode) {
        if (obisCode.getA() == 0) {
            return dsfg.getRegistrationInstance().toUpperCase();
        } else {
            return valueToLetter(obisCode.getA()).toUpperCase();
        }
    }

    private String buildRegisterAddress(ObisCode obisCode, String subAddress) {

        String address;
        if (isHistoricalValue(obisCode)) {
            address = "ca";
            if (obisCode.getC() == 0) {
                address += dsfg.getArchiveInstance() + valueToLetter(obisCode.getD()) + subAddress;
            } else {
                address += valueToLetter(obisCode.getC()) + valueToLetter(obisCode.getD()) + subAddress;
            }
        } else {
            address = valueToLetter(obisCode.getB()) + valueToLetter(obisCode.getC()) +
                    valueToLetter(obisCode.getD());
            if (obisCode.getE() > 0) {
                address += valueToLetter(obisCode.getE());
            }
            if (obisCode.getF() > 0) {
                address += valueToLetter(obisCode.getF());
            }
        }
        return address;
    }

    private boolean isHistoricalValue(ObisCode obisCode) {
        return obisCode.getB() == 0;
    }

    private Date calculateValueDate(ObisCode obisCode, TimeZone devTimeZone) {

        Calendar cDate = Calendar.getInstance(devTimeZone);

        cDate.set(Calendar.MILLISECOND, 0);
        cDate.set(Calendar.SECOND, 0);
        cDate.set(Calendar.MINUTE, 0);
        cDate.set(Calendar.HOUR_OF_DAY, obisCode.getE());

        if (obisCode.getF() < 100) {
            cDate.set(Calendar.DAY_OF_MONTH, 1);
            cDate.add(Calendar.MONTH, -obisCode.getF());
        } else {
            cDate.add(Calendar.DAY_OF_MONTH, -(obisCode.getF() - 100));
        }

        return cDate.getTime();
    }

    private String valueToLetter(int n) {
        // 'a' = 97
        if ((n < 101) || (n > 127)) {
            //throw new Exception("ConvertError");
        }
        return "" + (char) (n - 4);
    }
}
