package com.energyict.mdc.engine.offline.gui.windows.taskinfo;

import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.offline.DeviceComTaskWrapper;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class SecuritySetInfoPnl extends JScrollPane {

    private Map<DeviceComTaskWrapper, ComTaskEnablement> comTaskEnablementMap;
    private Map<SecurityPropertySet, List<DeviceMasterDataExtractor.SecurityProperty>> securitySetPropertiesMap;
    private List<ComTaskExecution> comTaskExecutions;
    private JPanel mainPnl;

    public SecuritySetInfoPnl(Map<DeviceComTaskWrapper, ComTaskEnablement> comTaskEnablementMap,
                              Map<SecurityPropertySet, List<DeviceMasterDataExtractor.SecurityProperty>> securitySetPropertiesMap,
                              List<ComTaskExecution> comTaskExecutions) {
        this.comTaskEnablementMap = comTaskEnablementMap;
        this.securitySetPropertiesMap = securitySetPropertiesMap;
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

            mainPnl.setBorder(new TitledBorder(null, UiHelper.translate("securitySets"), TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 16)));

            int labelColumn = 1;
            int fieldColumn = 2;
            int yCoordinate = -1;

            for (ComTaskExecution eachTask : comTaskExecutions) {

                GridBagConstraints gbc = new GridBagConstraints(
                    labelColumn, ++yCoordinate, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0);

                gbc.gridx = fieldColumn;
                gbc.insets = new Insets(10, 0, 5, 5);
                JLabel label = new JLabel(UiHelper.translate("comTask")+" '"+eachTask.getComTask().getName() + "'");
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                mainPnl.add(label, gbc);

                for (DeviceComTaskWrapper eachWrapper : comTaskEnablementMap.keySet()) {
                    if (eachWrapper.getComTaskId() == eachTask.getComTask().getId()) {
                        ComTaskEnablement enablement = comTaskEnablementMap.get(eachWrapper);
                        if (enablement != null) {
                            SecurityPropertySet propertySet = enablement.getSecurityPropertySet();

                            gbc.gridx = labelColumn;
                            gbc.gridy = ++yCoordinate;
                            gbc.insets = new Insets(0, 0, 5, 5);
                            mainPnl.add(new JLabel(UiHelper.translate("name") + ":"), gbc);
                            JTextField nameField = new JTextField();
                            nameField.setEditable(false);
                            nameField.setText(propertySet.getName());
                            gbc.gridx = fieldColumn;
                            mainPnl.add(nameField, gbc);

                            gbc.gridy = ++yCoordinate;
                            gbc.gridx = labelColumn;
                            mainPnl.add(new JLabel(UiHelper.translate("authenticationLevel") + ":"), gbc);
                            JTextField authField = new JTextField();
                            authField.setEditable(false);
                            authField.setText(String.valueOf(propertySet.getAuthenticationDeviceAccessLevel().getTranslation()));
                            gbc.gridx = fieldColumn;
                            mainPnl.add(authField, gbc);

                            gbc.gridy = ++yCoordinate;
                            gbc.gridx = labelColumn;
                            mainPnl.add(new JLabel(UiHelper.translate("encryptionLevel") + ":"), gbc);
                            JTextField encField = new JTextField();
                            encField.setEditable(false);
                            encField.setText(String.valueOf(propertySet.getEncryptionDeviceAccessLevel().getTranslation()));
                            gbc.gridx = fieldColumn;
                            mainPnl.add(encField, gbc);

                            // Don't show the properties, since you don't know if it are passwords (that shouldn't be shown in clear text) or others
//                        List<SecurityProperty> securityProperties = securitySetPropertiesMap.get(propertySet);
//                        for (SecurityProperty securityProperty : securityProperties) {
//                            gbc.gridy = ++yCoordinate;
//                            gbc.gridx = labelColumn;
//                            mainPnl.add(new JLabel(securityProperty.getName()+":"), gbc);
//                            JTextField valueField = new JTextField();
//                            valueField.setEditable(false);
//                            valueField.setText(securityProperty.getValue().toString());
//                            gbc.gridx = fieldColumn;
//                            mainPnl.add(valueField, gbc);
//                        }

                        }
                    }
                }
            }
        }
        return mainPnl;
    }
}
