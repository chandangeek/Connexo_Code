package com.energyict.mdc.engine.offline.gui.core;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.math.BigInteger;

/**
 * User: gde
 * Date: 17/04/13
 */
public class HexStringDocument extends PlainDocument {

    /** The maximum length of the HexString, expressed in nr of bytes **/
    private int maximumLength = -1;

    @Override
    public void insertString(int index, String s, AttributeSet a) throws BadLocationException {
        if (s == null || s.length() == 0) {
            return;
        }
        StringBuilder t = new StringBuilder(getLength() + s.length());
        t.append(getText(0, index));
        t.append(s.toUpperCase());
        t.append(getText(index, getLength() - index));
        try {
            new BigInteger(t.toString().trim(), 16);
        } catch (NumberFormatException e) {
            return;
        }
        if(respectsLengthLimitation(t)) {
            super.insertString(index, s.toUpperCase(), a);
        }
    }

    /**
     * Validate the length limitation is respected.
     *
     * @param builder the StringBuilder to validate
     * @return  true if the length of the HexString is below or equal the maximum length
     *          false if the length of the HexString exceeds the maximum length
     */
    private boolean respectsLengthLimitation(StringBuilder builder) {
        if (getMaximumLength() != -1) {
            if (builder.length() > getMaximumCharCount()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Getter for the maximum length of the {@link com.energyict.cbo.HexString}, expressed in nr of bytes <br/>
     * E.g. a length of 16 corresponds to 16 bytes/32 HEX chars
     */
    public int getMaximumLength() {
        return maximumLength;
    }

    /**
     * Getter for the desired length of the {@link com.energyict.cbo.HexString}, expressed as character count.
     */
    private int getMaximumCharCount() {
        return maximumLength * 2;
    }

    /**
     * Setter for the maximum length of the {@link com.energyict.cbo.HexString}, expressed in nr of bytes <br/>
     * @param maximumLength the nr of bytes
     */
    public void setMaximumLength(int maximumLength) {
        this.maximumLength = maximumLength;
    }
}
