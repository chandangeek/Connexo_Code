package com.energyict.mdc.engine.offline.gui.editors;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.core.IntegerDocument;
import com.energyict.mdc.engine.offline.gui.core.JIntegerField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * @author Karel, Geert
 */
public class TimeDurationAspectEditor extends AspectEditor<JPanel> implements DocumentListener {

    private JPanel valueComponent;
    private JLabel jLabel;
    private JIntegerField jValue;
    private JComboBox<Integer> unitComboBox;
    private boolean bSkipChanges = false;
    private List<Integer> timeDurationUnits = null;
    private TimeDuration timeDurationToRepresent;
    private boolean readOnly;
    private boolean nullable = false;
    private JCheckBox setNullChkBox;

    /**
     * Creates a new instance of TimeDurationAspectEditor
     */
    public TimeDurationAspectEditor() {
        this(getAllTimeUnitCodes());
    }

    public TimeDurationAspectEditor(List<Integer> possibleTimeUnits) {
        this.timeDurationUnits = possibleTimeUnits;

        jLabel = new JLabel();
        jValue = new JIntegerField(0, 4);
        jValue.setAllowNegativeValues(false);
        jValue.getDocument().addDocumentListener(this);

        setNullChkBox = new JCheckBox(TranslatorProvider.instance.get().getTranslator().getTranslation("undefined"));
        setNullChkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (bSkipChanges||!isNullable()) {
                    return;
                }
                jValue.setEnabled(!readOnly && !setNullChkBox.isSelected());
                if (timeDurationUnits.size() > 1) {
                    getUnitCombo().setEnabled(!readOnly && !setNullChkBox.isSelected());
                }
                updateModel();
            }
        });

        initUnitCombo();
        // We added the MILLISECONDS to have the combo wide enough
        // but by default it wasn't in, so:
        List<Integer> timeUnits2Remove = new ArrayList<>();
        timeUnits2Remove.add(Calendar.MILLISECOND);
        removeTimeUnits(timeUnits2Remove);

        if (timeDurationUnits.size() == 1) {
            getUnitCombo().setEnabled(false);
        } else {
            getUnitCombo().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    validateAndUpdateModel();
                }
            });
        }
    }

    public void removeTimeUnits(List<Integer> timeDurationUnitsToRemove) {
        Integer currentlyChosenUnit = (Integer) getUnitCombo().getSelectedItem();
        for (Iterator<Integer> it = timeDurationUnits.iterator(); it.hasNext();) {
            Integer each = it.next();
            if (timeDurationUnitsToRemove.contains(each)) {
                it.remove();
            }
        }
        DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>(timeDurationUnits.toArray(new Integer[timeDurationUnits.size()]));
        if (model.getIndexOf(currentlyChosenUnit)!=-1) {
            model.setSelectedItem(currentlyChosenUnit);
        } else {
            model.setSelectedItem(model.getElementAt(0));
        }
        getUnitCombo().setModel(model);
        setViewValue(timeDurationToRepresent);
    }

    public void addTimeUnits(List<Integer> timeDurationUnitsToAdd) {
        Integer currentlyChosenUnit = (Integer) getUnitCombo().getSelectedItem();
        for (Integer each : timeDurationUnitsToAdd) {
            if (!timeDurationUnits.contains(each)) {
                timeDurationUnits.add(each);
            }
        }
        // Order them
        Collections.sort(timeDurationUnits, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                switch (o1) {
                    case Calendar.MILLISECOND: return -1;
                    case Calendar.SECOND:
                        switch(o2) {
                            case Calendar.MILLISECOND: return 1;
                            case Calendar.SECOND: return 0;
                            case Calendar.MINUTE:
                            case Calendar.HOUR:
                            case Calendar.DATE:
                            case Calendar.WEEK_OF_YEAR:
                            case Calendar.MONTH:
                            case Calendar.YEAR: return -1;
                        }
                    case Calendar.MINUTE:
                        switch(o2) {
                            case Calendar.MILLISECOND:
                            case Calendar.SECOND: return 1;
                            case Calendar.MINUTE: return 0;
                            case Calendar.HOUR:
                            case Calendar.DATE:
                            case Calendar.WEEK_OF_YEAR:
                            case Calendar.MONTH:
                            case Calendar.YEAR: return -1;
                        }
                    case Calendar.HOUR:
                        switch(o2) {
                            case Calendar.MILLISECOND:
                            case Calendar.SECOND:
                            case Calendar.MINUTE: return 1;
                            case Calendar.HOUR: return 0;
                            case Calendar.DATE:
                            case Calendar.WEEK_OF_YEAR:
                            case Calendar.MONTH:
                            case Calendar.YEAR: return -1;
                        }
                    case Calendar.DATE:
                        switch(o2) {
                            case Calendar.MILLISECOND:
                            case Calendar.SECOND:
                            case Calendar.MINUTE:
                            case Calendar.HOUR: return 1;
                            case Calendar.DATE: return 0;
                            case Calendar.WEEK_OF_YEAR:
                            case Calendar.MONTH:
                            case Calendar.YEAR: return -1;
                        }
                    case Calendar.WEEK_OF_YEAR:
                        switch(o2) {
                            case Calendar.MILLISECOND:
                            case Calendar.SECOND:
                            case Calendar.MINUTE:
                            case Calendar.HOUR:
                            case Calendar.DATE: return 1;
                            case Calendar.WEEK_OF_YEAR: return 0;
                            case Calendar.MONTH:
                            case Calendar.YEAR: return -1;
                        }
                    case Calendar.MONTH:
                        switch(o2) {
                            case Calendar.MILLISECOND:
                            case Calendar.SECOND:
                            case Calendar.MINUTE:
                            case Calendar.HOUR:
                            case Calendar.DATE:
                            case Calendar.WEEK_OF_YEAR: return 1;
                            case Calendar.MONTH: return 0;
                            case Calendar.YEAR: return -1;
                        }
                    case Calendar.YEAR: return 1;
                    default: return 0;
                }
            }
        });
        DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<>(timeDurationUnits.toArray(new Integer[timeDurationUnits.size()]));
        if (model.getIndexOf(currentlyChosenUnit)!=-1) {
            model.setSelectedItem(currentlyChosenUnit);
        } else {
            model.setSelectedItem(model.getElementAt(0));
        }
        getUnitCombo().setModel(model);
        setViewValue(timeDurationToRepresent);
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
        setNullChkBox.setVisible(nullable);
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

    public JComboBox<Integer> getUnitCombo() {
        return unitComboBox;
    }

    protected JPanel doGetValueComponent() {
        JPanel result = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        result.add(jValue, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        result.add(getUnitCombo(), gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0,5,0,0);
        result.add(setNullChkBox, gbc);
        setNullChkBox.setVisible(isNullable());
        return result;
    }

    private void initUnitCombo() {
        unitComboBox = new JComboBox<>(timeDurationUnits.toArray(new Integer[timeDurationUnits.size()]));
        unitComboBox.setRenderer(new TimeDurationUnitListCellRenderer());
        // gde
        // Make the height of the unit combobox fit the value edit box
        java.awt.Dimension dU = unitComboBox.getPreferredSize();
        java.awt.Dimension dV = jValue.getPreferredSize();
        dU.height = dV.height;
        unitComboBox.setPreferredSize(dU); // gde [end]
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    protected Object getViewValue() {
        if (isNullable() && setNullChkBox.isSelected()) {
            return null;
        }
        int count = jValue.getValue();
        int unitCode = (Integer) getUnitCombo().getSelectedItem();
        try {
            return new TimeDuration(count, unitCode);
        } catch (IllegalArgumentException iae) {
            UiHelper.reportException(iae, null);
        }
        return TimeDuration.NONE;
    }

    protected void setViewValue(Object value) {
        timeDurationToRepresent = TimeDuration.NONE;
        if (value != null) {
            timeDurationToRepresent = (TimeDuration) value;
        }
        bSkipChanges = true;
        setNullChkBox.setSelected(nullable && value==null);
        jValue.setValue(timeDurationToRepresent.getCount());
        getUnitCombo().setSelectedItem(timeDurationToRepresent.getTimeUnitCode());
        bSkipChanges = false;
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        this.readOnly = readOnly;
        jValue.setEnabled(!readOnly && !setNullChkBox.isSelected());
        if (timeDurationUnits.size() > 1) {
            getUnitCombo().setEnabled(!readOnly && !setNullChkBox.isSelected());
        }
        setNullChkBox.setEnabled(!readOnly);
    }

    /* 
     * parameter is a list of Integer's indicating which units to remove 
     * (possible values are TimeDuration.SECONDS, ..., TimeDuration.YEARS) 
     */

    public void excludeUnits(List<Integer> toExclude) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) getUnitCombo().getModel();
        for (Integer each : toExclude) {
            int index = timeDurationUnits.indexOf(each);
            if (index >= 0) {
                model.removeElementAt(index);
                timeDurationUnits.remove(index);
            }
        }
    }

    // DocumentListener interface
    public void changedUpdate(DocumentEvent e) {
        validateAndUpdateModel();
    }

    public void insertUpdate(DocumentEvent e) {
        validateAndUpdateModel();
    }

    public void removeUpdate(DocumentEvent e) {
        validateAndUpdateModel();
    }

    private void validateAndUpdateModel() {
        if (bSkipChanges) {
            return;
        }
        try {
            new TimeDuration(jValue.getValue(), (Integer) getUnitCombo().getSelectedItem());
        } catch (IllegalArgumentException x) {
            UiHelper.reportException(x);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateView(); // make the editor display the last valid value again
                }
            });
            return; //=don't update the model / skip the change
        }
        updateModel();
    }

    private static List<Integer> getAllTimeUnitCodes() {
        List<Integer> all = new ArrayList<>(8);
        all.add(Calendar.MILLISECOND);
        all.add(Calendar.SECOND);
        all.add(Calendar.MINUTE);
        all.add(Calendar.HOUR);
        all.add(Calendar.DATE);
        all.add(Calendar.WEEK_OF_YEAR);
        all.add(Calendar.MONTH);
        all.add(Calendar.YEAR);
        return all;
    }


    private class TimeDurationUnitListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, TranslatorProvider.instance.get().getTranslator().getTranslation(TimeDuration.getTimeUnitDescription((Integer) value)), index, isSelected, cellHasFocus);
        }
    }

    public void setAllowNegativeValue(boolean allowNegativeValue) {
        ((IntegerDocument) jValue.getDocument()).setAllowNegativeValues(allowNegativeValue);
    }
}

