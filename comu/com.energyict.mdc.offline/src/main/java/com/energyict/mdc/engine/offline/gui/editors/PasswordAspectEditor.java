package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.gui.core.PatchedJPasswordField;
import com.energyict.mdc.engine.offline.model.Password;
import com.jidesoft.swing.ButtonStyle;
import com.jidesoft.swing.JideToggleButton;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyDescriptor;

/**
 * Depending on the 'Privilege' assigned to this <Code>AspectEditor</Code>
 * an editable JTextField or a non editable JPasswordField will be used as 'Value Component'
 * <p/>
 * With 'Privilege' we mean a UserAction on a Object having a given TypeId that can be performed by a User having a particular Role
 *
 * @see com.energyict.mdw.core.Privilege
 *      <p/>
 *      Copyrights EnergyICT
 *      Date: 13-sep-2010
 *      Time: 13:42:07
 */
public class PasswordAspectEditor extends AspectEditor<JPasswordField> implements DocumentListener {

    private JLabel jLabel;
    protected JPasswordField jValue;
    private JideToggleButton plainTextToggleButton;
    private boolean plainText;
    private boolean isEditingPassword = false;
    private boolean forceReadOnly;

    /**
     * Creates an editable PasswordAspectEditor
     */
    public PasswordAspectEditor() {
        this(false);
    }

    /**
     * Creates a PasswordAspectEditor with indication whether the password should be displayed in plain text or masked
     *
     * @param plainText if true the password is displayed as readable text, if false the password will be masked by bullets
     */
    public PasswordAspectEditor(boolean plainText) {
        this.jLabel = new JLabel();
        this.plainText = plainText;
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    public JPasswordField getValueComponent() {
        if (jValue == null) {
            initValueComponent();
        }
        return jValue;
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    public String getLabelString() {
        return getLabelString(true);
    }

    @Override
    // overriden because this editor can be used as a Password Editor but also an
    // editor for Strings that are treated as passwords (e.g. Device.getPassword => String but must be handled as a password field);
    public void updateView() {
        PropertyDescriptor descriptor = getDescriptor();
        isEditingPassword = descriptor.getPropertyType().equals(Password.class);
        super.updateView();
    }

    @Override
    protected Object getViewValue() {
        if (jValue.getText() == null) {
            return null;
        }
        if (this.isEditingPassword) {
            return new Password(jValue.getText());
        } else {
            return jValue.getText();
        }
    }

    @Override
    protected void setViewValue(Object value) {
        if (jValue != null) {
            setText(value);
        }
    }

    public void setReadOnly(boolean readOnly) {
        this.forceReadOnly = readOnly;
        super.setReadOnly(readOnly);
        if (jValue != null) {
            jValue.setEditable(!readOnly);
        }
    }

    //----------------- DocumentListener interface ---------------

    public void changedUpdate(DocumentEvent e) {
        updateModel();
    }

    public void insertUpdate(DocumentEvent e) {
        updateModel();
    }

    public void removeUpdate(DocumentEvent e) {
        updateModel();
    }
    //----------------------------------------------------------------

    private void initValueComponent() {
        jValue = getPasswordField();


        setText(getModelValue());
        jValue.setEditable(!forceReadOnly);
    }

    private JPasswordField getPasswordField() {
        PatchedJPasswordField result = new PatchedJPasswordField(20);
        result.setPlainText(plainText);
        return result;
    }

    private void setText(Object value) {
        jValue.getDocument().removeDocumentListener(this);
        if (value == null) {
            jValue.setText(null);
            if (!jValue.isEditable()) {
                jValue.setCaretPosition(0);
            }
        } else {
            if (isEditingPassword) {
                jValue.setText(((Password) value).getValue());
            } else {
                jValue.setText(value.toString());
            }
        }
        jValue.getDocument().addDocumentListener(this);
    }

    private void setPlainText(boolean plainText) {
        this.plainText = plainText;
        ((PatchedJPasswordField) jValue).setPlainText(plainText);
    }

    public JComponent getPlainTextToggleComponent(){
        if (plainTextToggleButton == null) {
            plainTextToggleButton = new JideToggleButton(new ImageIcon(getClass().getResource("/mdw/images/eye.png")));
            plainTextToggleButton.setButtonStyle(ButtonStyle.TOOLBOX_STYLE);
            plainTextToggleButton.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    setPlainText(e.getStateChange() == ItemEvent.SELECTED);
                }
            });
        }
        return plainTextToggleButton;
    }


    protected boolean hasValidModel() {
        Object o = getModelValue();
        if(o != null && o instanceof Password){
            Password password = (Password)o;
            return  !password.getValue().trim().isEmpty();
        }
        return false;
    }
}
