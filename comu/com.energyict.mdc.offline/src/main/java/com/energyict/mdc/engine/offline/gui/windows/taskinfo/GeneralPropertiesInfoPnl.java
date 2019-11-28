package com.energyict.mdc.engine.offline.gui.windows.taskinfo;

import com.energyict.mdc.engine.offline.gui.UiHelper;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Enumeration;
import java.util.Properties;

public class GeneralPropertiesInfoPnl extends JScrollPane {

    private Properties properties;
    private JPanel mainPnl;

    public GeneralPropertiesInfoPnl(Properties properties) {
        this.properties = properties;
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

            mainPnl.setBorder(new TitledBorder(null, UiHelper.translate("protocol.generalProperties"), TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 16)));

            int labelColumn = 1;
            int fieldColumn = 2;
            int yCoordinate = -1;

            Enumeration enumeration = properties.propertyNames();
            while (enumeration.hasMoreElements()) {
                String name = (String) enumeration.nextElement();

                GridBagConstraints gbc = new GridBagConstraints(
                    labelColumn, ++yCoordinate, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0);

                gbc.gridy = ++yCoordinate;
                gbc.gridx = labelColumn;
                mainPnl.add(new JLabel(name+":"), gbc);
                JTextField valueField = new JTextField();
                valueField.setEditable(false);
                valueField.setText(properties.getProperty(name));
                gbc.gridx = fieldColumn;
                mainPnl.add(valueField, gbc);
            }
        }
        return mainPnl;
    }
}
