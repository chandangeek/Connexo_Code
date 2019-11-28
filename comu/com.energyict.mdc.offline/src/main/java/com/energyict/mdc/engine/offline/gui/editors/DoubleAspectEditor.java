/*
 * StringAspectEditor.java
 *
 * Created on 5 februari 2003, 17:33
 */

package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.gui.core.JDoubleField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class DoubleAspectEditor extends AspectEditor<JDoubleField> implements DocumentListener {

    private JLabel jLabel;
    private JDoubleField jValue;
    private boolean bSkipDocChanges = false;

    /**
     * Creates a new instance of StringAspectEditor
     */
    public DoubleAspectEditor() {
        jLabel = new JLabel();
        jValue = new JDoubleField(0d, 10);
        jValue.getDocument().addDocumentListener(this);
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    public JDoubleField getValueComponent() {
        return jValue;
    }

    protected Object getViewValue() {
        return jValue.getValue();
    }

    protected void setViewValue(Object value) {
        double d = (value != null) ? ((Double) value) : 0d;
        bSkipDocChanges = true;
        jValue.setValue(d);
        bSkipDocChanges = false;
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        jValue.setEditable(!readOnly);
    }

    // ---------- DocumentListener interface

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    public void changedUpdate(DocumentEvent e) {
        if (bSkipDocChanges) {
            return;
        }
        updateModel();
    }

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    public void insertUpdate(DocumentEvent e) {
        if (bSkipDocChanges) {
            return;
        }
        updateModel();
    }

    /**
     * Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param e the document event
     */
    public void removeUpdate(DocumentEvent e) {
        if (bSkipDocChanges) {
            return;
        }
        updateModel();
    }
}