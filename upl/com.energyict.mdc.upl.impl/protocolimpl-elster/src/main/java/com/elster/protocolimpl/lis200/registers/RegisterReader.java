/**
 *
 */
package com.elster.protocolimpl.lis200.registers;

import com.elster.protocolimpl.lis200.LIS200Utils;
import com.elster.protocolimpl.lis200.Lis200Value;
import com.elster.protocolimpl.lis200.objects.*;
import com.elster.protocolimpl.lis200.utils.RawArchiveLine;
import com.elster.protocolimpl.lis200.utils.RawArchiveLineInfo;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static com.elster.protocolimpl.lis200.utils.utils.splitLine;

/**
 * Basic functionality for Register reading support
 *
 * @author gna
 * @since 22-mrt-2010
 *        <p/>
 *        modified: 6/8/2010, gh historical register support removed
 */
@SuppressWarnings({"unused"})
public class RegisterReader {

    /* "historical" register interface */
    private final IRegisterReadable registerReadable;
    /* register information */
    private final RegisterMapN registers;
    /* cache for historical archive data */
    private HashMap<Integer, HistoricalArchive> cache;
    /* Interface to get archive line split information */
    /* IHistoricArchiveInfo */

    /**
     * Constructor with the {@link com.energyict.protocolimpl.iec1107.ProtocolLink}
     *
     * @param registerReadable - interface to retrive data for register reading
     * @param registers - an array of Lis200Register
     */
    public RegisterReader(IRegisterReadable registerReadable, RegisterMapN registers) {
        this.registerReadable = registerReadable;
        this.registers = registers;
        this.cache = new HashMap<Integer, HistoricalArchive>();
    }

    /**
     * Read the {@link com.energyict.protocol.RegisterValue} from the device
     *
     * @param obisCode - the obisCode to read
     * @param tst      - time stamp to work with
     * @return the {@link com.energyict.protocol.RegisterValue}
     * @throws java.io.IOException - in case of errors
     */
    public RegisterValue getRegisterValue(ObisCode obisCode, Date tst) throws IOException {

        RegisterValue result;

        Lis200RegisterN reg = registers.forObisCode(obisCode);
        if (reg == null) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                    + " is not supported by the protocol");
        }

        SimpleObject object = reg.getObject();

        try {
            if (object instanceof MaxDemandObject) {
                MaxDemandObject mdo = (MaxDemandObject) object;

                int bod = registerReadable.getBeginOfDay();

                Lis200Value val = mdo.getLis200Value();

                Calendar maxDate = mdo.getMaxCalendar();
                result = new RegisterValue(obisCode,
                        val.toQuantity(),
                        maxDate.getTime(),
                        getBillingFrom(maxDate, bod).getTime(),
                        getBillingTo(maxDate, bod).getTime(),
                        tst, 0, null);
            } else if (object instanceof StatusObject) {
                StatusObject so = (StatusObject) object;
                String value = Integer.toString(so.getStatusInt());
                Quantity quantity = new Quantity(value, Unit.getUndefined());
                result = new RegisterValue(obisCode, quantity, null, tst); //, null, tst, 0);
            } else if (object instanceof HistoricalValueObject) {
                HistoricalValueObject hvo = (HistoricalValueObject) object;
                int archive = hvo.getArchiveInstance();

                // get archive where value is stored
                HistoricalArchive ha = getHistoricalArchive(archive);
                if (ha == null) {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                            + " is not supported by the device");
                }

                // compute date to get archive line
                Calendar cal = Calendar.getInstance(registerReadable.getTimeZone());
                cal.setTime(tst);
                cal.add(Calendar.MONTH, -obisCode.getF() + 1);

                // get archive line
                String archiveLine = ha.getArchiveLine(cal);
                if ((archiveLine == null) || (archiveLine.length() == 0)) {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                            + " is not supported by the device");
                }

                // get values of archive line
                RawArchiveLine ral;
                try {
                    ral = new RawArchiveLine(hvo.getArchiveLineInfo(), archiveLine);
                } catch (ParseException pe) {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
                            + " is not supported by the device");
                }

                Date d = ral.getTimeStampUtc(registerReadable.getTimeZone());
                BigDecimal v = ral.getValue(0);

                String[] units = splitLine(ha.getUnits());
                int index = hvo.getArchiveLineInfo().getValueColumn(0);
                Unit unit = LIS200Utils.getUnitFromString(units[index]);
                Quantity val = new Quantity(v, unit);

                Date billTo = null;
                Date billFrom = null;
                try {
                    ral = new RawArchiveLine(new RawArchiveLineInfo(",,TST"), archiveLine);
                    billTo = ral.getTimeStampUtc(registerReadable.getTimeZone());
                    cal.setTime(billTo);
                    cal.add(Calendar.MONTH, -1);
                    billFrom = cal.getTime();
                } catch (ParseException ignored) {
                }

                result = new RegisterValue(obisCode,
                        val,
                        d,
                        billFrom,
                        billTo,
                        tst, 0, null);
            } else {
                String value = object.getValue();
                try {
                    Lis200Value val = new Lis200Value(value);
                    result = new RegisterValue(obisCode, val.toQuantity(), null, tst); //, null, tst, 0);
                } catch (Exception e) {
                    result = new RegisterValue(obisCode, null, null, null, null,
                            tst, 0, value);
                }
            }
        } catch (NoSuchRegisterException nsre) {
            throw new NoSuchRegisterException(nsre.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(
                    "Failed while reading register with ObisCode " + obisCode);
        }
        return result;
    }

    private HistoricalArchive getHistoricalArchive(int archive) {
        if (!cache.containsKey(archive)) {
            HistoricalArchive ha = registerReadable.getHistoricalArchive(archive);
            if (ha != null) {
                cache.put(archive, ha);
            }
            return ha;
        } else {
            return cache.get(archive);
        }
    }

    public Lis200RegisterN getRegister(ObisCode obisCode) {
        return registers.forObisCode(obisCode);
    }

    private Calendar getBillingFrom(Calendar date, int bodValue) throws IOException {

        Calendar bd = (Calendar) date.clone();

        /* correct date by begin of day */
        bd.add(Calendar.HOUR_OF_DAY, -bodValue);
        bd.add(Calendar.MINUTE, -1);

        /* now set values for begin of month */
        bd.set(Calendar.DAY_OF_MONTH, 1);
        bd.set(Calendar.HOUR_OF_DAY, bodValue);
        bd.set(Calendar.MINUTE, 0);
        bd.set(Calendar.SECOND, 0);
        bd.set(Calendar.MILLISECOND, 0);

        return bd;
    }

    public Calendar getBillingTo(Calendar date, int bodValue) throws IOException {

        Calendar bd = (Calendar) date.clone();

        /* correct date by begin of day */
        bd.add(Calendar.HOUR_OF_DAY, -bodValue);
        bd.add(Calendar.MINUTE, -1);

        /* now set values for begin of month */
        bd.set(Calendar.DAY_OF_MONTH, 1);
        bd.set(Calendar.HOUR_OF_DAY, bodValue);
        bd.set(Calendar.MINUTE, 0);
        bd.set(Calendar.SECOND, 0);
        bd.set(Calendar.MILLISECOND, 0);

        /* and now add a month to get end date */
        bd.add(Calendar.MONTH, 1);

        return bd;
    }

}
