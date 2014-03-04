package com.energyict.protocolimpl.ametek;

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
 *
 * @author cju
 * 
 */
public class JemStar extends Jem implements MessageProtocol  {


	/** Creates a new instance of JemStar */
	public JemStar() {
	}

    @Override
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add(PROP_TIMEOUT);
        result.add(PROP_ECHO_CANCELING);
        result.add(PROP_FORCED_DELAY);
        return result;
    }

    @Override
    public int getProfileInterval() {
        return getInfoTypeProfileInterval(); // this is dependent on device config and therefore not fixed - as we cannot read it out from the device we use value from custom property "ProfileInterval"
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		pd = new ProfileData();

		Calendar calFrom = Calendar.getInstance(getTimeZone());
		calFrom.setTime(from);
		Calendar calTo = Calendar.getInstance(getTimeZone());

		int dateRangeCmd = 0xff;
		int dateRng = Calendar.getInstance(getTimeZone()).get(Calendar.DAY_OF_YEAR)-calFrom.get(Calendar.DAY_OF_YEAR);
		if(dateRng<45)
			dateRangeCmd = dateRng;
		if(to==null)
			to = new Date();

		calTo.setTime(to);

		byte[] send = new byte[]{(byte)getInfoTypeNodeAddressNumber(),0x44,0x01,0x10,0x02,(byte)dateRangeCmd,0x10,0x03};
        InputStream dataInStream = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));

		processHeader(dataInStream);
		processInterval(dataInStream, calFrom, calTo);

		return pd;
	}

    protected void processInterval(InputStream byteStream, Calendar cal, Calendar calTo) throws IOException {
        boolean isEvent = false;
        int eiStatus = 0;
        byteStream.skip(99);
        int len = 2;
        int eventIndicator = 0x8000;
        int intervalIndicator = 0x4000;
        int powerOutEvent = 0x800;
        int testModeEvent = 0x100;
        Date startDate = cal.getTime();
        Date now = calTo.getTime();
        cal.setTimeInMillis(0);
        boolean noDate = true;
        long startTime = cal.getTimeInMillis();
        ArrayList dataList = new ArrayList();
        Date lastDate = null;
        List partialVals = new ArrayList();

        ParseUtils.roundDown2nearestInterval(cal, getProfileInterval());
        while (byteStream.available() > 0) {
            List values = new ArrayList();

            for (int i = 0; i < channelCount; i++) {
                long val = convertHexToLongLE(byteStream, len);
                if ((val & eventIndicator) == eventIndicator) {
                    isEvent = true;
                    val = val ^ eventIndicator;
                }
                BigDecimal bd;
                if (partialVals.size() > 0)
                    bd = (BigDecimal) partialVals.remove(0);
                else
                    bd = new BigDecimal(0);
                values.add(bd.add(new BigDecimal(val)));

            }

            if (isEvent) {
                partialVals = new ArrayList(values);
                isEvent = false;
                long eventCode = convertHexToLongLE(byteStream, len);
                startTime = convertHexToLongLE(byteStream, 4);

                if (noDate) {
                    //on the first event set the time
                    startTime *= 1000l;
                    startTime -= getTimeZone().getOffset(startTime);
                    noDate = false;
                    cal.setTimeInMillis(startTime);
                    ParseUtils.roundUp2nearestInterval(cal, getProfileInterval());
                }

                long endTime = convertHexToLongLE(byteStream, 4);
                endTime *= 1000l;
                endTime -= getTimeZone().getOffset(endTime);

                if (((eventCode & powerOutEvent) == powerOutEvent) || ((eventCode & testModeEvent) == testModeEvent)) //powerOutage
                {
                    if (endTime < cal.getTimeInMillis()) {
                        //Power up power down happens inside the interval
                        eiStatus = IntervalStateBits.POWERDOWN | IntervalStateBits.POWERUP;
                        continue;
                    } else {
                        //save values now with power down, then jump cal to power up interval
                        eiStatus = IntervalStateBits.POWERDOWN;
                        IntervalData id = new IntervalData(cal.getTime(), eiStatus);
                        id.addValues(values);
                        addInterval(cal, id, startDate, now);
                        partialVals = new ArrayList();
                        cal.setTimeInMillis(endTime);
                        ParseUtils.roundUp2nearestInterval(cal, getProfileInterval());
                        eiStatus = IntervalStateBits.POWERUP;
                        continue;
                    }
                } else if ((eventCode & eventIndicator) == eventIndicator) //midnight
                {
                    partialVals = new ArrayList();
                } else {
                    continue;
                }
            }

            if (noDate) {
                dataList.add(values);
            } else {
                if (dataList.size() > 0) {
                    Calendar c = (Calendar) cal.clone();
                    c.setTimeInMillis(startTime);
                    processList(dataList, c, startDate, now);
                    dataList = new ArrayList();
                }
                if (cal.getTime().after(startDate) && cal.getTime().before(now)) {
                    IntervalData id = new IntervalData(cal.getTime(), eiStatus);
                    id.addValues(values);
                    addInterval(cal, id, startDate, now);
                }
                lastDate = cal.getTime();
                cal.add(Calendar.SECOND, getProfileInterval());
                eiStatus = 0;
            }
        }
    }

    private void processHeader(InputStream byteStream) throws IOException {
        time = new Date(convertHexToLongLE(byteStream, 4) * 1000);
        channelCount = (int) convertHexToLongLE(byteStream, 1);

        if (getProfileInterval() <= 0) {
            throw new IOException("load profile interval must be > 0 sec. (is " + getProfileInterval() + ")");
        }

        for (int i = 0; i < channelCount; i++) {
            pd.addChannel(new ChannelInfo(i, i, "JemStarChannel_" + i, Unit.get(BaseUnit.UNITLESS)));
        }

    }

    private void addInterval(Calendar cal, IntervalData id, Date startDate, Date now) {
        if (cal.getTime().after(startDate) && cal.getTime().before(now)) {
            pd.addInterval(id);
        }
    }

    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
	 *******************************************************************************************/

    protected void retrieveRegisters() throws IOException {
        registerValues = new HashMap();

        int dateRangeCmd = 0xff;

        //FREEZE REGISTERS BEFORE READ
        byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x4C, 0x01, 0x10, 0x02, 0x10, 0x03};
        InputStream dataInStream = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));

        int inval = dataInStream.read();
        if (inval != 6) {
            getLogger().warning("Failed to freeze registers");
        } else {
            getLogger().info("Registers frozen successfully");
        }

        //READ REGULAR REGISTERS
        send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x52, 0x02, 0x10, 0x02, (byte) dateRangeCmd, 0x10, 0x03};
        dataInStream = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        processRegisters(dataInStream, REGULAR);

        //READ ALTERNATE REGISTERS
        send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x52, 0x03, 0x10, 0x02, (byte) dateRangeCmd, 0x10, 0x03};
        dataInStream = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        processAlternateRegisters(dataInStream, ALTERNATE);
    }

    protected void processRegisters(InputStream byteStream, int obisCValue) throws IOException {
        int startOffset = 0;
        int len = 2;
        int pos = startOffset;

        channelCount = (int) convertHexToLongLE(byteStream, 1);
        byteStream.skip(3);
        pos += 4;//1 eaten during channel count

        while (byteStream.available() > 0) {
            for (int i = 0; i < channelCount; i++) {
                long val = convertHexToLongLE(byteStream, 4);

                float f = -1;
                Date tstamp = null;
                RegisterValue rv = null;

                pos += 4;//eaten duting val

                byteStream.skip(4);
                pos += 4;

                int registerNumber = (int) convertHexToLongLE(byteStream, len);

                pos += 2;//eaten during registerNumber

                byteStream.skip(3);
                pos += 3;

                int type = byteStream.read();

                pos++;//eaten during type

                String s = "";
                for (int ii = 0; ii < 20; ii++) {
                    int iii = byteStream.read();
                    if (iii > 0) {
                        s += (char) ((byte) iii);
                    }
                    //s+=(char) Byte.parseByte(byteList.get(pos).toString());

                    pos++;
                }

                ObisCode ob = new ObisCode(1, registerNumber, obisCValue, 0, 0, 0);

                switch (type) {
                    case 0:
                        f = Float.intBitsToFloat((int) val);
                        rv = new RegisterValue(ob, new Quantity(new BigDecimal(f), Unit.getUndefined()), new Date(), null, new Date(), time);
                        break;
                    case 1:
                    case 2:
                        tstamp = new Date(val * 1000);
                        if (s.trim().equals("Present Time")) {
                            time = tstamp;
                        }
                        break;
                    case 3:
                        rv = new RegisterValue(ob, new Quantity(new BigDecimal(val), Unit.getUndefined()), null, null, null, new Date(), 0, s);
                        break;
                    case 4:
                        rv = new RegisterValue(ob, new Quantity(new BigDecimal(val), Unit.getUndefined()));
                }
                if (rv != null) {
                    registerValues.put(ob.toString(), rv);
                }
            }
        }
    }

    protected void processAlternateRegisters(InputStream byteStream, int obisCValue) throws IOException {
        int startOffset = 0;
        int len = 2;
        int pos = startOffset;
        Date billingDate = null;
        RegisterValue tempRegisterValue = null;

        int channelCount = (int) convertHexToLongLE(byteStream, 1);
        byteStream.skip(3);
        pos += 4;//1 eaten during channel count

        while (byteStream.available() > 0) {
            for (int i = 0; i < channelCount; i++) {
                long val = convertHexToLongLE(byteStream, 4);

                float f = -1;
                Date tstamp = null;
                RegisterValue rv = null;

                pos += 4;//eaten duting val

                byteStream.skip(4);
                pos += 4;

                int registerNumber = (int) convertHexToLongLE(byteStream, len);

                pos += 2;//eaten during registerNumber

                byteStream.skip(3);
                pos += 3;

                int type = byteStream.read();

                pos++;//eaten during type

                String s = "";
                for (int ii = 0; ii < 20; ii++) {
                    int iii = byteStream.read();
                    if (iii > 0) {
                        s += (char) ((byte) iii);
                    }
                    //s+=(char) Byte.parseByte(byteList.get(pos).toString());

                    pos++;
                }

                ObisCode ob = new ObisCode(1, registerNumber, obisCValue, 0, 0, 0);

                switch (type) {
                    case 0:
                        f = Float.intBitsToFloat((int) val);
                        rv = new RegisterValue(ob, new Quantity(new BigDecimal(f), Unit.getUndefined()), null, billingDate);
                        tempRegisterValue = rv;
                        break;
                    case 1:
                    case 2:
                        tstamp = new Date(val * 1000);
                        if (s.trim().equals("Last BPR Time")) {
                            billingDate = tstamp;
                        } else if (s.contains("TPkD,Time:")) {
                            registerValues.remove(tempRegisterValue.getObisCode().toString());
                            registerValues.put(tempRegisterValue.getObisCode().toString(), new RegisterValue(tempRegisterValue.getObisCode(), tempRegisterValue.getQuantity(), tstamp, billingDate));
                        }
                        break;
                    case 3:
                        rv = new RegisterValue(ob, new Quantity(new BigDecimal(val), Unit.getUndefined()), null, billingDate);
                        break;
                    case 4:
                        rv = new RegisterValue(ob, new Quantity(new BigDecimal(val), Unit.getUndefined()), null, billingDate);
                }
                if (rv != null) {
                    registerValues.put(ob.toString(), rv);
                }
            }

        }
    }

    public int getNumberOfChannels() throws IOException {
        if (this.channelCount == 0) {
            int inval = 0;
            byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x06, 0x01, 0x10, 0x02, 0x10, 0x03};
            ByteArrayInputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
            bais.skip(27);
            inval = bais.read();
            if (!((inval & 0x80) == 0x80)) {
                this.channelCount = 12;
            } else if (!((inval & 0x40) == 0x40)) {
                this.channelCount = 4;
            } else {
                this.channelCount = 12;
            }
        }
        return this.channelCount;
    }

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

        getLogger().info("Setting time to " + cal.getTime());
        byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x54, 0x05, 0x10, 0x02,
                (byte) (yy / 10), (byte) (yy % 10),
                (byte) (mm / 10), (byte) (mm % 10),
                (byte) (dd / 10), (byte) (dd % 10),
                (byte) (hh / 10), (byte) (hh % 10),
                (byte) (mn / 10), (byte) (mn % 10),
                (byte) (ss / 10), (byte) (ss % 10),
                (byte) (w), 0x10, 0x03};
        ByteArrayInputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));

        int inval = 0;
        inval = bais.read();
        if (inval != 6) {
            throw new IOException("Failed to set time");
        }
        getLogger().info("Set time successful");
    }

    @Override
    public String getProtocolDescription() {
        return "Ametek Power JemStar";
    }

    public String getProtocolVersion() {
        return "$Date: 2014-01-15 16:39:12 +0100 (wo, 15 jan 2014) $";
    }

    public String getFirmwareVersion() throws IOException {
        String instr = "";
        byte[] send = new byte[]{(byte) getInfoTypeNodeAddressNumber(), 0x06, 0x01, 0x10, 0x02, 0x10, 0x03};
        ByteArrayInputStream bais = new ByteArrayInputStream(getConnection().sendRequestAndReceiveResponse(send));
        bais.skip(4);
        for (int i = 0; i < 8; i++) {
            instr += String.valueOf((char) bais.read());
        }

        return instr;
    }
}
