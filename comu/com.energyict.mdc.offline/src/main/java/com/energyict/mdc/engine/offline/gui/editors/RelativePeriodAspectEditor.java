package com.energyict.mdc.engine.offline.gui.editors;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RelativePeriodAspectEditor extends AspectEditor<JComboBox> {

    private JLabel theLabel;
    private JComboBox theValueBox;

    public RelativePeriodAspectEditor() {
        theLabel = new JLabel();
        theValueBox = new JComboBox();
        theValueBox.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        ValueActionPerformed(evt);
                    }
                }
        );
        initValues();
    }

    private void initValues() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
//        RelativePeriodFactory factory = (RelativePeriodFactory)FormatProvider.instance.get().findFactory(MeteringWarehouse.FACTORYID_RELATIVEPERIOD);
//        List<RelativePeriod> relativePeriods = factory.findAll();
//        for(RelativePeriod period: relativePeriods) {
//            if (!period.isCustomPeriod())
//                model.addElement(period);
//        }
        theValueBox.setModel(model);
    }

    public JLabel getLabelComponent() {
        return theLabel;
    }

    public JComboBox getValueComponent() {
        return theValueBox;
    }

    private void ValueActionPerformed(ActionEvent evt) {
        updateModel();
    }

    protected Object getViewValue() {
        return theValueBox.getSelectedItem();
    }

    protected void setViewValue(Object value) {
        theValueBox.setSelectedItem(value);
    }

    protected void updateLabel() {
        theLabel.setText(getLabelString());
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        theValueBox.setEnabled(!readOnly);
    }
}
