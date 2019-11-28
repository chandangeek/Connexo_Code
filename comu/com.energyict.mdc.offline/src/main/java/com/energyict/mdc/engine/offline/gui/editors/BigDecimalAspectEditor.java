package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.gui.core.BigDecimalDocument;
import com.energyict.mdc.engine.offline.gui.core.JBigDecimalField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;

public class BigDecimalAspectEditor extends AspectEditor<JBigDecimalField> implements DocumentListener, PropertyChangeListener {

    protected JBigDecimalField jValue;
    private JLabel jLabel;
    private boolean bSkipDocChanges = false;
    private Color foregroundColor;

    public BigDecimalAspectEditor() {
        jLabel = new JLabel();
        jValue = new JBigDecimalField(BigDecimal.ZERO, 10);
        foregroundColor = jValue.getForeground();
        jValue.addPropertyChangeListener(this);
        jValue.getDocument().addDocumentListener(this);
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    public JBigDecimalField getValueComponent() {
        return jValue;
    }

    protected Object getViewValue() {
        return jValue.getValue();
    }

    protected void setViewValue(Object value) {
        // geert (2003-nov-14) don't check for null value
        bSkipDocChanges = true;
        doSetViewValue(value);
        if (((BigDecimalDocument) jValue.getDocument()).isValid()) {
            jValue.setForeground(foregroundColor);
        } else {
            jValue.setForeground(Color.RED);
        }
        bSkipDocChanges = false;
    }

    protected void doSetViewValue(Object value) {
        jValue.setValue((BigDecimal) value);
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        jValue.setEditable(!readOnly);
    }
    // ---------- PropertyChangeListener interface

    public void propertyChange(PropertyChangeEvent evt) {
        if ("document".equals(evt.getPropertyName())) {
            // 1) Remove this as listener from the old/previous document:
            if (evt.getOldValue() != null && evt.getOldValue() instanceof Document) {
                ((Document) evt.getOldValue()).removeDocumentListener(this);
            }
            // 2) Add this as listener to the new document:
            jValue.getDocument().addDocumentListener(this);
        }
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

    protected void updateModel() {
        if (((BigDecimalDocument) jValue.getDocument()).isValid()) {
            jValue.setForeground(foregroundColor);
            super.updateModel();
        } else {
            jValue.setForeground(Color.RED);
        }
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