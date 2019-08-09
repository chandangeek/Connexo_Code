package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.offline.core.FormatProvider;
import com.energyict.mdc.engine.offline.gui.LineComponent;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.core.JBigDecimalField;
import com.energyict.mdc.engine.offline.gui.dialogs.EisDialog;
import com.energyict.mdc.engine.offline.model.RegisterFields;
import com.energyict.mdc.engine.offline.model.ValueForObis;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author khe, geert
 */
public class ManualMeterReadingsDialog extends EisDialog {

    private JLabel msgLabel;
    private JPanel northPnl;
    private JPanel centerPnl;
    private JPanel southPnl;
    private JPanel buttonPnl;
    private JButton okBtn;
    private JButton cancelBtn;
    private JButton validateBtn;
    private Map<ObisCode, RegisterFields> fields = new HashMap<>();

    private final ComJobExecutionModel model;
    private DateFormat dateFormatter;

    public ManualMeterReadingsDialog(Frame parent, boolean modal, ComJobExecutionModel model) {
        super(parent, UiHelper.translate("manualMeterReadingDataEditor"), modal);
        this.model = model;
        dateFormatter = FormatProvider.instance.get().getFormatPreferences().getDateTimeFormat(true);
        initialize();
    }

    private void initialize() {
        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(getNorthPnl(), BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(getCenterPnl()), BorderLayout.CENTER);
        getContentPane().add(getSouthPnl(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel getNorthPnl() {
        if (northPnl == null) {
            northPnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
            northPnl.add(getMsgLabel());
        }
        return northPnl;
    }

    private JLabel getMsgLabel() {
        if (msgLabel == null) {
            msgLabel = new JLabel(UiHelper.translate("mmr.enterMMRDataBelow"));
        }
        return msgLabel;
    }

    private JPanel getCenterPnl() {
        if (centerPnl == null) {
            centerPnl = new JPanel(new GridBagLayout());
            centerPnl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            // Add the (underlined) header/label row
            addHeaderRow();

            int lineCounter = 2;
            for (OfflineRegister mmrRegister : model.getOfflineRegistersForMMR()) {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = lineCounter;
                gbc.insets = new Insets(2, 15, 2, 5);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.BOTH;
                JComponent component = getRegisterLbl(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx++;
                component = getPreviousLbl(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx++;
                gbc.insets = new Insets(2, 0, 2, 5);
                component = getPreviousRegisterValueLabel(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx++;
                component = getPreviousUnitLbl(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx++;
                gbc.insets = new Insets(2, 15, 2, 5);
                component = getPreviousToDateLbl(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx++;
                component = getPreviousEventDateLbl(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                // 2nd line
                // --------
                gbc.gridy = ++lineCounter;
                gbc.gridx = 0;
                component = getObisCodeLbl(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx++;

                gbc.gridx++;
                gbc.insets = new Insets(2, 0, 2, 5);
                component = getRegisterValueField(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx++;
                component = getUnitLbl(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx++;
                gbc.insets = new Insets(2, 15, 2, 5);
                component = getToDatePanel(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx++;
                component = getEventDatePanel(mmrRegister);
                addBorders(component, gbc);
                centerPnl.add(component, gbc);

                gbc.gridx = 0;
                gbc.gridy = ++lineCounter; // New delimiter line
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.insets = new Insets(0, 0, 5, 0);
                centerPnl.add(new LineComponent(Color.LIGHT_GRAY), gbc);

                lineCounter++;
            }
        }
        return centerPnl;
    }

    private void addBorders(JComponent component, GridBagConstraints gbc) {
//        int borderWidth = 1;
//        Color borderColor = Color.LIGHT_GRAY;
//        if (gbc.gridy == 0) {
//            if (gbc.gridx == 0) {
//                // Top left corner element => draw all sides
//                component.setBorder(BorderFactory.createLineBorder(borderColor));
//            } else {
//                // Top edge component => draw all sides except left edge
//                component.setBorder(BorderFactory.createMatteBorder(borderWidth, 0, borderWidth, borderWidth, borderColor));
//            }
//        } else {
//            if (gbc.gridx == 0) {
//                // Left-hand edge component => draw all sides except top
//                component.setBorder(BorderFactory.createMatteBorder(0, borderWidth, borderWidth, borderWidth, borderColor));
//            } else {
//                // Neither top edge nor left edge component => skip both top and left lines
//                component.setBorder(BorderFactory.createMatteBorder(0, 0, borderWidth, borderWidth, borderColor));
//            }
//        }
    }

    private void addHeaderRow() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 15, 2, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;

        JLabel nameLbl = new JLabel(UiHelper.translate("registerName") + " / " + UiHelper.translate("obisCode"));
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD));
        addBorders(nameLbl, gbc);
        centerPnl.add(nameLbl, gbc);

        gbc.gridx++; // extra column for the "Previous:" label

        JLabel valueLbl = new JLabel(UiHelper.translate("value"));
        valueLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD));
        gbc.gridx++;
        gbc.insets = new Insets(2, 0, 2, 5);
        addBorders(valueLbl, gbc);
        centerPnl.add(valueLbl, gbc);

        JLabel unitLbl = new JLabel(UiHelper.translate("unit"));
        unitLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD));
        gbc.gridx++;
        addBorders(unitLbl, gbc);
        centerPnl.add(unitLbl, gbc);

        JLabel toDateLbl = new JLabel(UiHelper.translate("toDate"));
        toDateLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD));
        gbc.gridx++;
        gbc.insets = new Insets(2, 15, 2, 5);
        addBorders(toDateLbl, gbc);
        centerPnl.add(toDateLbl, gbc);

        JLabel eventDateLbl = new JLabel(UiHelper.translate("eventDate"));
        eventDateLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD));
        gbc.gridx++;
        addBorders(eventDateLbl, gbc);
        centerPnl.add(eventDateLbl, gbc);

        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.gridy++;
        centerPnl.add(new LineComponent(), gbc);
    }

    private JLabel getRegisterLbl(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
            registerFields.setRegisterLabel(createRegisterLabel(mmrRegister));
        } else if (fields.get(mmrRegister.getObisCode()).getRegisterLabel() == null) {
            fields.get(mmrRegister.getObisCode()).setRegisterLabel(createRegisterLabel(mmrRegister));
        }
        return fields.get(mmrRegister.getObisCode()).getRegisterLabel();
    }

    private JLabel getObisCodeLbl(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
            registerFields.setObisCodeLabel(createObisCodeLabel(mmrRegister));
        } else if (fields.get(mmrRegister.getObisCode()).getObisCodeLabel() == null) {
            fields.get(mmrRegister.getObisCode()).setObisCodeLabel(createObisCodeLabel(mmrRegister));
        }
        return fields.get(mmrRegister.getObisCode()).getObisCodeLabel();
    }

