package com.energyict.mdc.engine.offline.gui.core;

import com.energyict.mdc.engine.offline.core.FormatProvider;
import com.energyict.mdc.engine.offline.core.Utils;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;

public class BigDecimalDocument extends PlainDocument {

    /**
     * The format the data should have.
     */
    protected DecimalFormat format = FormatProvider.instance.get().getFormatPreferences().getNumberFormat();
    private BigDecimal maximumValue = null; // The maximum value
    private boolean includeMaxValue = true; // is the max value allowed (included) or not
    private BigDecimal minimumValue = null; // The minimum value
    private boolean includeMinValue = true; // is the min value allowed (included) or not
    private BigDecimal zero = new BigDecimal("0.0");
    private ParsePosition parsePos = new ParsePosition(0);
    private boolean fireRemoveUpdate = true; // used to disable firing remove updates during replace operation


    public BigDecimalDocument() {
        format = FormatProvider.instance.get().getFormatPreferences().getNumberFormat();
        format.setParseBigDecimal(true);
    }

    public void setBigDecimal(BigDecimal value) throws BadLocationException {
        if (value == null) {
            remove(0, getLength());
            return;
        }
        try {
            fireRemoveUpdate = false;
            replace(0, getLength(), format.format(value), null);
        } finally {
            fireRemoveUpdate = true;
        }
    }

    @Override
    protected void fireRemoveUpdate(DocumentEvent e) {
        if (fireRemoveUpdate) {
            super.fireRemoveUpdate(e);
        }
    }

    public BigDecimal getBigDecimal() throws ParseException, BadLocationException {
        return getBigDecimal(getText(0, getLength()));
    }

    private BigDecimal getBigDecimal(String strValue) throws ParseException, BadLocationException {
        if (Utils.isNull(strValue)) {
            return null;
        }
        parsePos.setIndex(0);
        Number value = format.parse(strValue, parsePos);
        if (parsePos.getIndex() != strValue.length()) {
            throw new ParseException(
                    "Not a valid number: " + strValue, 0);
        }
        return new BigDecimal(value.toString());
    }

    public void setFormat(DecimalFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format cannot be null");
        }
        this.format = format;
        this.format.setParseBigDecimal(true);
        this.format.setParseIntegerOnly(this.format.getMaximumFractionDigits() == 0);
        try {
            reformat();
            notifyDocumentListenerSettingWasChanged();
        } catch (ParseException | BadLocationException e) {
            // do nothing;
        }
    }

    public void setMaximumValue(BigDecimal max) {
        if (minimumValue == null || max.compareTo(minimumValue) >= 0) {
            this.maximumValue = max;
            notifyDocumentListenerSettingWasChanged();
        }
    }

    public void setIncludeMaxValue(boolean include) {
        this.includeMaxValue = include;
        notifyDocumentListenerSettingWasChanged();
    }

    public void setMinimumValue(BigDecimal min) {
        if (maximumValue == null || maximumValue.compareTo(min) >= 0) {
            this.minimumValue = min;
            notifyDocumentListenerSettingWasChanged();
        }
    }
    @SuppressWarnings("unused")
    public void setIncludeMinValue(boolean include) {
        this.includeMinValue = include;
        notifyDocumentListenerSettingWasChanged();
    }

    public boolean isValid(){
        try{
            BigDecimal value = getBigDecimal();
            return value == null || checkBounds(value);
        }catch ( ParseException | BadLocationException ex){
            return false;
        }
    }

    public void insertString(int index, String s, AttributeSet a) throws BadLocationException {
        if (s == null || s.length() == 0) {
            return;
        }

        StringBuilder t = new StringBuilder(getLength() + s.length());
        t.append(getText(0, index));
        t.append(s);
        t.append(getText(index, getLength() - index));

        String strValue = t.toString().trim();

        if (isValidInput(strValue)) {
            super.insertString(index, s, a);
        }
    }

    public void reformat() throws ParseException, BadLocationException {
        String strValue = getText(0, getLength());
        if (!Utils.isNull(strValue)) {
            setBigDecimal(getBigDecimal(strValue));
        }
    }

    private void notifyDocumentListenerSettingWasChanged(){
        fireChangedUpdate(new DefaultDocumentEvent(0, getLength(),DocumentEvent.EventType.CHANGE));
    }

    private boolean isDecimalFormatSymbol(String s) {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        String groupSeparator = String.valueOf(symbols.getGroupingSeparator());
        String decimalSeparator = String.valueOf(symbols.getDecimalSeparator());
        return (groupSeparator).equals(s) || (decimalSeparator).equals(s);
    }

    private boolean isMinusSign(String s) {
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        String minus = String.valueOf(symbols.getMinusSign());
        return minus.equals(s);
    }

    private boolean isPlusSign(String s) {
        return ("+".equals(s));
    }

    private boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException exc) {
            return false;
        }
    }

    protected boolean isValidInput(String strValue) {
        if (Utils.isNull(strValue)) {
            return true;
        }
        // check each individual character
        boolean valid = true;
        for (int i = 0; i < strValue.length() && valid; i++) {
            String eachCharacter = strValue.substring(i, i + 1);
            valid = isValidCharacter(eachCharacter, i);
        }
        if (!valid) {
            return false;
        }
        if (strValue.length() == 1 && isMinusSign(strValue)) {  // a minus sign could not be entered as first character
            return isMinusSignAllowed();
        }

        try {
            BigDecimal value = this.getBigDecimal(strValue);
            return checkNbrOfDigits(value);
        } catch (ParseException | BadLocationException ex) {
            return false;
        }
    }

    protected boolean isValidCharacter(String s, int offset) {
        return isNumeric(s) ||
                isDecimalFormatSymbol(s) ||
                (isMinusSign(s) && offset == 0 && isMinusSignAllowed()) ||
                (isPlusSign(s) && offset == 0);
    }

    /**
     * Subclasses can use this for checking the number of integer digits and the number of fractiondigits
     *
     * @return By default always return true
     */
    protected boolean checkNbrOfDigits(BigDecimal value) {
        return true; // No check on number of digits
    }

    protected boolean isMinusSignAllowed() {
        return  (minimumValue == null || minimumValue.compareTo(zero) < 0);
    }

    private boolean checkBounds(BigDecimal value){
        if (minimumValue == null && maximumValue == null){
            return true;
        }
        if (minimumValue == null){
           return (maximumValue.compareTo(value) > 0 || (includeMaxValue && maximumValue.compareTo(value) == 0));
        }
        if (maximumValue == null){
           return (minimumValue.compareTo(value) < 0 || (includeMinValue && minimumValue.compareTo(value) == 0));
        }
        return (minimumValue.compareTo(value) < 0 || (includeMinValue && minimumValue.compareTo(value) == 0)) &&
               (maximumValue.compareTo(value) > 0 || (includeMaxValue && maximumValue.compareTo(value) == 0));
    }
}