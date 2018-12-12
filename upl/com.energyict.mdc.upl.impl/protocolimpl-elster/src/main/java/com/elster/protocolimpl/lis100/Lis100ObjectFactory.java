package com.elster.protocolimpl.lis100;

import com.elster.protocolimpl.lis100.objects.*;
import com.elster.protocolimpl.lis100.objects.api.*;

import java.io.IOException;

/**
 * Class to create objects of device data
 * <p/>
 * User: heuckeg
 * Date: 21.01.11
 * Time: 13:51
 */
public class Lis100ObjectFactory {

    private ProtocolLink link = null;

    /**
     * an object for the serial number
     */
    protected IBaseObject serialNumberObject = null;
    /**
     * The used SoftwareVersionObject
     */
    protected IIntegerObject softwareVersionObject = null;
    /**
     * The used object to get the meter interval
     */
    protected IntervalObject intervalObject = null;
    /**
     * the current (active) channel
     */
    protected IIntegerObject currentChannelObject = null;
    /**
     * the state register
     */
    protected IIntegerObject stateRegisterObject = null;
    /**
     * Unit
     */
    protected IBaseObject unitObject = null;
    /**
     * the used {@link com.elster.protocolimpl.dsfg.objects.ClockObject}
     */
    protected IClockObject clockObject = null;
    /**
     * Calculation type
     */
    protected IBaseObject calcTypeObject = null;
    /*
     * cp value
     */
    protected IDoubleObject cpValueObject = null;
    /*
     * calculation factor
     */
    protected IDoubleObject calcFactorObject = null;
    /*
     * Memory size
     */
    protected IIntegerObject memorySizeObject = null;
    /**
     * customer number
     */
    protected IBaseObject customerNoObject = null;
    /**
     * meter number
     */
    protected IBaseObject meterNoObject = null;
    /**
     * total counter
     */
    protected ICounterObject totalCounterObject = null;
    /**
     * programmable counter
     */
    protected ICounterObject progCounterObject = null;
    /**
     * counter H2 BOM
     */
    protected ICounterObject counterBeginOfMonth = null;
    /**
     * Begin of day
     */
    protected IIntegerObject beginOfDay = null;

    /**
     * Constructor for factory
     *
     * @param lis100 - link to link class (to read device data)
     */
    public Lis100ObjectFactory(LIS100 lis100) {

        link = lis100;
    }

    /**
     * getter for ProtocolLink
     *
     * @return a link to ProtocolLink
     */
    public ProtocolLink getLink() {
        return link;
    }

    public IIntegerObject getSoftwareVersionObject() throws IOException {
        if (softwareVersionObject == null) {
            softwareVersionObject = new SoftwareVersionObject(new BufferedObject(link, (byte) 'a'));
        }
        return softwareVersionObject;
    }

    public IBaseObject getSerialNumberObject() {
        if (serialNumberObject == null) {
            serialNumberObject = new BufferedObject(link, (byte) 'l');
        }
        return serialNumberObject;
    }

    public IntervalObject getIntervalObject() {
        if (intervalObject == null) {
            intervalObject = new IntervalObject(new IntegerObject(new AbstractObject(link, (byte) 'p'), 10));
        }
        return intervalObject;
    }

    public IIntegerObject getCurrentChannelObject() throws IOException {
        if (currentChannelObject == null) {
            currentChannelObject = new IntegerObject(new AbstractObject(link, (byte) 'Y'), 10);
        }
        return currentChannelObject;
    }

    public IIntegerObject getStateRegisterObject() {
        if (stateRegisterObject == null) {
            stateRegisterObject = new IntegerObject(new AbstractObject(link, (byte) 'r'), 16);
        }
        return stateRegisterObject;
    }

    public IBaseObject getUnitObject() {
        if (unitObject == null) {
            unitObject = new AbstractObject(link, (byte) '^');
        }
        return unitObject;
    }

    public IClockObject getClockObject() {
        if (clockObject == null) {
            clockObject = new ClockObject(link, (byte) 'z', (byte) '{');
        }
        return clockObject;
    }

    public IBaseObject getCalcTypeObject() {
        if (calcTypeObject == null) {
            calcTypeObject = new AbstractObject(link, (byte) 'V');
        }
        return calcTypeObject;
    }


    public IDoubleObject getCpValueObject() {
        if (cpValueObject == null) {
            cpValueObject = new CpValueObject(new AbstractObject(link, (byte) 'n'));
        }
        return cpValueObject;
    }

    public IDoubleObject getCalcFactorObject() throws IOException {
        if (calcFactorObject == null) {
            calcFactorObject = new CalcFactorObject(new AbstractObject(link, (byte) 'T'));
        }
        return calcFactorObject;
    }

    public IIntegerObject getMemorySizeObject() {
        if (memorySizeObject == null) {
            memorySizeObject = new IntegerObject(new AbstractObject(link, (byte) 'b'), 16);
        }
        return memorySizeObject;
    }

    public IBaseObject getCustomerNoObject() {
        if (customerNoObject == null) {
            customerNoObject = new AbstractObject(link, (byte) 'h');
        }
        return customerNoObject;
    }

    public IBaseObject getMeterNoObject() {
        if (meterNoObject == null) {
            meterNoObject = new AbstractObject(link, (byte) 'j');
        }
        return meterNoObject;
    }

    public ICounterObject getTotalCounterObject() throws IOException {
        if (totalCounterObject == null) {
            totalCounterObject = new CounterObject(new AbstractObject(link, (byte) 's'));
        }
        return totalCounterObject;
    }

    public ICounterObject getProgCounterObject() throws IOException {
        if (progCounterObject == null) {
            progCounterObject = new CounterObject(new AbstractObject(link, (byte) 't'));
        }
        return progCounterObject;
    }

    public ICounterObject getCounterBeginOfMonth() throws IOException {
        if (counterBeginOfMonth == null) {
            counterBeginOfMonth = new CounterObject(new AbstractObject(link, (byte) '|'));
        }
        return counterBeginOfMonth;
    }

    public IIntegerObject getBeginOfDay() {
        if (beginOfDay == null) {
            beginOfDay = new IntegerObject(new AbstractObject(link, (byte) '\\'), 10);
        }
        return beginOfDay;
    }


}
