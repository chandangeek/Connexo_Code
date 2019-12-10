package com.energyict.mdc.engine.offline.gui.fieldediting;


import com.energyict.mdc.engine.offline.gui.windows.EisPropsPnl;

public abstract class FieldEditingPnl extends EisPropsPnl {

    public abstract void setFieldText(String text);

    public abstract String getFieldText();

    public abstract boolean isCanceled();

    public void initForView() {
    }
}
