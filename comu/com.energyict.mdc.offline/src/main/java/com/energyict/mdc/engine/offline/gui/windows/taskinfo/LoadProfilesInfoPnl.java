package com.energyict.mdc.engine.offline.gui.windows.taskinfo;

import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class LoadProfilesInfoPnl extends JScrollPane {

    private List<OfflineLoadProfile> loadProfiles;

    private JPanel mainPnl;

    public LoadProfilesInfoPnl(List<OfflineLoadProfile> loadProfiles) {
        this.loadProfiles = loadProfiles;
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

            mainPnl.setBorder(new TitledBorder(null, UiHelper.translate("loadProfiles"), TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 16)));

            int labelColumn = 1;
            int fieldColumn = 2;
            int yCoordinate = -1;
            int profileCounter = 0;

            if (loadProfiles==null) {
                return mainPnl;
            }

            for (OfflineLoadProfile loadProfile : loadProfiles) {

                GridBagConstraints gbc = new GridBagConstraints(
                    labelColumn, ++yCoordinate, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0);

                if (loadProfiles.size()>1) {
                    gbc.gridx = fieldColumn;
                    gbc.insets = new Insets(10, 0, 5, 5);
                    JLabel label = new JLabel(UiHelper.translate("loadProfile")+" "+(++profileCounter));
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                    mainPnl.add(label, gbc);
                    gbc.gridx = labelColumn;
                    gbc.gridy = ++yCoordinate;
                    gbc.insets = new Insets(0, 0, 5, 5);
                }

                mainPnl.add(new JLabel(UiHelper.translate("name")+":"), gbc);
                JTextField nameField = new JTextField();
                nameField.setEditable(false);
                nameField.setText(loadProfile.getName());
                gbc.gridx = fieldColumn;
                mainPnl.add(nameField, gbc);

                gbc.gridy = ++yCoordinate;
                gbc.gridx = labelColumn;
                mainPnl.add(new JLabel(UiHelper.translate("obisCode")+":"), gbc);
                JTextField scheduleField = new JTextField();
                scheduleField.setEditable(false);
                scheduleField.setText(loadProfile.getObisCode().toString());
                gbc.gridx = fieldColumn;
                mainPnl.add(scheduleField, gbc);

                gbc.gridy = ++yCoordinate;
                gbc.gridx = labelColumn;
                mainPnl.add(new JLabel(UiHelper.translate("interval")+":"), gbc);
                JTextField urgencyField = new JTextField();
                urgencyField.setEditable(false);
                urgencyField.setText(loadProfile.getInterval().toString());
                gbc.gridx = fieldColumn;
                mainPnl.add(urgencyField, gbc);

                if (!loadProfile.getAllOfflineChannels().isEmpty()) {
                    gbc.gridy = ++yCoordinate;
                    gbc.gridx = labelColumn;
                    mainPnl.add(new JLabel(UiHelper.translate("channels")+":"), gbc);

                    gbc.gridx = fieldColumn;
                    for (OfflineLoadProfileChannel channel : loadProfile.getAllOfflineChannels()) {
                        JTextField field = new JTextField();
                        field.setEditable(false);
                        field.setText( channel.getName() + " [" + channel.getObisCode() + "]" );
                        mainPnl.add(field, gbc);
                        gbc.gridy = ++yCoordinate;
                    }
                }
            }
        }
        return mainPnl;
    }
}