package com.elster.protocolimpl.lis100.profile;

import com.elster.protocolimpl.lis100.ChannelData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MeterEvent;

import java.util.*;


/**
 * class to process lis100 raw data
 * <p/>
 * User: heuckeg
 * Date: 07.02.11
 * Time: 11:41
 */
public class Lis100Processing {

    private static final int CPVALUE = 0;
    private static final int CALCFAC = 1;

    private ChannelData channelData;

    private long interval;
    private double ivFactor;
    private double mrFactor;

    private ArrayList<DataElement> pivd;
    private ArrayList<MeterEvent> events;

    public Lis100Processing(ChannelData channelData) {
        this.channelData = channelData;
        pivd = new ArrayList<DataElement>();
        events = new ArrayList<MeterEvent>();
    }

    @SuppressWarnings({"unused"})
    public ArrayList<DataElement> getProcessedData() {
        return pivd;
    }

    public String pivdToString() {
        StringBuilder result = new StringBuilder();

        for (DataElement de : pivd) {
            result.append(de.toString(","));
            result.append("\r");
        }

        return result.toString();
    }

    public void processReadData(Date from, Date to) throws ProcessingException {

        int ivv;
        boolean dateInitialized;
        ControlCodeData ccd;
        RawData rd;
        Integer state = null;
        events.clear();

        this.ivFactor = channelData.getIntervalValueFactor();
        this.mrFactor = channelData.getMeterReadingFactor();

        Calendar workDate = GregorianCalendar.getInstance(/*channelData.getTimeZone()*/);

        dateInitialized = false;
        if (channelData.getReadDate() != null) {
            workDate.setTime(channelData.getReadDate());
        } else {
            workDate.set(Calendar.SECOND, 0);
            workDate.set(Calendar.MILLISECOND, 0);
        }

        interval = channelData.getInterval() * 1000; /* interval in ms */
        if (interval <= 0) {
            interval = determineInterval();
        }

        rd = channelData.getRawData();
        rd.setDateDirection(1);
        rd.setPos(rd.size());
        while (rd.getPos() > 0) {

            ivv = rd.getValue();

            /* if found control code, parse code... */
            if (ivv >= 0xFF0) {
                ccd = rd.readCCData(workDate, ivv);
                if (rd.isFoundCC()) {
                    continue;
                }
                switch (ivv) {
                    case 0xFFE: /* change of interval */
                        interval = (Integer) (ccd.getParams())[0] * 1000;
                        if (dateInitialized) {
                            addEvent(workDate, MeterEvent.CONFIGURATIONCHANGE, "Interval changed to " + (interval / (60/*s*/ * 1000/*ms*/)) + " min");
                        }
                        break;

                    case 0xFFC: /* Start of readout */
                        if (dateInitialized) {
                            if (!isNewDateValid(workDate, ccd.getDate())) {
                                dateInitialized = false;
                                break;
                            }
                        }
                        workDate.setTime(ccd.getDate());
                        dateInitialized = (interval > 0);
                        break;

                    case 0xFFB: /* Begin of day */
                        /* in new interval end processing, FF2 can <br>follow<br> FFA */
                        int ffb_save = rd.getPos();
                        if (rd.getValue() != 0xFF2) {
                            rd.setPos(ffb_save);
                        } else {
                            ControlCodeData ff2 = rd.readCCData(workDate, 0xFF2);
                            workDate.setTime(ff2.getDate());
                            dateInitialized = (interval > 0);
                            state = IntervalStateBits.SHORTLONG;
                            addEvent(workDate, MeterEvent.POWERUP, "Restart");
                        }
                        /* only if we have found a previous full date...*/
                        if (ccd.hasDate()) {
                            if (dateInitialized) {
                                if (!isNewDateValid(workDate, ccd.getDate())) {
                                    dateInitialized = false;
                                    break;
                                }
                            }
                            workDate.setTime(ccd.getDate());
                            dateInitialized = (interval > 0);
                        }
                        break;

                    case 0xFFA: /* Begin of month */
                        /* in new interval end processing, FF2 can <br>follow<br> FFA */
                        int ffa_save = rd.getPos();
                        if (rd.getValue() != 0xFF2) {
                            rd.setPos(ffa_save);
                        } else {
                            ControlCodeData ff2 = rd.readCCData(workDate, 0xFF2);
                            workDate.setTime(ff2.getDate());
                            state = IntervalStateBits.SHORTLONG;
                            addEvent(workDate, MeterEvent.POWERUP, "Restart");
                        }
                        if (dateInitialized) {
                            isNewDateValid(workDate, ccd.getDate());
                        }
                        workDate.setTime(ccd.getDate());
                        interval = (Integer) (ccd.getParams())[1] * 1000;
                        dateInitialized = (interval > 0);
                        break;

                    case 0xFF8: /* large encoded control code */
                        switch (ccd.getSubCode()) {
                            case 0x002: /* date correction forwards */
                                addEvent(workDate, MeterEvent.SETCLOCK, "Time correction to " + ccd.getDate());
                                workDate.setTime(ccd.getDate());
                                dateInitialized = (interval > 0);
                                state = IntervalStateBits.SHORTLONG;
                                break;
                            case 0x003: /* new cp value */
                                setFactors(CPVALUE, 1 / (Double) (ccd.getParams())[1]);
                                if (dateInitialized) {
                                    addEvent(workDate, MeterEvent.CONFIGURATIONCHANGE, "Cp value changed to " + (ccd.getParams())[1]);
                                }
                                break;
                            case 0x004: /* new factor */
                                setFactors(CALCFAC, (Double) (ccd.getParams())[1]);
                                if (dateInitialized) {
                                    addEvent(workDate, MeterEvent.CONFIGURATIONCHANGE, "Factor changed to " + (ccd.getParams())[1]);
                                }
                                break;
                            case 0x00A: /* date correction backwards */
                                if (dateInitialized) {
                                    addEvent(workDate, MeterEvent.POWERUP, "Time correction to " + ccd.getDate());
                                }
                                dateInitialized = false;
                        }
                        break;

                    case 0xFF1: /* complete restart */
                        state = IntervalStateBits.SHORTLONG;
                        if (dateInitialized) {
                            addEvent(workDate, MeterEvent.FATAL_ERROR, "FF1 restart");
                        }
                        break;

                    case 0xFF2: /* power fail restart */
                        workDate.setTime(ccd.getDate());
                        state = IntervalStateBits.SHORTLONG;
                        dateInitialized = (interval > 0);
                        addEvent(workDate, MeterEvent.POWERUP, "Restart");
                        break;

                    case 0xFF3: /* wrong value */
                        if (dateInitialized) {
                            state = IntervalStateBits.CORRUPTED;
                            addEvent(workDate, MeterEvent.HARDWARE_ERROR, "Wrong value");
                        }
                        break;

                    case 0xFF4: /* alternative value */
                        if (dateInitialized) {
                            state = IntervalStateBits.CORRUPTED;
                            addEvent(workDate, MeterEvent.HARDWARE_ERROR, "Corrected value");
                        }
                        break;

                    case 0xFF5: /* disturbed values */
                        if (dateInitialized) {
                            state = IntervalStateBits.CORRUPTED;
                            addEvent(workDate, MeterEvent.HARDWARE_ERROR, "Disturbed value");
                        }
                        break;
                }
            } else {
                if (dateInitialized) {
                    long date = workDate.getTimeInMillis();
                    date = ((date / interval) + 1) * interval;
                    workDate.setTimeInMillis(date);

                    if (date > to.getTime()) {
                        break;
                    }
                    if (date >= from.getTime()) {
                        Double d = ivv * ivFactor;
                        pivd.add(new DataElement(date, d, state));
                    }
                    state = null;
                }
            }
        }
    }

