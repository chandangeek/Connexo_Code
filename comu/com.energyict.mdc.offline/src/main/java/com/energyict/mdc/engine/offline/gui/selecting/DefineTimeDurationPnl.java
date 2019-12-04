package com.energyict.mdc.engine.offline.gui.selecting;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.beans.FormBuilder;
import com.energyict.mdc.engine.offline.gui.windows.EisPropsPnl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DefineTimeDurationPnl extends EisPropsPnl {

    private JPanel centerPnl;
    private JPanel southPnl;
    private JPanel buttonPnl;
    private JButton okBtn;
    private JButton cancelBtn;
    private TimeDuration timeDuration;
    private JLabel timeDurationLbl;
    private JPanel timeDurationPnl;
    private FormBuilder builder;
    private String label;

    public DefineTimeDurationPnl() {
    }

    public void initialize(TimeDuration defaultTimeDuration, String label) {
        this.label = label;
        if (defaultTimeDuration != null) {
            timeDuration = defaultTimeDuration;
        } else {
            timeDuration = TimeDuration.NONE;
        }
        builder = new FormBuilder(this);
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        add(getCenterPnl(), BorderLayout.CENTER);
        add(getSouthPnl(), BorderLayout.SOUTH);
    }

    private JPanel getSouthPnl() {
        if (southPnl == null) {
            southPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            southPnl.add(getButtonPnl());
        }
        return southPnl;
    }

    private JPanel getButtonPnl() {
        if (buttonPnl == null) {
            buttonPnl = new JPanel(new GridLayout(1, 0, 6, 0));
            buttonPnl.add(getOkBtn());
            buttonPnl.add(getCancelBtn());
        }
        return buttonPnl;
    }

    private JButton getOkBtn() {
        if (okBtn == null) {
            okBtn = new JButton();
            okBtn.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
            okBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    okBtnActionPerformed();
                }
            });
        }
        return okBtn;
    }

    private void okBtnActionPerformed() {
        doClose();
    }

    private JButton getCancelBtn() {
        if (cancelBtn == null) {
            cancelBtn = new JButton();
            cancelBtn.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    cancelBtnActionPerformed();
                }
            });
        }
        return cancelBtn;
    }

    private void cancelBtnActionPerformed() {
        timeDuration = null;
        doClose();
    }

    public void setTimeDuration(TimeDuration td) {
        timeDuration = td;
    }

    public TimeDuration getTimeDuration() {
        return timeDuration;
    }

    private JPanel getCenterPnl() {
        if (centerPnl == null) {

            JPanel innerPnl = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.gridx = 0;
            gbc.gridy = 0;
            innerPnl.add(getTimeDurationLbl(), gbc);
            gbc.gridx = 1;
            innerPnl.add(getTimeDurationPnl(), gbc);

            centerPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
            centerPnl.add(innerPnl);
        }
        return centerPnl;
    }

    private JLabel getTimeDurationLbl() {
        if (timeDurationLbl == null) {
            timeDurationLbl = new JLabel(label);
        }
        return timeDurationLbl;
    }

    private JPanel getTimeDurationPnl() {
        if (timeDurationPnl == null) {
            timeDurationPnl = builder.getPanel("timeDuration");
        }
        return timeDurationPnl;
    }

    public void performEscapeAction() {
        cancelBtnActionPerformed();
    }
}