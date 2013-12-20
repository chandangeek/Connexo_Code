package com.energyict.mdc.common.coordinates;

import com.energyict.mdc.common.FormatPreferences;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public abstract class AbstractWorldCoordinate implements WorldCoordinate {

    private DecimalFormat formatter = new FormatPreferences().getNumberFormat("#0.#####");

    private BigDecimal value;

    public AbstractWorldCoordinate() {
        super();
    }

    public AbstractWorldCoordinate(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public int getSign() {
        return value.signum();
    }

    public String toString() {
        if (value == null) {
            return "Undefined";
        } else {
            return (getSign() == -1 ? "-" : "") +
                    getDegrees() + "\u00B0 " +
                    getMinutes() + "' " +
                    formatter.format(getSeconds()) + "''";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractWorldCoordinate)) {
            return false;
        }

        AbstractWorldCoordinate that = (AbstractWorldCoordinate) o;

        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
