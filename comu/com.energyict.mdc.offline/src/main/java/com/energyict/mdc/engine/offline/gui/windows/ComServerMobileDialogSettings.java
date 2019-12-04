package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.gui.UiHelper;

import java.util.prefs.Preferences;

public class ComServerMobileDialogSettings {

    static public int DO_CENTER = -9999;
    private String prefKey;
    private String KEY_HEIGHT = ".height";
    private String KEY_WIDTH = ".width";
    private String KEY_POSX = ".x";
    private String KEY_POSY = ".y";
    private int preferredWidth;
    private int preferredHeight;
    private int posX = DO_CENTER;
    private int posY = DO_CENTER;
    private int maxWidth;
    private int maxHeight;

    public ComServerMobileDialogSettings(String prefKey, int initalWidth, int initialHeight) {
        this.prefKey = prefKey;
        this.preferredWidth = initalWidth;
        this.preferredHeight = initialHeight;
    }

    public String getPrefKey() {
        return prefKey;
    }

    public void setPrefKey(String prefKey) {
        this.prefKey = prefKey;
    }

    public int getPreferredWidth() {
        return preferredWidth;
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }

    public int getPreferredHeight() {
        return preferredHeight;
    }

    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void restore() {
        Preferences userPrefs = UiHelper.getUserPreferences();
        // Make sure the width & height falls inside the screen boundaries
        setPreferredWidth( Math.min(maxWidth, userPrefs.getInt(getPrefKey() + KEY_WIDTH, preferredWidth)) );
        setPreferredHeight( Math.min(maxHeight, userPrefs.getInt(getPrefKey() + KEY_HEIGHT, preferredHeight)) );
        // Make sure the x and y are > 0 (exception: DO_CENTER initially)
        int newPosX = userPrefs.getInt(getPrefKey() + KEY_POSX, DO_CENTER);
        int newPosY = userPrefs.getInt(getPrefKey() + KEY_POSY, DO_CENTER);
        if (newPosX!=DO_CENTER || newPosY!=DO_CENTER) {
            setPosX(Math.max(0, newPosX));
            setPosY(Math.max(0, newPosY));
        }
    }

    public void store() {
        Preferences userPrefs = UiHelper.getUserPreferences();
        userPrefs.putInt(getPrefKey() + KEY_WIDTH, getPreferredWidth());
        userPrefs.putInt(getPrefKey() + KEY_HEIGHT, getPreferredHeight());
        userPrefs.putInt(getPrefKey() + KEY_POSX, getPosX());
        userPrefs.putInt(getPrefKey() + KEY_POSY, getPosY());
    }
}
