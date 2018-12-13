package com.elster.protocolimpl.lis100;

import com.elster.protocolimpl.lis100.objects.MeterReading;
import com.elster.protocolimpl.lis100.profile.*;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;

import java.io.IOException;
import java.util.*;

/**
 * Class holding all data to build up a eis channel
 * <p/>
 * User: heuckeg
 * Date: 07.02.11
 * Time: 09:05
 */
@SuppressWarnings({"unused"})
public class ChannelData {

    /* "owner" of the channel data */
    private DeviceData owner;

    /* channel no of channel */
    private int channelNo;

    private int stateRegister;

    private boolean ivUsingFactor;
    private boolean counterUsingFactor;

    private double calcFactor;
    private double cpValue;

    private String unit;

    private MeterReading h1;
    private MeterReading h2;
    private MeterReading h2Bom;

    private String sensorNumber;

    private int interval;
    private RawData ivd = null;

    private Calendar readDate;


    /**
     * Constructor for ChannelData
     *
     * @param deviceData - device where channel resides
     */
    public ChannelData(DeviceData deviceData) {
        owner = deviceData;
    }

    /**
     * reads all the needed data of a channel
     *
     * @throws IOException - in case of io errors
     */
    public void readChannelData() throws IOException {

        Lis100ObjectFactory objectFactory = owner.getObjectFactory();

        // channel number...
        if (owner.getNumberOfChannels() > 1) {
            this.channelNo = objectFactory.getCurrentChannelObject().getIntValue();
        } else {
            this.channelNo = 0;
        }

        stateRegister = objectFactory.getStateRegisterObject().getIntValue();
        System.out.println("-- State register channel " + this.channelNo + " = " + Integer.toHexString(stateRegister));

        sensorNumber = objectFactory.getMeterNoObject().getValue();

        // value type of channel
        String calcType;
        if (owner.isHavingCalcType()) {
            calcType = objectFactory.getCalcTypeObject().getValue();
        } else {
            calcType = "VZ"; // == SU
        }

        ivUsingFactor = calcType.charAt(0) >= 'a';
        counterUsingFactor = calcType.charAt(1) >= 'a';

        this.cpValue = objectFactory.getCpValueObject().getDoubleValue();
        // calcFactor only if needed
        if (ivUsingFactor || counterUsingFactor) {
            this.calcFactor = objectFactory.getCalcFactorObject().getDoubleValue();
        } else {
            calcFactor = 1.0;
        }

        // unit of channel data
        this.unit = objectFactory.getUnitObject().getValue().trim();

        /* "register" data */
        this.h1 = new MeterReading(objectFactory.getTotalCounterObject().getCounterValue(),
                objectFactory.getClockObject().getDate());
        this.h2 = new MeterReading(objectFactory.getProgCounterObject().getCounterValue(),
                objectFactory.getClockObject().getDate());

        int beginOfDay = 6;
        if (owner.isHavingCalcType()) {
            beginOfDay = objectFactory.getBeginOfDay().getIntValue() / 100;
        }
        Calendar cal = GregorianCalendar.getInstance(owner.getTimeZone());
        Date d = objectFactory.getClockObject().getDate();
        cal.setTimeInMillis(d.getTime() - (beginOfDay * 60/*min*/ * 60/*sec*/ * 1000));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, beginOfDay);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.h2Bom = new MeterReading(objectFactory.getCounterBeginOfMonth().getCounterValue(),
                cal.getTime());

