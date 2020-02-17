package com.energyict.mdc.engine.offline.gui.windows.taskinfo;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.offline.gui.UiHelper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class ComTasksInfoPnl extends JScrollPane {

    private List<ComTaskExecution> comTaskExecutions;

    private JPanel mainPnl;

    public ComTasksInfoPnl(List<ComTaskExecution> comTaskExecutions) {
        this.comTaskExecutions = comTaskExecutions;
        initComponents();
    }

    private void initComponents() {
        setBorder(null);
        setViewportView(getMainPnl());
    }

    private JPanel getMainPnl() {
        if (mainPnl == null) {
            GridBagLayout layout = new GridBagLayout();
            layout.columnWidths = new int[]{10, 105, 0, 5, 0};
            layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            layout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0E-4};
            layout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
            mainPnl = new JPanel(layout);

            mainPnl.setBorder(new TitledBorder(null, UiHelper.translate("communicationTasks"), TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 16)));
            int labelColumn = 1;
            int fieldColumn = 2;
            int yCoordinate = -1;
            int taskCounter = 0;

            for (ComTaskExecution eachExecution : comTaskExecutions) {
                GridBagConstraints gbc = new GridBagConstraints(
                        labelColumn, ++yCoordinate, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0);

                if (comTaskExecutions.size() > 1) {
                    gbc.gridx = fieldColumn;
                    gbc.insets = new Insets(10, 0, 5, 5);
                    JLabel label = new JLabel(UiHelper.translate("comTask") + " " + (++taskCounter));
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    mainPnl.add(label, gbc);
                    gbc.gridx = labelColumn;
                    gbc.gridy = ++yCoordinate;
                    gbc.insets = new Insets(0, 0, 5, 5);
                }

                mainPnl.add(new JLabel(UiHelper.translate("name") + ":"), gbc);
                JTextField nameField = new JTextField();
                nameField.setEditable(false);
                nameField.setText(eachExecution.getComTask().getName());
                gbc.gridx = fieldColumn;
                mainPnl.add(nameField, gbc);

                gbc.gridy = ++yCoordinate;
                gbc.gridx = labelColumn;
                mainPnl.add(new JLabel(UiHelper.translate("schedule") + ":"), gbc);
                JTextField scheduleField = new JTextField();
                scheduleField.setEditable(false);
                scheduleField.setText(
                        eachExecution.getNextExecutionSpecs().isPresent()
                                ? eachExecution.getNextExecutionSpecs().get().getTemporalExpression().toString()
                                : UiHelper.translate("none")
                );
                gbc.gridx = fieldColumn;
                mainPnl.add(scheduleField, gbc);

                gbc.gridy = ++yCoordinate;
                gbc.gridx = labelColumn;
                mainPnl.add(new JLabel(UiHelper.translate("urgency") + ":"), gbc);
                JTextField urgencyField = new JTextField();
                urgencyField.setEditable(false);
//                urgencyField.setText(eachExecution.getPriority() + "");
                gbc.gridx = fieldColumn;
                mainPnl.add(urgencyField, gbc);
            }
        }
        return mainPnl;
    }
}
