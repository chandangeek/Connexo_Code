package com.energyict.mdc.engine.offline.gui.editors;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.ParseException;

public class SpinnerEditor extends AspectEditor<JSpinner> {

    private JLabel label;
    private JSpinner spinner;

    public SpinnerEditor(SpinnerModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model cannot be null");
        }

        this.label = new JLabel();
        this.spinner = new JSpinner(model);
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
    public JSpinner getValueComponent() {
        return this.spinner;
    }

    @Override
    protected Object getViewValue() {
        return this.spinner.getValue();
    }

    @Override
    protected void setViewValue(Object value) {
        try {
            if (value != null){
                this.spinner.setValue(value);
            }
            // When initiating the spinner with an invalid value the foreground color is set to RED
            setEditorForeground(value == null || isValueValid(value) ? Color.BLACK : Color.RED);
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
        this.spinner.setEnabled(!readOnly);
        super.setReadOnly(readOnly);
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
