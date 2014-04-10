package com.energyict.mdc.pluggable.rest.impl.properties.validators;

import com.energyict.mdc.pluggable.rest.impl.properties.PropertyValidationRule;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines rules/options to validate a <i>Number</i>
 * <p/>
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 13:28
 */
@XmlRootElement
public class NumberValidationRules<T> implements PropertyValidationRule {

    public Boolean allowDecimals;
    public T minimumValue;
    public T maximumValue;
    public Integer minimumDigits;
    public Integer maximumDigits;
    public Boolean even;

     public Boolean getAllowDecimals() {
        return allowDecimals;
    }

    public void setAllowDecimals(Boolean allowDecimals) {
        this.allowDecimals = allowDecimals;
    }

    public T getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(T minimumValue) {
        this.minimumValue = minimumValue;
    }

    public T getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(T maximumValue) {
        this.maximumValue = maximumValue;
    }

    public Integer getMinimumDigits() {
        return minimumDigits;
    }

    public void setMinimumDigits(Integer minimumDigits) {
        this.minimumDigits = minimumDigits;
    }

    public Integer getMaximumDigits() {
        return maximumDigits;
    }

    public void setMaximumDigits(Integer maximumDigits) {
        this.maximumDigits = maximumDigits;
    }

    public Boolean getEven() {
        return even;
    }

    public void setEven(Boolean even) {
        this.even = even;
    }
}
