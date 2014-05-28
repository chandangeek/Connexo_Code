package com.elster.jupiter.issue.share.cep.controls;

import com.elster.jupiter.issue.share.cep.ParameterControl;

public class SimpleControl implements ParameterControl {
    public static final SimpleControl TEXT_FIELD = new SimpleControl("textfield");
    public static final SimpleControl NUMBER_FIELD = new SimpleControl("numberfield");

    private String xtype;

    public SimpleControl(String xtype) {
        this.xtype = xtype;
    }

    @Override
    public String getXtype() {
        return xtype;
    }
}
