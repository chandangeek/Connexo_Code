package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.model.RegisterFields;
import com.energyict.mdc.upl.meterdata.Register;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;

import javax.swing.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by H216758 on 05/10/2017.
 */
class MMRDataChecker {
    private final Map<ObisCode, RegisterFields> fields;
    private final ComJobExecutionModel model;

    public MMRDataChecker(Map<ObisCode, RegisterFields> fields, ComJobExecutionModel model) {
        this.fields = fields;
        this.model = model;
    }

    public static OfflineRegister getOfflineRegister(ComJobExecutionModel model, ObisCode obisCode) {
        List<OfflineRegister> allOfflineRegisters = model.getOfflineDevice().getAllOfflineRegisters();
        for (OfflineRegister offlineRegister : allOfflineRegisters) {
            if (offlineRegister.getObisCode().equals(obisCode)) {
                return offlineRegister;
            }
        }
        return null;
    }

    public boolean isValid() {
        if (emptyEventDates()) return false;

        if (wrongToDate()) return false;

//        if (wrongNumberOfDigits()) return false;

        if (wrongNumberOfFractionDigits()) return false;

        return true;
    }

    private boolean emptyEventDates() {
        // Check that if the OBIS code's F part is 0 to 254, the corresponding eventTime is filled in
        List<ObisCode> obisCodesWithEmptyEventDate = new ArrayList<>();
        for (ObisCode each : fields.keySet()) {
            if (each.getF() < 255 && fields.get(each).getEventDate() == null && fields.get(each).getValueField().getValue() != null) {
                obisCodesWithEmptyEventDate.add(each);
            }
        }
        if (obisCodesWithEmptyEventDate.isEmpty()) {
            return false;
        }
        String message;
        if (obisCodesWithEmptyEventDate.size() == 1) {
            String pattern = UiHelper.translate("mmr.eventDateOfRegisterXShouldBeDefined");
            message = Utils.format(pattern,
                    new Object[]{fields.get(obisCodesWithEmptyEventDate.get(0)).getRegisterLabel().getText()});
        } else {
            message = buildMultiErrorMessage(obisCodesWithEmptyEventDate, UiHelper.translate("mmr.eventDateOfTheseRegistersShouldBeDefined"));
        }
        showErrorMessage(message);

        return true;
    }

    private boolean wrongToDate() {
        List<ObisCode> obisCodesWithWrongToDate = new ArrayList<>();
        for (ObisCode obisCode : fields.keySet()) {
            Reading previousRegisterReading = getPreviousRegisterReading(obisCode);
            if ((previousRegisterReading != null) && (previousRegisterReading.getTimeStamp() != null) &&
                    (fields.get(obisCode).getToDate() == null || fields.get(obisCode).getToDate().before(Date.from(previousRegisterReading.getReportedDateTime())))) {
                obisCodesWithWrongToDate.add(obisCode);
            }
        }

        if (obisCodesWithWrongToDate.isEmpty()) {
            return false;
        }
        String message;
        if (obisCodesWithWrongToDate.size() == 1) {
            String pattern = UiHelper.translate("mmr.incorrectToDateForRegisterX");
            message = Utils.format(pattern,
                    new Object[]{fields.get(obisCodesWithWrongToDate.get(0)).getRegisterLabel().getText()});
        } else {
            message = buildMultiErrorMessage(obisCodesWithWrongToDate, UiHelper.translate("mmr.incorrectToDatesForRegisters"));
        }
        showErrorMessage(message);

        return true;
    }

//    private boolean wrongNumberOfDigits() {
//        List<ObisCode> obisCodesWithWrongNumberOfDigits = new ArrayList<>();
//        for (ObisCode obisCode : fields.keySet()) {
//            OfflineRegister offlineRegister = getOfflineRegister(model, obisCode);
//            if (offlineRegister != null) {
//                Register register = model.getRegisterMap().get(offlineRegister);
//                if (register instanceof NumericalRegister && fields.get(obisCode).getValueField().getValue() != null) {
//                    String[] split = fields.get(obisCode).getValueField().getValue().toString().split("\\.");
//                    int numberOfDigits = split[0].length();
//                    if (numberOfDigits > ((NumericalRegister)register).getNumberOfDigits()) {
//                        obisCodesWithWrongNumberOfDigits.add(obisCode);
//                    }
//                }
//            }
//        }
//
//        if (obisCodesWithWrongNumberOfDigits.isEmpty()) {
//            return false;
//        }
//        String message;
//        if (obisCodesWithWrongNumberOfDigits.size() == 1) {
//            String pattern = UiHelper.translate("mmr.invalidNumberOfDigitsForRegisterX");
//            ObisCode obisCode = obisCodesWithWrongNumberOfDigits.get(0);
//            message = Utils.format(pattern,
//                    new Object[]{fields.get(obisCode).getRegisterLabel().getText(), model.getRegisterMap().get(getOfflineRegister(model, obisCode)).getNumberOfDigits()});
//        } else {
//            StringBuilder buffer = new StringBuilder("<html>");
//            buffer.append(UiHelper.translate("mmr.invalidNumberOfDigitsForMultipleRegisters"));
//            buffer.append(":</br><ul>");
//            for (OfflineRegister mmrRegister : model.getOfflineRegistersForMMR()) {
//                if (!obisCodesWithWrongNumberOfDigits.contains(mmrRegister.getObisCode())) {
//                    continue;
//                }
//                buffer.append("<li>");
//                buffer.append(fields.get(mmrRegister.getObisCode()).getRegisterLabel().getText());
//                buffer.append(" (").append(UiHelper.translate("digits")).append(": ").append(model.getRegisterMap().get(mmrRegister).getNumberOfDigits()).append(")");
//                buffer.append("</li>");
//            }
//            buffer.append("</ul></html>");
//            message = buffer.toString();
//        }
//        showErrorMessage(message);
//
//        return true;
//    }

