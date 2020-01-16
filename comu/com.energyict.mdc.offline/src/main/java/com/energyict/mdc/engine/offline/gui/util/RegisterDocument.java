package com.energyict.mdc.engine.offline.gui.util;

import com.energyict.mdc.engine.offline.core.FormatProvider;
import com.energyict.mdc.engine.offline.gui.core.BigDecimalDocument;
import com.energyict.mdc.engine.offline.gui.models.RegisterConfiguration;

import javax.swing.text.BadLocationException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This Document can be used for editing RtuRegisterValues
 * It takes into account the number of Digits and the number of fraction digits defined for the register
 * <p/>
 * example:  AspectEditor registerEditor = builder.getEditor("value");
 * valueField = (JBigDecimalField)registerEditor.getValueComponent();
 * valueField.setDocument(new RegisterDocument(rtuRegister.getRtuRegisterSpec()));
 */
public class RegisterDocument extends BigDecimalDocument {

    private int nbrOfNonDecimals;
    private int nbrOfFractionDigits;
    private final boolean negativeSign;

    public RegisterDocument() {
        this(10, 3, false);
    }

    public RegisterDocument(RegisterConfiguration registerConfiguration) {
        this(registerConfiguration.getNumberOfDigits(), registerConfiguration.getNumberOfFractionDigits(), false);
    }

    /**
     * Constructor with several configurations. Alternative separator can be
     * given in order to make input easier. Eg. Comma and point can be used
     * for decimal separation.
     *
     * @param nbrOfDigits         Maximum number of non-decimals.
     * @param nbrOfFractionDigits Maximum number of decimals.
     * @param negativeSign        Negative sign allowed.
     */
    public RegisterDocument(int nbrOfDigits, int nbrOfFractionDigits, boolean negativeSign) {
        this.nbrOfNonDecimals = nbrOfDigits;
        this.nbrOfFractionDigits = nbrOfFractionDigits;
        this.negativeSign = negativeSign;
        super.setFormat(getDecimalFormat());
    }

    @Override
    protected boolean isMinusSignAllowed() {
        return negativeSign;
    }

    @Override
    protected boolean checkNbrOfDigits(BigDecimal value) {
        if (value == null) {
            return true;
        }
        // Check the number of Integer Digits
        NumberFormat integerFormat = NumberFormat.getIntegerInstance();
        integerFormat.setGroupingUsed(false);
        if (integerFormat.format(value.intValue()).length() <= this.nbrOfNonDecimals) {
            // Check number of fraction digits
            DecimalFormat defaultFormat = FormatProvider.instance.get().getFormatPreferences().getNumberFormat();
            defaultFormat.setGroupingUsed(false);
            String stringValue = defaultFormat.format(value);
            int decimalPointPosition = stringValue.indexOf(defaultFormat.getDecimalFormatSymbols().getDecimalSeparator());
            if (decimalPointPosition <= 0 || decimalPointPosition == stringValue.length()) {
                return true;
            } else {
                return stringValue.substring(decimalPointPosition + 1).length() <= this.nbrOfFractionDigits;
            }
        } else {
            return false;
        }
    }

    private DecimalFormat getDecimalFormat() {
        DecimalFormat format = FormatProvider.instance.get().getFormatPreferences().getNumberFormat();
        if (nbrOfNonDecimals > 0) {
            format.setMaximumIntegerDigits(nbrOfNonDecimals);
        }
        if (nbrOfFractionDigits >= 0) {
            format.setMaximumFractionDigits(nbrOfFractionDigits);
            format.setMinimumFractionDigits(nbrOfFractionDigits);
        }
        return format;
    }

    @Override
    public void setBigDecimal(BigDecimal value) throws BadLocationException {
        super.setBigDecimal(scaled(value));
    }

    private BigDecimal scaled(BigDecimal value) {
        if (value != null && nbrOfFractionDigits != 0) {
            return value.setScale(nbrOfFractionDigits, BigDecimal.ROUND_HALF_UP);
        }
        return value;
    }
}
