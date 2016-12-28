package com.energyict.protocolimpl.ametek;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author cju
 */
@Deprecated     //Protocol was never released, only kept as a technical class
public abstract class Jem10 extends Jem implements MessageProtocol {

    public Jem10(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        pd = new ProfileData();

        Calendar calFrom = Calendar.getInstance(getTimeZone());
        calFrom.setTime(from);
        Calendar calTo = Calendar.getInstance(getTimeZone());

        int dateRangeCmd = 0xff;
        int dateRng = Calendar.getInstance(getTimeZone()).get(Calendar.DAY_OF_YEAR) - calFrom.get(Calendar.DAY_OF_YEAR);
        if (dateRng < 45) {
            dateRangeCmd = dateRng;
        }
        if (to == null) {
            to = new Date();
        }

        calTo.setTime(to);

        byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x44, 0x01, 0x10, 0x02, (byte) dateRangeCmd, 0x10, 0x03};
        ByteArrayInputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));

        processHeader(bais);
        processInterval(bais, calFrom, calTo);
        return pd;
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return "1.register number.0.0.0.255 Misc";
    }

    private void processInterval(InputStream byteStream, Calendar cal, Calendar calTo) throws IOException {
        int eventVal = 0;
        int len = 2;
        int eventIndicator = 0x8000;
        int intervalIndicator = 0x4000;
        int powerOutEvent = 0x40;
        int eiStatus = 0;
        Date startDate = cal.getTime();
        Date now = calTo.getTime();
        cal.setTimeInMillis(0);
        boolean noDate = true;
        Date startTime = cal.getTime();
        List<List<BigDecimal>> dataList = new ArrayList<>();
        List<BigDecimal> partialVals = new ArrayList<>();

        ParseUtils.roundDown2nearestInterval(cal, getProfileInterval());

        long val = convertHexToLong(byteStream, len);

        while (byteStream.available() > 0) {
            List<BigDecimal> values = new ArrayList<>();

            boolean readMore = false;
            if ((val & eventIndicator) == eventIndicator) {
                eventVal = (int) (val & 0xff);
                readMore = true;
            }
            for (int i = 0; i < channelCount; i++) {
                if (readMore) {
                    val = convertHexToLong(byteStream, len);
                } else {
                    readMore = true;
                }

                if ((val & intervalIndicator) == intervalIndicator) {
                    val = val ^ intervalIndicator;
                }
                BigDecimal bd;
                if (!partialVals.isEmpty()) {
                    bd = partialVals.remove(0);
                } else {
                    bd = new BigDecimal(0);
                }
                values.add(bd.add(new BigDecimal(val)));
            }

            if (eventVal > 0) {
                partialVals = new ArrayList<>(values);
                try {
                    startTime = getShortDateFormatter().parse(convertHexToString(byteStream, 5, true));

                    if (noDate) {
                        //on the first event set the time
                        noDate = false;
                        cal.setTime(startTime);
                        ParseUtils.roundUp2nearestInterval(cal, getProfileInterval());
                    }

                    Date endTime = null;
                    if (byteStream.available() > 0 && !((((val = convertHexToLong(byteStream, len)) & intervalIndicator) == intervalIndicator) //)){
                            || (((val) & eventIndicator) == eventIndicator))) {
                        String s = Long.toHexString(val);
                        while (s.length() < 4) {
                            s = "0" + s;
                        }
                        s += convertHexToString(byteStream, 3, true);
                        endTime = getShortDateFormatter().parse(s);
                        val = convertHexToLong(byteStream, len);
                    }

                    if ((eventVal & powerOutEvent) == powerOutEvent) { //powerOutage
                        eventVal = 0;
                        if (endTime.before(cal.getTime())) {
                            //Power up power down happens inside the interval
                            eiStatus = IntervalStateBits.POWERDOWN | IntervalStateBits.POWERUP;
                            continue;
                        } else {
                            //save values now with power down, then jump cal to power up interval
                            eiStatus = IntervalStateBits.POWERDOWN;
                            IntervalData id = new IntervalData(cal.getTime(), eiStatus);
                            id.addValues(values);
                            pd.addInterval(id);
                            partialVals = new ArrayList<>();
                            cal.setTime(endTime);
                            ParseUtils.roundUp2nearestInterval(cal, getProfileInterval());
                            eiStatus = IntervalStateBits.POWERUP;
                            continue;
                        }
                    } else if ((eventVal & 0x80) == 0x80) { //midnight
                        eventVal = 0;
                    } else {
                        eventVal = 0;
                        continue;
                    }

                } catch (Exception e) {
                    new IOException(e.getMessage());
                }

                eventVal = 0;
            } else if (byteStream.available() > 0) {
                val = convertHexToLong(byteStream, len);
            }

            if (noDate) {
                dataList.add(values);
                partialVals = new ArrayList<>();
            } else {
                partialVals = new ArrayList<>();
                if (!dataList.isEmpty()) {
                    Calendar c = (Calendar) cal.clone();
                    c.setTime(startTime);
                    processList(dataList, c, startDate, now);
                    dataList = new ArrayList();
                }
                if (cal.getTime().getTime() >= startDate.getTime() && cal.getTime().before(now)) {
                    IntervalData id = new IntervalData(cal.getTime(), eiStatus);
                    id.addValues(values);
                    pd.addInterval(id);
                }
                cal.add(Calendar.SECOND, getProfileInterval());
                eiStatus = 0;
            }
        }
    }

    private void processHeader(InputStream byteStream) throws IOException {
        try {
            time = getDateFormatter().parse(convertHexToString(byteStream, 12, false));
            byteStream.read(); //Eat day of week byte
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }

        channelCount = (int) convertHexToLong(byteStream, 1);

        if (getProfileInterval() <= 0) {
            throw new IOException("load profile interval must be > 0 sec. (is " + getProfileInterval() + ")");
        }

        for (int i = 0; i < channelCount; i++) {
            pd.addChannel(new ChannelInfo(i, i, "JemStarChannel_" + i, Unit.get(BaseUnit.UNITLESS)));
        }
    }

    private void processRegisters(InputStream byteStream, int obisCValue) throws IOException {
        int startOffset = 0;
        int len = 2;
        int pos = startOffset;

        while (byteStream.available() > 0) {
            int registerNumber = (int) convertHexToLongLE(byteStream, len);

            pos += len;

            int bt = byteStream.read();
            byteStream.skip(3);

            pos += 4;

            long val = convertHexToLongLE(byteStream, 4);

            pos += 4;

            RegisterValue rv = null;

            ObisCode ob = new ObisCode(1, registerNumber, obisCValue, 0, 0, 0);


            int type = 0;

            if ((bt & 0x80) == 0x80) {
                type = 1;
            }

            switch (type) {
                case 0:
                    float f = val;
                    if ((bt & 4) == 4 || (bt & 1) == 1) {
                        f *= .1;
                    } else if ((bt & 8) == 8 || (bt & 2) == 2) {
                        f *= .01;
                    } else if ((bt & 12) == 12 || (bt & 3) == 3) {
                        f *= .001;
                    }
                    rv = new RegisterValue(ob, new Quantity(new BigDecimal(f), Unit.getUndefined()), null, null, time);
                    break;
                case 1:
                    Calendar cal = Calendar.getInstance(getTimeZone());
                    cal.set(1990, 0, 1, 0, 0, 0);
                    Date tstamp = new Date(val * 1000 - getTimeZone().getOffset(val * 1000)); // Warning: val is number of seconds since midnight 1970, expressed in local timezone - so should convert to regular EPOCH first!
                    time = tstamp;
                    break;
            }

            if (rv != null) {
                registerValues.put(ob.toString(), rv);
            }
        }

    }

    private String convertHexToString(InputStream byteStream, int length, boolean pad) throws IOException {
        String instr = "";
        for (int i = 0; i < length; i++) {
            int inval = byteStream.read();
            String zeropad = "";
            if (pad && Integer.toHexString(inval & 0xff).length() < 2) {
                zeropad = "0";
            }
            instr += zeropad + Integer.toHexString(inval & 0xff);
        }

        return instr;
    }

    @Override
    protected void retrieveRegisters() throws IOException {
        registerValues = new HashMap();

        int dateRangeCmd = 0xff;

        //FREEZE REGISTERS BEFORE READ
        byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x4C, 0x01, 0x10, 0x02, 0x10, 0x03};
        InputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        int inval = bais.read();
        if (inval != 6) {
            getLogger().warning("Failed to freeze regiser");
        }
        getLogger().info("Registers frozen successfully");

        send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x52, 0x06, 0x10, 0x02, (byte) dateRangeCmd, 0x10, 0x03};
        bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        processRegisters(bais, REGULAR);

        send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x52, 0x07, 0x10, 0x02, (byte) dateRangeCmd, 0x10, 0x03};
        bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        processRegisters(bais, ALTERNATE);

    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (this.channelCount == 0) {
            byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x56, 0x08, 0x10, 0x02, 0x10, 0x03};
            ByteArrayInputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
            bais.skip(8);
            this.channelCount = bais.read();
        }
        return this.channelCount;
    }

    @Override
    public Date getTime() throws IOException {
        String instr = "";
        byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x54, 0x02, 0x10, 0x02, 0x10, 0x03};
        ByteArrayInputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        for (int i = 0; i < 12; i++) {
            instr += Integer.toHexString(bais.read());
        }

        try {
            Date date = getDateFormatter().parse(instr);
            return date;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }


    @Override
    public void setTime() throws IOException {
        Calendar cal = Calendar.getInstance(getTimeZone());
        int yy = cal.get(Calendar.YEAR) % 100;
        //Month starts with 1 on meter, 0 in java (for Jan)
        int mm = cal.get(Calendar.MONTH) + 1;
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        int hh = cal.get(Calendar.HOUR_OF_DAY);
        int mn = cal.get(Calendar.MINUTE);
        int ss = cal.get(Calendar.SECOND);
        int w = cal.get(Calendar.DAY_OF_WEEK);

        byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x54, 0x05, 0x10, 0x02,
                (byte) (yy / 10), (byte) (yy % 10),
                (byte) (mm / 10), (byte) (mm % 10),
                (byte) (dd / 10), (byte) (dd % 10),
                (byte) (hh / 10), (byte) (hh % 10),
                (byte) (mn / 10), (byte) (mn % 10),
                (byte) (ss / 10), (byte) (ss % 10),
                (byte) (w), 0x10, 0x03};

        getLogger().info("Setting time to " + cal.getTime());
        byte[] check = connection.getCheckSumBytes(send, send.length);
        ByteArrayInputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        int inval = 0;

        inval = bais.read();
        if (inval != 6) {
            throw new IOException("Failed to set time");
        }
        getLogger().info("Set time successful");
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-01-15 16:39:12 +0100 (wo, 15 jan 2014) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        String instr = "";
        byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x06, 0x01, 0x10, 0x02, 0x10, 0x03};
        ByteArrayInputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        bais.skip(15);
        for (int i = 0; i < 8; i++) {
            instr += String.valueOf((char) bais.read());
        }
        return instr;
    }

}