package com.energyict.mdc.common.interval;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Represents a consumption
 */
public class BasicConsumption implements Serializable {

    private Interval interval;
    private Quantity quantity;

    /**
     * Creates a new consumption
     *
     * @param interval   consumption's time period
     * @param quantity quantity consumed
     */
    public BasicConsumption(Interval interval, Quantity quantity) {
        this.interval = interval;
        this.quantity = quantity;
    }

    /**
     * Creates a new consumption
     *
     * @param from     start of consumption period
     * @param to       end of consumption period
     * @param quantity quantity consumed
     */
    public BasicConsumption(Date from, Date to, Quantity quantity) {
        this(new Interval(from, to), quantity);
    }

    /**
     * Creates a new consumption
     *
     * @param interval consumption's time period
     * @param amount amount consumed
     * @param unit   unit consumed
     */
    public BasicConsumption(Interval interval, BigDecimal amount, Unit unit) {
        this(interval, new Quantity(amount, unit));
    }

    /**
     * Creates a new consumption
     *
     * @param from   start of consumption period
     * @param to     end of consumption period
     * @param amount amount consumed
     * @param unit   consumption unit
     */
    public BasicConsumption(Date from, Date to, BigDecimal amount, Unit unit) {
        this(new Interval(from, to), new Quantity(amount, unit));
    }

    /**
     * Returns the consumption's time period
     *
     * @return the time period
     */
    public Interval getInterval() {
        return interval;
    }

    /**
     * Returns the quantity consumed
     *
     * @return the consumed quantity
     */
    public Quantity getQuantity() {
        return quantity;
    }

    /**
     * Returns the start of the consumption period
     *
     * @return start of consumption period
     */
    public Date getFrom() {
        return interval.getStart();
    }

    /**
     * Returns the end of the consumption period
     *
     * @return end of consumption period
     */
    public Date getTo() {
        return interval.getEnd();
    }

    /**
     * Returns the consumed amount
     *
     * @return the amount consumed
     */
    public BigDecimal getAmount() {
        return quantity.getAmount();
    }

    /**
     * Returns the consumption's unit
     *
     * @return the consumption unit
     */
    public Unit getUnit() {
        return quantity.getUnit();
    }
}
