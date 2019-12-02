package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.MdwIcons;

import javax.swing.*;

public class IconListCellRenderer extends JLabel implements ListCellRenderer {

    public IconListCellRenderer() {
        setOpaque(true);
    }

    public java.awt.Component getListCellRendererComponent(
            javax.swing.JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            setIcon(MdwIcons.MISSINGOBJECT_ICON);
        } else {
            if (value instanceof String) {
                String iconPath = (String) value;
                setText(iconPath);
                setIcon(MdwIcons.getCustomIcon(iconPath));
            }
        }
        setBackground(isSelected ?
                list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ?
                list.getSelectionForeground() : list.getForeground());
        return this;
    }
}