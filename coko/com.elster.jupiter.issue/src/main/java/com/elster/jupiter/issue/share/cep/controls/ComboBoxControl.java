package com.elster.jupiter.issue.share.cep.controls;

public class ComboBoxControl extends SimpleControl{
    public static final SimpleControl COMBOBOX = new ComboBoxControl();

    public ComboBoxControl() {
        super("combobox");
    }

    public static class Values {
        public Object id;
        public Object title;
    }
}
