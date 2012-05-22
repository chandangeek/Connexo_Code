package com.energyict.genericprotocolimpl.lgadvantis.parser;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.genericprotocolimpl.lgadvantis.*;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.IntervalData;

import java.io.IOException;
import java.util.*;

public class ProfileParser extends AbstractParser implements Parser {

    private TimeZone timeZone;

    public ProfileParser(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void parse(AbstractDataType adt, Task task)
            throws IOException {

        AbstractDataType dt =
                task.getDirectAction(0x6288, XmlTag.RDLMS).getAbstractDataType();
        task.setInterval(dt.intValue());

        List parsingResult = parse(adt, task.getInterval());
        for (Iterator it = parsingResult.iterator(); it.hasNext(); ) {
            IntervalData id = (IntervalData) it.next();
            task.getProfileData().addInterval(id);
        }
    }

    public void parse(byte[] binaryData, Task task) throws IOException {
        AbstractDataType dt =
                task.getDirectAction(0x6288, XmlTag.RDLMS).getAbstractDataType();
        task.setInterval(dt.intValue());

        List parsingResult = parse(binaryData, task.getInterval());
        for (Iterator it = parsingResult.iterator(); it.hasNext(); ) {
            IntervalData id = (IntervalData) it.next();
            task.getProfileData().addInterval(id);
        }
    }

    public List parse(byte[] binaryData, int interval) throws IOException {
        //defer to code of Koen, which is common to both Actaris and Landis
        LoadProfileDecompressor decompressor = new LoadProfileDecompressor(binaryData, timeZone);
        decompressor.deCompress();
        List entries = decompressor.getLoadProfileEntries();
        List result = new ArrayList();
        Calendar calendar = null;
        for (Iterator it = entries.iterator(); it.hasNext(); ) {
            LoadProfileEntry entry = (LoadProfileEntry) it.next();
            if (entry != null) {
                if (entry.getCalendar() != null) {
                    calendar = entry.getCalendar();
                } else {
                    calendar.add(Calendar.SECOND, interval);
                }
                IntervalData id = new IntervalData(calendar.getTime(), entry.getStatus());
                id.addValue(entry.getValue());
                result.add(id);
            }
        }
        return result;
    }

    public List parse(AbstractDataType adt, int interval) throws IOException {
        Array array = (Array) adt.getArray();
        Calendar currentTime = null;
        List result = new ArrayList();

        int pStatus = 0;

        for (int i = 0; i < array.nrOfDataTypes(); i++) {

            Structure structure = (Structure) array.getDataType(i);

            if (!(structure.getDataType(0) instanceof NullData)) {
                currentTime = toCalendar(structure.getDataType(0));
            } else {
                currentTime.add(Calendar.SECOND, interval);
            }

            Unsigned8 state = (Unsigned8) structure.getDataType(1);
            Unsigned32 value = (Unsigned32) structure.getDataType(2);

            if (isIntervalBoundary(currentTime, interval)) {

                pStatus |= state.getValue();
                int eiStatus = StatusCodeProfile.intervalStateBits(pStatus);

                Date cd = currentTime.getTime();
                IntervalData id = new IntervalData(cd, eiStatus, pStatus);

                id.addValue(value.toBigDecimal());
                result.add(id);

                pStatus = 0;        /* clear */

            } else {

                pStatus |= state.getValue();

            }
        }
        return result;
    }

    private boolean isIntervalBoundary(Calendar calendar, int sInterval) {

        int mInterval = sInterval * 1000;
        long remainder = calendar.getTimeInMillis() % mInterval;

        return remainder == 0;

    }

    private Calendar toCalendar(AbstractDataType dataType) throws IOException {

        byte[] ber = dataType.getBEREncodedByteArray();
        DateTime dt = new DateTime(ber, 0, timeZone);

        return dt.getValue();

    }

    private static boolean isProfileCompressed(String deviceSerial) {
        List rtus = MeteringWarehouse.getCurrent().getRtuFactory().findBySerialNumber(deviceSerial);
        for (Iterator it = rtus.iterator(); it.hasNext(); ) {
            Rtu rtu = (Rtu) it.next();
            return isProfileCompressed(rtu);
        }
        return false;
    }

    public static boolean isProfileCompressed(Rtu rtu) {
        String compressed = (String) rtu.getProperties().getProperty(Constant.PK_LOAD_PROFILE_COMPRESSED);
        if (compressed != null && (compressed.equals("1") || compressed.equals("true"))) {
            return true;
        }
        return false;
    }
}
