package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.channel.serial.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: gde
 * Date: 8/03/13
 */
public class SerialPortConfigurationAspectEditor extends AspectEditor<JPanel> {

    private JPanel valueComponent;
    private JLabel theLabel;
    private JComboBox<BaudrateValue> baudrateCombo;
    private JComboBox<NrOfDataBits> dataBitsCombo;
    private JComboBox<Parities> parityCombo;
    private JComboBox<NrOfStopBits> stopBitsCombo;
    private JComboBox<FlowControl> flowControlCombo;
    private boolean bSkipChanges = false;


    public SerialPortConfigurationAspectEditor() {
        theLabel = new JLabel();

        DefaultComboBoxModel<BaudrateValue> baudrateComboModel = new DefaultComboBoxModel<>(BaudrateValue.values());
        baudrateComboModel.setSelectedItem(BaudrateValue.BAUDRATE_9600);
        baudrateCombo = new JComboBox(baudrateComboModel);
        baudrateCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                valueChanged();
            }
        });

        DefaultComboBoxModel<NrOfDataBits> dataBitsComboModel = new DefaultComboBoxModel<>(NrOfDataBits.values());
        dataBitsComboModel.setSelectedItem(NrOfDataBits.EIGHT);
        dataBitsCombo = new JComboBox(dataBitsComboModel);
        dataBitsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                valueChanged();
            }
        });

        DefaultComboBoxModel<Parities> parityComboModel = new DefaultComboBoxModel<>(Parities.values());
        parityComboModel.setSelectedItem(Parities.NONE);
        parityCombo = new JComboBox(parityComboModel);
        parityCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                valueChanged();
            }
        });

        DefaultComboBoxModel<NrOfStopBits> stopBitsComboModel = new DefaultComboBoxModel<>(NrOfStopBits.values());
        stopBitsComboModel.setSelectedItem(NrOfStopBits.ONE);
        stopBitsCombo = new JComboBox(stopBitsComboModel);
        stopBitsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                valueChanged();
            }
        });

        DefaultComboBoxModel<FlowControl> flowControlComboModel = new DefaultComboBoxModel<>(FlowControl.values());
        flowControlComboModel.setSelectedItem(FlowControl.NONE);
        flowControlCombo = new JComboBox(flowControlComboModel);
        flowControlCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                valueChanged();
            }
        });
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
        JPanel result = new JPanel(new GridBagLayout());
        result.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2,0,2,2);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.weightx = 0.5;
        result.add(baudrateCombo, gbc);
        gbc.gridx++;
        gbc.weightx = 0.25;
        gbc.insets = new Insets(2,2,2,2);
        result.add(dataBitsCombo, gbc);
        gbc.gridx++;
        gbc.insets = new Insets(2,2,2,0);
        result.add(stopBitsCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(2,0,2,2);
        result.add(parityCombo, gbc);
        gbc.gridx++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(2,2,2,0);
        result.add(flowControlCombo, gbc);

        return result;
    }

    protected Object getViewValue() {
        return new SerialPortConfiguration("",
            (BaudrateValue)baudrateCombo.getSelectedItem(),
            (NrOfDataBits)dataBitsCombo.getSelectedItem(),
            (NrOfStopBits)stopBitsCombo.getSelectedItem(),
            (Parities)parityCombo.getSelectedItem(),
            (FlowControl)flowControlCombo.getSelectedItem());
    }

    protected void setViewValue(Object value) {
        SerialPortConfiguration serialPortConfiguration = new SerialPortConfiguration("",
            BaudrateValue.BAUDRATE_9600, NrOfDataBits.EIGHT, NrOfStopBits.ONE, Parities.NONE, FlowControl.NONE);
        if (value != null) {
            serialPortConfiguration = (SerialPortConfiguration)value;
        }

        bSkipChanges = true;
        baudrateCombo.setSelectedItem(serialPortConfiguration.getBaudrate());
        dataBitsCombo.setSelectedItem(serialPortConfiguration.getBaudrate());
        stopBitsCombo.setSelectedItem(serialPortConfiguration.getNrOfStopBits());
        parityCombo.setSelectedItem(serialPortConfiguration.getParity());
        flowControlCombo.setSelectedItem(serialPortConfiguration.getFlowControl());
        bSkipChanges = false;
    }

    protected void updateLabel() {
        theLabel.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        baudrateCombo.setEnabled(!readOnly);
        dataBitsCombo.setEnabled(!readOnly);
        stopBitsCombo.setEnabled(!readOnly);
        parityCombo.setEnabled(!readOnly);
        flowControlCombo.setEnabled(!readOnly);
    }

    private void valueChanged() {
        if (bSkipChanges) {
            return;
        }
        updateModel();
    }
}