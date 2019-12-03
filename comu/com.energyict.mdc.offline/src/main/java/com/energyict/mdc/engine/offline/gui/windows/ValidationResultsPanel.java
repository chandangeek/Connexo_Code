package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.gui.UiHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

public class ValidationResultsPanel extends EisPropsPnl {

    private JPanel centerPnl;
    private JPanel southPnl;
    private JPanel buttonPnl;
    private JButton closeBtn;

    public ValidationResultsPanel(List<String> validationResultMessages) {
        setLayout(new BorderLayout(5,5));
        add(getCenterPnl(validationResultMessages), BorderLayout.CENTER);
        add(getSouthPnl(), BorderLayout.SOUTH);
    }

    private JPanel getCenterPnl(List<String> validationResultMessages) {
        if (centerPnl==null) {
            centerPnl = new JPanel(new GridBagLayout());
            centerPnl.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(2,2,2,2);
            gbc.gridx = 0;
            gbc.gridy = 0;

            for (String each : validationResultMessages) {
                centerPnl.add(new JLabel(each), gbc);
                gbc.gridy++;
            }
        }
        return centerPnl;
    }

    private JPanel getSouthPnl() {
        if (southPnl==null) {
            southPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            southPnl.add(getButtonPnl());
        }
        return southPnl;
    }

    private JPanel getButtonPnl() {
        if (buttonPnl==null) {
            buttonPnl = new JPanel(new GridLayout(1,0,3,3));
            buttonPnl.add(getCloseBtn());
        }
        return buttonPnl;
    }

    private JButton getCloseBtn() {
        if (closeBtn==null) {
            closeBtn = new JButton(UiHelper.translate("close"));
            closeBtn.setMnemonic(KeyEvent.VK_C);
            closeBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doClose();
                }
            });
        }
        return closeBtn;
    }

}
