package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.core.Utils;

import javax.swing.*;
import java.awt.*;

/**
 * CellRenderer used to render enums
 * The enum's value is translated using a Translator
 * Date: 11/03/13
 * Time: 13:26
 */
public class LocalizedEnumListCellRenderer extends DefaultListCellRenderer {

    private boolean classNameAsPrefix = false;

    public LocalizedEnumListCellRenderer(){
    }

    public void setClassNameAsPrefix(boolean flag){
        this.classNameAsPrefix = flag;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value != null) {
            setText(Utils.translateEnum((Enum) value, this.classNameAsPrefix));
        }
        return this;
    }
}
