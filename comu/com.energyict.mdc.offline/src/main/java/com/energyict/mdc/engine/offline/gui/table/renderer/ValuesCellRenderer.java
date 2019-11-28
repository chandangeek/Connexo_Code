package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import java.awt.*;

public class ValuesCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null){
            this.setText(TranslatorProvider.instance.get().getTranslator().getTranslation(value.toString()));
        }
        return this;
    }
}
