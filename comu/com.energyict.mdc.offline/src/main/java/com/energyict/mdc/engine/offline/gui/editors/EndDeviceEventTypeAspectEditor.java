package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.cim.EndDeviceEventType;
import com.energyict.mdc.engine.offline.UserEnvironment;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.core.EscDialog;
import com.energyict.mdc.engine.offline.gui.dialogs.ExceptionDialog;
import com.energyict.mdc.engine.offline.gui.panels.EndDeviceEventTypeEditorPnl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: gde
 * Date: 16/05/12
 */
public class EndDeviceEventTypeAspectEditor extends AspectEditor<JPanel> implements DocumentListener {

    private JLabel label;
    private EndDeviceEventTypeField field;
    private JButton browseBtn;
    private JPanel valueComponent;
    private boolean bSkipDocChanges = false;
    private Color originalFgColor = null;
    private boolean readOnly = false;

    public EndDeviceEventTypeAspectEditor() {
        label = new JLabel();
        field = new EndDeviceEventTypeField(20);
        originalFgColor = field.getForeground();
        field.getDocument().addDocumentListener(this);
        browseBtn = new JButton("...");
        browseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseButtonActionPerformed();
            }
        });
    }

    public JLabel getLabelComponent() {
        return label;
    }

    public JPanel getValueComponent() {
        if (valueComponent == null) {
            valueComponent = doGetValueComponent();
        }
        return valueComponent;
    }

    protected JPanel doGetValueComponent() {
        JPanel result = new JPanel();
        result.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        result.add(field, c);

        c.gridx = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 5, 0, 0);
        result.add(browseBtn, c);

        return result;
    }

    protected void updateLabel() {
        label.setText(getLabelString());
    }

    protected Object getViewValue() {
        return (field.hasValue()) ? field.getValue() : null;
    }

    protected void setViewValue(Object value) {
        bSkipDocChanges = true;
        field.setValue((EndDeviceEventType) value);
        bSkipDocChanges = false;
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        this.readOnly = readOnly;
        field.setEditable(!readOnly);
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

    // Only update the model (eg. shadow) if a VALID EndDeviceEventType is entered
    @Override
    public void updateModel() {
        if (Utils.isNull(field.getText()) || doHasValidValue()) {
            field.setForeground(originalFgColor);
            super.updateModel();
        } else {
            field.setForeground(Color.RED);
            setModelValue(null);
        }
    }

    @Override
    protected boolean doHasValidValue() {
        return field.hasValue();
    }

    protected void browseButtonActionPerformed() {
        JFrame parentFrame = (JFrame) (UserEnvironment.getDefault().get(ExceptionDialog.PARENTFRAME));
        EscDialog dlg = new EscDialog(parentFrame == null ? new JFrame() : parentFrame, true);
        String title = TranslatorProvider.instance.get().getTranslator().getTranslation("cimEventEditor");
        if (!title.startsWith("MR") && !title.startsWith("NR")) {
            dlg.setTitle(title);
        }
        dlg.getContentPane().setLayout(new BorderLayout());
        EndDeviceEventTypeEditorPnl pnl = getEndDeviceEventTypeEditorPnl(this.readOnly);
        dlg.getContentPane().add(pnl, BorderLayout.CENTER);
        dlg.pack();
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        if (pnl.isCanceled()) {
            return;
        }
        setViewValue(pnl.getEventType());
        updateModel();
    }

    protected EndDeviceEventTypeEditorPnl getEndDeviceEventTypeEditorPnl(boolean readOnly) {
        return new EndDeviceEventTypeEditorPnl((EndDeviceEventType) getViewValue(), readOnly);
    }
}
