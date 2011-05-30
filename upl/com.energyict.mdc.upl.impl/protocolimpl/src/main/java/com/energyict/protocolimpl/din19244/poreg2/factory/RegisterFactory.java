package com.energyict.protocolimpl.din19244.poreg2.factory;


import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.request.register.*;

import java.io.IOException;
import java.util.*;

/**
 * Factory able to request registers.
 *
 * Copyrights EnergyICT
 * Date: 20-apr-2011
 * Time: 14:09:45
 */
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

    public long readBillingData(int tariff, int level) throws IOException {
        BankConfiguration config = getBankConfig(tariff, level);
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

    private BankConfiguration getBankConfig(int tariff, int level) throws IOException {
        List<BankConfiguration> configs = readBillingConfiguration();
        for (BankConfiguration config : configs) {
            if (((tariff == - 1) || (config.isTariffRate(tariff))) && config.isResultLevel(level)) {
                bankId = config.getBankId();
                return config;
            }
        }
        if (level == 3) {
            throw new NoSuchRegisterException("Apparent energy is not configured to be stored as billing data");
        }

        if (tariff == 0) {
            throw new NoSuchRegisterException("Billing data is not configured to be stored without a tariff rate");
        }
        throw new NoSuchRegisterException("Billing data for tariff rate " + tariff + " is not configured to be stored");
    }

    public Date readBillingDataLastPeriodTimeStamp() throws IOException {
        checkBankId();
        BillingDataLastPeriodTimeStamp timeStamp = new BillingDataLastPeriodTimeStamp(poreg, bankId, 0, 1, 1);
        timeStamp.doRequest();
        return timeStamp.getTimeStamp();
    }

    private Date readBillingDataCurrentPeriodTimeStamp() throws IOException {
        checkBankId();
        BillingDataCurrentPeriodTimeStamp timeStamp = new BillingDataCurrentPeriodTimeStamp(poreg, bankId, 0, 1, 1);
        timeStamp.doRequest();
        return timeStamp.getTimeStamp();
    }

    private void checkBankId() throws IOException {
        if (bankId == -1) {
            getBankConfig(-1, 0);
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