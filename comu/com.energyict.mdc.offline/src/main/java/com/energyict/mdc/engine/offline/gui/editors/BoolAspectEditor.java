package com.energyict.mdc.engine.offline.gui.editors;


import com.energyict.mdc.engine.offline.gui.models.BooleanAdapter;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyDescriptor;

public class BoolAspectEditor extends AspectEditor<JCheckBox> {

    private JCheckBox jValue;

    /**
     * Creates a new instance of StringAspectEditor
     */
    public BoolAspectEditor() {
        jValue = new JCheckBox();
        jValue.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        jValueStateChanged(evt);
                    }
                }
        );
        // Added for progammatically checkBox.setSelected(...) support
        jValue.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                updateModel();
            }
        });
    }

    private void jValueStateChanged(java.awt.event.ActionEvent evt) {
        updateModel();
    }

    protected Object getViewValue() {
        return jValue.isSelected();
    }

    protected void setViewValue(Object value) {
        jValue.setSelected(value != null && ((Boolean) value));
    }

    protected void updateLabel() {
        jValue.setText(getLabelString(false));
    }

    public javax.swing.JLabel getLabelComponent() {
        return null;
    }

    public JCheckBox getValueComponent() {
        return jValue;
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        jValue.setEnabled(!readOnly);
    }

    @Override
    public void init(Object model, PropertyDescriptor descriptor) {
        super.init(model, descriptor);
        if (getModel() instanceof BooleanAdapter) {
            if (((BooleanAdapter) getModel()).isNull()) {
                // Although the model's (=BooleanAdapter) method getValue() returns false, its real value is null.
                // So to bring the model's value in sync with the checkbox control (being able to show only false or true)
                // we should change it from null to false.
                ((BooleanAdapter) getModel()).setValue(false);
            }
        }
    }

}

