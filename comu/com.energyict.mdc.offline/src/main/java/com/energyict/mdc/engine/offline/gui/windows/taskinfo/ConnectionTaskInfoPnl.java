package com.energyict.mdc.engine.offline.gui.windows.taskinfo;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTaskProperty;
import com.energyict.mdc.engine.offline.gui.UiHelper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class ConnectionTaskInfoPnl extends JScrollPane {

    private ConnectionTask connectionTask;

    private JPanel mainPnl;

    public ConnectionTaskInfoPnl(ConnectionTask connectionTask) {
        this.connectionTask = connectionTask;
        initComponents();
    }

    private void initComponents() {
        setBorder(null);
        setViewportView(getMainPnl());
    }

    private JPanel getMainPnl() {
        if (mainPnl==null) {
            GridBagLayout layout = new GridBagLayout();
            layout.columnWidths = new int[]{10, 105, 0, 5, 0};
            layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            layout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0E-4};
            layout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
            mainPnl = new JPanel(layout);

            mainPnl.setBorder(new TitledBorder(null, UiHelper.translate("connectionTask"), TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 16)));
            int labelColumn = 1;
            int fieldColumn = 2;
            int yCoordinate = 0;

            GridBagConstraints gbc = new GridBagConstraints(
                labelColumn, yCoordinate, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0);
            mainPnl.add(new JLabel(UiHelper.translate("name")+":"), gbc);
            JTextField nameField = new JTextField();
            nameField.setEditable(false);
            nameField.setText(connectionTask.getName());
            gbc.gridx = fieldColumn;
            mainPnl.add(nameField, gbc);

            gbc.gridy = ++yCoordinate;
            gbc.gridx = labelColumn;
            mainPnl.add(new JLabel(UiHelper.translate("comPortPool")+":"), gbc);
            JTextField poolField = new JTextField();
            poolField.setEditable(false);
            poolField.setText(connectionTask.getComPortPool().getName());
            gbc.gridx = fieldColumn;
            mainPnl.add(poolField, gbc);

            gbc.gridy = ++yCoordinate;
            gbc.gridx = labelColumn;
            mainPnl.add(new JLabel(UiHelper.translate("protocolDialect") + ":"), gbc);
            JTextField dialectField = new JTextField();
            dialectField.setEditable(false);
            ProtocolDialectConfigurationProperties dialectConfigurationProperties = connectionTask.getProtocolDialectConfigurationProperties();
            dialectField.setText(dialectConfigurationProperties == null ? "" : dialectConfigurationProperties.getName());
            gbc.gridx = fieldColumn;
            mainPnl.add(dialectField, gbc);

            if (connectionTask.getConnectionType().getDirection().equals(ConnectionType.ConnectionTypeDirection.OUTBOUND)) {

                gbc.gridy = ++yCoordinate;
                gbc.gridx = labelColumn;
                mainPnl.add(new JLabel(UiHelper.translate("connectionStrategy")+":"), gbc);
                JTextField strategyField = new JTextField();
                strategyField.setEditable(false);
                strategyField.setText(((ScheduledConnectionTask)connectionTask).getConnectionStrategy().name());
                gbc.gridx = fieldColumn;
                mainPnl.add(strategyField, gbc);

                gbc.gridy = ++yCoordinate;
                gbc.gridx = labelColumn;
                mainPnl.add(new JLabel(UiHelper.translate("connectionSchedule")+":"), gbc);
                JTextField scheduleField = new JTextField();
                scheduleField.setEditable(false);
                scheduleField.setText(
                        ((ScheduledConnectionTask)connectionTask).getNextExecutionSpecs() != null
                                ? ((ScheduledConnectionTask)connectionTask).getNextExecutionSpecs().getTemporalExpression().toString()
                                : UiHelper.translate("none")
                );
                gbc.gridx = fieldColumn;
                mainPnl.add(scheduleField, gbc);

                gbc.gridy = ++yCoordinate;
                gbc.gridx = labelColumn;
                mainPnl.add(new JLabel(UiHelper.translate("connectionWindow")+":"), gbc);
                JTextField windowField = new JTextField();
                windowField.setEditable(false);
                ComWindow window = ((ScheduledConnectionTask)connectionTask).getCommunicationWindow();
                if (window!=null && window.getStart().getMillis()>0 && window.getEnd().getMillis()>0) {
                    StringBuffer buffer = new StringBuffer();
                    if (window.getStart()!=null) {
                        buffer.append(PartialTime.fromMilliSeconds(window.getStart().getMillis()).toString());
                    }
                    buffer.append(" - ");
                    if (window.getEnd()!=null) {
                        buffer.append(PartialTime.fromMilliSeconds(window.getEnd().getMillis()).toString());
                    }
                    windowField.setText(buffer.toString());
                }
                gbc.gridx = fieldColumn;
                mainPnl.add(windowField, gbc);
            }

            List<ConnectionTaskProperty> connectionTaskProperties = connectionTask.getProperties();

            if (!connectionTaskProperties.isEmpty()) {
                gbc.gridy = ++yCoordinate;
                gbc.gridx = fieldColumn;
                mainPnl.add(new JLabel(UiHelper.translate("connectionDetails") + ":"), gbc);

                for (ConnectionTaskProperty each : connectionTaskProperties) {
                    gbc.gridy = ++yCoordinate;
                    gbc.gridx = labelColumn;
                    mainPnl.add(new JLabel(each.getName()+":"), gbc);
                    JTextField propField = new JTextField();
                    propField.setEditable(false);
                    propField.setText(each.getValue().toString());
                    gbc.gridx = fieldColumn;
                    mainPnl.add(propField, gbc);
                }
            }

       }
        return mainPnl;
    }
}
