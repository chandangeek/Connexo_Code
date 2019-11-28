package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.engine.offline.gui.core.JBigDecimalField;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.math.BigDecimal;

public class QuantityAspectEditor extends AspectEditor<JPanel>
        implements DocumentListener {

    private JPanel valueComponent;
    private JLabel jLabel;
    private JBigDecimalField jValue;
    private JComboBox<Unit> jUnit;
    private boolean bSkipChanges = false;

    public QuantityAspectEditor() {
        jLabel = new JLabel();
        jValue = new JBigDecimalField(new BigDecimal(0), 10);
        jValue.getDocument().addDocumentListener(this);

        jUnit = new JComboBox<>(getChoices());
        // gde
        // Make the height of the unit combobox fit the value edit box
        java.awt.Dimension dU = jUnit.getPreferredSize();
        java.awt.Dimension dV = jValue.getPreferredSize();
        dU.height = dV.height;
        jUnit.setPreferredSize(dU); // gde [end]

        jUnit.addActionListener(
                new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        if (bSkipChanges) {
                            return;
                        }
                        updateModel();
                    }
                }
        );
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

    protected JPanel doGetValueComponent() {
        JPanel result = new JPanel();
        java.awt.GridBagLayout grid = new java.awt.GridBagLayout();
        result.setLayout(grid);
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        grid.setConstraints(jValue, c);
        result.add(jValue);
        c.gridx = 1;
        c.weightx = 0;
        c.fill = java.awt.GridBagConstraints.NONE;
        grid.setConstraints(jUnit, c);
        result.add(jUnit);
        return result;
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    protected Object getViewValue() {
        BigDecimal amount = jValue.getValue();
        Unit unit = (Unit) jUnit.getSelectedItem();
        return (amount == null || unit == null) ?
                null : new Quantity(amount, unit);
    }

    protected void setViewValue(Object value) {
        bSkipChanges = true;
        if (value == null) {
            jValue.setValue(null);
            jUnit.setSelectedItem(Unit.getUndefined());
        } else {
            Quantity quantity = (Quantity) value;
            jValue.setValue(quantity.getAmount());
            jUnit.setSelectedItem(quantity.getUnit());
        }
        bSkipChanges = false;
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        jValue.setEditable(!readOnly);
        jUnit.setEnabled(!readOnly);
    }

    // DocumentListener interface
    public void changedUpdate(DocumentEvent e) {
        if (bSkipChanges) {
            return;
        }
        updateModel();
    }

    public void insertUpdate(DocumentEvent e) {
        if (bSkipChanges) {
            return;
        }
        updateModel();
    }

    public void removeUpdate(DocumentEvent e) {
        if (bSkipChanges) {
            return;
        }
        updateModel();
    }

    // Previously this was a static method of UnitAspectEditor.
    // But due to a complete change of the UnitAspectEditor, we don't need it anymore over there. 
    // So the method has moved to this class.
    protected Unit[] getChoices() {
        return new Unit[]{
                Unit.getUndefined(),
                // Keep them alphabetically ordered!
                Unit.get("A"),
                Unit.get("A2h"),
                Unit.get("bar"),
                Unit.get("\u00B0C"),
                Unit.get("ccf"),
                Unit.get("ccf/h"),
                Unit.get("cf"),
                Unit.get("cf/h"),
                Unit.get("F"),
                Unit.get("\u00B0F"),
                Unit.get("GJ"),
                Unit.get("GJ/h"),
                Unit.get("H"),
                Unit.get("Hz"),
                Unit.get("J"),
                Unit.get("J/h"),
                Unit.get("K"),
                Unit.get("kg"),
                Unit.get("kg/h"),
                Unit.get("kJ"),
                Unit.get("kJ/h"),
                Unit.get("kV"),
                Unit.get("kVA"),
                Unit.get("kVAh"),
                Unit.get("kvar"),
                Unit.get("kvarh"),
                Unit.get("kW"),
                Unit.get("kWh"),
                Unit.get("kWh/m3"),
                Unit.get("kWh/Nm3"),
                Unit.get("l"),
                Unit.get("l/h"),
                Unit.get("m3"),
                Unit.get("m3/h"),
                Unit.get("mbar"),
                Unit.get("mcf"),
                Unit.get("mcf/h"),
                Unit.get("MJ"),
                Unit.get("MJ/h"),
                Unit.get("MJ/Nm3"),
                Unit.get("mmcf"),
                Unit.get("mmcf/h"),
                Unit.get("mol%"),
                Unit.get("mVA"),
                Unit.get("MVA"),
                Unit.get("mVAh"),
                Unit.get("MVAh"),
                Unit.get("mvar"),
                Unit.get("Mvar"),
                Unit.get("mvarh"),
                Unit.get("Mvarh"),
                Unit.get("mW"),
                Unit.get("MW"),
                Unit.get("mWh"),
                Unit.get("MWh"),
                Unit.get("Nm3"),
                Unit.get("Nm3/h"),
                Unit.get("t"),
                Unit.get("t/h"),
                Unit.get("therm"),
                Unit.get("therm/h"),
                Unit.get("V"),
                Unit.get("V2h"),
                Unit.get("VA"),
                Unit.get("VAh"),
                Unit.get("var"),
                Unit.get("varh"),
                Unit.get("W"),
                Unit.get("Wh")
                // Keep them alphabetically ordered!
        };
    }

}
