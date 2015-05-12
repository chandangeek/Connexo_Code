package com.elster.jupiter.properties;

/**
 * Created by dvy on 11/05/2015.
 */
public class BoundedLongPropertySpecImpl extends BasicPropertySpec implements BoundedLongPropertySpec {

    private Long lowerLimit;
    private Long upperLimit;

    /**
     * a PropertySpec for properties of type Long having values between the lower and upper limit (included)
     *
     * @param name for the property
     * @param lowerLimit smallest value allowed
     * @param upperLimit greates value allowed
     */
    public BoundedLongPropertySpecImpl(String name, Long lowerLimit, Long upperLimit) {
        super(name, new LongFactory());
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public Long getLowerLimit () {
        return lowerLimit;
    }

    public Long getUpperLimit () {
        return upperLimit;
    }

    @Override
    public boolean validateValue(Object objectValue) throws InvalidValueException {
        if (objectValue instanceof Long) {
            Long value = (Long) objectValue;
            boolean valid = super.validateValue(value);
            if (lowerLimit != null && value.compareTo(lowerLimit)<0) {
                throw new InvalidValueException("XisTooLow", "The objectValue is too small", this.getName(), lowerLimit, upperLimit);
            }
            if (upperLimit != null && value.compareTo(upperLimit)>0) {
                throw new InvalidValueException("XisTooHigh", "The objectValue is too high", this.getName(), lowerLimit, upperLimit);
            }
            return valid;
        }
        else {
            return false;
        }
    }
}
