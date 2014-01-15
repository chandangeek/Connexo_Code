package com.energyict.cpo;

import com.energyict.dynamicattributes.HexStringFactory;
import com.energyict.mdc.common.HexString;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;

/**
 *  Defines a PropertySpec containing a {@link HexString} of a fixed length
 *
 *  @author sva
 * @since 29/10/13 - 12:17
 */
public class FixedLengthHexStringPropertySpec extends BasicPropertySpec<HexString> {

    /** The desired length of the HexString, expressed in nr of bytes **/
    private int length;

    /**
     * a PropertySpec for properties of type HexString having a desired length
     * @param name  for the property
     * @param length required length of the string
     */
    public FixedLengthHexStringPropertySpec(String name, int length){
        super(name, new HexStringFactory(),new ValueDomain(HexString.class));
        this.length = length;
    }

    /**
     * Getter for the desired length of the {@link HexString}, expressed as nr of bytes <br/>
     * E.g. a length of 16 corresponds to 16 bytes/32 HEX chars
     */
    public int getLength() {
        return length;
    }

    /**
     * Getter for the desired length of the {@link HexString}, expressed as character count.
     */
    private int getCharCount() {
        return length * 2;
    }

    @Override
    public boolean validateValue(HexString value, boolean isRequired) throws InvalidValueException {
        super.validateValue(value, isRequired);
        if (value.getContent().length() != getCharCount()) {
            throw new InvalidValueException("XhasInvalidLength", "The value \"{1}\" of attribute specification {0} should be {2} characters long.", this.getKey(), value, getCharCount());
        }
        return true;
    }

}