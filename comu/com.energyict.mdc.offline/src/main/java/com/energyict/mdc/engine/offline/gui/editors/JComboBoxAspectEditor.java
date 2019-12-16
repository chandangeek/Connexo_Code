package com.energyict.mdc.engine.offline.gui.editors;

import javax.swing.*;
import java.beans.PropertyDescriptor;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 11:04:19
 */
public class JComboBoxAspectEditor<E> extends AspectEditor<JComboBox<E>> {

    private JLabel label;
    private JComboBox<E> comboBox;

    protected boolean settingModel = false;

    public JComboBoxAspectEditor() {
        label = new JLabel();
        comboBox = new JComboBox<>();
        comboBox.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        if (!settingModel){
                            updateModel();
                        }
                    }
                }
        );
    }

    @Override
    public void init(Object model, PropertyDescriptor descriptor) {
        super.init(model, descriptor);
        settingModel = false;
    }

    public JComboBoxAspectEditor(ComboBoxModel<E> comboBoxModel) {
        this(comboBoxModel, null);
    }

    public JComboBoxAspectEditor(ComboBoxModel<E> comboBoxModel, ListCellRenderer<E> renderer) {
        label = new JLabel();
        comboBox = new JComboBox<>(comboBoxModel);
        if (renderer!=null) comboBox.setRenderer(renderer);
        comboBox.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        if (!settingModel){
                            JComboBoxAspectEditor.super.updateModel();
                        }
                    }
                }
        );
    }

    public JLabel getLabelComponent() {
        return label;
    }

    @Override
    public JComboBox<E> getValueComponent() {
        return comboBox;
    }

    @Override
    public void setForceReadOnly(boolean forceReadOnly) {
        super.setForceReadOnly(forceReadOnly);
        comboBox.setEnabled(!forceReadOnly);
    }

    @Override
    protected void doInit() {
        settingModel = true;
    }

    @Override
    protected Object getViewValue() {
        return comboBox.getSelectedItem();
    }

    @Override
    protected void setViewValue(Object value) {
        if (value == null) {
            comboBox.setSelectedIndex(-1);
        } else {
            comboBox.setSelectedItem(value);
        }
    }

    @Override
    protected void updateLabel() {
        label.setText(getLabelString());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        comboBox.setEnabled(!readOnly);
    }

}
