package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public class LevelListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null){
            this.setText(TranslatorProvider.instance.get().getTranslator().getTranslation(((Level) value).getName()));
        }
        return this;
    }
}
