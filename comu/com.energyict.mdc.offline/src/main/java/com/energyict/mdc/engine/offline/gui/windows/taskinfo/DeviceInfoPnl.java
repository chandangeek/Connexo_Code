package com.energyict.mdc.engine.offline.gui.windows.taskinfo;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.offline.core.DeviceInfo;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.upl.offline.OfflineDevice;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * @author jme
 */
public class DeviceInfoPnl extends JScrollPane {

    private final String NLS_DEVICEINFO = UiHelper.translate("deviceInformation");
    private final String NLS_NAME = UiHelper.translate("name");
    private final String NLS_EXTERNAL_NAME = UiHelper.translate("externalName");
    private final String NLS_SERIAL_NUMBER = UiHelper.translate("serialNumber");
    private final String NLS_LOCATION = UiHelper.translate("location");
    private final String NLS_USAGE_POINT = UiHelper.translate("usagePoint");
    private final String NLS_DEVICE_TYPE = UiHelper.translate("rtuType");
    private final String NLS_DEVICE_CONFIG = UiHelper.translate("deviceConfiguration");
    private final String NLS_TIME_ZONE = UiHelper.translate("timeZone");
    private final String NLS_LAST_READING = UiHelper.translate("lastReading");
    private final String NLS_LAST_LOGBOOK = UiHelper.translate("lastLogbook");

    private JPanel jPanel;
    private JLabel jLabelDeviceName;
    private JTextField jTextFieldDeviceName;
    private JLabel externalNameLbl;
    private JTextField externalNameField;
    private JLabel jLabelSerialNumber;
    private JTextField jTextFieldSerialNumber;
    private JLabel locationLbl;
    private JTextField locationField;
    private JLabel usagePointLbl;
    private JTextField usagePointField;
    private JLabel jLabelDeviceType;
    private JTextField jTextFieldDeviceType;
    private JLabel deviceConfigLbl;
    private JTextField deviceConfigField;
    private JLabel jLabelTimeZone;
    private JTextField jTextFieldTimeZone;
    private JLabel jLabelLastReading;
    private JTextField jTextFieldLastReading;
    private JLabel jLabelLastLogbook;
    private JTextField jTextFieldLastLogBook;

    private DeviceInfo deviceInfo;

