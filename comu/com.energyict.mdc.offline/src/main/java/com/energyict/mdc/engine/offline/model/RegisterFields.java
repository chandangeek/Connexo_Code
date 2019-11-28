package com.energyict.mdc.engine.offline.model;

import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.beans.FormBuilder;
import com.energyict.mdc.engine.offline.gui.core.JBigDecimalField;
import com.energyict.mdc.engine.offline.gui.editors.DateAspectEditor;

import javax.swing.*;
import java.util.Date;

public class RegisterFields {
    private JLabel registerLabel;
    private JLabel obisCodeLabel;
    private JLabel previousLabel = new JLabel(UiHelper.translate("previous")+":");
    private JLabel previousValueLabel;
    private JLabel previousUnitLabel;
    private JLabel previousToDateLabel;
    private JLabel previousEventDateLabel;

    private JBigDecimalField valueField;
    private JLabel unitLabel;
    private JPanel toDatePanel;
    private JPanel eventDatePanel;

    private FormBuilder builder;
    private Date toDate;
    private Date eventDate;

    public RegisterFields() {
    }

    public JLabel getRegisterLabel() {
        return registerLabel;
    }

    public void setRegisterLabel(JLabel registerLabel) {
        this.registerLabel = registerLabel;
    }

    public JLabel getObisCodeLabel() {
        return obisCodeLabel;
    }

    public void setObisCodeLabel(JLabel obisCodeLabel) {
        this.obisCodeLabel = obisCodeLabel;
    }

    public JLabel getPreviousLabel() {
        return previousLabel;
    }

    public JLabel getPreviousValueLabel() {
        return previousValueLabel;
    }

    public void setPreviousValueLabel(JLabel previousValueLabel) {
        this.previousValueLabel = previousValueLabel;
    }

    public JBigDecimalField getValueField() {
        return valueField;
    }

    public void setValueField(JBigDecimalField valueField) {
        this.valueField = valueField;
    }

    public JLabel getUnitLabel() {
        return unitLabel;
    }

    public void setUnitLabel(JLabel unitLabel) {
        this.unitLabel = unitLabel;
    }

    public JLabel getPreviousUnitLabel() {
        return previousUnitLabel;
    }

    public void setPreviousUnitLabel(JLabel previousUnitLabel) {
        this.previousUnitLabel = previousUnitLabel;
    }

    public JLabel getPreviousToDateLabel() {
        return previousToDateLabel;
    }

    public void setPreviousToDateLabel(JLabel previousToDateLabel) {
        this.previousToDateLabel = previousToDateLabel;
    }

    public JLabel getPreviousEventDateLabel() {
        return previousEventDateLabel;
    }

    public void setPreviousEventDateLabel(JLabel previousEventDateLabel) {
        this.previousEventDateLabel = previousEventDateLabel;
    }

    public JPanel getToDatePanel() {
        if (toDatePanel == null) {
            setToDate(new Date());
            DateAspectEditor dateEditor = (DateAspectEditor)getBuilder().getEditor("toDate");
            dateEditor.setShowSeconds(true);
            toDatePanel = dateEditor.getValueComponent();
        }
        return toDatePanel;
    }

    public JPanel getEventDatePanel() {
        if (eventDatePanel == null) {
            DateAspectEditor dateEditor = (DateAspectEditor)getBuilder().getEditor("eventDate");
            dateEditor.setShowSeconds(true);
            eventDatePanel = dateEditor.getValueComponent();
        }
        return eventDatePanel;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public FormBuilder getBuilder() {
        if (builder == null) {
            builder = new FormBuilder(this);
        }
        return builder;
    }
}
