package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

public class NullableSpinnerEditor extends AspectEditor<JPanel> {

    private JPanel valueComponent;
    private JLabel label;
    private JSpinner spinner;
    private JCheckBox setNullChkBox;

    private boolean readOnly;

    public NullableSpinnerEditor(SpinnerModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model cannot be null");
        }

        this.label = new JLabel();
        this.spinner = new JSpinner(model);
        setNullChkBox = new JCheckBox(TranslatorProvider.instance.get().getTranslator().getTranslation("undefined"));
        setNullChkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spinner.setEnabled(!readOnly && !setNullChkBox.isSelected());
                spinner.setEnabled(!readOnly && !setNullChkBox.isSelected());
                spinner.setEnabled(!readOnly && !setNullChkBox.isSelected());
                updateModel();
            }
        });

        model.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setModelValue(getViewValue());
                setEditorForeground(isValueValid(getViewValue()) ? Color.BLACK : Color.RED);
            }
        });
    }

    @Override
    public JLabel getLabelComponent() {
        return this.label;
    }

    @Override
    public JPanel getValueComponent() {
        if (valueComponent == null) {
            valueComponent = doGetValueComponent();
        }
        return valueComponent;
    }

    protected JPanel doGetValueComponent() {
        JPanel result = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        result.add(spinner, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0,5,0,0);
        result.add(setNullChkBox, gbc);
        return result;
    }

    @Override
    protected Object getViewValue() {
        if (setNullChkBox.isSelected()) {
            return 0;
        }
        return this.spinner.getValue();
    }

    @Override
    protected void setViewValue(Object value) {
        try {
            setNullChkBox.setSelected(isValueUndefined(value));
            if (isValueUndefined(value)) {
                spinner.setValue(0);
                spinner.setEnabled(false);
            } else {
                spinner.setValue(value);
                // When initiating the spinner with an invalid value the foreground color is set to RED
                setEditorForeground(isValueValid(value) ? Color.BLACK : Color.RED);
            }
        } catch (IllegalArgumentException ex) {
            setEditorForeground(Color.RED);
        }
    }

    @Override
    protected void updateLabel() {
        this.label.setText(getLabelString());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        this.readOnly = readOnly;
        this.spinner.setEnabled(!isValueUndefined(getModelValue()) && !readOnly);
    }

    private boolean isValueUndefined(Object value) {
        return value == null || value.equals(0);
    }

    protected boolean isValueValid(Object value) {
        if (this.spinner.getModel() instanceof SpinnerNumberModel) {
            // testing if the given value is between the minimum and maximum limits
            NumberFormatter formatter = (NumberFormatter) ((JSpinner.NumberEditor) this.spinner.getEditor()).getTextField().getFormatter();
            try {
                formatter.stringToValue(value.toString());
            } catch (ParseException pe) {
                return false;
            }
        }
        return true;
    }

    private void setEditorForeground(Color color) {
        ((JSpinner.NumberEditor) this.spinner.getEditor()).getTextField().setForeground(color);
    }

}
