package com.energyict.cpo;

import com.energyict.dynamicattributes.StringFactory;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

/**
 *  Defines a PropertySpec containing a {@link String} of a fixed length
 *
 *  @author sva
 * @since 29/10/13 - 12:17
 */
public class FixedLengthStringPropertySpec extends BasicPropertySpec<String> {

    /** The desired length of the String, expressed in nr of bytes **/
    private int length;

    /**
     * a PropertySpec for properties of type String having a desired length
     * @param name  for the property
     * @param length required length of the string
     */
    public FixedLengthStringPropertySpec(String name, int length){
        super(name, new StringFactory(),new ValueDomain(String.class));
        this.length = length;
    }

    /**
     * Getter for the desired length of the String
     */
    public int getLength() {
        return length;
    }

    @Override
    public boolean validateValue(String value, boolean isRequired) throws InvalidValueException {
        super.validateValue(value, isRequired);
        if (value.length() != getLength()) {
            throw new InvalidValueException("XhasInvalidLength", "The value \"{1}\" of attribute specification {0} should should be {2} characters long.", this.getKey(), value, getLength());
        }
        return true;
    }

}