package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.core.Utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * CellRenderer used to render enums
 * The enum's value is translated using a Translator
 * Date: 11/03/13
 * Time: 13:26
 */
public class LocalizedEnumTableCellRenderer extends DefaultTableCellRenderer implements ColumnWidthCalculator {

    private Class<? extends Enum> enumClass;
    private boolean classNameAsPrefix = false;

    public LocalizedEnumTableCellRenderer(Class<? extends Enum> enumClass) {
        this.enumClass = enumClass;
    }

    public LocalizedEnumTableCellRenderer(Class<? extends Enum> enumClass, boolean classNameAsPrefix) {
        this.enumClass = enumClass;
        this.classNameAsPrefix = classNameAsPrefix;
    }

    public void setClassNameAsPrefix(boolean flag) {
        this.classNameAsPrefix = flag;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null) {
            setText(Utils.translateEnum((Enum) value, this.classNameAsPrefix));
        }
        return this;
    }

    @Override
    public int calcPreferredWidth() {
        int max = Integer.MIN_VALUE;
        Enum[] values = enumClass.getEnumConstants();
        for (Enum value : values) {
            String enumName = Utils.translateEnum(value, this.classNameAsPrefix);
            int width = this.getFontMetrics(this.getFont()).stringWidth(enumName);
            if (width > max) {
                max = width;
            }
        }
        return Math.max(0, max);
    }
}
