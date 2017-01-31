/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Quantity.java
 *
 * Created on 7 oktober 2002, 18:32
 */

package com.energyict.mdc.common;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Quantity represents the combination of  an
 * amount (a <CODE>BigDecimal</CODE>) and a <CODE>Unit</CODE>
 * Like <CODE>String</CODE> and <CODE>Number</CODE>,
 * Quantities are immutable objects.
 *
 * @author Karel
 */
public class Quantity extends Number implements Comparable, Serializable {

    private final BigDecimal amount;
    private final Unit unit;

    /**
     * Creates a new instance of Quantity
     *
     * @param amount amount of the new <CODE>Quantity</CODE>
     * @param unit   unit of the new <CODE>Quantity</CODE>
     */
    public Quantity(BigDecimal amount, Unit unit) {
        this.amount = amount;
        this.unit = unit;
    }

    /**
     * Creates a new instance of Quantity
     *
     * @param amount amount of the new <CODE>Quantity</CODE>
     * @param unit   unit of the new <CODE>Quantity</CODE>
     */
    public Quantity(Number amount, Unit unit) {
        this(toBigDecimal(amount), unit);
    }

    /**
     * Creates a new instance of Quantity
     *
     * @param amount amount of the new <CODE>Quantity</CODE>
     * @param unit   unit of the new <CODE>Quantity</CODE>
     */
    public Quantity(String amount, Unit unit) {
        this(new BigDecimal(amount), unit);
    }

    /**
     * Creates a new instance of Quantity
     *
     * @param amount   amount of the new <CODE>Quantity</CODE>
     * @param dlmsCode code of the Unit's <CODE>BaseUnit</CODE>
     * @param scale    unit scale of the new Quantity
     */
    public Quantity(BigDecimal amount, int dlmsCode, int scale) {
        this(amount, Unit.get(dlmsCode, scale));
    }

    /**
     * Creates a new instance of Quantity
     *
     * @param amount   amount of the new <CODE>Quantity</CODE>
     * @param dlmsCode code of the Unit's <CODE>BaseUnit</CODE>
     * @param scale    unit scale of the new Quantity
     */
    public Quantity(String amount, int dlmsCode, int scale) {
        this(new BigDecimal(amount), Unit.get(dlmsCode, scale));
    }

    private static BigDecimal toBigDecimal(Number in) {
        BigDecimal result;
        if (in instanceof BigDecimal) {
            result = (BigDecimal) in;
        } else {
            result = new BigDecimal(in.toString());
        }
        if (result.scale() < 0) {
            result = result.setScale(0);
        }
        return result;
    }

    /**
     * get the receiver's amount
     *
     * @return the receiver's amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * get the receiver's unit
     *
     * @return the receiver's unit
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * get the <CODE>BaseUnit</CODE> of the receiver's unit
     *
     * @return the baseunit of the receiver's unit
     */
    public BaseUnit getBaseUnit() {
        return getUnit().getBaseUnit();
    }

    /**
     * adds the argument to the receiver
     *
     * @param quantity quantity to add
     * @return a new quantity
     * @throws ArithmeticException if a conversion error occurred
     */
    public Quantity add(Quantity quantity) throws ArithmeticException {
        return new Quantity(this.getAmount().add(convert(quantity).getAmount()), getUnit());
    }

    /**
     * subtracts the argument from the receiver
     *
     * @param quantity quantity to subtract
     * @return a new quantity
     * @throws ArithmeticException if a conversion error occurred
     */
    public Quantity subtract(Quantity quantity) throws ArithmeticException {
        return new Quantity(this.getAmount().subtract(convert(quantity).getAmount()), getUnit());
    }

    /**
     * converts the argument to unit of the receiver
     *
     * @param quantity quantity to convert
     * @return a new quantity
     * @throws ArithmeticException in case the conversion can not be completed or is impossible
     */
    protected Quantity convert(Quantity quantity) throws ArithmeticException {
        return quantity.convertTo(getUnit());
    }

    /**
     * return a new <CODE>Quantity</CODE>,
     * representing the receiver in an other unit
     *
     * @param unit new unit
     * @return the converted <CODE>Quantity</CODE>
     * @throws ArithmeticException if the receiver cannot be converted to the new unit
     */
    public Quantity convertTo(Unit unit) throws ArithmeticException {

        if (unit == null) {
            return this; // KV 21012003 if unit null, return same quantity!
        }

        return UnitConversion.convertQuantity(this, unit);
    }

    /**
     * return a new <CODE>Quantity</CODE>, representing the receiver in an other unit
     *
     * @param unit                    new unit
     * @param allowUnitlessConversion if the source or the result is allowed to be unitless
     * @return the converted <CODE>Quantity</CODE>
     * @throws ArithmeticException if the receiver cannot be converted to the new unit
     */
    public Quantity convertTo(Unit unit, boolean allowUnitlessConversion) throws ArithmeticException {
        if (!allowUnitlessConversion) {
            return convertTo(unit);
        }
        if (unit.isNeutral() && !getUnit().isUndefined()) {
            return new Quantity(getAmount(), unit);
        }
        if (getUnit().isNeutral() && !unit.isUndefined()) {
            return new Quantity(getAmount(), unit);
        }
        return convertTo(unit);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     */

    public int compareTo(Object o) {
        Quantity other = (Quantity) o;
        if (!getBaseUnit().equals(other.getBaseUnit())) {
            throw new ClassCastException("Units do not match");
        }
        int scaleDifference = this.getUnit().getScale() - other.getUnit().getScale();
        BigDecimal testAmount = getAmount().movePointRight(scaleDifference);
        return testAmount.compareTo(other.getAmount());
    }

    /**
     * Indicates whether some other object is "equal to" the receiver.
     *
     * @param o the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        try {
            Quantity other = (Quantity) o;
            return
                    this.getUnit().equals(other.getUnit()) &&
                            this.getAmount().equals(other.getAmount());
        } catch (ClassCastException ex) {
            return false;
        }
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return getAmount().hashCode();
    }

    /**
     * Returns a string representation of the receiver
     *
     * @return a string representation
     */
    public String toString() {
        return "" + getAmount() + " " + getUnit();
    }

    /**
     * multiplies the receiver with the argument
     *
     * @param factor factor
     * @return a new quantity
     */
    public Quantity multiply(BigDecimal factor) {
        return new Quantity(getAmount().multiply(factor), getUnit());
    }


    /**
     * Returns the receiver's amount as a double
     *
     * @return the amount
     */
    public double doubleValue() {
        return amount.doubleValue();
    }

    /**
     * Returns the receiver's amount as a float
     *
     * @return the amount
     */
    public float floatValue() {
        return amount.floatValue();
    }

    /**
     * Returns the receiver's amount as an int
     *
     * @return the amount
     */
    public int intValue() {
        return amount.intValue();
    }

    /**
     * Returns the receiver's amount as a long
     *
     * @return the amount
     */
    public long longValue() {
        return amount.longValue();
    }

    /**
     * checks if this Quantity has no unit (undefined)
     *
     * @return true if this Quantity has no unit
     */
    public boolean isUnitless() {
        return getUnit().isUndefined();
    }
}
