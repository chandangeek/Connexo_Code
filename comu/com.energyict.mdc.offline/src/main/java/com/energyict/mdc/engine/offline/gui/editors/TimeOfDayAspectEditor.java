/*
 * TimeOfDayAspectEditor.java
 *
 * Created on 14 oktober 2005, 13:27
 */

package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @author Geert
 */
public class TimeOfDayAspectEditor extends AspectEditor<JPanel> implements DocumentListener {

    private JPanel valueComponent;
    private JLabel jLabel;
    private JSpinner jSpinHH;
    private SpinnerRolloverModel modelHH;
    private JSpinner jSpinMM;
    private SpinnerRolloverModel modelMM;
    private JSpinner jSpinSS;
    private SpinnerRolloverModel modelSS;
    private boolean bSkipChanges = false;
    private boolean valid = true;
    private boolean nullable = false;
    private boolean includeSecons = false;
    private boolean readOnly;
    private JCheckBox setNullChkBox;

    private class SpinnerRolloverModel extends SpinnerNumberModel implements ChangeListener {

        private TimeOfDayAspectEditor editor;

        public SpinnerRolloverModel(int value, int min, int max, int step, TimeOfDayAspectEditor editor) {
            super(value, min, max, step);
            this.editor = editor;
        }

        public Object getNextValue() {
            int nextValue;
            if ((getValue()).equals(getMaximum())) {
                nextValue = ((Integer) getMinimum()).intValue();
            } else {
                nextValue = ((Integer) getValue()).intValue() + 1;
            }
            changedUpdate(null);
            return (new Integer(nextValue));
        }

        public Object getPreviousValue() {
            int prevValue;
            if ((getValue()).equals(getMinimum())) {
                prevValue = ((Integer) getMaximum()).intValue();
            } else {
                prevValue = ((Integer) getValue()).intValue() - 1;
            }
            changedUpdate(null);
            return (new Integer(prevValue));
        }

        public void stateChanged(ChangeEvent e) {
            if (bSkipChanges) {
                return;
            }
            editor.updateModel();
        }
    }

    /**
     * Creates a new instance of TimeOfDayAspectEditor
     */
    public TimeOfDayAspectEditor() {
        bSkipChanges = true;
        jLabel = new JLabel();
        setNullChkBox = new JCheckBox(TranslatorProvider.instance.get().getTranslator().getTranslation("undefined"));
        setNullChkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (bSkipChanges || !isNullable()) {
                    return;
                }
                jSpinHH.setEnabled(!readOnly && !setNullChkBox.isSelected());
                jSpinMM.setEnabled(!readOnly && !setNullChkBox.isSelected());
                jSpinSS.setEnabled(!readOnly && !setNullChkBox.isSelected());
                updateModel();
            }
        });

        FocusAdapter focusListener = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                valid = true;
                try {
                    ((JSpinner) e.getSource()).commitEdit();
                } catch (java.text.ParseException err) {
                    valid = false;
                }
            }
        };

        modelHH = new SpinnerRolloverModel(0, 0, 23, 1, this);
        modelHH.addChangeListener(modelHH);
        jSpinHH = new JSpinner(modelHH);
        jSpinHH.addFocusListener(focusListener);

        modelMM = new SpinnerRolloverModel(0, 0, 59, 1, this);
        modelMM.addChangeListener(modelMM);
        jSpinMM = new JSpinner(modelMM);
        jSpinMM.addFocusListener(focusListener);

        modelSS = new SpinnerRolloverModel(0, 0, 59, 1, this);
        modelSS.addChangeListener(modelSS);
        jSpinSS = new JSpinner(modelSS);
        jSpinSS.addFocusListener(focusListener);

        bSkipChanges = false;
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    public JPanel getValueComponent() {
        if (valueComponent == null) {
            valueComponent = doGetValueComponent();
        }
        return valueComponent;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
        setNullChkBox.setVisible(nullable);
    }

    public void setIncludeSeconds(boolean includeSeconds) {
        this.includeSecons = includeSeconds;
        jSpinSS.setVisible(includeSeconds);
    }

    protected JPanel doGetValueComponent() {
        JPanel resultPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        gbc.fill = GridBagConstraints.VERTICAL;
        resultPanel.add(jSpinHH, gbc);

        gbc.gridx++;
        resultPanel.add(jSpinMM, gbc);

        if (includeSecons) {
            gbc.gridx++;
            resultPanel.add(jSpinSS, gbc);
            jSpinSS.setVisible(includeSecons);
        }

        gbc.gridx++;
        gbc.insets = new Insets(0, 5, 0, 0);
        resultPanel.add(setNullChkBox, gbc);
        setNullChkBox.setVisible(isNullable());

        return resultPanel;
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    protected boolean doHasValidValue() {
        return valid;
    }

    protected Object getViewValue() {
        if (isNullable() && setNullChkBox.isSelected()) {
            return null;
        }
        return new TimeOfDay(
                ((Integer) modelHH.getValue()).intValue() * 3600 +
                        ((Integer) modelMM.getValue()).intValue() * 60 +
                        (includeSecons ? ((Integer) modelSS.getValue()).intValue() : 0)
        );
    }

    protected void setViewValue(Object value) {
        bSkipChanges = true;
        setNullChkBox.setSelected(nullable && value == null);
        if (value == null) {
            modelHH.setValue(new Integer(0));
            modelMM.setValue(new Integer(0));
        } else {
            TimeOfDay tod = (TimeOfDay) value;
            modelHH.setValue(new Integer(tod.getHours()));
            modelMM.setValue(new Integer(tod.getMinutes()));
            modelSS.setValue(new Integer(tod.getSeconds()));
        }
        bSkipChanges = false;
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        this.readOnly = readOnly;
        jSpinHH.setEnabled(!readOnly && !setNullChkBox.isSelected());
        jSpinMM.setEnabled(!readOnly && !setNullChkBox.isSelected());
        jSpinSS.setEnabled(!readOnly && !setNullChkBox.isSelected());
        setNullChkBox.setEnabled(!readOnly);
    }

    // DocumentListener interface

    public void changedUpdate(DocumentEvent e) {
        if (bSkipChanges) {
            return;
        }
        doUpdate();
    }

    public void insertUpdate(DocumentEvent e) {
        if (bSkipChanges) {
            return;
        }
        doUpdate();
    }

    public void removeUpdate(DocumentEvent e) {
        if (bSkipChanges) {
            return;
        }
        doUpdate();
    }

    private void doUpdate() {
        updateModel();
    }
}
