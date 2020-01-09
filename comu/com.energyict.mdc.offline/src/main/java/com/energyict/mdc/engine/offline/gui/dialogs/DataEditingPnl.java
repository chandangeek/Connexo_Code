package com.energyict.mdc.engine.offline.gui.dialogs;

import java.awt.event.KeyEvent;

public interface DataEditingPnl {

    boolean isDataDirty();

    void performEscapeAction();

    boolean performEnterAction(KeyEvent evt);

}
