package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.core.JIntegerField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class IntAspectEditor extends AspectEditor<JIntegerField> implements DocumentListener {

    protected JLabel jLabel;
    protected JIntegerField jValue;
    private boolean bSkipDocChanges = false;

    /**
     * Creates a new instance of StringAspectEditor
     */
    public IntAspectEditor() {
        jLabel = new JLabel();
        jValue = new JIntegerField(0, 10);
        jValue.getDocument().addDocumentListener(this);
    }

    public void setAllowNegativeValues(boolean flag){
        jValue.setAllowNegativeValues(flag);
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    public JIntegerField getValueComponent() {
        return jValue;
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    protected Object getViewValue() {
        return jValue.getValue();
    }

    protected void setViewValue(Object value) {
        int i = (value != null) ? (Integer) value : 0;
        bSkipDocChanges = true;
        jValue.setValue(i);
        bSkipDocChanges = false;
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        jValue.setEditable(!readOnly);
    }

    // DocumentListener interface

    public void changedUpdate(DocumentEvent e) {
        if (bSkipDocChanges) {
            return;
        }
        updateModel();
    }

    public void insertUpdate(DocumentEvent e) {
        if (bSkipDocChanges) {
            return;
        }
        updateModel();
    }

    public void removeUpdate(DocumentEvent e) {
        if (bSkipDocChanges) {
            return;
        }
        updateModel();
    }

    /**
     * Set the flag whether to ignore document changes. This suspends the document listener when changes in the document are being done
     * and we don't want validations or events or such to fire.
     *
     * @param ignore true if document changes must be ignored, false if not
     */
    protected final void ignoreDocumentChanges(final boolean ignore) {
        this.bSkipDocChanges = ignore;
    }

    /**
     * Returns the JInteger widget that is shown on screen.
     *
     * @return The JInteger widget that is shown on screen.
     */
    protected final JIntegerField getJIntegerField() {
        return this.jValue;
    }

    protected boolean doHasValidValue() {
        return !Utils.isNull(jValue.getText());
    }

}

