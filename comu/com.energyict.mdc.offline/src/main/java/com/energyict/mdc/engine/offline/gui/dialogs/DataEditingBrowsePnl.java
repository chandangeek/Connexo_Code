package com.energyict.mdc.engine.offline.gui.dialogs;

public interface DataEditingBrowsePnl {

    boolean isDataChanged();

    String getSaveChangesYesNoCancelQuestion();

    void saveData();
}
