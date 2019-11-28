/*
 * Form.java
 *
 * Created on 16 april 2003, 10:58
 */

package com.energyict.mdc.engine.offline.gui.panels;

import com.energyict.mdc.engine.offline.gui.beans.FormBuilder;

import javax.swing.*;

/**
 * @author Karel
 */
public class Form extends JPanel {

    private FormBuilder builder;

    /**
     * Creates a new instance of Form
     */
    public Form() {
    }

    /**
     * Creates a new instance of Form
     */
    public Form(Object model) {
        this.builder = new FormBuilder(model);
    }

    protected FormBuilder getBuilder() {
        return builder;
    }

    public Object getModel() {
        return builder.getModel();
    }

    public void setModel(Object model) {
        this.builder = new FormBuilder(model);
    }

    /* utility methods */

    public JLabel getLabel(String aspect) {
        return getBuilder().getLabel(aspect);
    }

    public JComponent getWidget(String aspect) {
        return getWidget(aspect, false);
    }

    public JComponent getWidget(String aspect, boolean readOnly) {
        return getBuilder().getWidget(aspect, readOnly);
    }

    public JTextField getTextField(String aspect) {
        return getTextField(aspect, false);
    }

    public JTextField getTextField(String aspect, boolean readOnly) {
        return (JTextField) getBuilder().getWidget(aspect, readOnly);
    }

    public JFormattedTextField getFormattedTextField(String aspect) {
        return getFormattedTextField(aspect, false);
    }

    public JFormattedTextField getFormattedTextField(String aspect, boolean readOnly) {
        return (JFormattedTextField) getWidget(aspect, readOnly);
    }

    public JCheckBox getCheckBox(String aspect) {
        return getCheckBox(aspect, false);
    }

    public JCheckBox getCheckBox(String aspect, boolean readOnly) {
        return (JCheckBox) getWidget(aspect, readOnly);

    }

    public JPanel getPanel(String aspect) {
        return getPanel(aspect, false);
    }

    public JPanel getPanel(String aspect, boolean readOnly) {
        return (JPanel) getWidget(aspect, readOnly);
    }

    public JComboBox getComboBox(String aspect) {
        return getComboBox(aspect, false);
    }

    public JComboBox getComboBox(String aspect, boolean readOnly) {
        return (JComboBox) getWidget(aspect, readOnly);
    }


}