    public DeviceInfoPnl(Device device, OfflineDevice offlineDevice) {
        this.deviceInfo = new DeviceInfo(device, offlineDevice);
        initComponents();
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    private void initComponents() {
        jPanel = new JPanel();
        jLabelDeviceName = new JLabel();
        jTextFieldDeviceName = new JTextField();
        jLabelSerialNumber = new JLabel();
        jTextFieldSerialNumber = new JTextField();
        locationLbl = new JLabel();
        locationField = new JTextField();
        usagePointLbl = new JLabel();
        usagePointField = new JTextField();
        jLabelDeviceType = new JLabel();
        jTextFieldDeviceType = new JTextField();
        deviceConfigLbl = new JLabel();
        deviceConfigField = new JTextField();
        jLabelTimeZone = new JLabel();
        jTextFieldTimeZone = new JTextField();
        jLabelLastReading = new JLabel();
        jTextFieldLastReading = new JTextField();
        jLabelLastLogbook = new JLabel();
        jTextFieldLastLogBook = new JTextField();

        //======== this ========
        setBorder(null);

        //======== jPanel ========
        {
            jPanel.setBorder(new TitledBorder(null, NLS_DEVICEINFO, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 16)));
            GridBagLayout layout = new GridBagLayout();
            layout.columnWidths = new int[]{10, 105, 0, 5, 0};
            layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            layout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0E-4};
            layout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
            jPanel.setLayout(layout);

            int yCoordinate = 0;
            GridBagConstraints gbc = new GridBagConstraints(1, yCoordinate, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0);
            //---- Device Name ----
            jLabelDeviceName.setText(NLS_NAME + ":");
            jPanel.add(jLabelDeviceName, gbc);
            jTextFieldDeviceName.setEditable(false);
            jTextFieldDeviceName.setText(getDeviceInfo().getName());
            gbc.gridx = 2;
            jPanel.add(jTextFieldDeviceName, gbc);

            // --- external name ----
            externalNameLbl = new JLabel(NLS_EXTERNAL_NAME + ":");
            gbc.gridx = 1;
            gbc.gridy = ++yCoordinate;
            jPanel.add(externalNameLbl, gbc);
            externalNameField = new JTextField();
            externalNameField.setEditable(false);
            externalNameField.setText(getDeviceInfo().getExternalName());
            gbc.gridx = 2;
            jPanel.add(externalNameField, gbc);

            //---- Serial Number ----
            jLabelSerialNumber.setText(NLS_SERIAL_NUMBER + ":");
            gbc.gridx = 1;
            gbc.gridy = ++yCoordinate;
            jPanel.add(jLabelSerialNumber, gbc);
            jTextFieldSerialNumber.setEditable(false);
            jTextFieldSerialNumber.setText(getDeviceInfo().getSerialNumber());
            gbc.gridx = 2;
            jPanel.add(jTextFieldSerialNumber, gbc);

            //---- Location ----
            locationLbl.setText(NLS_LOCATION + ":");
            gbc.gridx = 1;
            gbc.gridy = ++yCoordinate;
            jPanel.add(locationLbl, gbc);
            locationField.setEditable(false);
            locationField.setText(getDeviceInfo().getLocation());
            gbc.gridx = 2;
            jPanel.add(locationField, gbc);

            //---- Usage point ----
            usagePointLbl.setText(NLS_USAGE_POINT + ":");
            gbc.gridx = 1;
            gbc.gridy = ++yCoordinate;
            jPanel.add(usagePointLbl, gbc);
            usagePointField.setEditable(false);
            usagePointField.setText(getDeviceInfo().getUsagePoint());
            gbc.gridx = 2;
            jPanel.add(usagePointField, gbc);

            //---- Device Type ----
            jLabelDeviceType.setText(NLS_DEVICE_TYPE + ":");
            gbc.gridx = 1;
            gbc.gridy = ++yCoordinate;
            jPanel.add(jLabelDeviceType, gbc);
            jTextFieldDeviceType.setEditable(false);
            gbc.gridx = 2;
            jTextFieldDeviceType.setText(getDeviceInfo().getDeviceType());
            jPanel.add(jTextFieldDeviceType, gbc);

            //----  Configuration ----
            deviceConfigLbl.setText(NLS_DEVICE_CONFIG + ":");
            gbc.gridx = 1;
            gbc.gridy = ++yCoordinate;
            jPanel.add(deviceConfigLbl, gbc);
            deviceConfigField.setEditable(false);
            deviceConfigField.setText(getDeviceInfo().getDeviceConfig());
            gbc.gridx = 2;
            jPanel.add(deviceConfigField, gbc);

            //---- Time Zone ----
            jLabelTimeZone.setText(NLS_TIME_ZONE + ":");
            gbc.gridx = 1;
            gbc.gridy = ++yCoordinate;
            jPanel.add(jLabelTimeZone, gbc);
            jTextFieldTimeZone.setEditable(false);
            jTextFieldTimeZone.setText(getDeviceInfo().getTimeZone());
            gbc.gridx = 2;
            jPanel.add(jTextFieldTimeZone, gbc);

            //---- Last Reading ----
            jLabelLastReading.setText(NLS_LAST_READING + ":");
            gbc.gridx = 1;
            gbc.gridy = ++yCoordinate;
            jPanel.add(jLabelLastReading, gbc);
            jTextFieldLastReading.setEditable(false);
            jTextFieldLastReading.setText(getDeviceInfo().getLastReading());
            gbc.gridx = 2;
            jPanel.add(jTextFieldLastReading, gbc);

            //---- Last Logbook ----
            jLabelLastLogbook.setText(NLS_LAST_LOGBOOK + ":");
            gbc.gridx = 1;
            gbc.gridy = ++yCoordinate;
            jPanel.add(jLabelLastLogbook, gbc);
            jTextFieldLastLogBook.setEditable(false);
            jTextFieldLastLogBook.setText(getDeviceInfo().getLastLogbook());
            gbc.gridx = 2;
            jPanel.add(jTextFieldLastLogBook, gbc);
        }

        setViewportView(jPanel);
    }
}