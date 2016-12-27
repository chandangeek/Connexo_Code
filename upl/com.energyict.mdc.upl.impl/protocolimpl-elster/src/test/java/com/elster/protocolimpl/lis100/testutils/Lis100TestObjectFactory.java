package com.elster.protocolimpl.lis100.testutils;

import com.elster.protocolimpl.lis100.Lis100ObjectFactory;
import com.elster.protocolimpl.lis100.objects.ConstantObject;
import com.elster.protocolimpl.lis100.objects.CounterObject;
import com.elster.protocolimpl.lis100.objects.CpValueObject;
import com.elster.protocolimpl.lis100.objects.DoubleObject;
import com.elster.protocolimpl.lis100.objects.IntegerObject;
import com.elster.protocolimpl.lis100.objects.IntervalObject;
import com.elster.protocolimpl.lis100.objects.api.IBaseObject;
import com.elster.protocolimpl.lis100.objects.api.IClockObject;
import com.elster.protocolimpl.lis100.objects.api.ICounterObject;
import com.elster.protocolimpl.lis100.objects.api.IDoubleObject;
import com.elster.protocolimpl.lis100.objects.api.IIntegerObject;
import com.elster.protocolimpl.lis100.profile.IIntervalDataStreamReader;

import java.io.IOException;
import java.util.Date;

/**
 * Object factory for tests (needs a TempReader class to get data from)
 * <p/>
 * User: heuckeg
 * Date: 02.02.11
 * Time: 09:23
 */
@SuppressWarnings({"unused"})
public class Lis100TestObjectFactory
        extends Lis100ObjectFactory
        implements IIntervalDataStreamReader {

    /* to get sample values out of the read temp file */
    private TempReader tempReader;

    /* to emulate interval data reading...*/
    private int ivPos = 0;

    public Lis100TestObjectFactory(TempReader tr) {
        super(null);
        this.tempReader = tr;
    }

    public IIntegerObject getSoftwareVersionObject() throws IOException {
        if (softwareVersionObject == null) {
            softwareVersionObject = new IntegerObject(new ConstantObject(tempReader.getSwVersion()), 10);
        }
        return softwareVersionObject;
    }

    public IBaseObject getSerialNumberObject() {
        if (serialNumberObject == null) {
            serialNumberObject = new ConstantObject(tempReader.getDeviceNo());
        }
        return serialNumberObject;
    }

    public IntervalObject getIntervalObject() {
        if (intervalObject == null) {
            intervalObject = new IntervalObject(new IntegerObject(new ConstantObject(tempReader.getInterval()), 10));
        }
        return intervalObject;
    }

    public IIntegerObject getCurrentChannelObject() throws IOException {
        if (currentChannelObject == null) {
            currentChannelObject = new IntegerObject(new ConstantObject("1"), 10);
        }
        return currentChannelObject;
    }

    public IIntegerObject getStateRegisterObject() {
        if (stateRegisterObject == null) {
            stateRegisterObject = new IntegerObject(new ConstantObject(tempReader.getStateRegister()), 16);
        }
        return stateRegisterObject;
    }

    public IBaseObject getUnitObject() {
        if (unitObject == null) {
            unitObject = new ConstantObject(tempReader.getUnit());
        }
        return unitObject;
    }

    public IClockObject getClockObject() {
        if (clockObject == null) {
            clockObject = new TestClockObject();
        }
        return clockObject;
    }

    public IBaseObject getCalcTypeObject() {
        if (calcTypeObject == null) {
            calcTypeObject = new ConstantObject(tempReader.getCalcType());
        }
        return calcTypeObject;
    }


    public IDoubleObject getCpValueObject() {
        if (cpValueObject == null) {
            cpValueObject = new CpValueObject(new ConstantObject(tempReader.getCpValue()));
        }
        return cpValueObject;
    }

    public IDoubleObject getCalcFactorObject() throws IOException {
        if (calcFactorObject == null) {
            calcFactorObject = new DoubleObject(new ConstantObject(tempReader.getFactor()));
        }
        return calcFactorObject;
    }

    public IIntegerObject getMemorySizeObject() {
        if (memorySizeObject == null) {
            memorySizeObject = new IntegerObject(new ConstantObject("2000"), 16);
        }
        return memorySizeObject;
    }

    public IBaseObject getCustomerNoObject() {
        if (customerNoObject == null) {
            customerNoObject = new ConstantObject(tempReader.getCustomerNo());
        }
        return customerNoObject;
    }

    public IBaseObject getMeterNoObject() {
        if (meterNoObject == null) {
            meterNoObject = new ConstantObject(tempReader.getMeterNo());
        }
        return meterNoObject;
    }

    public ICounterObject getTotalCounterObject() throws IOException {
        if (totalCounterObject == null) {
            double d = Double.parseDouble(tempReader.getTotalCounter());
            totalCounterObject = new CounterObject(new ConstantObject("" + d));
        }
        return totalCounterObject;
    }

    public ICounterObject getProgCounterObject() throws IOException {
        if (progCounterObject == null) {
            double d = Double.parseDouble(tempReader.getSetblCounter());
            progCounterObject = new CounterObject(new ConstantObject("" + d));
        }
        return progCounterObject;
    }

    public void prepareRead() throws IOException {
        ivPos = this.tempReader.getIvData().size() - 1;
    }

    public int getCurrentChannel() {
        try {
            return getCurrentChannelObject().getIntValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public int readWord() throws IOException {
        if (ivPos >= 0) {
            return tempReader.getIvData().get(ivPos--);
        } else {
            return -1;
        }
    }

    public void switchToNextChannel() throws IOException {

    }

    private class TestClockObject implements IClockObject {
        public Date getDate() {
            return new Date();
        }

        public void setDate(Date date) {
        }
    }

    public ICounterObject getCounterBeginOfMonth() throws IOException {
        if (counterBeginOfMonth == null) {
            double d = Double.parseDouble(tempReader.getH2Bom());
            counterBeginOfMonth = new CounterObject(new ConstantObject("" + d));
        }
        return counterBeginOfMonth;

    }

    public IIntegerObject getBeginOfDay() {
        if (beginOfDay == null) {
            beginOfDay = new IntegerObject(new ConstantObject("0600"), 10);
        }
        return beginOfDay;
    }

}
