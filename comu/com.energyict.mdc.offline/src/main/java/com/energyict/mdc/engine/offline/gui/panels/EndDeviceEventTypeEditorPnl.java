package com.energyict.mdc.engine.offline.gui.panels;

import com.energyict.cim.*;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.windows.EisPropsPnl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: gde
 * Date: 16/05/12
 */
public class EndDeviceEventTypeEditorPnl extends EisPropsPnl {

    private JLabel deviceTypeLbl;
    private JLabel domainLbl;
    private JLabel subDomainLbl;
    private JLabel eventOrActionLbl;
    private JComboBox deviceTypeCombo;
    private JComboBox domainCombo;
    private JComboBox subDomainCombo;
    private JComboBox eventOrActionCombo;
    private JPanel innerPnl;
    private JPanel centerPnl;
    private JPanel southPnl;
    private JButton okBtn;
    private JButton cancelBtn;

    private boolean canceled = true;
    private EndDeviceEventType eventType = null;
    private boolean readOnly;

    public EndDeviceEventTypeEditorPnl(EndDeviceEventType eventType, boolean readOnly) {
        this.readOnly = readOnly;
        this.eventType = eventType;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        add(getCenterPnl(), BorderLayout.CENTER);
        add(getSouthPnl(), BorderLayout.SOUTH);
    }

    private JPanel getCenterPnl() {
        if (centerPnl == null) {
            centerPnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
            centerPnl.add(getInnerPnl());
        }
        return centerPnl;
    }

    private JPanel getSouthPnl() {
        if (southPnl == null) {
            southPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JPanel btnPnl = new JPanel(new GridLayout(1, 0, 3, 3));
            if (!readOnly) {
                btnPnl.add(getOkBtn());
            }
            btnPnl.add(getCancelBtn());
            southPnl.add(btnPnl);
        }
        return southPnl;
    }

