/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.factory;


import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.request.register.BankConfiguration;
import com.energyict.protocolimpl.din19244.poreg2.request.register.BillingCounter;
import com.energyict.protocolimpl.din19244.poreg2.request.register.BillingDataCurrentPeriod;
import com.energyict.protocolimpl.din19244.poreg2.request.register.BillingDataCurrentPeriodTimeStamp;
import com.energyict.protocolimpl.din19244.poreg2.request.register.BillingDataLastPeriod;
import com.energyict.protocolimpl.din19244.poreg2.request.register.BillingDataLastPeriodTimeStamp;
import com.energyict.protocolimpl.din19244.poreg2.request.register.BillingParameters;
import com.energyict.protocolimpl.din19244.poreg2.request.register.DateTime;
import com.energyict.protocolimpl.din19244.poreg2.request.register.DaylightAlgorithm;
import com.energyict.protocolimpl.din19244.poreg2.request.register.DstSettings;
import com.energyict.protocolimpl.din19244.poreg2.request.register.Events;
import com.energyict.protocolimpl.din19244.poreg2.request.register.Level0Parameters;
import com.energyict.protocolimpl.din19244.poreg2.request.register.Level0Results;
import com.energyict.protocolimpl.din19244.poreg2.request.register.Level3Parameters;
import com.energyict.protocolimpl.din19244.poreg2.request.register.Level3Results;
import com.energyict.protocolimpl.din19244.poreg2.request.register.MeasuringPeriod;
import com.energyict.protocolimpl.din19244.poreg2.request.register.ProfileParameters;
import com.energyict.protocolimpl.din19244.poreg2.request.register.SerialNumber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegisterFactory {

    Poreg poreg;
    private static final int STEP = 10;

    //Cached
    private int bankId = -1;
    private BillingParameters billingParameters = null;
    private ProfileParameters profileParameters = null;
    private Level3Parameters level3parameters = null;
    private Level0Parameters level0parameters = null;
    private DstSettings settings = null;


    public RegisterFactory(Poreg poreg) {
        this.poreg = poreg;
    }

    public DstSettings readDst() throws IOException {
        if (settings == null) {
            settings = new DstSettings(poreg, 0, 0, 2, 5);
            settings.doRequest();
        }
        return settings;
    }

    public Date readDstStart() throws IOException {
        if (settings == null) {
            settings = new DstSettings(poreg, 0, 0, 2, 5);
            settings.doRequest();
        }
        return settings.getStart();
    }

    public Date readDstEnd() throws IOException {
        if (settings == null) {
            settings = new DstSettings(poreg, 0, 0, 2, 5);
            settings.doRequest();
        }
        return settings.getEnd();
    }

    public void writeDstStart(int startMonth, int startDay, int startWDay) throws IOException {
        DstSettings dstSettings = new DstSettings(poreg, 0, 1, 1, 3);
        dstSettings.setStart(startMonth, startDay, startWDay);
        dstSettings.write();
    }

    public void writeDstEnd(int endMonth, int endDay, int endWDay) throws IOException {
        DstSettings dstSettings = new DstSettings(poreg, 1, 1, 1, 3);
        dstSettings.setStart(endMonth, endDay, endWDay);
        dstSettings.write();
    }

    public void writeDstAlgorithms(DaylightAlgorithm startAlgorithm, DaylightAlgorithm endAlgorithm) throws IOException {
        DstSettings dstSettings = new DstSettings(poreg, 0, 4, 2, 1);
        dstSettings.setAlgorithms(startAlgorithm, endAlgorithm);
        dstSettings.write();
    }

    public Date readTime() throws IOException {
        DateTime dateTime = new DateTime(poreg);
        dateTime.doRequest();
        return dateTime.getTime();
    }

    public String readSerialNumber() throws IOException {
        SerialNumber serialNumber = new SerialNumber(poreg);
        serialNumber.doRequest();
        return serialNumber.getSerialNumber();
    }


    /**
     * Read out the event table entries in steps of 10.
     * Check the event time, stop querying if no longer in requested interval
     */
    public List<MeterEvent> readEvents(Date lastReading, Date toDate) throws IOException {
        int offset = 0;
        List<MeterEvent> result = new ArrayList<MeterEvent>();

        while (true) {
            Events events = new Events(poreg, offset, 0, STEP, 4);
            events.doRequest();
            for (MeterEvent meterEvent : events.getMeterEvents()) {
                if (meterEvent.getTime().after(lastReading) && meterEvent.getTime().before(toDate)) {
                    result.add(meterEvent);
                }
            }
            if (events.getMeterEvents().size() < STEP) {
                break;                          //End condition reached, stop querying
            }
            offset += STEP;
        }
        return result;
    }

    public ProfileParameters readProfileParameters() throws IOException {
        if (profileParameters == null) {
            profileParameters = new ProfileParameters(poreg);
            profileParameters.doRequest();
        }
        return profileParameters;
    }

    public int readMeasurementPeriod(int profileIntervalIndex) throws IOException {
        MeasuringPeriod period = new MeasuringPeriod(poreg);
        period.doRequest();
        return period.getPeriodsInSeconds()[profileIntervalIndex];
    }

    public long readTotalActiveEnergy() throws IOException {
        if (getNumberOfChannels() == 0) {
            throw new NoSuchRegisterException("Active energy is not configured to be stored (see level 0 parameters)");
        }
        Level0Results level0Results = new Level0Results(poreg);
        level0Results.doRequest();
        long total = 0;
        for (ExtendedValue value : level0Results.getValues()) {
            total += value.getValue();
        }
        return total;
    }

    public List<ExtendedValue> readLevel3Parameters() throws IOException {
        if (level3parameters == null) {
            level3parameters = new Level3Parameters(poreg);
            level3parameters.doRequest();
        }
        return level3parameters.getValues();
    }

    public List<ExtendedValue> readLevel0Parameters() throws IOException {
        if (level0parameters == null) {
            level0parameters = new Level0Parameters(poreg);
            level0parameters.doRequest();
        }
        return level0parameters.getValues();
    }

    public long readTotalApparentEnergy() throws IOException {
        Level3Results level3Results = new Level3Results(poreg);
        level3Results.doRequest();
        List<ExtendedValue> config = readLevel3Parameters();
        List<ExtendedValue> values = level3Results.getValues();

        long total = 0;
        boolean supported = false;
        for (int i = 0; i < values.size(); i++) {
            if (config.get(i).getValue() == 1) {
                total += values.get(i).getValue();     //Only add the value if the config parameters indicate it is apparent energy
                supported = true;
            }
        }
        if (!supported) {
            throw new NoSuchRegisterException("Apparent energy is not configured to be stored (see level 3 parameters)");
        }
        return total;
    }

    public List<BankConfiguration> readBillingConfiguration() throws IOException {
        if (billingParameters == null) {
            billingParameters = new BillingParameters(poreg);
            billingParameters.doRequest();
        }
        return billingParameters.getConfigs();
    }

    public long readBillingData(int tariff, int level, int dField) throws IOException {
        tariff = tariff == 4 ? 0 : tariff;
        BankConfiguration config = getBankConfig(tariff, level, dField);
        List<ExtendedValue> values;
        values = readBillingDataLastPeriod();

        long total = 0;
        for (int i = 0; i < values.size(); i++) {
            if (config.getChannels()[i] != 0xFF) {
                total += values.get(i).getValue();
            }
        }
        return total;
    }

    public long readCurrentData(int tariff, int level, int dField) throws IOException {
        tariff = tariff == 4 ? 0 : tariff;
        BankConfiguration config = getBankConfig(tariff, level, dField);
        List<ExtendedValue> values;
        values = readBillingDataCurrentPeriod();

        long total = 0;
        for (int i = 0; i < values.size(); i++) {                     //Sum all values of the row (concentrator)
            if (config.getChannels()[i] != 0xFF) {
                total += values.get(i).getValue();
            }
        }
        return total;
    }

    /**
     * Read a specific row (indicated by bankId) in GID = 20 containing billing data.
     */
    private List<ExtendedValue> readBillingDataCurrentPeriod() throws IOException {
        checkBankId();
        BillingDataCurrentPeriod billingDataCurrentPeriod = new BillingDataCurrentPeriod(poreg, bankId, 0, 1, 8);
        billingDataCurrentPeriod.doRequest();
        return billingDataCurrentPeriod.getValues();
    }

    private List<ExtendedValue> readBillingDataLastPeriod() throws IOException {
        checkBankId();
        BillingDataLastPeriod billingDataLastPeriod = new BillingDataLastPeriod(poreg, bankId, 0, 1, 8);
        billingDataLastPeriod.doRequest();
        return billingDataLastPeriod.getValues();
    }

    /**
     * Iterate over the bank definitions, find the right bank id for the tariff, level and dField.
     */
    private BankConfiguration getBankConfig(int tariff, int level, int dField) throws IOException {
        List<BankConfiguration> configs = readBillingConfiguration();
        for (BankConfiguration config : configs) {
            if (((tariff == -1) || (config.isTariffRate(tariff))) && checkLevel(level, config) && ((dField == -1) || config.isResultRenewal(dField))) {
                bankId = config.getBankId();
                return config;
            }
        }
        throw new NoSuchRegisterException("Billing data for tariff rate " + tariff + " is not configured to be stored");
    }

    private boolean checkLevel(int level, BankConfiguration config) {
        if (poreg.getApparentEnergyResultLevel() == 0) {                        //Apparent energy on level 0
            return (level == 3) && config.getResultLevel() != 0xFF;             //In this case, every configured level contains apparent energy
        } else {
            if (level == 0) {
                return config.getResultLevel() != 3;                            //Level0, 1 or 2 contain active energy
            }
            return level == config.getResultLevel();
        }
    }

    public Date readBillingDataLastPeriodTimeStamp() throws IOException {
        checkBankId();
        BillingDataLastPeriodTimeStamp timeStamp = new BillingDataLastPeriodTimeStamp(poreg, bankId, 0, 1, 1);
        timeStamp.doRequest();
        return timeStamp.getTimeStamp();
    }

    public Date readBillingDataCurrentPeriodTimeStamp() throws IOException {
        checkBankId();
        BillingDataCurrentPeriodTimeStamp timeStamp = new BillingDataCurrentPeriodTimeStamp(poreg, bankId, 0, 1, 1);
        timeStamp.doRequest();
        return timeStamp.getTimeStamp();
    }

    private void checkBankId() throws IOException {
        if (bankId == -1) {
            getBankConfig(-1, 0, -1);
        }
    }

    public int getNumberOfChannels() throws IOException {
        int channels = 0;
        for (ExtendedValue enabled : readLevel0Parameters()) {
            channels += (enabled.getValue() == 0) ? 0 : 1;
        }
        return channels;
    }

    public int readBillingCounter() throws IOException {
        BillingCounter billingCounter = new BillingCounter(poreg);
        billingCounter.doRequest();
        return billingCounter.getCount();
    }

    public boolean isDST(Date date) throws IOException {
        Date dstStart = poreg.getRegisterFactory().readDstStart();
        Date dstEnd = poreg.getRegisterFactory().readDstEnd();
        return date.after(dstStart) && date.before(dstEnd);
    }
}