package com.energyict.mdc.engine.offline.gui.editors;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class UnitAspectEditor extends AspectEditor<JPanel> {

    private JLabel jLabel;
    private JComboBox baseUnitCombo;
    private JComboBox scaleCombo;
    private JPanel component;
    private JLabel multiplyLabel;

    public UnitAspectEditor() {
        jLabel = new JLabel();
        multiplyLabel = new JLabel(" x 10^ ");
        baseUnitCombo = new JComboBox(new Vector(getBaseUnits()));
        baseUnitCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                unitComboActionPerformed(evt);
            }
        });
        scaleCombo = new JComboBox(new Vector(getScaleChoices()));
        scaleCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                scaleComboActionPerformed(evt);
            }
        });
        component = new JPanel(new BorderLayout(0, 0));
        JPanel unitPnl = new JPanel(new BorderLayout(0, 0));
        unitPnl.add(baseUnitCombo, BorderLayout.CENTER);
        unitPnl.add(multiplyLabel, BorderLayout.EAST);

        component.add(unitPnl, BorderLayout.WEST);
        component.add(scaleCombo, BorderLayout.CENTER);
    }

    protected List<BaseUnit> getBaseUnits() {
        List<BaseUnit> baseUnits = new ArrayList<BaseUnit>();
        for (Iterator it = BaseUnit.iterator(); it.hasNext(); ) {
            baseUnits.add((BaseUnit) it.next());
        }
        Collections.sort(baseUnits, new Comparator<BaseUnit>() {
            public int compare(BaseUnit o1, BaseUnit o2) {
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });
        return baseUnits;
    }

    protected List<Integer> getScaleChoices() {
        List<Integer> scales = new ArrayList<Integer>();
        for (int i = 9; i > -10; i--) {
            scales.add(new Integer(i));
        }
        return scales;
    }

    public JLabel getLabelComponent() {
        return jLabel;
    }

    public JPanel getValueComponent() {
        return component;
    }

    private void unitComboActionPerformed(ActionEvent evt) {
        updateModel();
    }

    private void scaleComboActionPerformed(ActionEvent evt) {
        updateModel();
    }

    protected Object getViewValue() {
        if (baseUnitCombo.getSelectedItem() == null || scaleCombo.getSelectedItem() == null) {
            return null;
        }

        return Unit.get(
                ((BaseUnit) baseUnitCombo.getSelectedItem()).getDlmsCode(),
                ((Integer) scaleCombo.getSelectedItem()).intValue());
//        return jValue.getSelectedItem();   
    }

    protected void setViewValue(Object value) {
        Unit unit = (Unit) value;
        baseUnitCombo.setSelectedItem(unit.getBaseUnit());
        scaleCombo.setSelectedItem(new Integer(unit.getScale()));
    }

    protected void updateLabel() {
        jLabel.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        baseUnitCombo.setEnabled(!readOnly);
        scaleCombo.setEnabled(!readOnly);
    }
}