    private void setFactors(int factorType, double factor) {

        if (factorType == CPVALUE) {
            ivFactor = !channelData.isIvUsingFactor() ? factor : ivFactor;
            mrFactor = !channelData.isCounterUsingFactor() ? factor : mrFactor;
        } else {
            ivFactor = channelData.isIvUsingFactor() ? factor : ivFactor;
            mrFactor = channelData.isCounterUsingFactor() ? factor : mrFactor;
        }
    }

    private long determineInterval() {

        int result = -1;
        ControlCodeData ccd;

        Calendar workDate = GregorianCalendar.getInstance(/*channelData.getTimeZone()*/);

        RawData rd = channelData.getRawData();
        rd.setDateDirection(1);
        rd.setPos(rd.size());

        int cIV = 0;
        int ivv;
        int state = 0;
        while ((rd.getPos() > 0) && (state < 2)) {

            ivv = rd.getValue();

            /* if found control code, parse code... */
            if (ivv >= 0xFF0) {
                ccd = rd.readCCData(workDate, ivv);
                if (rd.isFoundCC()) {
                    continue;
                }
                switch (ivv) {
                    case 0xFF2:
                        state = 0;
                        break;

                    case 0xFFE: /* change of interval */
                        /* because it's the new interval,
                           we couldn't detect the previous, so return -1 */
                        return result;

                    case 0xFFA: /* Begin of month */
                        /* here we have the interval... */
                        return (Integer) (ccd.getParams())[1] * 1000;

                    case 0xFFB: /* Begin of day */
                        int ffb_save = rd.getPos();
                        if (rd.getValue() != 0xFF2) {
                            rd.setPos(ffb_save);
                            state++;
                        } else {
                            rd.readCCData(workDate, 0xFF2);
                            state = 0;
                        }
                        break;

                    case 0xFF8: /* extended code */
                        if ((ccd.getSubCode() == 0x002) ||
                                (ccd.getSubCode() == 0x00A) ||
                                (ccd.getSubCode() == 0x00B) ||
                                (ccd.getSubCode() == 0x00C) ||
                                (ccd.getSubCode() == 0x011)) {
                            state = 0;
                        }
                }
            } else {
                if (state == 0) {
                    cIV = 0;
                } else {
                    cIV++;
                }
            }
        }

        if (state > 1) {
            state = 1440 /*min*/ / cIV;
            if (state > 40) {
                result = 60;
            } else if (state > 25) {
                result = 30;
            } else if (state > 17) {
                result = 20;
            } else if (state > 12) {
                result = 15;
            } else if (state > 8) {
                result = 10;
            } else if (state > 3) {
                result = 5;
            } else {
                result = state;
            }
            result = result * 60 * 1000;
        }

        return result;
    }

    private boolean isNewDateValid(Calendar workDate, Date newDate) throws ProcessingException {
        long l1 = workDate.getTimeInMillis();
        long l2 = newDate.getTime();

        boolean result = ((l1 / interval) == (l2 / interval));

        if (!result) {
            addEvent(workDate, MeterEvent.FATAL_ERROR, "Missing or too many interval values: should be " + workDate.getTime() + " - is " + newDate);
        }
        return result;
    }

    private void addEvent(Calendar date, int eventCode, String msg) {
        events.add(new MeterEvent(date.getTime(), eventCode, "Channel " + channelData.getChannelNo() + " : " + msg));
    }

    public List getEvents() {
        return events;
    }

    public String eventsToString() {

        StringBuilder result = new StringBuilder();

        for (MeterEvent me : events) {
            result.append(me.toString());
            result.append("\r");
        }

        return result.toString();
    }
}