/*
 * StringAspectEditor.java
 *
 * Created on 5 februari 2003, 17:33
 */

package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.gui.core.LimitedTextDocument;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Karel
 */
public class StringAspectEditor extends AspectEditor<JTextComponent> implements DocumentListener {

    protected JLabel jLabel;
    protected JTextComponent jValue;

    public StringAspectEditor() {
        jLabel = new JLabel();
        initValueComponent();
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    public JTextComponent getValueComponent() {
        return jValue;
    }

    public void setDocument(Document document) {
        jValue.getDocument().removeDocumentListener(this);
        document.addDocumentListener(this);
        jValue.setDocument(document);
    }

    protected void initValueComponent() {
        jValue = new JTextField(new LimitedTextDocument(4000), null, 35);   //In relations the 'String' attributes are limites to 4K (varchar2(4000)) -> see StringFactory.dbtype()
        jValue.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateToolTip();
            }
        });
        jValue.getDocument().addDocumentListener(this);
    }

    protected Object getViewValue() {
        return jValue.getText();
    }

    protected void setViewValue(Object value) {
        if (value == null) {
            updateField("");
        } else {
            updateField(value.toString());
        }
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        jValue.setEditable(!readOnly);
        if (readOnly) {
            jValue.setCaretPosition(0);
        }
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    public void changedUpdate(DocumentEvent e) {
        updateModel();
    }

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    public void insertUpdate(DocumentEvent e) {
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
        updateModel();
    }

    protected boolean hasValidModel() {
        Object o = getModelValue();
        return o != null && ((String) o).trim().length() > 0;
    }

    protected void updateField(String text) {
        jValue.getDocument().removeDocumentListener(this);
        jValue.setText(text);
        jValue.getDocument().addDocumentListener(this);

        if (!jValue.isEditable()) {
            jValue.setCaretPosition(0);
        }
        updateToolTip();
    }

    private void updateToolTip() {
        if (!jValue.isVisible() || jValue.getText() == null) {
            jValue.setToolTipText(null);
            return;
        }
        String text = jValue.getText();
        FontMetrics fm = jValue.getFontMetrics(jValue.getFont());
        Rectangle rectText = fm.getStringBounds(text, jValue.getGraphics()).getBounds();
        Rectangle rectField = jValue.getBounds();
        Insets insets = jValue.getInsets();
        if (rectField.width - (insets.left + insets.right) < rectText.width) {
            jValue.setToolTipText(text);
        } else {
            jValue.setToolTipText(null);
        }
    }
}