    private JLabel getPreviousLbl(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
        }
        return fields.get(mmrRegister.getObisCode()).getPreviousLabel();
    }

    private JLabel createRegisterLabel(OfflineRegister mmrRegister) {
        return new JLabel(mmrRegister.getName());
    }

    private JLabel createObisCodeLabel(OfflineRegister mmrRegister) {
        return new JLabel(mmrRegister.getObisCode().toString());
    }

    private JLabel createPreviousRegisterValueLabel(OfflineRegister mmrRegister) {
        Reading previousReading = model.getPreviousValuesMap().get(mmrRegister);
        return new JLabel(previousReading != null && previousReading.getActualReading().getValue() != null ? previousReading.getActualReading().getValue().toString() : "-");
    }

    private JLabel createPreviousToDateLabel(OfflineRegister mmrRegister) {
        Reading previousReading = model.getPreviousValuesMap().get(mmrRegister);
        return new JLabel(previousReading != null && previousReading.getTimeStamp() != null ? dateFormatter.format(previousReading.getTimeStamp()) : "-");
    }

    private JLabel createPreviousEventDateLabel(OfflineRegister mmrRegister) {
        Reading previousReading = model.getPreviousValuesMap().get(mmrRegister);
        return new JLabel(previousReading != null && previousReading.getEventDate() != null ? dateFormatter.format(previousReading.getEventDate() ) : "-");
    }

    private JLabel getPreviousRegisterValueLabel(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
            registerFields.setPreviousValueLabel(createPreviousRegisterValueLabel(mmrRegister));
        } else if (fields.get(mmrRegister.getObisCode()).getPreviousValueLabel() == null) {
            fields.get(mmrRegister.getObisCode()).setPreviousValueLabel(createPreviousRegisterValueLabel(mmrRegister));
        }
        return fields.get(mmrRegister.getObisCode()).getPreviousValueLabel();
    }

    private JBigDecimalField getRegisterValueField(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
            registerFields.setValueField(new JBigDecimalField(null, 10));
        } else if (fields.get(mmrRegister.getObisCode()).getValueField() == null) {
            fields.get(mmrRegister.getObisCode()).setValueField(new JBigDecimalField(null, 10));
        }
        return fields.get(mmrRegister.getObisCode()).getValueField();
    }

    private JLabel getUnitLbl(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
            registerFields.setUnitLabel(new JLabel(mmrRegister.getUnit().toString()));
        } else if (fields.get(mmrRegister.getObisCode()).getUnitLabel() == null) {
            fields.get(mmrRegister.getObisCode()).setUnitLabel(new JLabel(mmrRegister.getUnit().toString()));
        }
        return fields.get(mmrRegister.getObisCode()).getUnitLabel();
    }

    private JPanel getToDatePanel(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
        }
        return fields.get(mmrRegister.getObisCode()).getToDatePanel();
    }

    private JPanel getEventDatePanel(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
        }
        return fields.get(mmrRegister.getObisCode()).getEventDatePanel();
    }

    private JLabel getPreviousUnitLbl(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
            registerFields.setPreviousUnitLabel(new JLabel(mmrRegister.getUnit().toString()));
        } else if (fields.get(mmrRegister.getObisCode()).getUnitLabel() == null) {
            fields.get(mmrRegister.getObisCode()).setPreviousUnitLabel(new JLabel(mmrRegister.getUnit().toString()));
        }
        return fields.get(mmrRegister.getObisCode()).getPreviousUnitLabel();
    }

    private JLabel getPreviousToDateLbl(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
            registerFields.setPreviousToDateLabel(createPreviousToDateLabel(mmrRegister));
        } else if (fields.get(mmrRegister.getObisCode()).getUnitLabel() == null) {
            fields.get(mmrRegister.getObisCode()).setPreviousToDateLabel(createPreviousToDateLabel(mmrRegister));
        }
        return fields.get(mmrRegister.getObisCode()).getPreviousToDateLabel();
    }

    private JLabel getPreviousEventDateLbl(OfflineRegister mmrRegister) {
        if (!fields.containsKey(mmrRegister.getObisCode())) {
            RegisterFields registerFields = new RegisterFields();
            fields.put(mmrRegister.getObisCode(), registerFields);
            registerFields.setPreviousEventDateLabel(createPreviousEventDateLabel(mmrRegister));
        } else if (fields.get(mmrRegister.getObisCode()).getUnitLabel() == null) {
            fields.get(mmrRegister.getObisCode()).setPreviousEventDateLabel(createPreviousEventDateLabel(mmrRegister));
        }
        return fields.get(mmrRegister.getObisCode()).getPreviousEventDateLabel();
    }

    private JPanel getSouthPnl() {
        if (southPnl == null) {
            southPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            southPnl.add(getButtonPnl());
        }
        return southPnl;
    }

    private JPanel getButtonPnl() {
        if (buttonPnl == null) {
            buttonPnl = new JPanel(new GridLayout(1, 0, 3, 3));
            buttonPnl.add(getValidateBtn());
            buttonPnl.add(getOkBtn());
            buttonPnl.add(getCancelBtn());
        }
        return buttonPnl;
    }

    private JButton getValidateBtn() {
        if (validateBtn == null) {
            validateBtn = new JButton(UiHelper.translate("validate"));
            validateBtn.setMnemonic(KeyEvent.VK_V);
            validateBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onValidateBtnPressed();
                }
            });
        }
        return validateBtn;
    }

    private void onValidateBtnPressed() {
//        List<ValueForObis> values = getValues();
//        List<String> validationResultMessages = new ArrayList<>();
//        if (!values.isEmpty()) { // else: user canceled
//            for (ValueForObis valueForObis : values) {
//                if (valueForObis.getValue() == null) {
//                    continue;
//                }
//                OfflineRegister offlineRegister = MMRDataChecker.getOfflineRegister(model, valueForObis.getObisCode());
//                Quantity quantity = new Quantity(valueForObis.getValue(), offlineRegister.getUnit());
//
//                Date eventTime = null;
//                Date toTime = new Date();
//
//                RegisterValue registerValue = new RegisterValue(offlineRegister, quantity, eventTime, toTime);
//                registerValue.setRtuRegisterId(offlineRegister.getRegisterId());
//
//                List<RegisterValidationRule> validationRules = model.getValidationRulesMap().get(offlineRegister);
//                if (validationRules != null) {
//                    for (RegisterValidationRule validationRule : validationRules) {
//                        RegisterExtractor.RegisterReading previousReading = model.getPreviousValuesMap().get(offlineRegister);
//                        Register register = model.getRegisterMap().get(offlineRegister);
//
//                        //Use the to-time of the previous reading as the from-time for this new reading
//                        Date fromDate = null;
//                        if (previousReading != null && previousReading.get.getConsumption() != null) {
//                            fromDate = previousReading.getConsumption().getTo();
//                        }
//
//                        MockRegisterReading registerReading = new MockRegisterReading(
//                                registerValue.getQuantity().getAmount(),
//                                registerValue.getToTime(),
//                                fromDate,
//                                previousReading,
//                                register);
//
//                        RegisterValidator registerValidator = validationRule.newValidator();
//
//                        //Provide the reference value to the validator if necessary, so it should not be fetched from the database
//                        LicensedRegisterValidationRule type = validationRule.getValidationMethod().getValidatorFactory().getLicensedRegisterValidationRule();
//                        if (LicensedRegisterValidationRule.REFERENCE_REGISTER == type) {
//                            ((MainCheckValidator) registerValidator).setReferenceConsumption(model.getReferenceValuesMap().get(offlineRegister));
//                        }
//
//                        RegisterValidationResult validationResult = registerValidator.check(registerReading);
//
//                        StringBuilder buffer = new StringBuilder("<html>");
//                        buffer.append("Validation of value '");
//                        buffer.append(registerValue.getQuantity());
//                        buffer.append("' for register ");
//                        buffer.append(offlineRegister.getName());
//                        buffer.append(" (");
//                        buffer.append(offlineRegister.getObisCode());
//                        buffer.append(")");
//                        buffer.append(" using method '");
//                        buffer.append(type);
//                        buffer.append("': ");
//                        buffer.append(validationResult.getSuccess() ? "<font color='green'>OK</font>" : "<font color='red'>NOT OK</font>");
//                        if (!validationResult.getSuccess()) {
//                            buffer.append(" (").append(validationResult.getReason().toString()).append(")");
//                        }
//                        validationResultMessages.add(buffer.toString());
//                    }
//                }
//            }
//        }
//        if (validationResultMessages.isEmpty()) {
//            validationResultMessages.add(UiHelper.translate("mmr.NoDataToValidateOrNoValidationRulesDefined"));
//        }
//        UiHelper.showModalDialog((JFrame) getParent(), new ValidationResultsPanel(validationResultMessages), UiHelper.translate("validationResult"));
    }

    private JButton getOkBtn() {
        if (okBtn == null) {
            okBtn = new JButton(UiHelper.translate("ok"));
            okBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (new MMRDataChecker(fields, model).isValid()) {
                        dispose();
                    }
                }
            });
        }
        return okBtn;
    }

    private JButton getCancelBtn() {
        if (cancelBtn == null) {
            cancelBtn = new JButton(UiHelper.translate("cancel"));
            cancelBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    performEscapeAction(null);
                }
            });
        }
        return cancelBtn;
    }

    public List<ValueForObis> getValues() {
        List<ValueForObis> values = new ArrayList<>();
        for (ObisCode obisCode : fields.keySet()) {
            RegisterFields registerFields = fields.get(obisCode);
            ValueForObis valueForObis = new ValueForObis(registerFields.getValueField().getValue(), obisCode);
            if (registerFields.getToDate() != null) {
                valueForObis.setToDate(registerFields.getToDate());
            }
            if (registerFields.getEventDate() != null) {
                valueForObis.setEventDate(registerFields.getEventDate());
            }
            values.add(valueForObis);
        }
        return values;
    }

    @Override
    public void performEscapeAction(KeyEvent evt) {
        fields.clear();
        dispose();
    }

}