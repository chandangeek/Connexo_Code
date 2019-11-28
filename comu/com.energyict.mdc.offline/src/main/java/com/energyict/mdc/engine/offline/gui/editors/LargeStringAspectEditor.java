package com.energyict.mdc.engine.offline.gui.editors;
import com.energyict.mdc.engine.offline.core.Utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;

public class LargeStringAspectEditor extends AspectEditor<JTextPane> implements DocumentListener {

    protected JLabel jLabel;
    protected JTextPane jValue;

    private int maxCharacters;

    public LargeStringAspectEditor() {
        jLabel = new JLabel();
        jValue = new JTextPane();
        jValue.getDocument().addDocumentListener(this);
    }

    public LargeStringAspectEditor(int maxCharacters) {
        jLabel = new JLabel();
        jValue = new JTextPane();
        this.maxCharacters = maxCharacters;
        jValue.setDocument(new DefaultStyledDocument(){
        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            if (str != null && str.length() + getLength() <= LargeStringAspectEditor.this.maxCharacters) {
                super.insertString(offset, str, a);
            } else{
                Toolkit.getDefaultToolkit().beep();
            }
        }
        });
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    public JTextPane getValueComponent() {
        return jValue;
    }

    protected Object getViewValue() {
        return jValue.getText();
    }

    protected void setViewValue(Object value) {
        jValue.getDocument().removeDocumentListener(this);   // otherwise the shadow is marked dirty
        if (value == null) {
            jValue.setText("");
        } else {
            jValue.setText(value.toString());
        }
        jValue.getDocument().addDocumentListener(this);
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        jValue.setEditable(!readOnly);
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    public void changedUpdate(DocumentEvent e) {
        System.out.println(e.getLength());

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
        String content = (String) getModelValue();
        return !Utils.isNull(content) && (maxCharacters == 0 || content.length() < maxCharacters);
    }

}