    private JPanel getInnerPnl() {
        if (innerPnl == null) {
            innerPnl = new JPanel(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(2, 2, 2, 2);
            innerPnl.add(getDeviceTypeLbl(), gbc);
            gbc.gridx++;
            innerPnl.add(getDeviceTypeCombo(), gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            innerPnl.add(getDomainLbl(), gbc);
            gbc.gridx++;
            innerPnl.add(getDomainCombo(), gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            innerPnl.add(getSubDomainLbl(), gbc);
            gbc.gridx++;
            innerPnl.add(getSubDomainCombo(), gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            innerPnl.add(getEventOrActionLbl(), gbc);
            gbc.gridx++;
            innerPnl.add(getEventOrActionCombo(), gbc);
        }
        return innerPnl;
    }

    private JLabel getDeviceTypeLbl() {
        if (deviceTypeLbl == null) {
            deviceTypeLbl = new JLabel(TranslatorProvider.instance.get().getTranslator().getTranslation("rtuType") + ":");
        }
        return deviceTypeLbl;
    }

    private JComboBox getDeviceTypeCombo() {
        if (deviceTypeCombo == null) {
            List<EndDeviceType> endDeviceTypes = Arrays.asList(EndDeviceType.values());
            Collections.sort(endDeviceTypes, new Comparator<EndDeviceType>() {
                public int compare(EndDeviceType obj1, EndDeviceType obj2) {
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                }
            });
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (EndDeviceType endDeviceType : endDeviceTypes) {
                model.addElement(endDeviceType);
            }
            if (eventType == null || eventType.getCode().length() == 0) {
                model.setSelectedItem(EndDeviceType.NOT_APPLICABLE);
                eventType = new EndDeviceEventType(
                        EndDeviceType.NOT_APPLICABLE,
                        EndDeviceDomain.NOT_APPLICABLE,
                        EndDeviceSubdomain.NOT_APPLICABLE,
                        EndDeviceEventOrAction.NOT_APPLICABLE);
            } else {
                model.setSelectedItem(eventType.getType());
            }
            deviceTypeCombo = new JComboBox();
            if (readOnly) deviceTypeCombo.setEnabled(false);
            deviceTypeCombo.setModel(model);
        }
        return deviceTypeCombo;
    }

    private JLabel getDomainLbl() {
        if (domainLbl == null) {
            domainLbl = new JLabel(TranslatorProvider.instance.get().getTranslator().getTranslation("domain") + ":");
        }
        return domainLbl;
    }

    private JComboBox getDomainCombo() {
        if (domainCombo == null) {
            List<EndDeviceDomain> endDeviceDomains = Arrays.asList(EndDeviceDomain.values());
            Collections.sort(endDeviceDomains, new Comparator<EndDeviceDomain>() {
                public int compare(EndDeviceDomain obj1, EndDeviceDomain obj2) {
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                }
            });
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (EndDeviceDomain endDeviceDomain : endDeviceDomains) {
                model.addElement(endDeviceDomain);
            }
            if (eventType == null || eventType.getCode().length() == 0) {
                model.setSelectedItem(EndDeviceDomain.NOT_APPLICABLE);
                eventType = new EndDeviceEventType(
                        EndDeviceType.NOT_APPLICABLE,
                        EndDeviceDomain.NOT_APPLICABLE,
                        EndDeviceSubdomain.NOT_APPLICABLE,
                        EndDeviceEventOrAction.NOT_APPLICABLE);
            } else {
                model.setSelectedItem(eventType.getDomain());
            }
            domainCombo = new JComboBox();
            if (readOnly) domainCombo.setEnabled(false);
            domainCombo.setModel(model);
        }
        return domainCombo;
    }

    private JLabel getSubDomainLbl() {
        if (subDomainLbl == null) {
            subDomainLbl = new JLabel(TranslatorProvider.instance.get().getTranslator().getTranslation("subdomain") + ":");
        }
        return subDomainLbl;
    }

    private JComboBox getSubDomainCombo() {
        if (subDomainCombo == null) {
            List<EndDeviceSubdomain> endDeviceSubdomains = Arrays.asList(EndDeviceSubdomain.values());
            Collections.sort(endDeviceSubdomains, new Comparator<EndDeviceSubdomain>() {
                public int compare(EndDeviceSubdomain obj1, EndDeviceSubdomain obj2) {
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                }
            });
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (EndDeviceSubdomain endDeviceSubdomain : endDeviceSubdomains) {
                model.addElement(endDeviceSubdomain);
            }
            if (eventType == null || eventType.getCode().length() == 0) {
                model.setSelectedItem(EndDeviceSubdomain.NOT_APPLICABLE);
                eventType = new EndDeviceEventType(
                        EndDeviceType.NOT_APPLICABLE,
                        EndDeviceDomain.NOT_APPLICABLE,
                        EndDeviceSubdomain.NOT_APPLICABLE,
                        EndDeviceEventOrAction.NOT_APPLICABLE);
            } else {
                model.setSelectedItem(eventType.getSubdomain());
            }
            subDomainCombo = new JComboBox();
            if (readOnly) subDomainCombo.setEnabled(false);
            subDomainCombo.setModel(model);
        }
        return subDomainCombo;
    }

    private JLabel getEventOrActionLbl() {
        if (eventOrActionLbl == null) {
            eventOrActionLbl = new JLabel(TranslatorProvider.instance.get().getTranslator().getTranslation("eventOrAction") + ":");
        }
        return eventOrActionLbl;
    }

    private JComboBox getEventOrActionCombo() {
        if (eventOrActionCombo == null) {
            List<EndDeviceEventOrAction> endDeviceEventOrActions = Arrays.asList(EndDeviceEventOrAction.values());
            Collections.sort(endDeviceEventOrActions, new Comparator<EndDeviceEventOrAction>() {
                public int compare(EndDeviceEventOrAction obj1, EndDeviceEventOrAction obj2) {
                    return obj1.toString().compareToIgnoreCase(obj2.toString());
                }
            });
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            for (EndDeviceEventOrAction endDeviceEventOrAction : endDeviceEventOrActions) {
                model.addElement(endDeviceEventOrAction);
            }
            if (eventType == null || eventType.getCode().length() == 0) {
                model.setSelectedItem(EndDeviceEventOrAction.NOT_APPLICABLE);
                eventType = new EndDeviceEventType(
                        EndDeviceType.NOT_APPLICABLE,
                        EndDeviceDomain.NOT_APPLICABLE,
                        EndDeviceSubdomain.NOT_APPLICABLE,
                        EndDeviceEventOrAction.NOT_APPLICABLE);
            } else {
                model.setSelectedItem(eventType.getEventOrAction());
            }
            eventOrActionCombo = new JComboBox();
            if (readOnly) eventOrActionCombo.setEnabled(false);
            eventOrActionCombo.setModel(model);
        }
        return eventOrActionCombo;
    }

    private JButton getOkBtn() {
        if (okBtn == null) {
            okBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
            okBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    canceled = false;
                    doClose();
                }
            });
        }
        return okBtn;
    }

    private JButton getCancelBtn() {
        if (cancelBtn == null) {
            cancelBtn = new JButton(TranslatorProvider.instance.get().getTranslator().getTranslation(readOnly ? "close" : "cancel"));
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doClose();
                }
            });
        }
        return cancelBtn;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public EndDeviceEventType getEventType() {
        return new EndDeviceEventType(
                (EndDeviceType) deviceTypeCombo.getSelectedItem(),
                (EndDeviceDomain) domainCombo.getSelectedItem(),
                (EndDeviceSubdomain) subDomainCombo.getSelectedItem(),
                (EndDeviceEventOrAction) eventOrActionCombo.getSelectedItem());
    }
}
