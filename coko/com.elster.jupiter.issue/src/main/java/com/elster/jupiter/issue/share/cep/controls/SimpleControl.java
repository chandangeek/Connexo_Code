package com.elster.jupiter.issue.share.cep.controls;

import com.elster.jupiter.issue.share.cep.ParameterControl;

public class SimpleControl implements ParameterControl {
    public static final SimpleControl TEXT_FIELD = new SimpleControl("textfield");
    public static final SimpleControl TEXT_AREA = new SimpleControl("textArea");
    public static final SimpleControl NUMBER_FIELD = new SimpleControl("numberfield");
    public static final SimpleControl CHECKBOX_FIELD = new SimpleControl("checkBox");

    private String xtype;

    public SimpleControl(String xtype) {
        this.xtype = xtype;
    }

    @Override
    public String getXtype() {
        return xtype;
    }
}
