package com.energyict.mdc.engine.offline.gui.core;

import com.energyict.mdc.engine.offline.core.FormatProvider;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.ParseException;

public class JBigDecimalField extends JTextField {

    public JBigDecimalField() {
        setHorizontalAlignment(SwingConstants.LEFT);
        this.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                //do nothing
            }

            public void focusLost(FocusEvent e) {
                if (e.isTemporary()) {
                    return;
                }

                // Attempt to commit the value
                try {
                    ((BigDecimalDocument) JBigDecimalField.this.getDocument()).reformat();
                } catch (ParseException |  BadLocationException ex) {
                    UIManager.getLookAndFeel().provideErrorFeedback(JBigDecimalField.this);
                }

            }
        });
        // Enabling the use of the decimal button on the numeric keypad even if another symbol was set in the userpreferences
        // This for fast entry of numbers (with decimals) using the numeric key pad
        this.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD && e.getKeyCode() == KeyEvent.VK_DECIMAL) {
                    if (FormatProvider.instance.get().getFormatPreferences().getDecimalFormatSymbols().getDecimalSeparator() == (char) KeyEvent.VK_COMMA) {
                        try {
                            getDocument().insertString(getCaretPosition(), String.valueOf(FormatProvider.instance.get().getFormatPreferences().getDecimalFormatSymbols().getDecimalSeparator()), null);
                        } catch (BadLocationException ex) {
                            //?        
                        }
                    }
                }
            }
        });
    }

    public JBigDecimalField(BigDecimal value) {
        this();
        setValue(value);
    }

    public JBigDecimalField(BigDecimal value, int columns) {
        this(value);
        setColumns(columns);
    }

    @Override
    public void setDocument(Document doc) {
        String currentText = null;
        if (getDocument() != null) {
            // the text in the new document is replaced by the current contents
            currentText = getText();
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, currentText, null);
            } catch (BadLocationException e) {
                UIManager.getLookAndFeel().provideErrorFeedback(JBigDecimalField.this);
            }
        }
        super.setDocument(doc);
    }

    public BigDecimal getValue() {
        try {
            return ((BigDecimalDocument) getDocument()).getBigDecimal();
        } catch (ParseException e) {
            UIManager.getLookAndFeel().provideErrorFeedback(JBigDecimalField.this);
            return null;
        } catch (BadLocationException e) {
            UIManager.getLookAndFeel().provideErrorFeedback(JBigDecimalField.this);
            return null;
        }
    }

    public void setValue(BigDecimal value) {
        try {
            ((BigDecimalDocument) getDocument()).setBigDecimal(value);
        } catch (BadLocationException e) {
            UIManager.getLookAndFeel().provideErrorFeedback(JBigDecimalField.this);
        }
    }

    @Override
    public void postActionEvent() {
        try {
            ((BigDecimalDocument) getDocument()).reformat();
        } catch (ParseException | BadLocationException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(JBigDecimalField.this);
        }
        super.fireActionPerformed();
    }

    @Override
    protected Document createDefaultModel() {
        return new BigDecimalDocument();
    }
}