    private boolean wrongNumberOfFractionDigits() {
        List<ObisCode> obisCodesWithWrongNumberOfFractionDigits = new ArrayList<>();
        for (ObisCode obisCode : fields.keySet()) {
            OfflineRegister offlineRegister = getOfflineRegister(model, obisCode);
            if (offlineRegister != null) {
                Register register = model.getRegisterMap().get(offlineRegister);
                if (register instanceof NumericalRegister && fields.get(obisCode).getValueField().getValue() != null) {
                    String[] split = fields.get(obisCode).getValueField().getValue().toString().split("\\.");
                    int numberOfFractionDigits = (split.length == 1 ? 0 : split[1].length());
                    if (numberOfFractionDigits > ((NumericalRegister)register).getNumberOfFractionDigits()) {
                        obisCodesWithWrongNumberOfFractionDigits.add(obisCode);
                    }
                }
            }
        }

        if (obisCodesWithWrongNumberOfFractionDigits.isEmpty()) {
            return false;
        }
        String message;
        if (obisCodesWithWrongNumberOfFractionDigits.size() == 1) {
            String pattern = UiHelper.translate("mmr.invalidNumberOfFractionDigitsForRegisterX");
            ObisCode obisCode = obisCodesWithWrongNumberOfFractionDigits.get(0);
            message = Utils.format(pattern,
                    new Object[]{fields.get(obisCode).getRegisterLabel().getText(), ((NumericalRegister)model.getRegisterMap().get(getOfflineRegister(model, obisCode))).getNumberOfFractionDigits()});
        } else {
            StringBuilder buffer = new StringBuilder("<html>");
            buffer.append(UiHelper.translate("mmr.invalidNumberOfFractionDigitsForMultipleRegisters"));
            buffer.append(":</br><ul>");
            for (OfflineRegister mmrRegister : model.getOfflineRegistersForMMR()) {
                if (!obisCodesWithWrongNumberOfFractionDigits.contains(mmrRegister.getObisCode())) {
                    continue;
                }
                buffer.append("<li>");
                buffer.append(fields.get(mmrRegister.getObisCode()).getRegisterLabel().getText());
                buffer.append(" (").append(UiHelper.translate("numberOfFractionDigits")).append(": ").append(((NumericalRegister)model.getRegisterMap().get(mmrRegister)).getNumberOfFractionDigits()).append(")");
                buffer.append("</li>");
            }
            buffer.append("</ul></html>");
            message = buffer.toString();
        }
        showErrorMessage(message);

        return true;
    }

    private String buildMultiErrorMessage(List<ObisCode> obisCodes, String header) {
        StringBuilder buffer = new StringBuilder("<html>");
        buffer.append(header);
        buffer.append(":</br><ul>");
        for (OfflineRegister mmrRegister : model.getOfflineRegistersForMMR()) {
            if (!obisCodes.contains(mmrRegister.getObisCode())) {
                continue;
            }
            buffer.append("<li>");
            buffer.append(fields.get(mmrRegister.getObisCode()).getRegisterLabel().getText());
            buffer.append("</li>");
        }
        buffer.append("</ul></html>");
        return buffer.toString();
    }

    private void showErrorMessage(String message) {
        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(message);
        optionPane.setMessageType(JOptionPane.ERROR_MESSAGE);
        JDialog dialog = optionPane.createDialog(UiHelper.getMainWindow(), UiHelper.translate("message"));
        dialog.setVisible(true);
    }

    private Reading getPreviousRegisterReading(ObisCode obisCode) {
        for (OfflineRegister offlineRegister : model.getPreviousValuesMap().keySet()) {
            if (offlineRegister.getObisCode().equals(obisCode)) {
                return model.getPreviousValuesMap().get(offlineRegister);
            }
        }
        return null;
    }
}
