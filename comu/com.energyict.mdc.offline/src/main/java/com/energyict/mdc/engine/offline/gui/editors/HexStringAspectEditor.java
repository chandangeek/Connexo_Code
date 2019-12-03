package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.common.HexString;
import com.energyict.mdc.engine.offline.gui.core.HexStringField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * User: gde
 * Date: 17/04/13
 */
public class HexStringAspectEditor extends AspectEditor<HexStringField> implements DocumentListener {

    protected JLabel label;
    protected HexStringField field;

    public HexStringAspectEditor() {
        label = new JLabel();
        initValueComponent();
    }

    public JLabel getLabelComponent() {
        return label;
    }

    public HexStringField getValueComponent() {
        return field;
    }

    protected void initValueComponent() {
        field = new HexStringField();
        field.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateToolTip();
            }
        });
        field.getDocument().addDocumentListener(this);
    }

    protected Object getViewValue() {
        return field.getValue();
    }

    protected void setViewValue(Object value) {
        updateField((HexString)value);
    }

    protected void updateLabel() {
        label.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        field.setEditable(!readOnly);
        if (readOnly) {
            field.setCaretPosition(0);
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
        if(o != null && o instanceof HexString){
            HexString hexString = (HexString)o;
            return (hexString.getContent() != null) && !hexString.getContent().trim().isEmpty();
        }
        return false;
    }

    protected void updateField(HexString text) {
        field.getDocument().removeDocumentListener(this);
        field.setValue(text);
        field.getDocument().addDocumentListener(this);

        if (!field.isEditable()) {
            field.setCaretPosition(0);
        }
        updateToolTip();
    }

    private void updateToolTip() {
        if (!field.isVisible() || field.getText() == null) {
            field.setToolTipText(null);
            return;
        }
        String text = field.getText();
        FontMetrics fm = field.getFontMetrics(field.getFont());
        Rectangle rectText = fm.getStringBounds(text, field.getGraphics()).getBounds();
        Rectangle rectField = field.getBounds();
        Insets insets = field.getInsets();
        if (rectField.width - (insets.left + insets.right) < rectText.width) {
            field.setToolTipText(text);
        } else {
            field.setToolTipText(null);
        }
    }
}
