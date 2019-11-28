package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.UserEnvironment;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.core.EscDialog;
import com.energyict.mdc.engine.offline.gui.dialogs.ExceptionDialog;
import com.energyict.mdc.engine.offline.gui.panels.StringListEditorPnl;
import com.energyict.mdc.engine.offline.gui.util.MultiLineToolTip;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * User: gde
 * Date: 7/03/13
 */
public class StringListAspectEditor extends AspectEditor<JPanel> {

    private JPanel valueComponent;
    private JLabel theLabel;
    private JTextField textField;
    private JButton editBtn;
    private List<String> stringList = new ArrayList<>();
    protected boolean readOnly = false;

    public StringListAspectEditor() {
        theLabel = new JLabel();
        textField = new JTextField(20) {
            @Override
            public JToolTip createToolTip() {
                MultiLineToolTip tip = new MultiLineToolTip();
                tip.setComponent(this);
                return tip;
            }
        };
        textField.setEditable(false);
        String tooltipText = getText(stringList, System.getProperty("line.separator").charAt(0));
        textField.setToolTipText(Utils.isNull(tooltipText) ? null : tooltipText );
        editBtn = new JButton("...");
        editBtn.setMargin(new Insets(0, 2, 0, 2));
        editBtn.setToolTipText(TranslatorProvider.instance.get().getTranslator().getTranslation("clickToEdit"));
        editBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editStringList();
            }
        });
    }

    public JLabel getLabelComponent() {
        return theLabel;
    }

    public JPanel getValueComponent() {
        if (valueComponent == null) {
            valueComponent = doGetValueComponent();
        }
        return valueComponent;
    }

    protected JPanel doGetValueComponent() {
        JPanel result = new JPanel(new BorderLayout(2, 2));
        result.add(textField, BorderLayout.CENTER);
        result.add(editBtn, BorderLayout.EAST);
        return result;
    }

    protected Object getViewValue() {
        return stringList;
    }

    protected void setViewValue(Object value) {
        if (value != null) {
            stringList = (List<String>)value;
        } else {
            stringList = new ArrayList<>(0);
        }
        textField.setText(getText(stringList, ','));
        String tooltipText = getText(stringList, System.getProperty("line.separator").charAt(0));
        textField.setToolTipText(Utils.isNull(tooltipText) ? null : tooltipText );
    }

    private String getText(List<String> stringList, char delimiter) {
        StringBuilder result = new StringBuilder();
        for (String each : stringList) {
            result.append(each).append(delimiter);
        }
        if (!stringList.isEmpty()) {
            result.deleteCharAt(result.length()-1); // remove the last added delimiter
        }
        return result.toString();
    }

    protected void updateLabel() {
        theLabel.setText(getLabelString());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        this.readOnly = readOnly;
        editBtn.setEnabled(!readOnly);
    }

    private void editStringList() {
        JFrame parentFrame = (JFrame)(UserEnvironment.getDefault().get(ExceptionDialog.PARENTFRAME));
        EscDialog dlg = new EscDialog(parentFrame == null ? new JFrame() : parentFrame, true);
        if (TranslatorProvider.instance.get().getTranslator().hasTranslation("edit")) {
            dlg.setTitle(TranslatorProvider.instance.get().getTranslator().getTranslation("edit"));
        }
        dlg.getContentPane().setLayout(new BorderLayout());
        StringListEditorPnl pnl = new StringListEditorPnl(stringList);
        pnl.setReadOnly(this.readOnly);
        dlg.getContentPane().add(pnl, BorderLayout.CENTER);
        dlg.pack();
        dlg.setLocationRelativeTo(null);
        dlg.setVisible(true);
        if (pnl.isCanceled()) {
            return;
        }
        setViewValue(pnl.getStringList());
        updateModel();
    }
}
