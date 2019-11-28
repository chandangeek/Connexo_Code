package com.energyict.mdc.engine.offline;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.engine.offline.core.FormatPreferences;
import com.energyict.mdc.engine.offline.core.FormatProvider;
import com.energyict.mdc.engine.offline.core.Translator;

import java.io.*;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class DefaultFormatProvider implements FormatProvider {

    private Properties properties ;
    private FormatPreferences formatPreferences;

    public DefaultFormatProvider() {
    }

    public FormatPreferences getFormatPreferences() {
        if (formatPreferences == null) {
            formatPreferences = new FormatPreferences();
        }
        return formatPreferences;
    }

    public void setFormatPreferences(FormatPreferences formatPreferences) {
        this.formatPreferences = formatPreferences;
    }

    public DecimalFormatSymbols getDecimalFormatSymbols() {
        return getFormatPreferences().getDecimalFormatSymbols();
    }

    public char getDecimalSeparator() {
        return getDecimalFormatSymbols().getDecimalSeparator();
    }

    public char getGroupingSeparator() {
        return getDecimalFormatSymbols().getGroupingSeparator();
    }


}
