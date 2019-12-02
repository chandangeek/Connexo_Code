package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.model.TaskState;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TaskStateTableCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component defaultRenderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null && value instanceof TaskState) {
            TaskState state = (TaskState)value;
            setText(UiHelper.translate(state.getTranslationKey()));

            Color backGroundColor = defaultRenderer.getBackground();
            Color foreGroundColor = defaultRenderer.getForeground();
            if (state.equals(TaskState.READ_FAILED)) {
                backGroundColor = new Color(255, 155, 155); // red
                foreGroundColor = Color.BLACK;
            } else if (state.equals(TaskState.READ_SUCCESS)) {
                backGroundColor = new Color(200, 255, 200); // green
                foreGroundColor = Color.BLACK;
            } else {
                foreGroundColor = isSelected ? table.getSelectionForeground() : Color.BLACK;
                backGroundColor = isSelected ? table.getSelectionBackground() : Color.WHITE;
            }
            setForeground(foreGroundColor);
            setBackground(backGroundColor);
        }
        return this;
    }

}
