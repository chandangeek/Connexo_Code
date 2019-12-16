package com.energyict.mdc.engine.offline.gui.util;

import com.energyict.mdc.engine.offline.gui.UiHelper;

import java.util.prefs.Preferences;

public class ValidationOverviewFilterSettings {

    private int validation;
    private int upToDate;
    private int upToDateType;
    private int treshold;

    private String prefKey = EisConst.PREFKEY_VALIDATIONOVERVIEWFILTER;
    private static String KEY_VALIDATION = ".validation";
    private static String KEY_UPTODATE = ".uptodate";
    private static String KEY_UPTODATETYPE = ".uptodatetype";
    private static String KEY_TRESHOLD = ".treshold";


    public ValidationOverviewFilterSettings() {
    }

    /* 
    *  Store this class's attributes in the registry
    */
    public void store() {
        Preferences userPrefs = UiHelper.getAdditionalUserPreferences();
        userPrefs.putInt(prefKey + KEY_VALIDATION, validation);
        userPrefs.putInt(prefKey + KEY_UPTODATE, upToDate);
        userPrefs.putInt(prefKey + KEY_UPTODATETYPE, upToDateType);
        userPrefs.putInt(prefKey + KEY_TRESHOLD, treshold);
    }

    /* 
    *  Restore this class's attributes from the registry
    */
    public void restore() {
        Preferences userPrefs = UiHelper.getAdditionalUserPreferences();
        validation = userPrefs.getInt(prefKey + KEY_VALIDATION, 1);
        upToDate = userPrefs.getInt(prefKey + KEY_UPTODATE, 0);
        upToDateType = userPrefs.getInt(prefKey + KEY_UPTODATETYPE, 0);
        treshold = userPrefs.getInt(prefKey + KEY_TRESHOLD, 0);
    }

    // getters/setters
    public int getValidation() {
        return validation;
    }

    public void setValidation(int validation) {
        this.validation = validation;
    }

    public int getUpToDate() {
        return upToDate;
    }

    public void setUpToDate(int upToDate) {
        this.upToDate = upToDate;
    }

    public int getUpToDateType() {
        return upToDateType;
    }

    public void setUpToDateType(int upToDateType) {
        this.upToDateType = upToDateType;
    }

    public int getTreshold() {
        return treshold;
    }

    public void setTreshold(int treshold) {
        this.treshold = treshold;
    }
}
