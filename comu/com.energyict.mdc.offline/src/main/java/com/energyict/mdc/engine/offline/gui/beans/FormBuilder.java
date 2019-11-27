/*
 * EditorBuilder.java
 *
 * Created on 10 februari 2003, 8:59
 */

package com.energyict.mdc.engine.offline.gui.beans;

import com.energyict.mdc.engine.offline.gui.editors.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;


/**
 * Builds forms to manipulate bean properties
 *
 * @author Karel
 */
public class FormBuilder<T> {

    private Map<String, PropertyDescriptor> descriptors = null;
    private T model;
    private Map<String, AspectEditor> editors = new HashMap<>();
    private boolean readOnly;

    /**
     * Creates a new instance of EditorBuilder
     *
     * @param model the bean to edit
     */
    public FormBuilder(T model) {
        this(model, false);
    }

    public FormBuilder(T model, boolean readOnly) {
        this.model = model;
        this.readOnly = readOnly;
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
        for (AspectEditor each : editors.values()) {
            each.setModel(model);
        }
    }

    protected Map<String, PropertyDescriptor> getDescriptors() {
        if (descriptors == null) {
            buildDescriptors();
        }
        return descriptors;
    }

    protected void buildDescriptors() {
        descriptors = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(model.getClass());
            PropertyDescriptor[] allDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor each: allDescriptors){
                if (each.getReadMethod() != null){
                   descriptors.put(each.getName(), each);
                }
            }
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
    }

    public AspectEditor getEditor(String aspect) {
        AspectEditor result = editors.get(aspect);
        if (result == null) {
            return doGetEditor(aspect);
        } else {
            return result;
        }
    }

    public void setEditor(String aspect, AspectEditor editor){
        editors.put(aspect, editor);
    }

    protected AspectEditor doGetEditor(String aspect) {
        PropertyDescriptor descriptor = getDescriptors().get(aspect);
        AspectEditor editor = AspectEditorManager.getEditor(model, descriptor,this.readOnly);
        editors.put(aspect, editor);
        return editor;
    }

    public JLabel getLabel(String aspect) {
        return getEditor(aspect).getLabelComponent();
    }

    public JComponent getWidget(String aspect) {
        return getWidget(aspect, readOnly);
    }

    public JComponent getWidget(String aspect, boolean readOnly) {
        AspectEditor editor = getEditor(aspect);
        if (readOnly) {
            editor.setReadOnly(readOnly);
        }
        return editor.getValueComponent();
    }

    public JTextField getTextField(String aspect) {
        return getTextField(aspect, readOnly);
    }

    public JTextField getTextField(String aspect, boolean readOnly) {
        return (JTextField) getWidget(aspect, readOnly);
    }

    public JTextPane getTextPane(String aspect) {
        return getTextPane(aspect, readOnly);
    }

    public JTextPane getTextPane(String aspect, final int maxCharacters) {
        AspectEditor editor = new LargeStringAspectEditor(maxCharacters);
        PropertyDescriptor descriptor = getDescriptors().get(aspect);
        editor.init(model, descriptor);
        editors.put(aspect, editor);
        if (readOnly) {
            editor.setReadOnly(readOnly);
        }
        return (JTextPane) editor.getValueComponent();
    }

    public JTextPane getTextPane(String aspect, boolean readOnly) {
        AspectEditor editor = new LargeStringAspectEditor();
        PropertyDescriptor descriptor = getDescriptors().get(aspect);
        editor.init(model, descriptor);
        editors.put(aspect, editor);
        if (readOnly) {
            editor.setReadOnly(readOnly);
        }
        return (JTextPane) editor.getValueComponent();
    }

    public JFormattedTextField getFormattedTextField(String aspect) {
        return getFormattedTextField(aspect, readOnly);
    }

    /*
     * Depending on the 'Privilege' defined by the given Role, UserAction and TypeId
     * this will return a editable JTextField or a non-editable JPasswordField
     *
     * @see com.energyict.mdw.core.Privilege
     */
    public JTextComponent getPasswordField(String aspect, boolean plainText) {
        AspectEditor editor = editors.get(aspect);
        if (editor == null){
            editor = new PasswordAspectEditor(plainText);
            editor.init(model, getDescriptors().get(aspect));
            editor.setReadOnly(readOnly);
            editors.put(aspect, editor);
        }
        return (JTextComponent) editor.getValueComponent();
    }

    public JFormattedTextField getFormattedTextField(String aspect, boolean readOnly) {
        return (JFormattedTextField) getWidget(aspect, readOnly);
    }

    public JCheckBox getCheckBox(String aspect) {
        return getCheckBox(aspect, readOnly);
    }

    public JCheckBox getCheckBox(String aspect, boolean readOnly) {
        return (JCheckBox) getWidget(aspect, readOnly);
    }

    public JPanel getPanel(String aspect) {
        return getPanel(aspect, readOnly);
    }

    public JPanel getPanel(String aspect, boolean readOnly) {
        return (JPanel) getWidget(aspect, readOnly);
    }

    public JComboBox getComboBox(String aspect) {
        return getComboBox(aspect, readOnly);
    }

    public JComboBox getComboBox(String aspect, boolean readOnly) {
        return (JComboBox) getWidget(aspect, readOnly);
    }

    public JComboBox getComboBox(String aspect, ComboBoxModel model) {
        return getComboBox(aspect, model, readOnly);
    }

    public JComboBox getComboBox(String aspect, ComboBoxModel comboBoxModel, boolean readOnly) {
        JComboBoxAspectEditor editor = new JComboBoxAspectEditor(comboBoxModel);
        editor.init(getModel(), getDescriptors().get(aspect));

        JComboBox combo = (JComboBox) editor.getValueComponent();
        combo.setEnabled(!readOnly);

        return combo;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public JSpinner getSpinner(String aspect, SpinnerModel model) {
        return getSpinner(aspect, this.readOnly, model);
    }

    public JSpinner getSpinner(String aspect, boolean readOnly, SpinnerModel spinnerModel) {
        SpinnerEditor editor = new SpinnerEditor(spinnerModel);
        PropertyDescriptor descriptor = getDescriptors().get(aspect);
        editor.init(model, descriptor);
        editors.put(aspect, editor);
        if (readOnly) {
            editor.setReadOnly(readOnly);
        }
        return editor.getValueComponent();
    }

    public JPanel getNullableSpinner(String aspect, SpinnerModel spinnerModel) {
        return getNullableSpinner(aspect, this.readOnly, spinnerModel);
    }

    public JPanel getNullableSpinner(String aspect, boolean readOnly, SpinnerModel spinnerModel) {
        NullableSpinnerEditor editor = new NullableSpinnerEditor(spinnerModel);
        PropertyDescriptor descriptor = getDescriptors().get(aspect);
        editor.init(model, descriptor);
        editors.put(aspect, editor);
        if (readOnly) {
            editor.setReadOnly(readOnly);
        }
        return editor.getValueComponent();
    }

    public AspectEditor getIconSelectionEditor(String aspect) {
        PropertyDescriptor descriptor = getDescriptors().get(aspect);
        AspectEditor editor = new IconIdAspectEditor();
        editor.init(model, descriptor);
        editor.setReadOnly(readOnly);
        editors.put(aspect, editor);
        return editor;
    }

}
