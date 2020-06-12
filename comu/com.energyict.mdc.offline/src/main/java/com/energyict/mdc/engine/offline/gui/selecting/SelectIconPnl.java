/*
 * SelectIconPnl.java
 *
 * Created on 16 december 2003, 15:30
 */

package com.energyict.mdc.engine.offline.gui.selecting;

import com.energyict.mdc.engine.offline.gui.table.renderer.IconListCellRenderer;
import com.energyict.mdc.engine.offline.gui.windows.EisPropsPnl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * JPanel for selecting icons out of the resource bundle
 */
public class SelectIconPnl extends EisPropsPnl {

    private JButton okButton;
    private JList iconList;

    String selectedIconPath = null;

    public SelectIconPnl() {
        initComponents();
    }

    public void setSelectedIconPath(String selectedIconPath) {
        this.selectedIconPath = selectedIconPath;
        if (this.selectedIconPath != null) {
            iconList.setSelectedValue(this.selectedIconPath, true);
        } else {
            iconList.clearSelection();
        }
    }

    public String getSelectedIconPath() {
        return selectedIconPath;
    }

    private void initComponents() {
        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(getIconList()), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        DefaultButtonPanel buttons = constructDefaultButtonPanel();
        okButton = buttons.getOkButton();
        okButton.setEnabled(false);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                doClose();
            }
        });

        buttons.getCancelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectedIconPath = null;
                doClose();
            }
        });
        add(getDefaultButtonPanel(), BorderLayout.SOUTH);
    }

    private JList getIconList() {
        iconList = new JList();
        iconList.setVisibleRowCount(12);
        iconList.setModel(getIconListModel());
        iconList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        iconList.setCellRenderer(new IconListCellRenderer());
        iconList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    String selected = (String) iconList.getSelectedValue();
                    if (selected != null) {
                        okButton.setEnabled(true);
                        selectedIconPath = selected;
                    } else {
                        okButton.setEnabled(false);
                    }
                }
            }
        });
        return iconList;
    }

    private DefaultListModel getIconListModel() {
        // Fill up the list with possible icons

        DefaultListModel model = new DefaultListModel();
        try (InputStream inputStream =
                     SelectIconPnl.class.getResourceAsStream("/mdw/icons/icons.txt");
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(inputStream))) {
            String line = reader.readLine();
            while (line != null) {
                model.addElement(line.trim());
                line = reader.readLine();
            }
        } catch (IOException ex) {
        }
        return model;
    }
}
