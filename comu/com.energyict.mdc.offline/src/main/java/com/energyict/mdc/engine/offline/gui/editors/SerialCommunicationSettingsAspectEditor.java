/*
 * SerialCommunicationSettingsAspectEditor.java
 *
 * Created on 17 juni 2003, 13:14
 */

package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.SerialCommunicationSettings;
import com.energyict.mdc.engine.offline.gui.core.IntegerBasicComboBoxEditor;
import com.energyict.mdc.engine.offline.gui.models.IntegerComboBoxModel;

import javax.swing.*;

/**
 * @author GDE
 */
public class SerialCommunicationSettingsAspectEditor extends AspectEditor<JPanel> {

    private JPanel valueComponent;
    private JLabel theLabel;
    private JComboBox theSpeedValue;
    private IntegerComboBoxModel speedModel;
    private JComboBox theDataBitsValue;
    private IntegerComboBoxModel dataBitsModel;
    private JComboBox theParityValue;
    private JComboBox theStopBitsValue;
    private IntegerComboBoxModel stopBitsModel;
    private boolean bSkipChanges = false;
    private boolean multiEditMode = false;
    private static int[] speeds =
            {300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200};
    private static int[] speedsMulti =
            {-1, 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200};

    /**
     * Creates a new instance of SerialCommunicationSettingsAspectEditor
     */
    public SerialCommunicationSettingsAspectEditor() {
        theLabel = new JLabel();

        speedModel = new IntegerComboBoxModel(speeds);
        speedModel.setSelectedItem(new Integer(9600));
        theSpeedValue = new JComboBox(speedModel);
        theSpeedValue.setEditor(new IntegerBasicComboBoxEditor());
        theSpeedValue.setEditable(true);
        theSpeedValue.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        valueActionPerformed(evt);
                    }
                }
        );

        int[] dataBits = {7, 8};
        dataBitsModel = new IntegerComboBoxModel(dataBits);
        dataBitsModel.setSelectedItem(new Integer(8));
        theDataBitsValue = new JComboBox(dataBitsModel);
        theDataBitsValue.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        valueActionPerformed(evt);
                    }
                }
        );

        theParityValue = new JComboBox(getParityChoices());
        theParityValue.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        valueActionPerformed(evt);
                    }
                }
        );

        int[] stopBits = {1, 2};
        stopBitsModel = new IntegerComboBoxModel(stopBits);
        stopBitsModel.setSelectedItem(new Integer(1));
        theStopBitsValue = new JComboBox(stopBitsModel);
        theStopBitsValue.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        valueActionPerformed(evt);
                    }
                }
        );
    }

    public void setMultiEditMode() {
        multiEditMode = true;
        speedModel = new IntegerComboBoxModel(speedsMulti);
        speedModel.setSelectedItem(new Integer(-1));
        theSpeedValue.setModel(speedModel);
    }

    public JLabel getLabelComponent() {
        return theLabel;
    }

    public JPanel getValueComponent() {
        if (valueComponent == null) {
            valueComponent = doGetValueComponent();
        }
        return valueComponent;
    }

    protected JPanel doGetValueComponent() {
        JPanel result = new JPanel();
        java.awt.GridBagLayout grid = new java.awt.GridBagLayout();
        result.setLayout(grid);
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        grid.setConstraints(theSpeedValue, c);
        result.add(theSpeedValue);

        c.gridx = 1;
        c.weightx = 0;
        c.fill = java.awt.GridBagConstraints.NONE;
        grid.setConstraints(theDataBitsValue, c);
        result.add(theDataBitsValue);

        c.gridx = 2;
        c.weightx = 0;
        c.fill = java.awt.GridBagConstraints.NONE;
        grid.setConstraints(theParityValue, c);
        result.add(theParityValue);

        c.gridx = 3;
        c.weightx = 0;
        c.fill = java.awt.GridBagConstraints.NONE;
        grid.setConstraints(theStopBitsValue, c);
        result.add(theStopBitsValue);

        return result;
    }

    protected Object getViewValue() {
        int iSpeed = ((Integer) speedModel.getSelectedItem()).intValue();
        int iDataBits = ((Integer) dataBitsModel.getSelectedItem()).intValue();
        char cParity = SerialCommunicationSettings.NO_PARITY;
        if (theParityValue != null) {
            cParity = convertIndexToParity(theParityValue.getSelectedIndex());
        }
        int iStopBits = ((Integer) stopBitsModel.getSelectedItem()).intValue();
        return iSpeed == -1 ? null : new SerialCommunicationSettings(iSpeed, iDataBits, cParity, iStopBits);
    }

    protected void setViewValue(Object value) {
        SerialCommunicationSettings settings =
                SerialCommunicationSettings.getDefault();
        if (value != null) {
            settings = (SerialCommunicationSettings) value;
        } else if (multiEditMode) {
            settings = SerialCommunicationSettings.getMultiEditDefault();
        }

        bSkipChanges = true;
        speedModel.setSelectedItem(new Integer(settings.getSpeed()));
        dataBitsModel.setSelectedItem(new Integer(settings.getDataBits()));
        int index = findParityIndex(settings);
        theParityValue.setSelectedIndex(index);
        stopBitsModel.setSelectedItem(new Integer(settings.getStopBits()));
        bSkipChanges = false;
    }

    protected void updateLabel() {
        theLabel.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        theSpeedValue.setEnabled(!readOnly);
        theDataBitsValue.setEnabled(!readOnly);
        theParityValue.setEnabled(!readOnly);
        theStopBitsValue.setEnabled(!readOnly);
    }

    private void valueActionPerformed(java.awt.event.ActionEvent evt) {
        if (bSkipChanges) {
            return;
        }
        updateModel();
    }

    public String[] getParityChoices() {
        return new String[]{
                "N", "O", "E"
        };
    }

    protected int findParityIndex(SerialCommunicationSettings settings) {
        switch (settings.getParity()) {
            case SerialCommunicationSettings.NO_PARITY:
                return 0;
            case SerialCommunicationSettings.ODD_PARITY:
                return 1;
            case SerialCommunicationSettings.EVEN_PARITY:
                return 2;
            default:
                return 0;
        }
    }

    protected char convertIndexToParity(int index) {
        switch (index) {
            case 0:
                return SerialCommunicationSettings.NO_PARITY;
            case 1:
                return SerialCommunicationSettings.ODD_PARITY;
            case 2:
                return SerialCommunicationSettings.EVEN_PARITY;
            default:
                return SerialCommunicationSettings.NO_PARITY;
        }
    }
}