        this.interval = objectFactory.getIntervalObject().getIntervalSeconds();
    }


    public int getChannelNo() {
        return channelNo;
    }

    public int getStateRegister() {
        return stateRegister;
    }

    public String getSensorNumber() {
        return sensorNumber;
    }

    public String getUnit() {
        return unit;
    }

    public Unit getEISUnit() {
        return getUnitFromString(unit);
    }

    public int getInterval() {
        return interval;
    }

    public double getCpValue() {
        return cpValue;
    }

    public boolean isIvUsingFactor() {
        return ivUsingFactor;
    }

    public boolean isCounterUsingFactor() {
        return counterUsingFactor;
    }

    public double getCalcFactor() {
        return calcFactor;
    }

    public double getIntervalValueFactor() {
        if (ivUsingFactor) {
            return calcFactor;
        } else {
            return 1 / cpValue;
        }
    }

    public double getMeterReadingFactor() {
        if (counterUsingFactor) {
            return calcFactor;
        } else {
            return 1 / cpValue;
        }
    }

    public RawData getRawData() {
        return ivd;
    }

    public void setRawData(RawData ivd) {
        this.ivd = ivd;
    }

    public Date getReadDate() {
        return readDate.getTime();
    }

    public void setReadDate(Calendar readDate) {
        this.readDate = readDate;
    }

    public MeterReading getH1() {
        return h1;
    }

    public MeterReading getH2() {
        return h2;
    }

    public MeterReading getH2Bom() {
        return h2Bom;
    }

    public ChannelInfo getAsChannelInfo() {

        Unit u = getUnitFromString(unit); /* getValueUnit() */
        return new ChannelInfo(channelNo, "Channel " + channelNo, u);

    }

    /**
     * private method to read interval data of one channel
     *
     * @param from   - start date
     * @param reader - reader class to get interval data
     * @throws java.io.IOException - in case of error
     */
    public void readChannelProfile(Date from, IIntervalDataStreamReader reader) throws IOException {

        ControlCodeData ccd;

        ivd = new RawData();

        reader.prepareRead();

        int ivv;

        // while reading:
        // remember changed cp value / calculation factor / interval

        // break condition:
        // if date of interval value is before from date

        // date is valid at "Begin of day" (partly)
        //                  "Begin of month" (full)
        //                  "Readout start" (full)
        //                  "End of Power fail" (only new interval end!)
        //                  "Time correction backwards"
        //                  "Time correction forward"
        // exceptions:
        // if found "Time correction xxx", no dates from "Begin of day"
        // can be taken until a full date has been found!


        System.out.println(String.format("-- Channel %d: Reading back to: %s", this.getChannelNo(), from.toString()));

        ivd.setDateDirection(-1);
        readDate = null;
        boolean dateInitialized = false;
        Calendar workDate = GregorianCalendar.getInstance(owner.getTimeZone());
        workDate.set(Calendar.SECOND, 0);
        workDate.set(Calendar.MILLISECOND, 0);

        try {
            for (; ; ) {
                ivv = reader.readWord();
                if (ivv >= 0) {
                    ivd.add(ivv);
                }

                /* if found control code, parse code... */
                if (ivv >= 0xFF0) {

                    ivd.setPos(ivd.size() - 1);
                    ccd = ivd.readCCData(workDate, ivv);
                    switch (ivv) {
                        case 0xFFE: /* change of interval */
                            interval = -1; // (Integer)(ccd.getParams())[0];
                            break;

                        case 0xFFC: /* Start of readout */
                            workDate.setTime(ccd.getDate());
                            //System.out.println("-- FFC date " + workDate.getTime());
                            dateInitialized = true;
                            break;

                        case 0xFFB: /* Begin of day */
                            /* only if we have found a previous full date...*/
                            if (ccd.hasDate()) {
                                workDate.setTime(ccd.getDate());
                                //System.out.println("-- FFB date " + workDate.getTime());
                            }
                            break;

                        case 0xFFA: /* Begin of month */
                            workDate.setTime(ccd.getDate());
                            interval = (Integer) (ccd.getParams())[1];
                            dateInitialized = true;
                            //System.out.println("-- FFA date " + workDate.getTime());
                            break;

                        case 0xFF8: /* large encoded control code */
                            switch (ccd.getSubCode()) {
                                case 0x002: /* date correction forwards */
                                    dateInitialized = false;
                                    break;
                                case 0x003: /* new cp value */
                                    cpValue = (Double) (ccd.getParams())[0];
                                    break;
                                case 0x004: /* new factor */
                                    calcFactor = (Double) (ccd.getParams())[0];
                                    break;
                                case 0x00A: /* date correction backwards */
                                    dateInitialized = false;
                            }
                            break;

                        case 0xFF2: /* power fail restart */
                            dateInitialized = false;
                            break;

                        case 0xFF1: /* restart with data lost */
                            ivv = -1;
                            break;
                    }
                }

                if (ivv < 0) {
                    break;
                }

                /* having a valid date? */
                if (dateInitialized) {
                    //if ((readDate == null) || (readDate.getTimeInMillis() != workDate.getTimeInMillis())) {
                    //    System.out.println("current date: " + workDate.getTime());
                    //}
                    readDate = (Calendar) workDate.clone();

                    /* is read data before from (break condition)*/
                    if ((workDate.getTimeInMillis() <= from.getTime()) &&
                            (interval > 0)) {
                        /* then end... */
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }

        System.out.println("---> Read back to: " + workDate.getTime());
    }

    public void clearRawData() {
        ivd.clear();
    }

    /**
     * Convert the given String to the respective {@link Unit}.<br>
     * Implemented units:<br>
     * <li> {@link com.energyict.cbo.BaseUnit#CUBICMETER} <li> {@link com.energyict.cbo.BaseUnit#WATTHOUR} <li>
     * {@link com.energyict.cbo.BaseUnit#WATT} <br>
     * <br>
     * The last two can have a scaler of 3 when 'k' is added in the string
     *
     * @param strUnit - the given strUnit
     * @return the Unit
     */
    private static Unit getUnitFromString(String strUnit) {
        int scaler;
        if (strUnit.equalsIgnoreCase("m3")) {
            return Unit.get(BaseUnit.CUBICMETER);
        } else if (strUnit.equalsIgnoreCase("m3N")) {
            return Unit.get(BaseUnit.NORMALCUBICMETER);
        } else if (strUnit.equalsIgnoreCase("bar")) {
            return Unit.get(BaseUnit.BAR);
        } else if ((strUnit.equalsIgnoreCase("{C"))
                || (strUnit.equalsIgnoreCase("C"))) {
            return Unit.get(BaseUnit.DEGREE_CELSIUS);
        } else if (strUnit.equalsIgnoreCase("K")) {
            return Unit.get(BaseUnit.KELVIN);
        } else if ((strUnit.equalsIgnoreCase("{F"))
                || (strUnit.equalsIgnoreCase("F"))) {
            return Unit.get(BaseUnit.FAHRENHEIT);
        } else if (strUnit.contains("Wh")) {
            scaler = (strUnit.contains("k")) ? 3 : 0;
            return Unit.get(BaseUnit.WATTHOUR, scaler);
        } else if (strUnit.contains("W")) {
            scaler = (strUnit.contains("k")) ? 3 : 0;
            return Unit.get(BaseUnit.WATT, scaler);
        } else if ((strUnit.contains("m3|h")) ||
                (strUnit.contains("m3:h"))) {
            return Unit.get(BaseUnit.CUBICMETERPERHOUR);
        } else if (strUnit.equals("K")) {
            return Unit.get(BaseUnit.KELVIN);
        } else {
            return Unit.getUndefined();
        }
    }
}
